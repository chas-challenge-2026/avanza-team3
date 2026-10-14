package se.comerit.avanza.instrument.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.comerit.avanza.instrument.dto.InstrumentRequest;
import se.comerit.avanza.instrument.dto.InstrumentResponse;
import se.comerit.avanza.instrument.service.InstrumentService;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
@Tag(
        name = "Instruments",
        description = "Endpoints for viewing and managing financial instruments"
)
@SecurityRequirement(name = "bearerAuth")
public class InstrumentController {

    private final InstrumentService instrumentService;

    public InstrumentController(InstrumentService instrumentService) {
        this.instrumentService = instrumentService;
    }

    @Operation(
            summary = "Create instrument",
            description = "Creates a new financial instrument. Ticker values are normalized to uppercase and must be unique."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Instrument created successfully"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid instrument data"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "An instrument with the same ticker already exists"
            )
    })
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

    @Operation(
            summary = "Get instruments",
            description = "Returns all available instruments ordered by ticker."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Instruments retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            )
    })
    @GetMapping
    public ResponseEntity<List<InstrumentResponse>> listInstruments() {
        return ResponseEntity.ok(instrumentService.getAllInstruments());
    }

    @Operation(
            summary = "Get instrument by ID",
            description = "Returns a single instrument by its ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Instrument retrieved successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication required"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Instrument not found"
            )
    })
    @GetMapping("/{instrumentId}")
    public ResponseEntity<InstrumentResponse> getInstrument(
            @Parameter(
                    description = "ID of the instrument to retrieve",
                    example = "3"
            )
            @PathVariable("instrumentId")
            Integer instrumentId
    ) {
        return ResponseEntity.ok(
                instrumentService.getInstrumentById(instrumentId));
    }
}
