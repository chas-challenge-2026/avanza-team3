package se.comerit.avanza.accountallocation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.account.repository.AccountRepository;
import se.comerit.avanza.accountallocation.dto.ReplaceAccountTargetsRequest;
import se.comerit.avanza.accountallocation.dto.TargetItemRequest;
import se.comerit.avanza.accountallocation.model.AccountAllocationTarget;
import se.comerit.avanza.accountallocation.model.AssetCategory;
import se.comerit.avanza.accountallocation.repository.AccountAllocationTargetRepository;
import se.comerit.avanza.accountallocation.service.AccountAllocationTargetService;
import se.comerit.avanza.exception.BusinessRuleException;
import se.comerit.avanza.exception.ResourceNotFoundException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountAllocationTargetServiceTest {
    @Mock AccountRepository accountRepository;
    @Mock AccountAllocationTargetRepository targetRepository;
    @InjectMocks AccountAllocationTargetService service;

    private final Account account = new Account(7, "ISK", "ISK 1", "SEK");

    private ReplaceAccountTargetsRequest request(int stocks, int funds) {
        return new ReplaceAccountTargetsRequest(List.of(
                new TargetItemRequest(AssetCategory.STOCK, BigDecimal.valueOf(stocks)),
                new TargetItemRequest(AssetCategory.FUND, BigDecimal.valueOf(funds))));
    }

    @Test
    void getTargetsRequiresOwnership() {
        when(accountRepository.findByIdAndUserId(22, 7)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getTargets(22, 7));
        verifyNoInteractions(targetRepository);
    }

    @Test
    void getTargetsReturnsEmptyListForOwnedAccountWithoutSavedTargets() {
        when(accountRepository.findByIdAndUserId(11, 7)).thenReturn(Optional.of(account));
        assertEquals(List.of(), service.getTargets(11, 7).targets());
    }

    @Test
    void independentIskAccountsReturnDifferentAllocations() {
        when(accountRepository.findByIdAndUserId(anyInt(), eq(7))).thenReturn(Optional.of(account));
        when(targetRepository.findByAccountIdOrderByAssetCategoryAsc(11)).thenReturn(List.of(
                new AccountAllocationTarget(account, AssetCategory.STOCK, new BigDecimal("80.00")),
                new AccountAllocationTarget(account, AssetCategory.FUND, new BigDecimal("20.00"))));
        when(targetRepository.findByAccountIdOrderByAssetCategoryAsc(12)).thenReturn(List.of(
                new AccountAllocationTarget(account, AssetCategory.STOCK, new BigDecimal("20.00")),
                new AccountAllocationTarget(account, AssetCategory.FUND, new BigDecimal("80.00"))));
        assertEquals(new BigDecimal("80.00"), service.getTargets(11, 7).targets().get(0).targetPercentage());
        assertEquals(new BigDecimal("20.00"), service.getTargets(12, 7).targets().get(0).targetPercentage());
    }

    @Test
    void replaceRejectsOtherUsersAccountWithoutTouchingTargets() {
        when(accountRepository.findOwnedAccountForUpdate(11, 7)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.replaceTargets(11, 7, request(60, 40)));
        verifyNoInteractions(targetRepository);
    }

    @Test
    void replaceAddsTargetsForExistingOwnedAccount() {
        when(accountRepository.findOwnedAccountForUpdate(11, 7)).thenReturn(Optional.of(account));
        var response = service.replaceTargets(11, 7, request(70, 30));
        assertEquals(11, response.accountId());
        assertEquals(2, response.targets().size());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AccountAllocationTarget>> saved = ArgumentCaptor.forClass(List.class);
        verify(targetRepository).saveAll(saved.capture());
        assertEquals(2, saved.getValue().size());
        assertTrue(saved.getValue().stream().allMatch(t -> t.getAccount() == account));
        verify(targetRepository, never()).deleteAll(anyList());
    }

    @Test
    void replaceUpdatesExistingAndRemovesStaleCategory() {
        when(accountRepository.findOwnedAccountForUpdate(11, 7)).thenReturn(Optional.of(account));
        AccountAllocationTarget stock = new AccountAllocationTarget(account, AssetCategory.STOCK, new BigDecimal("60.00"));
        AccountAllocationTarget other = new AccountAllocationTarget(account, AssetCategory.OTHER, new BigDecimal("40.00"));
        when(targetRepository.findByAccountIdOrderByAssetCategoryAsc(11)).thenReturn(List.of(stock, other));
        service.replaceTargets(11, 7, request(50, 50));
        assertEquals(new BigDecimal("50"), stock.getTargetPct());
        verify(targetRepository).deleteAll(List.of(other));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<AccountAllocationTarget>> saved = ArgumentCaptor.forClass(List.class);
        verify(targetRepository).saveAll(saved.capture());
        assertEquals(2, saved.getValue().size());
        assertTrue(saved.getValue().contains(stock));
        assertTrue(saved.getValue().stream().anyMatch(t -> t.getAssetCategory() == AssetCategory.FUND));
    }

    @Test
    void replaceRejectsInvalidSumsWithoutWriting() {
        when(accountRepository.findOwnedAccountForUpdate(11, 7)).thenReturn(Optional.of(account));
        assertThrows(BusinessRuleException.class, () -> service.replaceTargets(11, 7, request(50, 40)));
        assertThrows(BusinessRuleException.class, () -> service.replaceTargets(11, 7, request(80, 30)));
        verifyNoInteractions(targetRepository);
    }

    @Test
    void replaceRejectsDuplicateCategoryWithoutWriting() {
        when(accountRepository.findOwnedAccountForUpdate(11, 7)).thenReturn(Optional.of(account));
        var request = new ReplaceAccountTargetsRequest(List.of(
                new TargetItemRequest(AssetCategory.STOCK, new BigDecimal("50")),
                new TargetItemRequest(AssetCategory.STOCK, new BigDecimal("50"))));
        assertThrows(BusinessRuleException.class, () -> service.replaceTargets(11, 7, request));
        verifyNoInteractions(targetRepository);
    }

    @Test
    void replaceRejectsInvalidNumbersEmptyListAndNulls() {
        when(accountRepository.findOwnedAccountForUpdate(11, 7)).thenReturn(Optional.of(account));
        assertThrows(BusinessRuleException.class, () -> service.replaceTargets(11, 7, new ReplaceAccountTargetsRequest(List.of())));
        assertThrows(BusinessRuleException.class, () -> service.replaceTargets(11, 7,
                new ReplaceAccountTargetsRequest(List.of(new TargetItemRequest(AssetCategory.STOCK, new BigDecimal("100.001"))))));
        assertThrows(BusinessRuleException.class, () -> service.replaceTargets(11, 7,
                new ReplaceAccountTargetsRequest(List.of(new TargetItemRequest(AssetCategory.STOCK, new BigDecimal("-1"))))));
        assertThrows(BusinessRuleException.class, () -> service.replaceTargets(11, 7,
                new ReplaceAccountTargetsRequest(List.of(new TargetItemRequest(null, new BigDecimal("100"))))));
        verifyNoInteractions(targetRepository);
    }

    @Test
    void deleteRejectsOtherUsersAccount() {
        when(accountRepository.findOwnedAccountForUpdate(11, 7)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.deleteTargets(11, 7));
        verifyNoInteractions(targetRepository);
    }

    @Test
    void deleteRemovesOnlyTargetsForSelectedAccount() {
        when(accountRepository.findOwnedAccountForUpdate(11, 7)).thenReturn(Optional.of(account));
        var target = new AccountAllocationTarget(account, AssetCategory.STOCK, new BigDecimal("100"));
        when(targetRepository.findByAccountIdOrderByAssetCategoryAsc(11)).thenReturn(List.of(target));
        service.deleteTargets(11, 7);
        verify(targetRepository).deleteAll(List.of(target));
        verify(targetRepository).findByAccountIdOrderByAssetCategoryAsc(11);
        verifyNoMoreInteractions(targetRepository);
    }

    @Test
    void mapperCategorizesEtfsAsFundsAndBondsSeparately() {
        assertEquals(AssetCategory.FUND, AssetCategory.fromInstrumentType(
                se.comerit.avanza.instrument.model.InstrumentType.ETF));
        assertEquals(AssetCategory.BOND, AssetCategory.fromInstrumentType(
                se.comerit.avanza.instrument.model.InstrumentType.BOND));
        assertEquals(AssetCategory.OTHER, AssetCategory.fromInstrumentType(null));
    }
}
