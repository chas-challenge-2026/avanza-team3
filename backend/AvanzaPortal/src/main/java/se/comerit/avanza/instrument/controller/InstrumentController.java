package se.comerit.avanza.instrument.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.comerit.avanza.instrument.dto.InstrumentRequest;
import se.comerit.avanza.instrument.dto.InstrumentResponse;
import se.comerit.avanza.instrument.service.InstrumentService;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @PostMapping
    public ResponseEntity<Void> addInstrument(
            @Valid
            @RequestBody
            InstrumentRequest request
    ) {

        instrumentService.create(
                request.ticker(),
                request.name(),
                request.instrumentType(),
                request.sector(),
                request.currency()
        );

        return ResponseEntity.status(201).build();
    }

    @GetMapping
    public ResponseEntity<List<InstrumentResponse>> listInstruments() {
        return ResponseEntity.ok(instrumentService.getAllInstruments());
    }

    @GetMapping("/{instrumentId}")
    public ResponseEntity<InstrumentResponse> getInstrument(
            @PathVariable("instrumentId")
            Integer instrumentId
    ) {
        return ResponseEntity.ok(
                instrumentService.getInstrumentById(instrumentId));
    }
}
