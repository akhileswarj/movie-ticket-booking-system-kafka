package com.movie.ticket.booking.system.scheduler;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingStatus;
import com.movie.ticket.booking.system.entity.BookingEntity;
import com.movie.ticket.booking.system.repository.BookingRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class BookingCleanupScheduler {

    @Autowired
    private BookingRepo bookingRepo;

    // runs every 5 minutes
    @Scheduled(fixedRate = 5 * 60 * 1000)
    @Transactional
    public void cancelExpiredPendingBookings() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(22);

        List<BookingEntity> staleBookings = bookingRepo
                .findByBookingStatusAndCreatedAtBefore(BookingStatus.PENDING, cutoff);

        if (staleBookings.isEmpty()) {
            log.info("No stale PENDING bookings found.");
            return;
        }

        staleBookings.forEach(booking -> {
            booking.setBookingStatus(BookingStatus.CANCELLED);
            log.info("Auto-cancelled stale booking: {}, created at: {}",
                    booking.getBookingId(), booking.getCreatedAt());
        });

        bookingRepo.saveAll(staleBookings);
        log.info("Auto-cancelled {} stale PENDING booking(s).", staleBookings.size());
    }
}