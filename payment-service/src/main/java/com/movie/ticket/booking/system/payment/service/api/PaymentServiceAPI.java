package com.movie.ticket.booking.system.payment.service.api;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import com.movie.ticket.booking.system.payment.service.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("payments")
@Slf4j
public class PaymentServiceAPI {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public BookingDto makePayment(@RequestBody BookingDto bookingDto){
        log.info("entered into PaymentServiceAPI makePayment method with request {}", bookingDto.toString());
        return paymentService.makePayment(bookingDto);
    }

    @GetMapping
    public ResponseEntity<String> paymentCallback(
            @RequestParam("razorpay_payment_id") String paymentId,
            @RequestParam("razorpay_payment_link_id") String paymentLinkId,
            @RequestParam("razorpay_payment_link_reference_id") String orderId,
            @RequestParam("razorpay_payment_link_status") String status,
            @RequestParam("razorpay_signature") String signature) {

        log.info("Payment callback received - paymentId: {}, status: {}", paymentId, status);

        BookingDto booking = paymentService.handlePaymentCallback(
                paymentId, paymentLinkId, orderId, status, signature
        );
        // Format a nice response
        String html = """
        <html>
        <head><meta charset="UTF-8"></head>
        <body style="font-family: Arial; padding: 40px; max-width: 500px; margin: auto;">
            <h2 style="color: green;">&#10004; Payment Successful!</h2>
            <hr/>
            <table style="width:100%%; border-collapse: collapse;">
                <tr><td><b>Payment ID</b></td><td>%s</td></tr>
                <tr><td><b>Booking ID</b></td><td>%s</td></tr>
                <tr><td><b>User ID</b></td><td>%s</td></tr>
                <tr><td><b>Movie ID</b></td><td>%s</td></tr>
                <tr><td><b>Seats Selected</b></td><td>%s</td></tr>
                <tr><td><b>Show Date</b></td><td>%s</td></tr>
                <tr><td><b>Show Time</b></td><td>%s</td></tr>
                <tr><td><b>Amount Paid</b></td><td>&#8377;%s</td></tr>
                <tr><td><b>Booking Status</b></td><td style="color: green;"><b>%s</b></td></tr>
            </table>
        </body>
        </html>
        """.formatted(
                paymentId,
                booking.getBookingId(),
                booking.getUserId(),
                booking.getMovieId(),
                booking.getSeatsSelected(),
                booking.getShowDate(),
                booking.getShowTime(),
                booking.getBookingAmount(),
                booking.getBookingStatus()
        );

        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("text/html; charset=UTF-8"))
                .body(html);
    }
}
