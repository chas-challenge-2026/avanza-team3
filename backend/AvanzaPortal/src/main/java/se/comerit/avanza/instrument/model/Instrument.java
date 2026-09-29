package se.comerit.avanza.instrument.model;

import jakarta.persistence.*;

@Entity
@Table(name = "instruments")
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String name;

    //Osäker, kanske bättre som enum
    @Column(length = 100)
    private String sector;

    private String instrumentType;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false, length = 10, unique = true)
    private String ticker;

}
