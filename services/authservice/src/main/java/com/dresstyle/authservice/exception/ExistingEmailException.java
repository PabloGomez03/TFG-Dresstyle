package com.dresstyle.authservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ExistingEmailException extends RuntimeException {

    public ExistingEmailException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExistingEmailException(String message) {
        super(message);
    }
}
