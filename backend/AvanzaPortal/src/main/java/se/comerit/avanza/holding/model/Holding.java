package se.comerit.avanza.holding.model;

//ej klart, till när jag byter till jpa senare
public class Holding {

    private Integer id;

    private String ticker;

    private String instrumentName;


    //OBS!!! snacka med gruppen om BigDecimal vid framtida möte
    private Double quantity;

    private Double avgBuyPrice;

    private String currency;

}
