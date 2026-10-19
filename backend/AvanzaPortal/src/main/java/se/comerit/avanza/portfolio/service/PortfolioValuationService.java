package se.comerit.avanza.portfolio.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.comerit.avanza.account.model.Account;
import se.comerit.avanza.holding.model.Holding;
import se.comerit.avanza.holding.repository.HoldingRepository;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.market.service.MarketDataService;
import se.comerit.avanza.portfolio.dto.ValuedHolding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PortfolioValuationService {

    public static final String BASE_CURRENCY = "SEK";

    private final HoldingRepository holdingRepository;
    private final MarketDataService marketDataService;

    public PortfolioValuationService(
            HoldingRepository holdingRepository,
            MarketDataService marketDataService) {
        this.holdingRepository = holdingRepository;
        this.marketDataService = marketDataService;
    }

    @Transactional(readOnly = true)
    public List<ValuedHolding> getValuedHoldingsByUserId(Integer userId) {
        return holdingRepository
                .findByAccountUserIdOrderByAccountAccountTypeAscInstrumentTickerAsc(userId)
                .stream()
                .map(this::value)
                .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal getPortfolioValueSek(Integer userId) {
        return getValuedHoldingsByUserId(userId).stream()
                .map(ValuedHolding::marketValueSek)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public ValuedHolding value(Holding holding) {
        Instrument instrument = requireInstrument(holding);

        BigDecimal quantity = orZero(holding.getQuantity());
        BigDecimal avgBuyPrice = orZero(holding.getAvgBuyPrice());
        BigDecimal currentPrice = marketDataService.getPrice(instrument.getTicker());
        BigDecimal fxRateToSek = marketDataService.getEffectiveFxRate(
                instrument.getCurrency(),
                BASE_CURRENCY
        );

        BigDecimal marketValueSek = quantity
                .multiply(currentPrice)
                .multiply(fxRateToSek);

        // MVP: purchase cost is converted with the current FX rate as historical FX is not stored yet.
        BigDecimal costBasisSek = quantity
                .multiply(avgBuyPrice)
                .multiply(fxRateToSek);

        BigDecimal pnlSek = marketValueSek.subtract(costBasisSek);
        BigDecimal pnlPct = costBasisSek.compareTo(BigDecimal.ZERO) > 0
                ? pnlSek.divide(costBasisSek, 8, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                : BigDecimal.ZERO;

        Account account = holding.getAccount();

        return new ValuedHolding(
                holding.getId(),
                holding.getAccountId(),
                account != null ? account.getAccountType() : null,
                account != null ? account.getAccountName() : null,
                instrument.getId(),
                instrument.getTicker(),
                instrument.getName(),
                instrument.getInstrumentType(),
                instrument.getSector(),
                instrument.getCurrency(),
                quantity,
                avgBuyPrice,
                roundMoney(currentPrice),
                fxRateToSek.setScale(8, RoundingMode.HALF_UP),
                roundMoney(marketValueSek),
                roundMoney(costBasisSek),
                roundMoney(pnlSek),
                roundMoney(pnlPct)
        );
    }

    private Instrument requireInstrument(Holding holding) {
        if (holding.getInstrument() == null) {
            throw new IllegalStateException(
                    "Holding " + holding.getId() + " is not linked to an instrument"
            );
        }
        return holding.getInstrument();
    }

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private BigDecimal roundMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
