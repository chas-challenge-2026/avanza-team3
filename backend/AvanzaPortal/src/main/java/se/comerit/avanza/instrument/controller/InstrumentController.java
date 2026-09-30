package se.comerit.avanza.instrument.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import se.comerit.avanza.instrument.model.Instrument;
import se.comerit.avanza.instrument.model.InstrumentType;
import se.comerit.avanza.instrument.model.Sector;
import se.comerit.avanza.instrument.service.InstrumentService;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Instrument create(
            @RequestBody CreateInstrumentRequest request
    ) {
        return instrumentService.create(
                request.ticker(),
                request.name(),
                request.instrumentType(),
                request.sector(),
                request.currency()
        );
    }

    @GetMapping("/{id}")
    public Instrument getById(
            @PathVariable Integer id
    ) {
        return instrumentService.getById(id);
    }

    public record CreateInstrumentRequest(
            String ticker,
            String name,
            InstrumentType instrumentType,
            Sector sector,
            String currency
    ) {
    }
}
