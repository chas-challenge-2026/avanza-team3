package se.comerit.avanza.accountallocation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.account.repository.AccountRepository;
import se.comerit.avanza.accountallocation.dto.AccountAllocationCategoryResponse;
import se.comerit.avanza.accountallocation.dto.AccountAllocationOverviewResponse;
import se.comerit.avanza.accountallocation.model.AccountAllocationTarget;
import se.comerit.avanza.accountallocation.model.AssetCategory;
import se.comerit.avanza.accountallocation.repository.AccountAllocationTargetRepository;
import se.comerit.avanza.accountallocation.service.AccountAllocationOverviewService;
import se.comerit.avanza.exception.ResourceNotFoundException;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.portfolio.dto.ValuedHolding;
import se.comerit.avanza.portfolio.service.PortfolioValuationService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountAllocationOverviewServiceTest {
    @Mock AccountRepository accountRepository;
    @Mock AccountAllocationTargetRepository targetRepository;
    @Mock PortfolioValuationService valuationService;
    @InjectMocks AccountAllocationOverviewService service;

    private static final int USER_ID = 7;
    private static final int ACCOUNT_ID = 11;
    private static final Account ACCOUNT = new Account(USER_ID, "ISK", "ISK 1", "SEK");

    private void owned(int accountId) {
        when(accountRepository.findByIdAndUserId(accountId, USER_ID))
                .thenReturn(Optional.of(ACCOUNT));
    }

    private AccountAllocationTarget target(AssetCategory category, String percent) {
        return new AccountAllocationTarget(ACCOUNT, category, new BigDecimal(percent));
    }

    private ValuedHolding valued(int accountId, InstrumentType type, String sek) {
        return new ValuedHolding(1, accountId, "ISK", "ISK", 4, "TICK", "Instrument",
                type, null, "SEK", BigDecimal.ONE, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ONE, new BigDecimal(sek),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private AccountAllocationCategoryResponse category(AccountAllocationOverviewResponse response,
                                                        AssetCategory category) {
        return response.categories().stream().filter(row -> row.category() == category)
                .findFirst().orElseThrow();
    }

    @Test
    void rejectsForeignOrMissingAccountBeforeReadingAnyPortfolioData() {
        when(accountRepository.findByIdAndUserId(ACCOUNT_ID, USER_ID))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getAllocation(ACCOUNT_ID, USER_ID));
        verifyNoInteractions(valuationService, targetRepository);
    }

    @Test
    void comparesActualToTargetWithSignedDriftAndIncludesAllCategories() {
        owned(ACCOUNT_ID);
        when(valuationService.getValuedHoldingsByAccountIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(List.of(
                valued(ACCOUNT_ID, InstrumentType.STOCK, "70000.00"),
                valued(ACCOUNT_ID, InstrumentType.FUND, "20000.00"),
                valued(ACCOUNT_ID, InstrumentType.ETF, "10000.00")));
        when(targetRepository.findByAccountIdOrderByAssetCategoryAsc(ACCOUNT_ID)).thenReturn(List.of(
                target(AssetCategory.STOCK, "50.00"), target(AssetCategory.FUND, "40.00"),
                target(AssetCategory.BOND, "10.00")));

        var response = service.getAllocation(ACCOUNT_ID, USER_ID);
        assertEquals(ACCOUNT_ID, response.accountId());
        assertEquals(new BigDecimal("100000.00"), response.totalValueSek());
        assertTrue(response.targetsConfigured());
        assertEquals(List.of(AssetCategory.values()),
                response.categories().stream().map(AccountAllocationCategoryResponse::category).toList());
        var stocks = category(response, AssetCategory.STOCK);
        assertEquals(new BigDecimal("70000.00"), stocks.valueSek());
        assertEquals(new BigDecimal("70.00"), stocks.actualPct());
        assertEquals(new BigDecimal("50.00"), stocks.targetPct());
        assertEquals(new BigDecimal("20.00"), stocks.differencePctPoints());
        assertEquals(new BigDecimal("20000.00"), stocks.differenceSek());
        var funds = category(response, AssetCategory.FUND);
        assertEquals(new BigDecimal("30000.00"), funds.valueSek());
        assertEquals(new BigDecimal("30.00"), funds.actualPct());
        assertEquals(new BigDecimal("40.00"), funds.targetPct());
        assertEquals(new BigDecimal("-10.00"), funds.differencePctPoints());
        assertEquals(new BigDecimal("-10000.00"), funds.differenceSek());
        var bond = category(response, AssetCategory.BOND);
        assertEquals(new BigDecimal("0.00"), bond.valueSek());
        assertEquals(new BigDecimal("-10.00"), bond.differencePctPoints());
        assertEquals(new BigDecimal("-10000.00"), bond.differenceSek());
        assertEquals(new BigDecimal("0.00"), category(response, AssetCategory.OTHER).targetPct());
        verify(valuationService).getValuedHoldingsByAccountIdAndUserId(ACCOUNT_ID, USER_ID);
    }

    @Test
    void noSavedTargetsMeansNullDeviationsNotZeroTargets() {
        owned(ACCOUNT_ID);
        when(valuationService.getValuedHoldingsByAccountIdAndUserId(ACCOUNT_ID, USER_ID))
                .thenReturn(List.of(valued(ACCOUNT_ID, InstrumentType.STOCK, "125.50")));
        var response = service.getAllocation(ACCOUNT_ID, USER_ID);
        assertFalse(response.targetsConfigured());
        assertEquals(new BigDecimal("125.50"), response.totalValueSek());
        assertEquals(new BigDecimal("100.00"), category(response, AssetCategory.STOCK).actualPct());
        assertTrue(response.categories().stream().allMatch(r ->
                r.targetPct() == null && r.differencePctPoints() == null && r.differenceSek() == null));
    }

    @Test
    void emptyAccountWithTargetsDoesNotInventPercentageDrift() {
        owned(ACCOUNT_ID);
        when(targetRepository.findByAccountIdOrderByAssetCategoryAsc(ACCOUNT_ID))
                .thenReturn(List.of(target(AssetCategory.STOCK, "100.00")));
        var response = service.getAllocation(ACCOUNT_ID, USER_ID);
        assertTrue(response.targetsConfigured());
        assertEquals(new BigDecimal("0.00"), response.totalValueSek());
        assertTrue(response.categories().stream().allMatch(r ->
                r.actualPct().equals(new BigDecimal("0.00")) &&
                        r.differencePctPoints() == null &&
                        r.differenceSek().equals(new BigDecimal("0.00"))));
        assertEquals(new BigDecimal("100.00"), category(response, AssetCategory.STOCK).targetPct());
    }

    @Test
    void bondAndUnknownTypesHaveSeparateBucketsAndRoundToTwoDecimals() {
        owned(ACCOUNT_ID);
        when(valuationService.getValuedHoldingsByAccountIdAndUserId(ACCOUNT_ID, USER_ID)).thenReturn(List.of(
                valued(ACCOUNT_ID, InstrumentType.BOND, "33.33"),
                valued(ACCOUNT_ID, InstrumentType.OPTION, "66.67"),
                valued(ACCOUNT_ID, null, "0.00")));
        var response = service.getAllocation(ACCOUNT_ID, USER_ID);
        assertEquals(new BigDecimal("33.33"), category(response, AssetCategory.BOND).actualPct());
        assertEquals(new BigDecimal("66.67"), category(response, AssetCategory.OTHER).actualPct());
        assertEquals(new BigDecimal("0.00"), category(response, AssetCategory.FUND).actualPct());
    }

    @Test
    void accountsWithSameTypeStaySeparateInValuationAndTargets() {
        owned(11);
        owned(12);
        when(valuationService.getValuedHoldingsByAccountIdAndUserId(11, USER_ID))
                .thenReturn(List.of(valued(11, InstrumentType.STOCK, "100.00")));
        when(valuationService.getValuedHoldingsByAccountIdAndUserId(12, USER_ID))
                .thenReturn(List.of(valued(12, InstrumentType.FUND, "200.00")));
        when(targetRepository.findByAccountIdOrderByAssetCategoryAsc(11))
                .thenReturn(List.of(target(AssetCategory.STOCK, "100.00")));
        when(targetRepository.findByAccountIdOrderByAssetCategoryAsc(12))
                .thenReturn(List.of(target(AssetCategory.FUND, "100.00")));
        var isk1 = service.getAllocation(11, USER_ID);
        var isk2 = service.getAllocation(12, USER_ID);
        assertEquals(new BigDecimal("100.00"), isk1.totalValueSek());
        assertEquals(new BigDecimal("200.00"), isk2.totalValueSek());
        assertEquals(new BigDecimal("100.00"), category(isk1, AssetCategory.STOCK).targetPct());
        assertEquals(new BigDecimal("100.00"), category(isk2, AssetCategory.FUND).targetPct());
        assertEquals(new BigDecimal("0.00"), category(isk2, AssetCategory.STOCK).targetPct());
    }
}
