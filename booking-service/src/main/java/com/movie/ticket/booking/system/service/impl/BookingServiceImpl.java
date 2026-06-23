package com.movie.ticket.booking.system.service.impl;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import com.example.democom.movie.ticket.booking.system.commons.dto.BookingStatus;
import com.example.democom.movie.ticket.booking.system.commons.dto.ResponseDto;
import com.movie.ticket.booking.system.broker.PaymentServiceBroker;
import com.movie.ticket.booking.system.entity.BookingEntity;
import com.movie.ticket.booking.system.kafka.BookingKafkaProducer;
import com.movie.ticket.booking.system.repository.BookingRepo;
import com.movie.ticket.booking.system.service.BookingService;
import com.movie.ticket.booking.system.utility.SeatSignatureUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class BookingServiceImpl implements BookingService {

    @Autowired
    private BookingRepo bookingRepo;

    @Autowired
    private PaymentServiceBroker paymentService;

    @Autowired
    private BookingKafkaProducer bookingKafkaProducer;

    @Override
    @Transactional
    public ResponseDto createBooking(BookingDto bookingDto) {
        log.info("entered into BookingService createBooking method with request data {}", bookingDto.toString());

        String incomingSignature = SeatSignatureUtil.generate(bookingDto.getSeatsSelected());

// ── Step 1: Check for PENDING (continue payment flow) ─────────────────
        BookingEntity pendingBooking = bookingRepo
                .findByUserIdAndMovieIdAndSeatSignatureAndShowDateAndShowTimeAndBookingStatus(
                        bookingDto.getUserId(), bookingDto.getMovieId(), incomingSignature,
                        bookingDto.getShowDate(), bookingDto.getShowTime(), BookingStatus.PENDING
                );

        if (pendingBooking != null) {
            log.warn("PENDING booking exists for same data — returning existing bookingId: {}",
                    pendingBooking.getBookingId());
            return ResponseDto.builder()
                    .bookingDto(BookingDto.builder()
                            .bookingId(pendingBooking.getBookingId())
                            .userId(pendingBooking.getUserId())
                            .userEmail(pendingBooking.getUserEmail())
                            .movieId(pendingBooking.getMovieId())
                            .seatsSelected(pendingBooking.getSeatsSelected())
                            .showDate(pendingBooking.getShowDate())
                            .showTime(pendingBooking.getShowTime())
                            .bookingAmount(pendingBooking.getBookingAmount())
                            .bookingStatus(pendingBooking.getBookingStatus())
                            .paymentLink(pendingBooking.getPaymentLink())
                            .build())
                    .build();
        }

// ── Step 2: Check for CONFIRMED (block duplicate confirmed booking) ────
        BookingEntity confirmedBooking = bookingRepo
                .findByUserIdAndMovieIdAndSeatSignatureAndShowDateAndShowTimeAndBookingStatus(
                        bookingDto.getUserId(), bookingDto.getMovieId(), incomingSignature,
                        bookingDto.getShowDate(), bookingDto.getShowTime(), BookingStatus.CONFIRMED
                );

        if (confirmedBooking != null) {
            log.warn("CONFIRMED booking already exists for same data — returning existing bookingId: {}",
                    confirmedBooking.getBookingId());
            return ResponseDto.builder()
                    .bookingDto(BookingDto.builder()
                            .bookingId(confirmedBooking.getBookingId())
                            .userId(confirmedBooking.getUserId())
                            .userEmail(confirmedBooking.getUserEmail())
                            .movieId(confirmedBooking.getMovieId())
                            .seatsSelected(confirmedBooking.getSeatsSelected())
                            .showDate(confirmedBooking.getShowDate())
                            .showTime(confirmedBooking.getShowTime())
                            .bookingAmount(confirmedBooking.getBookingAmount())
                            .bookingStatus(confirmedBooking.getBookingStatus())
                            .paymentLink(confirmedBooking.getPaymentLink())
                            .build())
                    .build();
        }

// ── Step 3: CANCELLED or no record → create fresh booking ─────────────
// ... rest of your createBooking logic unchanged

        // ── Save booking as PENDING ────────────────────────────────────────
        BookingEntity bookingEntity = BookingEntity.builder()
                .movieId(bookingDto.getMovieId())
                .bookingStatus(BookingStatus.PENDING)
                .seatsSelected(bookingDto.getSeatsSelected())
                .showDate(bookingDto.getShowDate())
                .showTime(bookingDto.getShowTime())
                .userId(bookingDto.getUserId())
                .userEmail(bookingDto.getUserEmail())
                .bookingAmount(bookingDto.getBookingAmount())
                .build();
        bookingRepo.save(bookingEntity);

        bookingDto.setBookingId(bookingEntity.getBookingId());
        bookingDto.setBookingStatus(BookingStatus.PENDING);
        // ── Publish to Kafka (async — no waiting, no timeout) ──────────────
        bookingKafkaProducer.publishPaymentRequest(bookingDto);
        log.info("Published to payment-request topic for bookingId: {}", bookingEntity.getBookingId());

        // ── Return 202-style response immediately ──────────────────────────
        // paymentLink will be null here — client polls GET /bookings/{id}
        return ResponseDto.builder()
                .bookingDto(BookingDto.builder()
                        .bookingId(bookingEntity.getBookingId())
                        .userId(bookingEntity.getUserId())
                        .userEmail(bookingEntity.getUserEmail())
                        .movieId(bookingEntity.getMovieId())
                        .seatsSelected(bookingEntity.getSeatsSelected())
                        .showDate(bookingEntity.getShowDate())
                        .showTime(bookingEntity.getShowTime())
                        .bookingAmount(bookingEntity.getBookingAmount())
                        .bookingStatus(BookingStatus.PENDING)
                        .build())
                .build();
    }


    @Override
    @Transactional
    public void updateBookingStatus(UUID bookingId, String status) {
        BookingEntity booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        booking.setBookingStatus(BookingStatus.valueOf(status));
        bookingRepo.save(booking);
        log.info("Booking {} status updated to {}", bookingId, status);
    }

    @Override
    public BookingDto getBookingById(UUID bookingId) {
        BookingEntity booking = bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + bookingId));
        return BookingDto.builder()
                .bookingId(booking.getBookingId())
                .userId(booking.getUserId())
                .userEmail(booking.getUserEmail())
                .movieId(booking.getMovieId())
                .seatsSelected(booking.getSeatsSelected())
                .showDate(booking.getShowDate())
                .showTime(booking.getShowTime())
                .bookingAmount(booking.getBookingAmount())
                .bookingStatus(booking.getBookingStatus())
                .paymentLink(booking.getPaymentLink())
                .build();
    }
}
