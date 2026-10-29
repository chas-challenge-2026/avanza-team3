package se.comerit.avanza.accountallocation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.account.repository.AccountRepository;
import se.comerit.avanza.accountallocation.dto.AccountAllocationTargetResponse;
import se.comerit.avanza.accountallocation.dto.AccountAllocationTargetsResponse;
import se.comerit.avanza.accountallocation.dto.ReplaceAccountTargetsRequest;
import se.comerit.avanza.accountallocation.dto.TargetItemRequest;
import se.comerit.avanza.accountallocation.model.AccountAllocationTarget;
import se.comerit.avanza.accountallocation.model.AssetCategory;
import se.comerit.avanza.accountallocation.repository.AccountAllocationTargetRepository;
import se.comerit.avanza.exception.BusinessRuleException;
import se.comerit.avanza.exception.ResourceNotFoundException;

@Service
public class AccountAllocationTargetService {
    private static final BigDecimal HUNDRED = new BigDecimal("100.00");

    private final AccountRepository accountRepository;
    private final AccountAllocationTargetRepository targetRepository;

    public AccountAllocationTargetService(AccountRepository accountRepository,
                                          AccountAllocationTargetRepository targetRepository) {
        this.accountRepository = accountRepository;
        this.targetRepository = targetRepository;
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional(readOnly = true)
    public AccountAllocationTargetsResponse getTargets(Integer accountId, Integer userId) {
        requireOwnedAccount(accountId, userId);
        return toResponse(accountId, targetRepository.findByAccountIdOrderByAssetCategoryAsc(accountId));
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    public AccountAllocationTargetsResponse replaceTargets(Integer accountId, Integer userId,
                                                           ReplaceAccountTargetsRequest request) {
        Account account = accountRepository.findOwnedAccountForUpdate(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        Map<AssetCategory, BigDecimal> requested = validateAndIndex(request);

        List<AccountAllocationTarget> existing =
                targetRepository.findByAccountIdOrderByAssetCategoryAsc(accountId);
        List<AccountAllocationTarget> toDelete = new ArrayList<>();
        List<AccountAllocationTarget> toSave = new ArrayList<>();
        EnumSet<AssetCategory> found = EnumSet.noneOf(AssetCategory.class);

        for (AccountAllocationTarget target : existing) {
            AssetCategory category = target.getAssetCategory();
            BigDecimal pct = requested.get(category);
            if (pct == null) {
                toDelete.add(target);
            } else {
                target.setTargetPct(pct);
                found.add(category);
                toSave.add(target);
            }
        }
        for (Map.Entry<AssetCategory, BigDecimal> entry : requested.entrySet()) {
            if (!found.contains(entry.getKey())) {
                toSave.add(new AccountAllocationTarget(account, entry.getKey(), entry.getValue()));
            }
        }

        if (!toDelete.isEmpty()) targetRepository.deleteAll(toDelete);
        targetRepository.saveAll(toSave);
        return toResponse(accountId, requested);
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional
    public void deleteTargets(Integer accountId, Integer userId) {
        accountRepository.findOwnedAccountForUpdate(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        targetRepository.deleteAll(targetRepository.findByAccountIdOrderByAssetCategoryAsc(accountId));
    }

    private Account requireOwnedAccount(Integer accountId, Integer userId) {
        return accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    private Map<AssetCategory, BigDecimal> validateAndIndex(ReplaceAccountTargetsRequest request) {
        if (request == null || request.targets() == null || request.targets().isEmpty()) {
            throw new BusinessRuleException("A non-empty set of targets is required");
        }
        EnumMap<AssetCategory, BigDecimal> percentages = new EnumMap<>(AssetCategory.class);
        BigDecimal sum = BigDecimal.ZERO;
        for (TargetItemRequest item : request.targets()) {
            if (item == null || item.category() == null || item.targetPercentage() == null) {
                throw new BusinessRuleException("Each target needs a category and percentage");
            }
            BigDecimal pct = item.targetPercentage();
            if (pct.scale() > 2 || pct.compareTo(BigDecimal.ZERO) < 0
                    || pct.compareTo(HUNDRED) > 0) {
                throw new BusinessRuleException("Target percentage must be between 0 and 100 with up to 2 decimals");
            }
            if (percentages.putIfAbsent(item.category(), pct) != null) {
                throw new BusinessRuleException("Duplicate asset category");
            }
            sum = sum.add(pct);
        }
        if (sum.compareTo(HUNDRED) != 0) {
            throw new BusinessRuleException("Target allocation must total 100%");
        }
        return percentages;
    }

    private AccountAllocationTargetsResponse toResponse(Integer accountId,
                                                        List<AccountAllocationTarget> targets) {
        return new AccountAllocationTargetsResponse(accountId, targets.stream()
                .map(t -> new AccountAllocationTargetResponse(t.getAssetCategory(), t.getTargetPct()))
                .toList());
    }

    private AccountAllocationTargetsResponse toResponse(Integer accountId,
                                                        Map<AssetCategory, BigDecimal> targets) {
        return new AccountAllocationTargetsResponse(accountId, targets.entrySet().stream()
                .map(e -> new AccountAllocationTargetResponse(e.getKey(), e.getValue()))
                .toList());
    }
}
