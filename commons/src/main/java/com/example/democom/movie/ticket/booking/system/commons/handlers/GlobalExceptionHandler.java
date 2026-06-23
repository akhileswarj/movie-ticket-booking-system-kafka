package com.example.democom.movie.ticket.booking.system.commons.handlers;

import com.example.democom.movie.ticket.booking.system.commons.dto.ResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseDto> methodArgumentsNotValidException(MethodArgumentNotValidException exception){
        log.info("entered into BookingApiHandler with an exception");

        ResponseDto response = ResponseDto.builder()
                .errorDescription(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .errorMessage(exception.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage).toList()).build();
        return new ResponseEntity<ResponseDto>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseDto> runtimeException(RuntimeException runtimeException){
        return new ResponseEntity<ResponseDto>(
                ResponseDto.builder()
                        .errorStatusMssg(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                        .errorDescription(runtimeException.getMessage())
                        .build(),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}
