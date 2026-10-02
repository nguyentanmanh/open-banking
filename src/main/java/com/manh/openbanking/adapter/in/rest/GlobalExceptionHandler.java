package com.manh.openbanking.adapter.in.rest;

import com.manh.openbanking.application.exception.OpenBankingException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(OpenBankingException.class)
    ResponseEntity<ExternalErrorResponse> openBanking(OpenBankingException ex) {
        return ResponseEntity.status(ex.status()).body(new ExternalErrorResponse(ex.code(), ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class,
            MissingRequestHeaderException.class, HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class})
    ResponseEntity<ExternalErrorResponse> invalidRequest(Exception ex) {
        return ResponseEntity.badRequest().body(new ExternalErrorResponse("INVALID_REQUEST", validationDescription(ex)));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ExternalErrorResponse> unknown(Exception ex) {
        LOG.error("Unhandled request failure", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ExternalErrorResponse("INTERNAL_ERROR", "An unexpected internal error occurred."));
    }

    private String validationDescription(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException invalid && !invalid.getBindingResult().getAllErrors().isEmpty()) {
            return invalid.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        }
        if (ex instanceof MissingRequestHeaderException missing) {
            return "Required header " + missing.getHeaderName() + " is missing.";
        }
        return "The request contains invalid data.";
    }
}
