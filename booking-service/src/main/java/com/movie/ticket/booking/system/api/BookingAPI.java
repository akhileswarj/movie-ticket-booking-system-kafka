package com.movie.ticket.booking.system.api;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import com.example.democom.movie.ticket.booking.system.commons.dto.BookingStatus;
import com.example.democom.movie.ticket.booking.system.commons.dto.ResponseDto;
import com.movie.ticket.booking.system.service.BookingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("bookings")
@Slf4j
public class BookingAPI {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private Environment environment;

    @PostMapping
    public ResponseEntity<ResponseDto> createBooking(@RequestBody @Valid BookingDto bookingDto){
        log.info("entered into BookingAPI with Json data request {}", bookingDto.toString());
        ResponseDto response = bookingService.createBooking(bookingDto);
        return new ResponseEntity<ResponseDto>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{bookingId}/status")
    public ResponseEntity<String> updateBookingStatus(
            @PathVariable UUID bookingId,
            @RequestParam String status) {
        bookingService.updateBookingStatus(bookingId, status);
        return ResponseEntity.ok("Booking status updated to: " + status);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable UUID bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    @GetMapping("/test")
    public String testingMethod(){
        System.out.println(environment.getProperty("server.port") + "test ");
        return "test in working.";
    }
}
