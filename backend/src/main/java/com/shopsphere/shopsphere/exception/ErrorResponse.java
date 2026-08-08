package com.shopsphere.shopsphere.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private boolean success;      // always false here
    private int status;           // HTTP status code
    private String error;         // short reason phrase, e.g. "NOT_FOUND"
    private String message;       // human-readable message
    private String path;          // request URI that failed
    private Instant timestamp;

    // Populated only for validation errors: field -> message
    private Map<String, String> fieldErrors;
    private List<String> details;
}
