package se.comerit.avanza.holding.service;

import org.springframework.stereotype.Service;
import se.comerit.avanza.holding.repository.HoldingRepository;

@Service
public class HoldingService {

    private final HoldingRepository holdingRepository;

    public HoldingService(HoldingRepository holdingRepository) {
        this.holdingRepository = holdingRepository;
    }




}
