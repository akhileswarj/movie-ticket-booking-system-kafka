package com.movie.ticket.booking.system.payment.service.kafka;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import com.movie.ticket.booking.system.payment.service.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentKafkaConsumer {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private PaymentKafkaProducer paymentKafkaProducer;

    @KafkaListener(topics = "payment-request", groupId = "payment-service-group")
    public void consumePaymentRequest(BookingDto bookingDto) {
        log.info("Consumed from 'payment-request': bookingId={}", bookingDto.getBookingId());

        // process Razorpay (7s async — no one is timing out)
        BookingDto result = paymentService.makePayment(bookingDto);

        // publish result (PENDING + paymentLink) back to booking-service
        paymentKafkaProducer.publishPaymentResponse(result);
    }
}