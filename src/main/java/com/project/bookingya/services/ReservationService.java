package com.project.bookingya.services;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.entities.ReservationEntity;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.exceptions.BusinessRuleException;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Reservation;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.repositories.IReservationRepository;
import com.project.bookingya.repositories.IRoomRepository;
import com.project.bookingya.shared.Constants;

@Service
public class ReservationService {

    private final IReservationRepository reservationRepository;
    private final IRoomRepository roomRepository;
    private final IGuestRepository guestRepository;
    private final ModelMapper mapper;

    public ReservationService(
        IReservationRepository reservationRepository,
        IRoomRepository roomRepository,
        IGuestRepository guestRepository,
        ModelMapper mapper
    ) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.mapper = mapper;
    }

    public List<Reservation> getAll() {
        List<ReservationEntity> reservations = reservationRepository.findAll();
        Type listType = new TypeToken<List<Reservation>>() {}.getType();
        return mapper.map(reservations, listType);
    }

    public Reservation getById(UUID id) {
        ReservationEntity reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new EntityNotExistsException(Constants.RESERVATION_NOT_FOUND));
        return mapper.map(reservation, Reservation.class);
    }

    public List<Reservation> getByRoomId(UUID roomId) {
        List<ReservationEntity> reservations = reservationRepository.findByRoomId(roomId);
        Type listType = new TypeToken<List<Reservation>>() {}.getType();
        return mapper.map(reservations, listType);
    }

    public List<Reservation> getByGuestId(UUID guestId) {
        List<ReservationEntity> reservations = reservationRepository.findByGuestId(guestId);
        Type listType = new TypeToken<List<Reservation>>() {}.getType();
        return mapper.map(reservations, listType);
    }

    public boolean isRoomAvailable(UUID roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        validateDateRange(checkIn, checkOut);
        ensureRoomExists(roomId);
        return !reservationRepository.existsOverlappingReservationForRoom(roomId, checkIn, checkOut, null);
    }

    @Transactional
    public Reservation create(ReservationDto reservationDto) {
        validateReservation(reservationDto, null);
        ReservationEntity entity = mapper.map(reservationDto, ReservationEntity.class);
        ReservationEntity saved = reservationRepository.saveAndFlush(entity);
        return mapper.map(saved, Reservation.class);
    }

    @Transactional
    public Reservation update(ReservationDto reservationDto, UUID id) {
        ReservationEntity existing = reservationRepository.findById(id)
            .orElseThrow(() -> new EntityNotExistsException(Constants.RESERVATION_NOT_FOUND));

        validateReservation(reservationDto, existing.getId());
        mapper.map(reservationDto, existing);

        ReservationEntity updated = reservationRepository.saveAndFlush(existing);
        return mapper.map(updated, Reservation.class);
    }

    @Transactional
    public void delete(UUID id) {
        ReservationEntity reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new EntityNotExistsException(Constants.RESERVATION_NOT_FOUND));
        reservationRepository.delete(reservation);
        reservationRepository.flush();
    }

    private void validateReservation(ReservationDto reservationDto, UUID excludeId) {
        validateDateRange(reservationDto.getCheckIn(), reservationDto.getCheckOut());

        if (reservationDto.getGuestsCount() == null || reservationDto.getGuestsCount() <= 0) {
            throw new BusinessRuleException(Constants.INVALID_GUESTS_COUNT);
        }

        RoomEntity room = roomRepository.findById(reservationDto.getRoomId())
            .orElseThrow(() -> new EntityNotExistsException(Constants.ROOM_NOT_FOUND));

        guestRepository.findById(reservationDto.getGuestId())
            .orElseThrow(() -> new EntityNotExistsException(Constants.GUEST_NOT_FOUND));

        if (Boolean.FALSE.equals(room.getAvailable())) {
            throw new BusinessRuleException(Constants.ROOM_NOT_AVAILABLE);
        }

        if (reservationDto.getGuestsCount() > room.getMaxGuests()) {
            throw new BusinessRuleException(Constants.ROOM_CAPACITY_EXCEEDED);
        }

        if (reservationRepository.existsOverlappingReservationForRoom(
            reservationDto.getRoomId(),
            reservationDto.getCheckIn(),
            reservationDto.getCheckOut(),
            excludeId
        )) {
            throw new BusinessRuleException(Constants.RESERVATION_OVERLAP_ROOM);
        }

        if (reservationRepository.existsOverlappingReservationForGuest(
            reservationDto.getGuestId(),
            reservationDto.getCheckIn(),
            reservationDto.getCheckOut(),
            excludeId
        )) {
            throw new BusinessRuleException(Constants.RESERVATION_OVERLAP_GUEST);
        }
    }

    private void validateDateRange(LocalDateTime checkIn, LocalDateTime checkOut) {
        if (checkIn == null || checkOut == null || !checkIn.isBefore(checkOut)) {
            throw new BusinessRuleException(Constants.INVALID_RESERVATION_RANGE);
        }
    }

    private void ensureRoomExists(UUID roomId) {
        roomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotExistsException(Constants.ROOM_NOT_FOUND));
    }
}
