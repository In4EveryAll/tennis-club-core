package com.in4everyall.tennisclubmanager.tennisclub.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppService {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromPhoneNumber;

    @PostConstruct
    public void init() {
        if (accountSid != null && !accountSid.isBlank() && authToken != null && !authToken.isBlank()) {
            Twilio.init(accountSid, authToken);
            System.out.println("Twilio inicializado correctamente.");
        } else {
            System.err.println("ADVERTENCIA: Credenciales de Twilio no configuradas. No se enviarán WhatsApps.");
        }
    }

    public void sendReservationReminder(String toPhoneNumber, String userName, String courtName,
            java.time.Instant startDatetime) {
        if (toPhoneNumber == null || toPhoneNumber.isBlank()) {
            System.err.println("No se puede enviar WhatsApp: Número de teléfono vacío.");
            return;
        }

        try {
            // Formatear fecha
            java.time.LocalDateTime localDateTime = java.time.LocalDateTime.ofInstant(startDatetime,
                    java.time.ZoneId.of("Europe/Madrid"));
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter
                    .ofPattern("dd/MM/yyyy 'a las' HH:mm");
            String formattedDate = localDateTime.format(formatter);

            String messageBody = "Hola " + userName + ", recordatorio de tu reserva en Club de Tenis Río Tormes.\n" +
                    "Pista: " + courtName + "\n" +
                    "Fecha: " + formattedDate + "\n" +
                    "¡Te esperamos!";

            // Twilio requiere el prefijo "whatsapp:"
            // En Sandbox el número 'from' también lleva "whatsapp:"
            // Asegúrate de que toPhoneNumber tiene el prefijo de país (ej. +34...)
            String to = toPhoneNumber.startsWith("whatsapp:") ? toPhoneNumber : "whatsapp:" + toPhoneNumber;
            String from = fromPhoneNumber.startsWith("whatsapp:") ? fromPhoneNumber : "whatsapp:" + fromPhoneNumber;

            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(from),
                    messageBody).create();

            System.out.println("WhatsApp enviado a " + to + ". SID: " + message.getSid());

        } catch (Exception e) {
            System.err.println("Error enviando WhatsApp a " + toPhoneNumber + ": " + e.getMessage());
            // No lanzamos excepción para no interrumpir el flujo de otros recordatorios
        }
    }
}
