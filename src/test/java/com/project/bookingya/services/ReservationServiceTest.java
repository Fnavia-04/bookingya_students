package com.project.bookingya.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

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

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService Unit Tests")
class ReservationServiceTest {

    @Mock
    private IReservationRepository reservationRepository;

    @Mock
    private IRoomRepository roomRepository;

    @Mock
    private IGuestRepository guestRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private ReservationService reservationService;

    private UUID reservationId;
    private UUID guestId;
    private UUID roomId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private ReservationEntity reservationEntity;
    private ReservationDto reservationDto;
    private Reservation reservation;
    private RoomEntity roomEntity;

    @BeforeEach
    void setUp() {
        reservationId = UUID.randomUUID();
        guestId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        checkIn = LocalDateTime.now().plusDays(1);
        checkOut = LocalDateTime.now().plusDays(5);

        reservationEntity = new ReservationEntity();
        reservationEntity.setId(reservationId);
        reservationEntity.setGuestId(guestId);
        reservationEntity.setRoomId(roomId);
        reservationEntity.setCheckIn(checkIn);
        reservationEntity.setCheckOut(checkOut);
        reservationEntity.setGuestsCount(2);
        reservationEntity.setNotes("Test reservation");

        reservationDto = new ReservationDto();
        reservationDto.setGuestId(guestId);
        reservationDto.setRoomId(roomId);
        reservationDto.setCheckIn(checkIn);
        reservationDto.setCheckOut(checkOut);
        reservationDto.setGuestsCount(2);
        reservationDto.setNotes("Test reservation");

        reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setGuestId(guestId);
        reservation.setRoomId(roomId);
        reservation.setCheckIn(checkIn);
        reservation.setCheckOut(checkOut);
        reservation.setGuestsCount(2);
        reservation.setNotes("Test reservation");

        roomEntity = new RoomEntity();
        roomEntity.setId(roomId);
        roomEntity.setAvailable(true);
        roomEntity.setMaxGuests(4);
    }

    @Nested
    @DisplayName("CREATE - Crear una reserva")
    class CreateReservationTests {

        @Test
        @DisplayName("Debe crear una reserva exitosamente con datos válidos")
        void shouldCreateReservationSuccessfully() {
            // Arrange
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(mock()));
            when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(false);
            when(reservationRepository.existsOverlappingReservationForGuest(
                guestId, checkIn, checkOut, null)).thenReturn(false);
            when(mapper.map(reservationDto, ReservationEntity.class))
                .thenReturn(reservationEntity);
            when(reservationRepository.saveAndFlush(any(ReservationEntity.class)))
                .thenReturn(reservationEntity);
            when(mapper.map(reservationEntity, Reservation.class))
                .thenReturn(reservation);

            // Act
            Reservation result = reservationService.create(reservationDto);

            // Assert
            assertNotNull(result);
            assertEquals(reservationId, result.getId());
            assertEquals(guestId, result.getGuestId());
            assertEquals(roomId, result.getRoomId());
            assertEquals(2, result.getGuestsCount());
            verify(reservationRepository, times(1)).saveAndFlush(any(ReservationEntity.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando las fechas son inválidas")
        void shouldThrowExceptionWhenCheckOutBeforeCheckIn() {
            // Arrange
            ReservationDto invalidDto = new ReservationDto();
            invalidDto.setGuestId(guestId);
            invalidDto.setRoomId(roomId);
            invalidDto.setCheckIn(checkOut);
            invalidDto.setCheckOut(checkIn);
            invalidDto.setGuestsCount(2);

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(invalidDto);
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando guestsCount es inválido")
        void shouldThrowExceptionWhenGuestsCountInvalid() {
            // Arrange
            reservationDto.setGuestsCount(0);

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando la habitación no existe")
        void shouldThrowExceptionWhenRoomNotFound() {
            // Arrange
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.create(reservationDto);
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el guest no existe")
        void shouldThrowExceptionWhenGuestNotFound() {
            // Arrange
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.create(reservationDto);
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando la habitación no está disponible")
        void shouldThrowExceptionWhenRoomNotAvailable() {
            // Arrange
            roomEntity.setAvailable(false);
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(mock()));

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando se excede la capacidad de la habitación")
        void shouldThrowExceptionWhenExceedsRoomCapacity() {
            // Arrange
            reservationDto.setGuestsCount(5); // Room has max 4 guests
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(mock()));

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando hay solapamiento con otra reserva de la habitación")
        void shouldThrowExceptionWhenRoomReservationOverlaps() {
            // Arrange
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(mock()));
            when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(true);

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando hay solapamiento con otra reserva del guest")
        void shouldThrowExceptionWhenGuestReservationOverlaps() {
            // Arrange
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(mock()));
            when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(false);
            when(reservationRepository.existsOverlappingReservationForGuest(
                guestId, checkIn, checkOut, null)).thenReturn(true);

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
        }
    }

    @Nested
    @DisplayName("READ - Consultar una reserva")
    class ReadReservationTests {

        @Test
        @DisplayName("Debe obtener una reserva por ID exitosamente")
        void shouldGetReservationByIdSuccessfully() {
            // Arrange
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));
            when(mapper.map(reservationEntity, Reservation.class))
                .thenReturn(reservation);

            // Act
            Reservation result = reservationService.getById(reservationId);

            // Assert
            assertNotNull(result);
            assertEquals(reservationId, result.getId());
            assertEquals(guestId, result.getGuestId());
            assertEquals(roomId, result.getRoomId());
            verify(reservationRepository, times(1)).findById(reservationId);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando la reserva no existe")
        void shouldThrowExceptionWhenReservationNotFound() {
            // Arrange
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.getById(reservationId);
            });
        }

        @Test
        @DisplayName("Debe obtener todas las reservas")
        void shouldGetAllReservations() {
            // Arrange
            List<ReservationEntity> entities = List.of(reservationEntity);
            List<Reservation> reservations = List.of(reservation);
            when(reservationRepository.findAll()).thenReturn(entities);
            when(mapper.map(eq(entities), any(java.lang.reflect.Type.class)))
                .thenReturn(reservations);

            // Act
            List<Reservation> result = reservationService.getAll();

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            verify(reservationRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Debe obtener reservas por room ID")
        void shouldGetReservationsByRoomId() {
            // Arrange
            List<ReservationEntity> entities = List.of(reservationEntity);
            List<Reservation> reservations = List.of(reservation);
            when(reservationRepository.findByRoomId(roomId)).thenReturn(entities);
            when(mapper.map(eq(entities), any(java.lang.reflect.Type.class)))
                .thenReturn(reservations);

            // Act
            List<Reservation> result = reservationService.getByRoomId(roomId);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(reservationRepository, times(1)).findByRoomId(roomId);
        }

        @Test
        @DisplayName("Debe obtener reservas por guest ID")
        void shouldGetReservationsByGuestId() {
            // Arrange
            List<ReservationEntity> entities = List.of(reservationEntity);
            List<Reservation> reservations = List.of(reservation);
            when(reservationRepository.findByGuestId(guestId)).thenReturn(entities);
            when(mapper.map(eq(entities), any(java.lang.reflect.Type.class)))
                .thenReturn(reservations);

            // Act
            List<Reservation> result = reservationService.getByGuestId(guestId);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(reservationRepository, times(1)).findByGuestId(guestId);
        }
    }

    @Nested
    @DisplayName("UPDATE - Actualizar una reserva")
    class UpdateReservationTests {

        @Test
        @DisplayName("Debe actualizar una reserva exitosamente")
        void shouldUpdateReservationSuccessfully() {
            // Arrange
            ReservationDto updateDto = new ReservationDto();
            updateDto.setGuestId(guestId);
            updateDto.setRoomId(roomId);
            updateDto.setCheckIn(checkIn.plusDays(1));
            updateDto.setCheckOut(checkOut.plusDays(1));
            updateDto.setGuestsCount(3);
            updateDto.setNotes("Updated reservation");

            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(mock()));
            when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, updateDto.getCheckIn(), updateDto.getCheckOut(), reservationId))
                .thenReturn(false);
            when(reservationRepository.existsOverlappingReservationForGuest(
                guestId, updateDto.getCheckIn(), updateDto.getCheckOut(), reservationId))
                .thenReturn(false);
            doNothing().when(mapper).map(any(ReservationDto.class), any(ReservationEntity.class));
            when(reservationRepository.saveAndFlush(any(ReservationEntity.class)))
                .thenReturn(reservationEntity);
            when(mapper.map(any(ReservationEntity.class), eq(Reservation.class)))
                .thenReturn(reservation);

            // Act
            Reservation result = reservationService.update(updateDto, reservationId);

            // Assert
            assertNotNull(result);
            verify(reservationRepository, times(1)).findById(reservationId);
            verify(reservationRepository, times(1)).saveAndFlush(any(ReservationEntity.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando la reserva no existe")
        void shouldThrowExceptionWhenReservationNotFoundOnUpdate() {
            // Arrange
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.update(reservationDto, reservationId);
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando las fechas actualizadas son inválidas")
        void shouldThrowExceptionWhenUpdatedDatesInvalid() {
            // Arrange
            ReservationDto invalidDto = new ReservationDto();
            invalidDto.setGuestId(guestId);
            invalidDto.setRoomId(roomId);
            invalidDto.setCheckIn(checkOut);
            invalidDto.setCheckOut(checkIn);
            invalidDto.setGuestsCount(2);

            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.update(invalidDto, reservationId);
            });
        }
    }

    @Nested
    @DisplayName("DELETE - Eliminar una reserva")
    class DeleteReservationTests {

        @Test
        @DisplayName("Debe eliminar una reserva exitosamente")
        void shouldDeleteReservationSuccessfully() {
            // Arrange
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));
            doNothing().when(reservationRepository).delete(any());
            doNothing().when(reservationRepository).flush();

            // Act
            reservationService.delete(reservationId);

            // Assert
            verify(reservationRepository, times(1)).findById(reservationId);
            verify(reservationRepository, times(1)).delete(reservationEntity);
            verify(reservationRepository, times(1)).flush();
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando la reserva no existe")
        void shouldThrowExceptionWhenReservationNotFoundOnDelete() {
            // Arrange
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.delete(reservationId);
            });
        }
    }

    @Nested
    @DisplayName("UTILITY - Métodos de utilidad")
    class UtilityTests {

        @Test
        @DisplayName("Debe verificar disponibilidad de habitación correctamente")
        void shouldCheckRoomAvailabilitySuccessfully() {
            // Arrange
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(false);

            // Act
            boolean result = reservationService.isRoomAvailable(roomId, checkIn, checkOut);

            // Assert
            assertTrue(result);
            verify(roomRepository, times(1)).findById(roomId);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando verifica disponibilidad con fechas inválidas")
        void shouldThrowExceptionWhenCheckingAvailabilityWithInvalidDates() {
            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.isRoomAvailable(roomId, checkOut, checkIn);
            });
        }
    }
}
