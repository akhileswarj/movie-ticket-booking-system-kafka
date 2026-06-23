package com.movie.ticket.booking.system;

import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BookingsAPIHandlerCallBack {

    @RequestMapping(value = "/booking-fallback",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> bookingFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Booking service is currently unavailable. Please try again later");
    }

    @RequestMapping(value = "/payment-fallback",
            method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public ResponseEntity<String> paymentFallback(){
        return new ResponseEntity<String>
                ("Payment service is currently unavailable. please try after some time.",
                        HttpStatus.SERVICE_UNAVAILABLE);
    }
}
