package com.movie.ticket.booking.system.service;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;

public interface EmailService {
    void sendBookingConfirmation(BookingDto bookingDto);
}