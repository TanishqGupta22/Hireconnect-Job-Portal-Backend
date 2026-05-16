package com.hireconnect.interview.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InterviewAlreadyScheduledException extends RuntimeException {
    
    public InterviewAlreadyScheduledException(String message) {
        super(message);
    }
    
    public InterviewAlreadyScheduledException(String message, Throwable cause) {
        super(message, cause);
    }
}
