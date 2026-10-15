package se.comerit.avanza.pricehistory.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import se.comerit.avanza.exception.ErrorResponse;
import se.comerit.avanza.pricehistory.provider.HistoricalPriceProviderException;

import java.time.Instant;

@Order(-1)
@RestControllerAdvice(assignableTypes = HistoricalPriceController.class)
public class HistoricalPriceExceptionHandler {
    @ExceptionHandler(HistoricalPriceProviderException.class)
    public ResponseEntity<ErrorResponse> handleProviderFailure(
            HistoricalPriceProviderException exception, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(new ErrorResponse(
                Instant.now(), 503, "Service Unavailable",
                "Historical price provider is unavailable", request.getRequestURI()));
    }
}
