package se.comerit.avanza.instrument.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Instrument {

    @Id
    private String id;

    private String name;

    private String sector;

    private String instrumentType;



}
