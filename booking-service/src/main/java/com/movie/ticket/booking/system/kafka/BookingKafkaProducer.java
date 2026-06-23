package com.movie.ticket.booking.system.kafka;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookingKafkaProducer {

    private static final String PAYMENT_REQUEST_TOPIC = "payment-request";

    @Autowired
    private KafkaTemplate<String, BookingDto> kafkaTemplate;

    public void publishPaymentRequest(BookingDto bookingDto) {
        log.info("Publishing to '{}' for bookingId: {}", PAYMENT_REQUEST_TOPIC, bookingDto.getBookingId());
        kafkaTemplate.send(PAYMENT_REQUEST_TOPIC, bookingDto.getBookingId().toString(), bookingDto);
    }
}