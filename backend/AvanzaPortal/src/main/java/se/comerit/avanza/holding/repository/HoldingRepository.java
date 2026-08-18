package se.comerit.avanza.holding.repository;

import org.springframework.stereotype.Repository;

public class HoldingRepository {
    //Denna klassen kommer bli ett interface och se ut på det här sättet istället
    //@Repository
    //Här under står JpaRepository<Holding,Long> Holding är då model klassen Holding
    //public interface HoldingRepository extends JpaRepository<Holding, Long>
    //istället för att använda queries kommer dom genereras automatiskt(till viss del)
    // med hjälp av metodnamn som findByName eller findAllHoldings
    //det viktiga för oss kommer vara att lägga på ById så det blir
    // findByNameAndId så man bara har tillgång till sina egna holdings
}
