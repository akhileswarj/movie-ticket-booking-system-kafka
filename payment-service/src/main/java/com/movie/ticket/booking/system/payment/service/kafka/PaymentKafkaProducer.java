package com.movie.ticket.booking.system.payment.service.kafka;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentKafkaProducer {

    private static final String PAYMENT_RESPONSE_TOPIC = "payment-response";

    @Autowired
    private KafkaTemplate<String, BookingDto> kafkaTemplate;

    public void publishPaymentResponse(BookingDto bookingDto) {
        log.info("Publishing to '{}' for bookingId: {}", PAYMENT_RESPONSE_TOPIC, bookingDto.getBookingId());
        kafkaTemplate.send(PAYMENT_RESPONSE_TOPIC, bookingDto.getBookingId().toString(), bookingDto);
    }
}