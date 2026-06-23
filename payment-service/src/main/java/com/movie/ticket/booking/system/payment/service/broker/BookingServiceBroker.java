package com.movie.ticket.booking.system.payment.service.broker;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "booking-service")
public interface BookingServiceBroker {

    @GetMapping("/bookings/{bookingId}")
    BookingDto getBookingById(@PathVariable UUID bookingId);
}
