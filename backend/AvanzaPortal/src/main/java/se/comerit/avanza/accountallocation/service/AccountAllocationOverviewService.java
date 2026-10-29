package se.comerit.avanza.accountallocation.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.account.repository.AccountRepository;
import se.comerit.avanza.accountallocation.dto.AccountAllocationCategoryResponse;
import se.comerit.avanza.accountallocation.dto.AccountAllocationOverviewResponse;
import se.comerit.avanza.accountallocation.model.AccountAllocationTarget;
import se.comerit.avanza.accountallocation.model.AssetCategory;
import se.comerit.avanza.accountallocation.repository.AccountAllocationTargetRepository;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.portfolio.dto.ValuedHolding;
import se.comerit.avanza.portfolio.service.PortfolioValuationService;

@Service
public class AccountAllocationOverviewService {
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final AccountRepository accountRepository;
    private final AccountAllocationTargetRepository targetRepository;
    private final PortfolioValuationService valuationService;

    public AccountAllocationOverviewService(AccountRepository accountRepository,
                                            AccountAllocationTargetRepository targetRepository,
                                            PortfolioValuationService valuationService) {
        this.accountRepository = accountRepository;
        this.targetRepository = targetRepository;
        this.valuationService = valuationService;
    }

    @PreAuthorize("#userId == authentication.details")
    @Transactional(readOnly = true)
    public AccountAllocationOverviewResponse getAllocation(Integer accountId, Integer userId) {
        accountRepository.findByIdAndUserId(accountId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        List<ValuedHolding> holdings = valuationService
                .getValuedHoldingsByAccountIdAndUserId(accountId, userId);
        List<AccountAllocationTarget> targets = targetRepository
                .findByAccountIdOrderByAssetCategoryAsc(accountId);

        Map<AssetCategory, BigDecimal> values = new EnumMap<>(AssetCategory.class);
        BigDecimal total = BigDecimal.ZERO;
        for (ValuedHolding holding : holdings) {
            AssetCategory category = AssetCategory.fromInstrumentType(holding.instrumentType());
            BigDecimal value = holding.marketValueSek();
            values.merge(category, value, BigDecimal::add);
            total = total.add(value);
        }
        total = money(total);

        Map<AssetCategory, BigDecimal> targetPcts = new EnumMap<>(AssetCategory.class);
        for (AccountAllocationTarget target : targets) {
            targetPcts.put(target.getAssetCategory(), target.getTargetPct());
        }
        boolean targetsConfigured = !targets.isEmpty();
        List<AccountAllocationCategoryResponse> categories = new ArrayList<>();
        for (AssetCategory category : AssetCategory.values()) {
            BigDecimal categoryValue = money(values.getOrDefault(category, BigDecimal.ZERO));
            BigDecimal actualPct = total.signum() > 0
                    ? categoryValue.multiply(HUNDRED).divide(total, 2, RoundingMode.HALF_UP)
                    : money(BigDecimal.ZERO);

            BigDecimal targetPct = targetsConfigured
                    ? targetPcts.getOrDefault(category, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
                    : null;
            BigDecimal differencePctPoints = null;
            BigDecimal differenceSek = null;
            if (targetsConfigured) {
                if (total.signum() > 0) {
                    differencePctPoints = actualPct.subtract(targetPct).setScale(2, RoundingMode.HALF_UP);
                }
                BigDecimal targetValueSek = total.multiply(targetPct)
                        .divide(HUNDRED, 2, RoundingMode.HALF_UP);
                differenceSek = money(categoryValue.subtract(targetValueSek));
            }
            categories.add(new AccountAllocationCategoryResponse(category, categoryValue,
                    actualPct, targetPct, differencePctPoints, differenceSek));
        }
        return new AccountAllocationOverviewResponse(accountId, total, targetsConfigured,
                List.copyOf(categories));
    }

    private static BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
