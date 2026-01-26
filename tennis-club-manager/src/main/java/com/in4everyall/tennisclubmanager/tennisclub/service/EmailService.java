package com.in4everyall.tennisclubmanager.tennisclub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.mail.from:no-reply@tennisclubmanager.com}")
    private String from;

    public void sendPasswordResetEmail(String to, String resetUrl) {
        System.out.println("Intentando enviar email a: " + to);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Recuperación de contraseña - Tennis Club Manager");
        message.setText(
                "Hola,\n\n" +
                        "Has solicitado restablecer tu contraseña.\n" +
                        "Haz clic en el siguiente enlace (o cópialo en tu navegador) para establecer una nueva contraseña:\n\n"
                        +
                        resetUrl + "\n\n" +
                        "Si no has solicitado este cambio, puedes ignorar este mensaje.\n\n" +
                        "Un saludo,\n" +
                        "Club de Tenis Río Tormes");
        try {
            mailSender.send(message);
            System.out.println("Email enviado correctamente a: " + to);
        } catch (Exception e) {
            System.err.println("ERROR AL ENVIAR EMAIL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendReservationReminder(String to, String userName, String courtName, java.time.Instant startDatetime) {
        System.out.println("Enviando recordatorio de reserva a: " + to);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Recordatorio de Reserva - Tennis Club Manager");

        // Formatear fecha y hora
        java.time.LocalDateTime localDateTime = java.time.LocalDateTime.ofInstant(startDatetime,
                java.time.ZoneId.of("Europe/Madrid"));
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                .ofPattern("dd/MM/yyyy 'a las' HH:mm");
        String formattedDate = localDateTime.format(formatter);

        message.setText(
                "Hola " + userName + ",\n\n" +
                        "Te recordamos que tienes una reserva de pista mañana.\n\n" +
                        "Detalles de la reserva:\n" +
                        "- Pista: " + courtName + "\n" +
                        "- Fecha y Hora: " + formattedDate + "\n\n" +
                        "¡Te esperamos en la pista!\n\n" +
                        "Un saludo,\n" +
                        "Club de Tenis Río Tormes");

        try {
            mailSender.send(message);
            System.out.println("Recordatorio enviado correctamente a: " + to);
        } catch (Exception e) {
            System.err.println("ERROR AL ENVIAR RECORDATORIO: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
