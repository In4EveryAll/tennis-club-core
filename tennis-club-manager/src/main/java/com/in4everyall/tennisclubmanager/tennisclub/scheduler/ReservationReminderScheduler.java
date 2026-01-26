package com.in4everyall.tennisclubmanager.tennisclub.scheduler;

import com.in4everyall.tennisclubmanager.tennisclub.entity.CalendarEventEntity;
import com.in4everyall.tennisclubmanager.tennisclub.repository.CalendarEventRepository;
import com.in4everyall.tennisclubmanager.tennisclub.service.EmailService;
import com.in4everyall.tennisclubmanager.tennisclub.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationReminderScheduler {

    private final CalendarEventRepository calendarEventRepository;
    private final EmailService emailService;
    private final WhatsAppService whatsAppService;

    // Se ejecuta cada 15 minutos (para asegurar que cogemos las reservas a tiempo)
    // @Scheduled(cron = "0 0/1 * * * *")
    @Transactional
    public void sendReservationReminders() {
        System.out.println("Ejecutando planificador de recordatorios...");

        Instant now = Instant.now();

        // CAMBIO DE LÓGICA:
        // Buscamos cualquier reserva futura que empiece en las próximas 25 horas
        // y que NO haya sido notificada aún.
        // Así aseguramos que si el servidor estuvo apagado, al encenderse las pille.
        Instant startWindow = now;
        Instant endWindow = now.plus(25, ChronoUnit.HOURS);

        System.out.println("DEBUG: Buscando reservas SIN AVISAR entre " + startWindow + " y " + endWindow + " (UTC)");

        List<CalendarEventEntity> upcomingReservations = calendarEventRepository.findEventsForReminder(startWindow,
                endWindow);

        System.out.println("Encontradas " + upcomingReservations.size() + " reservas para recordar.");

        for (CalendarEventEntity event : upcomingReservations) {
            try {
                if (event.getUser() != null) {
                    // 1. Enviar Email
                    if (event.getUser().getEmail() != null) {
                        emailService.sendReservationReminder(
                                event.getUser().getEmail(),
                                event.getUser().getFirstName(),
                                event.getCourt() != null ? event.getCourt().getName() : "Pista de Tenis",
                                event.getStartDatetime());
                    }

                    // 2. Enviar WhatsApp (si tiene teléfono)
                    if (event.getUser().getPhone() != null && !event.getUser().getPhone().isBlank()) {
                        whatsAppService.sendReservationReminder(
                                event.getUser().getPhone(),
                                event.getUser().getFirstName(),
                                event.getCourt() != null ? event.getCourt().getName() : "Pista de Tenis",
                                event.getStartDatetime());
                    }

                    event.setReminderSent(true);
                    calendarEventRepository.save(event);
                    System.out.println("Recordatorio procesado para " + event.getUser().getEmail());
                }
            } catch (Exception e) {
                System.err.println("Error al enviar recordatorio para evento " + event.getId() + ": " + e.getMessage());
            }
        }
    }
}
