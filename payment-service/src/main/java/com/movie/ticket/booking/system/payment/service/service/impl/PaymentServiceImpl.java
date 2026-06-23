package com.movie.ticket.booking.system.payment.service.service.impl;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import com.example.democom.movie.ticket.booking.system.commons.dto.BookingStatus;
import com.movie.ticket.booking.system.payment.service.broker.BookingServiceBroker;
import com.movie.ticket.booking.system.payment.service.entity.PaymentEntity;
import com.movie.ticket.booking.system.payment.service.entity.PaymentStatus;
import com.movie.ticket.booking.system.payment.service.kafka.PaymentKafkaProducer;
import com.movie.ticket.booking.system.payment.service.repository.PaymentRepo;
import com.movie.ticket.booking.system.payment.service.service.PaymentService;
import com.movie.ticket.booking.system.payment.service.service.RazorpayApiPaymentGateway;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Autowired
    private PaymentRepo paymentRepo;

    @Autowired
    private RazorpayApiPaymentGateway razorpayApi;

    @Autowired
    private BookingServiceBroker bookingServiceBroker;

    @Autowired
    private PaymentKafkaProducer paymentKafkaProducer;

    @Override
    @Transactional(rollbackOn = RazorpayException.class)
    public BookingDto makePayment(BookingDto bookingDto) {
        log.info("entered into PaymentService with Received bookingDto: " + bookingDto.toString());

        bookingDto = razorpayApi.processPayment(bookingDto);

        PaymentEntity paymentEntity = PaymentEntity.builder()
                .bookingId(bookingDto.getBookingId())
                .paymentStatus(PaymentStatus.PENDING)
                .paymentAmount(bookingDto.getBookingAmount())
                .orderId(bookingDto.getRazorpayOrderId())
                .paymentLink(bookingDto.getPaymentLink())
                .build();
        paymentRepo.save(paymentEntity);

        // if payment link creation failed, mark as FAILED
        if(bookingDto.getBookingStatus().equals(BookingStatus.CANCELLED)){
            log.warn("Payment link creation failed for bookingId: {}", bookingDto.getBookingId());
            paymentEntity.setPaymentStatus(PaymentStatus.FAILED);
            paymentEntity.setPaymentTimeStamp(LocalDateTime.now());
        }
        // if PENDING, leave it as PENDING — customer hasn't paid yet

        return bookingDto;
    }


    @Override
    @Transactional
    public BookingDto handlePaymentCallback(String paymentId, String paymentLinkId, String orderId, String status, String signature) {
        log.info("Verifying signature for paymentLinkId: {}", paymentLinkId);
        log.info("orderId: {}", orderId);
        log.info("status: {}", status);
        log.info("paymentId: {}", paymentId);
        log.info("signature: {}", signature);

        try {
            JSONObject options = new JSONObject();
            options.put("payment_link_id", paymentLinkId);
            options.put("payment_link_reference_id", orderId);
            options.put("payment_link_status", status);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);

            boolean isValid = Utils.verifyPaymentLink(options, keySecret);
            log.info("Signature valid: {}", isValid); // ← key log

            if (isValid) {
                PaymentEntity payment = paymentRepo.findByOrderId(orderId);
                if (payment != null) {
                    // ← guard: if already APPROVED/FAILED, don't republish
                    if (payment.getPaymentStatus() == PaymentStatus.APPROVED
                            || payment.getPaymentStatus() == PaymentStatus.FAILED) {
                        log.warn("Callback already processed for orderId: {} — skipping", orderId);
                        return bookingServiceBroker.getBookingById(payment.getBookingId());
                    }

                    BookingStatus newStatus;
                    if (status.equals("paid")) {
                        payment.setPaymentStatus(PaymentStatus.APPROVED);
                        newStatus = BookingStatus.CONFIRMED;
                    } else {
                        payment.setPaymentStatus(PaymentStatus.FAILED);
                        newStatus = BookingStatus.CANCELLED;
                    }
                    payment.setPaymentTimeStamp(LocalDateTime.now());
                    paymentRepo.save(payment);

                    BookingDto fullBooking = bookingServiceBroker.getBookingById(payment.getBookingId());
                    fullBooking.setBookingStatus(newStatus);

                    paymentKafkaProducer.publishPaymentResponse(fullBooking);
                    log.info("Published {} status to payment-response for bookingId: {}",
                            newStatus, payment.getBookingId());

                    // Feign kept here — fast read for building HTML page
                    return fullBooking;

                } else {
                    log.error("No payment entity found for orderId: {}", orderId);
                }
            } else {
                log.error("Signature verification FAILED");
            }

        } catch (RazorpayException e) {
            log.error("Error verifying signature: {}", e.getMessage());
        }
        return BookingDto.builder().build();
    }
}
