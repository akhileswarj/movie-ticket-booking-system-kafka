package com.movie.ticket.booking.system.kafka;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import com.example.democom.movie.ticket.booking.system.commons.dto.BookingStatus;
import com.movie.ticket.booking.system.entity.BookingEntity;
import com.movie.ticket.booking.system.repository.BookingRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class BookingKafkaConsumer {

    @Autowired
    private BookingRepo bookingRepo;

    @KafkaListener(topics = "payment-response", groupId = "booking-service-group")
    @Transactional
    public void consumePaymentResponse(BookingDto bookingDto) {
        log.info("Consumed from 'payment-response': bookingId={}, status={}, paymentLink={}",
                bookingDto.getBookingId(), bookingDto.getBookingStatus(), bookingDto.getPaymentLink());

        BookingEntity booking = bookingRepo.findById(bookingDto.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingDto.getBookingId()));

        // Case 1: Razorpay link just created → store paymentLink (status stays PENDING)
        if (bookingDto.getPaymentLink() != null && booking.getPaymentLink() == null) {
            booking.setPaymentLink(bookingDto.getPaymentLink());
            log.info("PaymentLink stored for bookingId: {}", bookingDto.getBookingId());
        }

        // Case 2: User paid or payment failed → update status
        if (bookingDto.getBookingStatus() != null
                && !bookingDto.getBookingStatus().equals(BookingStatus.PENDING)) {
            booking.setBookingStatus(bookingDto.getBookingStatus());
            log.info("Booking {} status updated to {}", bookingDto.getBookingId(), bookingDto.getBookingStatus());
        }

        bookingRepo.save(booking);
    }
}