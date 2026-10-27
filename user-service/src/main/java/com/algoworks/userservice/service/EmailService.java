package com.algoworks.userservice.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) throws IOException {

        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);

        String subject = "Train Ticket Booking - Password Reset OTP";

        Content content = new Content("text/plain", "Your password reset OTP is: " + otp
                        + "\nThis OTP is valid for 5 minutes."
                        + "\nIf you did not request this, please ignore this email."
        );

        // Creates the sender, recipient, subject and message
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sendGrid = new SendGrid(System.getenv("SENDGRID_API_KEY")); // authenticates using an API key

        // Sends the email request
        Request request = new Request();

        try {
            request.setMethod(Method.POST); // asks SendGrid to process a new email-sending request.
            request.setEndpoint("mail/send"); // specifies the SendGrid end-point for sending e-mails.
            request.setBody(mail.build()); // converts the prepared email object into the request body format expected by SendGrid.

            Response response = sendGrid.api(request); // actually makes the API call.

            if (response.getStatusCode() < 200 || response.getStatusCode() >= 300) {
// accepts status codes from 200 through 299 as successful HTTP responses. Any other status causes an IOException.
                throw new IOException("Email service failed with status " + response.getStatusCode());
            }

        } catch (IOException ex) {
            throw ex;
        }
    }
}
