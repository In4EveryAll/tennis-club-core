package com.in4everyall.tennisclubmanager.tennisclub.repository;

import com.in4everyall.tennisclubmanager.tennisclub.entity.CalendarEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CalendarEventRepository extends JpaRepository<CalendarEventEntity, UUID> {
       List<CalendarEventEntity> findByContractId(UUID contractId);

       List<CalendarEventEntity> findByServiceId(UUID serviceId);

       List<CalendarEventEntity> findByEventType(CalendarEventEntity.EventType eventType);

       // Consulta optimizada con fetch joins para evitar LazyInitializationException
       // Incluye fetch join de user para reservas (necesario para mostrar información
       // del usuario cuando es ADMIN)
       @Query("SELECT e FROM CalendarEventEntity e " +
                     "LEFT JOIN FETCH e.service s " +
                     "LEFT JOIN FETCH e.monitor m " +
                     "LEFT JOIN FETCH e.user u " +
                     "LEFT JOIN FETCH e.court c " +
                     "WHERE e.startDatetime >= :start AND e.startDatetime <= :end " +
                     "ORDER BY e.startDatetime ASC")
       List<CalendarEventEntity> findByStartDatetimeBetween(@Param("start") Instant start, @Param("end") Instant end);

       // Buscar reservas de una pista en un rango de fechas (solo RESERVATIONS, no
       // CLASSES)
       // Con fetch join para cargar relaciones (court y user) necesarias para mostrar
       // información del usuario
       @Query("SELECT e FROM CalendarEventEntity e " +
                     "LEFT JOIN FETCH e.court " +
                     "LEFT JOIN FETCH e.user " +
                     "WHERE e.court.id = :courtId " +
                     "AND e.eventType = 'RESERVATION' " +
                     "AND e.startDatetime >= :startDate " +
                     "AND e.startDatetime < :endDate " +
                     "AND e.status != 'CANCELLED' " +
                     "ORDER BY e.startDatetime ASC")
       List<CalendarEventEntity> findByCourtIdAndDateRange(
                     @Param("courtId") UUID courtId,
                     @Param("startDate") Instant startDate,
                     @Param("endDate") Instant endDate);

       // Buscar eventos que solapen con un rango de tiempo (para validar
       // disponibilidad de reservas)
       // Solo busca RESERVATIONS en una pista específica
       @Query("SELECT e FROM CalendarEventEntity e " +
                     "WHERE e.court.id = :courtId " +
                     "AND e.eventType = 'RESERVATION' " +
                     "AND e.status != 'CANCELLED' " +
                     "AND (" +
                     "  (e.startDatetime < :endDatetime AND e.endDatetime > :startDatetime)" +
                     ")")
       List<CalendarEventEntity> findOverlappingEvents(
                     @Param("courtId") UUID courtId,
                     @Param("startDatetime") Instant startDatetime,
                     @Param("endDatetime") Instant endDatetime);

       // Buscar clases que solapen con un rango de tiempo (las clases bloquean todas
       // las pistas)
       // Busca clases que NO tienen court_id (court IS NULL) o que tienen court_id
       // explícitamente NULL
       @Query("SELECT e FROM CalendarEventEntity e " +
                     "WHERE e.eventType = 'CLASS' " +
                     "AND e.status != 'CANCELLED' " +
                     "AND (e.court IS NULL OR e.court.id IS NULL) " +
                     "AND (" +
                     "  (e.startDatetime < :endDatetime AND e.endDatetime > :startDatetime)" +
                     ")")
       List<CalendarEventEntity> findOverlappingClasses(
                     @Param("startDatetime") Instant startDatetime,
                     @Param("endDatetime") Instant endDatetime);

       // Método alternativo: buscar todas las clases del día sin filtrar por
       // solapamiento
       // Útil para debugging
       @Query("SELECT e FROM CalendarEventEntity e " +
                     "WHERE e.eventType = 'CLASS' " +
                     "AND e.status != 'CANCELLED' " +
                     "AND (e.court IS NULL OR e.court.id IS NULL) " +
                     "AND e.startDatetime >= :dayStart " +
                     "AND e.startDatetime < :dayEnd " +
                     "ORDER BY e.startDatetime ASC")
       List<CalendarEventEntity> findClassesByDateRange(
                     @Param("dayStart") Instant dayStart,
                     @Param("dayEnd") Instant dayEnd);

       // Buscar reservas de un usuario (con fetch join para cargar relaciones)
       @Query("SELECT e FROM CalendarEventEntity e " +
                     "LEFT JOIN FETCH e.court " +
                     "LEFT JOIN FETCH e.user " +
                     "WHERE e.eventType = 'RESERVATION' " +
                     "AND e.user.email = :userEmail " +
                     "AND e.status != 'CANCELLED' " +
                     "ORDER BY e.startDatetime DESC")
       List<CalendarEventEntity> findReservationsByUserEmail(@Param("userEmail") String userEmail);

       // Buscar evento por ID con todas las relaciones cargadas (para reservas)
       @Query("SELECT e FROM CalendarEventEntity e " +
                     "LEFT JOIN FETCH e.court " +
                     "LEFT JOIN FETCH e.user " +
                     "WHERE e.id = :eventId")
       java.util.Optional<CalendarEventEntity> findByIdWithRelations(@Param("eventId") UUID eventId);

       // Buscar reservas que empiezan en un rango y no se ha enviado recordatorio
       @Query("SELECT e FROM CalendarEventEntity e " +
                     "LEFT JOIN FETCH e.court " +
                     "LEFT JOIN FETCH e.user " +
                     "WHERE e.eventType = 'RESERVATION' " +
                     "AND e.status = 'CONFIRMED' " +
                     "AND e.reminderSent = false " +
                     "AND e.startDatetime >= :start " +
                     "AND e.startDatetime <= :end")
       List<CalendarEventEntity> findEventsForReminder(
                     @Param("start") Instant start,
                     @Param("end") Instant end);
}
