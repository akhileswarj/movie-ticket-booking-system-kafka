package com.movie.ticket.booking.system.payment.service.service;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import org.springframework.web.bind.annotation.RequestBody;

public interface PaymentService {

    public BookingDto makePayment(BookingDto bookingDto);

    BookingDto handlePaymentCallback(String paymentId, String paymentLinkId,
                                     String orderId, String status, String signature);

}
