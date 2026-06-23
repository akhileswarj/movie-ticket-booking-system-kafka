package com.movie.ticket.booking.system.payment.service.service;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import com.example.democom.movie.ticket.booking.system.commons.dto.BookingStatus;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RazorpayApiPaymentGateway {

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Value("${razorpay.callback-url}")
    private String callbackUrl;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() {
        try {
            razorpayClient = new RazorpayClient(keyId, keySecret);
        } catch (RazorpayException e) {
            log.error("Failed to initialize Razorpay client: " + e.getMessage());
            throw new RuntimeException("Razorpay initialization failed", e);
        }
    }

    public BookingDto processPayment(BookingDto bookingDTO) {
        log.info("entered into RazorpayApiPaymentGateway with request {}", bookingDTO.toString());
        try {
            // Step 1: Create Order
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", bookingDTO.getBookingAmount() * 100);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", "txn_" + System.currentTimeMillis());
            orderRequest.put("payment_capture", 1);

            JSONObject notes = new JSONObject();
            notes.put("description", "Test payment for booking service");
            orderRequest.put("notes", notes);

            Order order = razorpayClient.orders.create(orderRequest);
            String orderId = order.get("id");
            bookingDTO.setRazorpayOrderId(orderId);
            bookingDTO.setBookingStatus(BookingStatus.PENDING);

            // Step 2: Create Payment Link linked to that Order
            JSONObject paymentLinkRequest = new JSONObject();
            paymentLinkRequest.put("amount", bookingDTO.getBookingAmount() * 100);
            paymentLinkRequest.put("currency", "INR");
            paymentLinkRequest.put("description", "Movie Ticket Booking Payment");
            paymentLinkRequest.put("reference_id", orderId); // link to order

            JSONObject customer = new JSONObject();
            customer.put("name", "Movie Customer");
            customer.put("email", "customer@test.com");
            customer.put("contact", "9876543210");
            paymentLinkRequest.put("customer", customer);

            paymentLinkRequest.put("callback_url", callbackUrl);
            paymentLinkRequest.put("callback_method", "get");

            // Calculate Unix timestamp for 20 minutes from now (Current time in seconds + 20 * 60)
            long expiryUnixTimestamp = (System.currentTimeMillis() / 1000) + (20 * 60);
            paymentLinkRequest.put("expire_by", expiryUnixTimestamp);


            com.razorpay.PaymentLink paymentLink = razorpayClient.paymentLink.create(paymentLinkRequest);
            String shortUrl = paymentLink.get("short_url");
            log.info("Payment Link Created: " + shortUrl);

            bookingDTO.setPaymentLink(shortUrl); // ← set the link

        } catch (RazorpayException e) {
            log.error("Error encountered during payment process: " + e.getMessage());
            bookingDTO.setBookingStatus(BookingStatus.CANCELLED);
        }

        return bookingDTO;
    }
}