package com.movie.ticket.booking.system.handler;

import com.example.democom.movie.ticket.booking.system.commons.handlers.GlobalExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class BookingApiHandler extends GlobalExceptionHandler {

    // to define handlers for custom defined booking specific related exceptions
}
