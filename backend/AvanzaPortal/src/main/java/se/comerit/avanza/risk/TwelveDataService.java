package se.comerit.avanza.risk;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service 
public class TwelveDataService {
    private final WebClient webClient;
    private final String apiKey;

    public TwelveDataService(WebClient.Builder webClientBuilder, @Value("${twelvedata.api-key") String apiKey){
        this.webClient = webClientBuilder
                .baseUrl("https://api.twelvedata.com")
                .build();
        
        this.apiKey = apiKey;
    }

    public double getCurrentPrice(String symbol) {
        TwelveDataQuote response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/quote")
                        .queryParam("symbol", symbol)
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(TwelveDataQuote.class)
                .block();

        if (response == null || response.getClose() == null){
            throw new RuntimeException("Could not get current price for " + symbol);
        }

        return Double.parseDouble(response.getClose());
    }

    public List<Double> getHistoricalClosingPrices(String symbol, int outputSize){
        TwelveDataTimeSeries response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/time_series")
                        .queryParam("symbol", symbol)
                        .queryParam("interval", "1day")
                        .queryParam("outputSize", outputSize)
                        .queryParam("apikey", apiKey)
                        .build())
                .retrieve()
                .bodyToMono(TwelveDataTimeSeries.class)
                .block();

        if (response == null || response.getValues() == null){
            throw new RuntimeException("Could not get historical prices for" + symbol);
        }

        return response.getValues()
                .stream()
                .map(value -> Double.parseDouble(value.getClose()))
                .toList();
    }
}
