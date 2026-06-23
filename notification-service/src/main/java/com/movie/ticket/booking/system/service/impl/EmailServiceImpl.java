package com.movie.ticket.booking.system.service.impl;

import com.example.democom.movie.ticket.booking.system.commons.dto.BookingDto;
import com.movie.ticket.booking.system.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailUsername;

    @Override
    public void sendBookingConfirmation(BookingDto bookingDto) {
        log.info("Attempting to send email from: {}", mailUsername);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(bookingDto.getUserEmail());
            helper.setSubject("🎬 Booking Confirmed — Movie ID " + bookingDto.getMovieId());
            helper.setText(buildHtml(bookingDto), true);

            mailSender.send(message);
            log.info("Confirmation email sent to {} for bookingId: {}",
                    bookingDto.getUserEmail(), bookingDto.getBookingId());

        } catch (MessagingException e) {
            log.error("Failed to send email for bookingId {}: {}",
                    bookingDto.getBookingId(), e.getMessage());
        }
    }

    private String buildHtml(BookingDto b) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif; padding: 30px;
                         max-width: 480px; margin: auto; background: #f9f9f9;">
                <div style="background: white; padding: 30px; border-radius: 8px;
                            box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
                    <h2 style="color: #2e7d32; margin-top: 0;">
                        &#10004; Booking Confirmed!
                    </h2>
                    <hr style="border: none; border-top: 1px solid #eee;"/>
                    <table style="width: 100%%; border-collapse: collapse; margin-top: 16px;">
                        <tr style="border-bottom: 1px solid #f0f0f0;">
                            <td style="padding: 8px 0; color: #666;">Booking ID</td>
                            <td style="padding: 8px 0; font-weight: bold;">%s</td>
                        </tr>
                         <tr style="border-bottom: 1px solid #f0f0f0;">
                            <td style="padding: 8px 0; color: #666;">User ID</td>
                            <td style="padding: 8px 0;">%s</td>
                        </tr>
                        <tr style="border-bottom: 1px solid #f0f0f0;">
                            <td style="padding: 8px 0; color: #666;">Movie ID</td>
                            <td style="padding: 8px 0;">%s</td>
                        </tr>
                        <tr style="border-bottom: 1px solid #f0f0f0;">
                            <td style="padding: 8px 0; color: #666;">Seats</td>
                            <td style="padding: 8px 0;">%s</td>
                        </tr>
                        <tr style="border-bottom: 1px solid #f0f0f0;">
                            <td style="padding: 8px 0; color: #666;">Show Date</td>
                            <td style="padding: 8px 0;">%s</td>
                        </tr>
                        <tr style="border-bottom: 1px solid #f0f0f0;">
                            <td style="padding: 8px 0; color: #666;">Show Time</td>
                            <td style="padding: 8px 0;">%s</td>
                        </tr>
                        <tr>
                            <td style="padding: 8px 0; color: #666;">Amount Paid</td>
                            <td style="padding: 8px 0; color: #2e7d32; font-weight: bold;">
                                &#8377;%s
                            </td>
                        </tr>
                    </table>
                    <p style="margin-top: 24px; color: #555;">
                        Enjoy your movie! &#127902;
                    </p>
                </div>
            </body>
            </html>
            """.formatted(
                b.getBookingId(),
                b.getUserId(),
                b.getMovieId(),
                b.getSeatsSelected(),
                b.getShowDate(),
                b.getShowTime(),
                b.getBookingAmount()
        );
    }
}