package com.movie.ticket.booking.system.entity;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingStatus;
import com.movie.ticket.booking.system.utility.SeatSignatureUtil;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookings")
@Builder
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_id")
    private UUID bookingId;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "user_email")
    private String userEmail;
    @Column(name = "movie_id")
    private Integer movieId;
    @ElementCollection
    private List<String> seatsSelected;
    @Column(name = "show_date")
    private LocalDate showDate;
    @Column(name = "show_time")
    private LocalTime showTime;
    @Enumerated(EnumType.STRING)
    private BookingStatus bookingStatus;
    @Column(name = "booking_amount")
    private Double bookingAmount;
    @Column(name = "payment_link")
    private String paymentLink;
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "seat_signature")
    private String seatSignature;


    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.seatSignature = SeatSignatureUtil.generate(this.seatsSelected);
    }
}
