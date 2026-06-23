package com.movie.ticket.booking.system.broker;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service")
public interface PaymentServiceBroker {

    @PostMapping("/payments")
    public BookingDto makePayment(@RequestBody BookingDto bookingDto);
}
