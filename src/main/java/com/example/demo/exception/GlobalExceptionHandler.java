package com.example.demo.exception;

import com.example.demo.dto.ErrorResponseDto;
import com.example.demo.dto.ValidationErrorResponseDto;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDto> handleGlobalException(
          Exception exception,
          WebRequest webRequest
  ) {
    return createErrorResponse(exception, webRequest, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(CsvProcessingException.class)
  public ResponseEntity<ErrorResponseDto> handleCsvProcessingException(
          Exception exception,
          WebRequest webRequest
  ) {
    return createErrorResponse(exception, webRequest, HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler(FileProcessingException.class)
  public ResponseEntity<ErrorResponseDto> handleFileProcessingException(
          Exception exception,
          WebRequest webRequest
  ) {
    return createErrorResponse(exception, webRequest, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(InvalidFileException.class)
  public ResponseEntity<ErrorResponseDto> handleInvalidFileException(
          Exception exception,
          WebRequest webRequest
  ) {
    return createErrorResponse(exception, webRequest, HttpStatus.BAD_REQUEST);
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
          MethodArgumentNotValidException exception,
          @NonNull HttpHeaders headers,
          HttpStatusCode status,
          WebRequest webRequest
  ) {
    Map<String, String> errors = exception.getBindingResult()
            .getFieldErrors()
            .stream()
            .filter(error -> error.getDefaultMessage() != null)
            .collect(Collectors.toMap(
                    FieldError::getField,
                    FieldError::getDefaultMessage,
                    (existing, replacement) -> existing
            ));

    ValidationErrorResponseDto validationErrorResponseDto = new ValidationErrorResponseDto(
            webRequest.getDescription(false),
            status.value(),
            "Validation failed",
            LocalDateTime.now(),
            errors
    );

    return new ResponseEntity<>(validationErrorResponseDto, status);
  }

  private ResponseEntity<ErrorResponseDto> createErrorResponse(
          Exception exception,
          WebRequest webRequest,
          HttpStatus status
  ) {
    ErrorResponseDto errorResponseDto = new ErrorResponseDto(
            webRequest.getDescription(false),
            status.value(),
            exception.getMessage(),
            LocalDateTime.now()
    );

    return new ResponseEntity<>(errorResponseDto, status);
  }
}
