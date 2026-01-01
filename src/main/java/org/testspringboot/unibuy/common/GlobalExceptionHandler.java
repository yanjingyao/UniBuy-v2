package org.testspringboot.unibuy.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        log.error("System Error", e);
        return Result.error(e.getMessage() != null ? e.getMessage() : "System Error");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("Runtime Error", e);
        return Result.error(e.getMessage() != null ? e.getMessage() : "Operation Failed");
    }
}
