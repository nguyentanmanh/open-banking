package com.manh.openbanking.adapter.in.rest;

import com.manh.openbanking.application.exception.OpenBankingException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;

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
        ErrorMapping error = validationError(ex);
        return ResponseEntity.badRequest().body(new ExternalErrorResponse(error.code(), error.description()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ExternalErrorResponse> wrongMethod(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(new ExternalErrorResponse("WRONG_METHOD", "Sai phương thức HTTP."));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ExternalErrorResponse> responseStatus(ResponseStatusException ex) {
        String reason = ex.getReason() == null ? "request_failed" : ex.getReason();
        return ResponseEntity.status(ex.getStatusCode()).body(new ExternalErrorResponse(reason, reason));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ExternalErrorResponse> unknown(Exception ex) {
        LOG.error("Unhandled request failure", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ExternalErrorResponse("INTERNAL_ERROR", "An unexpected internal error occurred."));
    }

    private ErrorMapping validationError(Exception ex) {
        if (ex instanceof MethodArgumentNotValidException invalid
            && invalid.getBindingResult().getFieldError() instanceof FieldError field) {
            return switch (field.getField()) {
                case "accountId" -> mapping("ACCOUNT_ID_REQUIRED", "Dữ liệu trường accountId không được rỗng");
                case "fromDate" -> mapping(field.getRejectedValue() == null ? "FROMDATE_REQUIRED" : "FROMDATE_INVALID",
                    "Dữ liệu trường fromDate " + (field.getRejectedValue() == null ? "không được rỗng" : "không hợp lệ"));
                case "toDate" -> mapping(field.getRejectedValue() == null ? "TODATE_REQUIRED" : "TODATE_INVALID",
                    "Dữ liệu trường toDate " + (field.getRejectedValue() == null ? "không được rỗng" : "không hợp lệ"));
                case "page" -> mapping("PAGE_INVALID", "Dữ liệu trường page không hợp lệ");
                case "size" -> mapping("SIZE_INVALID", "Dữ liệu trường size không hợp lệ");
                default -> mapping("OTHER", "Yêu cầu không hợp lệ");
            };
        }
        if (ex instanceof MissingRequestHeaderException missing) {
            return switch (missing.getHeaderName()) {
                case "Request-ID" -> mapping("REQUEST_ID_REQUIRED", "Dữ liệu trường Request-ID không được rỗng");
                case "Request-DateTime" ->
                    mapping("REQUEST_DATETIME_REQUIRED", "Dữ liệu trường Request-DateTime không được rỗng");
                case "Provider-ID" -> mapping("PROVIDER_ID_REQUIRED", "Dữ liệu trường Provider-ID không được rỗng");
                case "TPP-ID" -> mapping("TPP_ID_REQUIRED", "Dữ liệu trường TPP-ID không được rỗng");
                case "JWS-Signature" ->
                    mapping("JWS_SIGNATURE_REQUIRED", "Dữ liệu trường JWS-Signature không được rỗng");
                default -> mapping("OTHER", "Thiếu header bắt buộc " + missing.getHeaderName());
            };
        }
        if (ex instanceof HttpMessageNotReadableException unreadable) {
            String message = unreadable.getMessage();
            if (message != null && message.contains("fromDate")) {
                return mapping("FROMDATE_INVALID", "Dữ liệu trường fromDate không hợp lệ");
            }
            if (message != null && message.contains("toDate")) {
                return mapping("TODATE_INVALID", "Dữ liệu trường toDate không hợp lệ");
            }
        }
        return mapping("OTHER", "Yêu cầu không hợp lệ");
    }

    private ErrorMapping mapping(String code, String description) {
        return new ErrorMapping(code, description);
    }

    private record ErrorMapping(String code, String description) {
    }
}
