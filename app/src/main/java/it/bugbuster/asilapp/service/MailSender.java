package it.bugbuster.asilapp.service;
import android.graphics.Bitmap;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import jakarta.mail.util.ByteArrayDataSource;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MailSender {

    private static void sendEmail(String toEmail, String subject, String body, Bitmap bitmap) {
        String fromEmail = "REMOVED_SECRET";
        String password = "REMOVED_SECRET";

        // SMTP server details for Gmail
        String host = "smtp.gmail.com";
        String port = "587";

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        // Create the session
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            // Create a MimeMessage
            MimeMessage message = new MimeMessage(session);

            // Set the from and to addresses
            message.setFrom(new InternetAddress(fromEmail, "AsilApp"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));

            // Set the subject and body
            message.setSubject(subject);

            // Create a Multipart to add the body and attachment
            Multipart multipart = new MimeMultipart();

            // Create the body part for the text message
            MimeBodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body, "utf-8", "html");
            multipart.addBodyPart(messageBodyPart);

            // Add the bitmap image as an attachment
            if (bitmap != null) {
                byte[] imageBytes = bitmapToByteArray(bitmap);
                MimeBodyPart imagePart = new MimeBodyPart();
                DataSource source = new ByteArrayDataSource(imageBytes, "image/png");
                imagePart.setDataHandler(new DataHandler(source));
                imagePart.setFileName("QRCodeAsilApp.png");
                imagePart.setHeader("Content-ID", "<qrcode>");
                imagePart.setDisposition(MimeBodyPart.INLINE);

                multipart.addBodyPart(imagePart);

                String htmlContent = "<html><body>"
                        + "<img src='cid:qrcode' width='200' height='200' style='max-width: 100%; height: auto;'/>"
                        + "</body></html>";

                MimeBodyPart htmlPart = new MimeBodyPart();
                htmlPart.setContent(htmlContent, "text/html");
                multipart.addBodyPart(htmlPart);
            }

            // Set the multipart as the message content
            message.setContent(multipart);

            // Send the email
            Transport.send(message);

        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void sendEmailService(String toEmail, String subject, String body, Bitmap qrCode) {
        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                sendEmail(toEmail, subject, body, qrCode);
            }
        });

        executorService.shutdown();
    }

    // Method to convert Bitmap to byte array
    private static byte[] bitmapToByteArray(android.graphics.Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
