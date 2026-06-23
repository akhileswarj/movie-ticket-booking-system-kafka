package com.movie.ticket.booking.system.kafka;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import com.example.democom.movie.ticket.booking.system.commons.dto.BookingStatus;
import com.movie.ticket.booking.system.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationKafkaConsumer {

    @Autowired
    private EmailService emailService;

    @KafkaListener(topics = "payment-response", groupId = "notification-service-group")
    public void consumePaymentResponse(BookingDto bookingDto) {
        log.info("Notification service consumed payment-response: bookingId={}, status={}",
                bookingDto.getBookingId(), bookingDto.getBookingStatus());

        if (BookingStatus.CONFIRMED.equals(bookingDto.getBookingStatus())
                && bookingDto.getUserEmail() != null) {
            emailService.sendBookingConfirmation(bookingDto);
        } else {
            log.info("Skipping email — status: {}, email present: {}",
                    bookingDto.getBookingStatus(), bookingDto.getUserEmail() != null);
        }
    }
}