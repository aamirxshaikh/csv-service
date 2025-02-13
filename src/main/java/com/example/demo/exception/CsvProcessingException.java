package com.example.demo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY, reason = "Error processing CSV file")
public class CsvProcessingException extends RuntimeException {
  public CsvProcessingException(String message) {
    super(message);
  }
}
