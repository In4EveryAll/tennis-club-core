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
}
