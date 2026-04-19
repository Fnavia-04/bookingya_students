package com.project.bookingya.services;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.entities.ReservationEntity;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.exceptions.BusinessRuleException;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.repositories.IReservationRepository;
import com.project.bookingya.repositories.IRoomRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * FASE 1 - TDD
 * Servicio de Reservaciones que implementa la lógica de negocio.
 * 
 * Validaciones de negocio:
 * - checkIn debe ser anterior a checkOut
 * - La habitación debe existir y estar disponible
 * - El huésped debe existir
 * - No puede haber solapamiento de reservas (habitación y huésped)
 * - guestsCount no puede exceder maxGuests de la habitación
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {

    private final IReservationRepository reservationRepository;
    private final IRoomRepository roomRepository;
    private final IGuestRepository guestRepository;
    private final ModelMapper mapper;

    // =========================================================================
    // CREATE
    // =========================================================================

    public Reservation create(ReservationDto dto) {
        // Validación 1: checkIn debe ser anterior a checkOut
        if (dto.getCheckIn().isAfter(dto.getCheckOut()) || dto.getCheckIn().isEqual(dto.getCheckOut())) {
            throw new BusinessRuleException("checkIn must be before checkOut");
        }

        // Validación 2: La habitación debe existir
        RoomEntity room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new EntityNotExistsException("Room not found"));

        // Validación 3: El huésped debe existir
        GuestEntity guest = guestRepository.findById(dto.getGuestId())
                .orElseThrow(() -> new EntityNotExistsException("Guest not found"));

        // Validación 4: La habitación debe estar disponible
        if (!room.getAvailable()) {
            throw new BusinessRuleException("Room is not available");
        }

        // Validación 5: guestsCount no puede exceder maxGuests
        if (dto.getGuestsCount() > room.getMaxGuests()) {
            throw new BusinessRuleException("guestsCount exceeds room capacity");
        }

        // Validación 6: No hay solapamiento en la habitación
        if (reservationRepository.existsOverlappingReservationForRoom(
                dto.getRoomId(), dto.getCheckIn(), dto.getCheckOut(), null)) {
            throw new BusinessRuleException("The room already has a reservation in that time range");
        }

        // Validación 7: No hay solapamiento con otras reservas del huésped
        if (reservationRepository.existsOverlappingReservationForGuest(
                dto.getGuestId(), dto.getCheckIn(), dto.getCheckOut(), null)) {
            throw new BusinessRuleException("The guest already has a reservation in that time range");
        }

        // Mapear DTO a Entity
        ReservationEntity entity = mapper.map(dto, ReservationEntity.class);
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());

        // Guardar
        ReservationEntity saved = reservationRepository.saveAndFlush(entity);

        // Mapear Entity a Model y retornar
        return mapper.map(saved, Reservation.class);
    }

    // =========================================================================
    // READ
    // =========================================================================

    @Transactional(readOnly = true)
    public List<Reservation> getAll() {
        List<ReservationEntity> entities = reservationRepository.findAll();
        Type listType = new TypeToken<List<Reservation>>() {}.getType();
        return mapper.map(entities, listType);
    }

    @Transactional(readOnly = true)
    public Reservation getById(UUID id) {
        ReservationEntity entity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotExistsException("Reservation not found"));
        return mapper.map(entity, Reservation.class);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getByRoomId(UUID roomId) {
        List<ReservationEntity> entities = reservationRepository.findByRoomId(roomId);
        Type listType = new TypeToken<List<Reservation>>() {}.getType();
        return mapper.map(entities, listType);
    }

    @Transactional(readOnly = true)
    public List<Reservation> getByGuestId(UUID guestId) {
        List<ReservationEntity> entities = reservationRepository.findByGuestId(guestId);
        Type listType = new TypeToken<List<Reservation>>() {}.getType();
        return mapper.map(entities, listType);
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    public Reservation update(ReservationDto dto, UUID id) {
        // Validación 1: La reserva debe existir
        ReservationEntity existing = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotExistsException("Reservation not found"));

        // Validación 2: checkIn debe ser anterior a checkOut
        if (dto.getCheckIn().isAfter(dto.getCheckOut()) || dto.getCheckIn().isEqual(dto.getCheckOut())) {
            throw new BusinessRuleException("checkIn must be before checkOut");
        }

        // Validación 3: La habitación debe existir
        RoomEntity room = roomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new EntityNotExistsException("Room not found"));

        // Validación 4: El huésped debe existir
        GuestEntity guest = guestRepository.findById(dto.getGuestId())
                .orElseThrow(() -> new EntityNotExistsException("Guest not found"));

        // Validación 5: La habitación debe estar disponible
        if (!room.getAvailable()) {
            throw new BusinessRuleException("Room is not available");
        }

        // Validación 6: guestsCount no puede exceder maxGuests
        if (dto.getGuestsCount() > room.getMaxGuests()) {
            throw new BusinessRuleException("guestsCount exceeds room capacity");
        }

        // Validación 7: No hay solapamiento en la habitación (excluyendo esta reserva)
        if (reservationRepository.existsOverlappingReservationForRoom(
                dto.getRoomId(), dto.getCheckIn(), dto.getCheckOut(), id)) {
            throw new BusinessRuleException("The room already has a reservation in that time range");
        }

        // Validación 8: No hay solapamiento con otras reservas del huésped (excluyendo esta reserva)
        if (reservationRepository.existsOverlappingReservationForGuest(
                dto.getGuestId(), dto.getCheckIn(), dto.getCheckOut(), id)) {
            throw new BusinessRuleException("The guest already has a reservation in that time range");
        }

        // Actualizar campos
        existing.setRoomId(dto.getRoomId());
        existing.setGuestId(dto.getGuestId());
        existing.setCheckIn(dto.getCheckIn());
        existing.setCheckOut(dto.getCheckOut());
        existing.setGuestsCount(dto.getGuestsCount());
        existing.setUpdatedAt(LocalDateTime.now());

        // Guardar
        ReservationEntity saved = reservationRepository.saveAndFlush(existing);

        // Mapear y retornar
        return mapper.map(saved, Reservation.class);
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    public void delete(UUID id) {
        ReservationEntity entity = reservationRepository.findById(id)
                .orElseThrow(() -> new EntityNotExistsException("Reservation not found"));
        reservationRepository.delete(entity);
        reservationRepository.flush();
    }

    // =========================================================================
    // AVAILABILITY
    // =========================================================================

    @Transactional(readOnly = true)
    public boolean isRoomAvailable(UUID roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        // Validación: checkIn debe ser anterior a checkOut
        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) {
            throw new BusinessRuleException("checkIn must be before checkOut");
        }

        // Verificar que la habitación existe
        roomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotExistsException("Room not found"));

        // Retornar true si NO hay solapamiento
        return !reservationRepository.existsOverlappingReservationForRoom(roomId, checkIn, checkOut, null);
    }
}
