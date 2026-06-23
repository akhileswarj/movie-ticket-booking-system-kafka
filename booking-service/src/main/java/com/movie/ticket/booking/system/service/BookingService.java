package com.movie.ticket.booking.system.service;


import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import com.example.democom.movie.ticket.booking.system.commons.dto.ResponseDto;

import java.util.UUID;

public interface BookingService {

    public ResponseDto createBooking(BookingDto bookingDto);

    void updateBookingStatus(UUID bookingId, String status);

    BookingDto getBookingById(UUID bookingId);
}
