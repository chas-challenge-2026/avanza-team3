package se.comerit.avanza.holding.model;

public class Holding {
    //det kan va bättre att döpa detta paketet till entity(men jag skapar alltid likadant i start, sorry)
    //Här i kommer vi skapa "objektet" Holding och skriva
    // alla nödvändiga attribut(tex, id, namn, etc)

    /*
    @Entity //viktigt att alla delar märks så här med vad dom är, annars fungerar inget
    public class Holding {

    Här skapas ett automatiskt id
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    i parentesen kan man lägga "regler" liknande valideringen som kan göras i dto(och bör göras i båda)
    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    Här är en relation till Account
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    konstruktor, getters och setters ska också finnas
    (getters och setters kan automatiskt skapas men helst inte en setter för id)->högerklicka->generera->getter och setter
    }
     */

}
