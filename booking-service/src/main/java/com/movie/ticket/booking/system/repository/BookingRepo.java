package com.movie.ticket.booking.system.repository;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import com.example.democom.movie.ticket.booking.system.commons.dto.BookingStatus;
import com.movie.ticket.booking.system.entity.BookingEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Book;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface BookingRepo extends CrudRepository<BookingEntity, UUID> {
    BookingEntity findByUserIdAndMovieIdAndSeatSignatureAndShowDateAndShowTimeAndBookingStatus(
            String userId,
            Integer movieId,
            String seatSignature,
            LocalDate showDate,
            LocalTime showTime,
            BookingStatus bookingStatus
    );

    // used for idempotency check — prevents duplicate bookings on retries
    BookingEntity findByUserIdAndMovieIdAndShowDateAndShowTimeAndBookingStatus(
            String userId,
            Integer movieId,
            LocalDate showDate,
            LocalTime showTime,
            BookingStatus bookingStatus
    );

    // finds all PENDING bookings older than given time
    List<BookingEntity> findByBookingStatusAndCreatedAtBefore(
            BookingStatus bookingStatus,
            LocalDateTime cutoff
    );
}
