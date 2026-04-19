package com.project.bookingya.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
<<<<<<< HEAD
import java.util.Arrays;
=======
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
<<<<<<< HEAD
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.entities.GuestEntity;
=======
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.project.bookingya.dtos.ReservationDto;
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
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

<<<<<<< HEAD
=======
    @InjectMocks
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
    private ReservationService reservationService;

    private UUID reservationId;
    private UUID guestId;
    private UUID roomId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
<<<<<<< HEAD

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
            reservationRepository,
            roomRepository,
            guestRepository,
            mapper
        );

=======
    private ReservationEntity reservationEntity;
    private ReservationDto reservationDto;
    private Reservation reservation;
    private RoomEntity roomEntity;

    @BeforeEach
    void setUp() {
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
        reservationId = UUID.randomUUID();
        guestId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        checkIn = LocalDateTime.now().plusDays(1);
<<<<<<< HEAD
        checkOut = LocalDateTime.now().plusDays(3);
    }

    @Nested
    @DisplayName("Create Reservation Tests")
    class CreateReservationTests {

        @Test
        @DisplayName("Should create a reservation successfully with valid data")
        void shouldCreateReservationSuccessfully() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
            ReservationEntity reservationEntity = createValidReservationEntity();
            Reservation expectedReservation = createValidReservation();

            RoomEntity room = createRoomEntity(true, 5);
            GuestEntity guest = new GuestEntity();

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));
            when(reservationRepository.existsOverlappingReservationForRoom(
                anyUUID(), any(LocalDateTime.class), any(LocalDateTime.class), isNull()
            )).thenReturn(false);
            when(reservationRepository.existsOverlappingReservationForGuest(
                anyUUID(), any(LocalDateTime.class), any(LocalDateTime.class), isNull()
            )).thenReturn(false);
            when(mapper.map(reservationDto, ReservationEntity.class))
                .thenReturn(reservationEntity);
            when(reservationRepository.saveAndFlush(reservationEntity))
                .thenReturn(reservationEntity);
            when(mapper.map(reservationEntity, Reservation.class))
                .thenReturn(expectedReservation);
=======
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
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065

            // Act
            Reservation result = reservationService.create(reservationDto);

            // Assert
            assertNotNull(result);
<<<<<<< HEAD
=======
            assertEquals(reservationId, result.getId());
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
            assertEquals(guestId, result.getGuestId());
            assertEquals(roomId, result.getRoomId());
            assertEquals(2, result.getGuestsCount());
            verify(reservationRepository, times(1)).saveAndFlush(any(ReservationEntity.class));
        }

        @Test
<<<<<<< HEAD
        @DisplayName("Should throw BusinessRuleException when check-in is after check-out")
        void shouldThrowExceptionWhenInvalidDateRange() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
            reservationDto.setCheckIn(checkOut);
            reservationDto.setCheckOut(checkIn);

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
            verify(reservationRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when guests count is invalid")
        void shouldThrowExceptionWhenInvalidGuestsCount() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
=======
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
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
            reservationDto.setGuestsCount(0);

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
<<<<<<< HEAD
            verify(reservationRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should throw EntityNotExistsException when room does not exist")
        void shouldThrowExceptionWhenRoomNotExists() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
=======
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando la habitación no existe")
        void shouldThrowExceptionWhenRoomNotFound() {
            // Arrange
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.create(reservationDto);
            });
<<<<<<< HEAD
            verify(reservationRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should throw EntityNotExistsException when guest does not exist")
        void shouldThrowExceptionWhenGuestNotExists() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
            RoomEntity room = createRoomEntity(true, 5);

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
=======
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el guest no existe")
        void shouldThrowExceptionWhenGuestNotFound() {
            // Arrange
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
            when(guestRepository.findById(guestId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.create(reservationDto);
            });
<<<<<<< HEAD
            verify(reservationRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when room is not available")
        void shouldThrowExceptionWhenRoomNotAvailable() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
            RoomEntity room = createRoomEntity(false, 5);

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(new GuestEntity()));
=======
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando la habitación no está disponible")
        void shouldThrowExceptionWhenRoomNotAvailable() {
            // Arrange
            roomEntity.setAvailable(false);
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(mock()));
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
<<<<<<< HEAD
            verify(reservationRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when guests exceed room capacity")
        void shouldThrowExceptionWhenGuestsExceedCapacity() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
            reservationDto.setGuestsCount(10);
            RoomEntity room = createRoomEntity(true, 5);

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(new GuestEntity()));
=======
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando se excede la capacidad de la habitación")
        void shouldThrowExceptionWhenExceedsRoomCapacity() {
            // Arrange
            reservationDto.setGuestsCount(5); // Room has max 4 guests
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(mock()));
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
<<<<<<< HEAD
            verify(reservationRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when room has overlapping reservations")
        void shouldThrowExceptionWhenRoomOverlaps() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
            RoomEntity room = createRoomEntity(true, 5);

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(new GuestEntity()));
            when(reservationRepository.existsOverlappingReservationForRoom(
                anyUUID(), any(LocalDateTime.class), any(LocalDateTime.class), isNull()
            )).thenReturn(true);
=======
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando hay solapamiento con otra reserva de la habitación")
        void shouldThrowExceptionWhenRoomReservationOverlaps() {
            // Arrange
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(mock()));
            when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(true);
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
<<<<<<< HEAD
            verify(reservationRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when guest has overlapping reservations")
        void shouldThrowExceptionWhenGuestOverlaps() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
            RoomEntity room = createRoomEntity(true, 5);

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(new GuestEntity()));
            when(reservationRepository.existsOverlappingReservationForRoom(
                anyUUID(), any(LocalDateTime.class), any(LocalDateTime.class), isNull()
            )).thenReturn(false);
            when(reservationRepository.existsOverlappingReservationForGuest(
                anyUUID(), any(LocalDateTime.class), any(LocalDateTime.class), isNull()
            )).thenReturn(true);
=======
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
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
<<<<<<< HEAD
            verify(reservationRepository, never()).saveAndFlush(any());
=======
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
        }
    }

    @Nested
<<<<<<< HEAD
    @DisplayName("Get Reservation by ID Tests")
    class GetReservationByIdTests {

        @Test
        @DisplayName("Should retrieve reservation by ID successfully")
        void shouldGetReservationByIdSuccessfully() {
            // Arrange
            ReservationEntity reservationEntity = createValidReservationEntity();
            Reservation expectedReservation = createValidReservation();

            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));
            when(mapper.map(reservationEntity, Reservation.class))
                .thenReturn(expectedReservation);
=======
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
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065

            // Act
            Reservation result = reservationService.getById(reservationId);

            // Assert
            assertNotNull(result);
<<<<<<< HEAD
=======
            assertEquals(reservationId, result.getId());
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
            assertEquals(guestId, result.getGuestId());
            assertEquals(roomId, result.getRoomId());
            verify(reservationRepository, times(1)).findById(reservationId);
        }

        @Test
<<<<<<< HEAD
        @DisplayName("Should throw EntityNotExistsException when reservation does not exist")
        void shouldThrowExceptionWhenReservationNotExists() {
=======
        @DisplayName("Debe lanzar excepción cuando la reserva no existe")
        void shouldThrowExceptionWhenReservationNotFound() {
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
            // Arrange
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.getById(reservationId);
            });
        }

        @Test
<<<<<<< HEAD
        @DisplayName("Should return correct reservation data")
        void shouldReturnCorrectReservationData() {
            // Arrange
            ReservationEntity reservationEntity = new ReservationEntity();
            reservationEntity.setId(reservationId);
            reservationEntity.setGuestId(guestId);
            reservationEntity.setRoomId(roomId);
            reservationEntity.setCheckIn(checkIn);
            reservationEntity.setCheckOut(checkOut);
            reservationEntity.setGuestsCount(3);
            reservationEntity.setNotes("Test notes");

            Reservation reservation = new Reservation();
            reservation.setId(reservationId);
            reservation.setGuestId(guestId);
            reservation.setRoomId(roomId);
            reservation.setCheckIn(checkIn);
            reservation.setCheckOut(checkOut);
            reservation.setGuestsCount(3);
            reservation.setNotes("Test notes");

            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));
            when(mapper.map(reservationEntity, Reservation.class))
                .thenReturn(reservation);

            // Act
            Reservation result = reservationService.getById(reservationId);

            // Assert
            assertEquals(reservationId, result.getId());
            assertEquals(guestId, result.getGuestId());
            assertEquals(roomId, result.getRoomId());
            assertEquals(3, result.getGuestsCount());
            assertEquals("Test notes", result.getNotes());
=======
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
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
        }
    }

    @Nested
<<<<<<< HEAD
    @DisplayName("Update Reservation Tests")
    class UpdateReservationTests {

        @Test
        @DisplayName("Should update reservation successfully with valid data")
        void shouldUpdateReservationSuccessfully() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
            ReservationEntity existingEntity = createValidReservationEntity();
            Reservation expectedReservation = createValidReservation();

            RoomEntity room = createRoomEntity(true, 5);
            GuestEntity guest = new GuestEntity();

            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(existingEntity));
            when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(room));
            when(guestRepository.findById(guestId))
                .thenReturn(Optional.of(guest));
            when(reservationRepository.existsOverlappingReservationForRoom(
                anyUUID(), any(LocalDateTime.class), any(LocalDateTime.class), eq(reservationId)
            )).thenReturn(false);
            when(reservationRepository.existsOverlappingReservationForGuest(
                anyUUID(), any(LocalDateTime.class), any(LocalDateTime.class), eq(reservationId)
            )).thenReturn(false);
            doNothing().when(mapper).map(reservationDto, existingEntity);
            when(reservationRepository.saveAndFlush(existingEntity))
                .thenReturn(existingEntity);
            when(mapper.map(existingEntity, Reservation.class))
                .thenReturn(expectedReservation);

            // Act
            Reservation result = reservationService.update(reservationDto, reservationId);

            // Assert
            assertNotNull(result);
=======
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
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
            verify(reservationRepository, times(1)).saveAndFlush(any(ReservationEntity.class));
        }

        @Test
<<<<<<< HEAD
        @DisplayName("Should throw EntityNotExistsException when reservation to update does not exist")
        void shouldThrowExceptionWhenReservationNotExists() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
=======
        @DisplayName("Debe lanzar excepción cuando la reserva no existe")
        void shouldThrowExceptionWhenReservationNotFoundOnUpdate() {
            // Arrange
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.update(reservationDto, reservationId);
            });
<<<<<<< HEAD
            verify(reservationRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when invalid date range in update")
        void shouldThrowExceptionWhenInvalidDateRangeInUpdate() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
            reservationDto.setCheckIn(checkOut);
            reservationDto.setCheckOut(checkIn);
            ReservationEntity existingEntity = createValidReservationEntity();

            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(existingEntity));

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.update(reservationDto, reservationId);
            });
            verify(reservationRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should exclude current reservation from overlap check during update")
        void shouldExcludeCurrentReservationFromOverlapCheck() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
            ReservationEntity existingEntity = createValidReservationEntity();
            Reservation expectedReservation = createValidReservation();

            RoomEntity room = createRoomEntity(true, 5);
            GuestEntity guest = new GuestEntity();

            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(existingEntity));
            when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(room));
            when(guestRepository.findById(guestId))
                .thenReturn(Optional.of(guest));
            when(reservationRepository.existsOverlappingReservationForRoom(
                anyUUID(), any(LocalDateTime.class), any(LocalDateTime.class), eq(reservationId)
            )).thenReturn(false);
            when(reservationRepository.existsOverlappingReservationForGuest(
                anyUUID(), any(LocalDateTime.class), any(LocalDateTime.class), eq(reservationId)
            )).thenReturn(false);
            doNothing().when(mapper).map(reservationDto, existingEntity);
            when(reservationRepository.saveAndFlush(existingEntity))
                .thenReturn(existingEntity);
            when(mapper.map(existingEntity, Reservation.class))
                .thenReturn(expectedReservation);

            // Act
            reservationService.update(reservationDto, reservationId);

            // Assert
            verify(reservationRepository).existsOverlappingReservationForRoom(
                anyUUID(), any(LocalDateTime.class), any(LocalDateTime.class), eq(reservationId)
            );
            verify(reservationRepository).existsOverlappingReservationForGuest(
                anyUUID(), any(LocalDateTime.class), any(LocalDateTime.class), eq(reservationId)
            );
=======
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
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
        }
    }

    @Nested
<<<<<<< HEAD
    @DisplayName("Delete Reservation Tests")
    class DeleteReservationTests {

        @Test
        @DisplayName("Should delete reservation successfully")
        void shouldDeleteReservationSuccessfully() {
            // Arrange
            ReservationEntity reservationEntity = createValidReservationEntity();
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));
=======
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
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065

            // Act
            reservationService.delete(reservationId);

            // Assert
<<<<<<< HEAD
=======
            verify(reservationRepository, times(1)).findById(reservationId);
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
            verify(reservationRepository, times(1)).delete(reservationEntity);
            verify(reservationRepository, times(1)).flush();
        }

        @Test
<<<<<<< HEAD
        @DisplayName("Should throw EntityNotExistsException when reservation to delete does not exist")
        void shouldThrowExceptionWhenReservationNotExists() {
=======
        @DisplayName("Debe lanzar excepción cuando la reserva no existe")
        void shouldThrowExceptionWhenReservationNotFoundOnDelete() {
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
            // Arrange
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.delete(reservationId);
            });
<<<<<<< HEAD
            verify(reservationRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should verify deletion is confirmed with flush")
        void shouldVerifyDeletionWithFlush() {
            // Arrange
            ReservationEntity reservationEntity = createValidReservationEntity();
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));

            // Act
            reservationService.delete(reservationId);

            // Assert
            verify(reservationRepository, times(1)).delete(reservationEntity);
            verify(reservationRepository, times(1)).flush();
=======
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
        }
    }

    @Nested
<<<<<<< HEAD
    @DisplayName("Get All Reservations Tests")
    class GetAllReservationsTests {

        @Test
        @DisplayName("Should retrieve all reservations successfully")
        void shouldGetAllReservationsSuccessfully() {
            // Arrange
            ReservationEntity entity1 = createValidReservationEntity();
            ReservationEntity entity2 = createValidReservationEntity();
            List<ReservationEntity> entities = Arrays.asList(entity1, entity2);

            Reservation reservation1 = createValidReservation();
            Reservation reservation2 = createValidReservation();
            List<Reservation> expectedReservations = Arrays.asList(reservation1, reservation2);

            when(reservationRepository.findAll()).thenReturn(entities);
            when(mapper.map(eq(entities), any(java.lang.reflect.Type.class)))
                .thenReturn(expectedReservations);

            // Act
            List<Reservation> result = reservationService.getAll();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(reservationRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no reservations exist")
        void shouldReturnEmptyListWhenNoReservations() {
            // Arrange
            when(reservationRepository.findAll()).thenReturn(Arrays.asList());
            when(mapper.map(eq(Arrays.asList()), any(java.lang.reflect.Type.class)))
                .thenReturn(Arrays.asList());

            // Act
            List<Reservation> result = reservationService.getAll();

            // Assert
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("Get Reservations by Guest ID Tests")
    class GetReservationsByGuestIdTests {

        @Test
        @DisplayName("Should retrieve reservations by guest ID successfully")
        void shouldGetReservationsByGuestIdSuccessfully() {
            // Arrange
            ReservationEntity entity1 = createValidReservationEntity();
            ReservationEntity entity2 = createValidReservationEntity();
            List<ReservationEntity> entities = Arrays.asList(entity1, entity2);

            Reservation reservation1 = createValidReservation();
            Reservation reservation2 = createValidReservation();
            List<Reservation> expectedReservations = Arrays.asList(reservation1, reservation2);

            when(reservationRepository.findByGuestId(guestId)).thenReturn(entities);
            when(mapper.map(eq(entities), any(java.lang.reflect.Type.class)))
                .thenReturn(expectedReservations);

            // Act
            List<Reservation> result = reservationService.getByGuestId(guestId);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(reservationRepository, times(1)).findByGuestId(guestId);
        }
    }

    @Nested
    @DisplayName("Get Reservations by Room ID Tests")
    class GetReservationsByRoomIdTests {

        @Test
        @DisplayName("Should retrieve reservations by room ID successfully")
        void shouldGetReservationsByRoomIdSuccessfully() {
            // Arrange
            ReservationEntity entity1 = createValidReservationEntity();
            ReservationEntity entity2 = createValidReservationEntity();
            List<ReservationEntity> entities = Arrays.asList(entity1, entity2);

            Reservation reservation1 = createValidReservation();
            Reservation reservation2 = createValidReservation();
            List<Reservation> expectedReservations = Arrays.asList(reservation1, reservation2);

            when(reservationRepository.findByRoomId(roomId)).thenReturn(entities);
            when(mapper.map(eq(entities), any(java.lang.reflect.Type.class)))
                .thenReturn(expectedReservations);

            // Act
            List<Reservation> result = reservationService.getByRoomId(roomId);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(reservationRepository, times(1)).findByRoomId(roomId);
        }
    }

    // Helper methods to create test data
    private ReservationDto createValidReservationDto() {
        ReservationDto dto = new ReservationDto();
        dto.setGuestId(guestId);
        dto.setRoomId(roomId);
        dto.setCheckIn(checkIn);
        dto.setCheckOut(checkOut);
        dto.setGuestsCount(2);
        dto.setNotes("Test reservation");
        return dto;
    }

    private ReservationEntity createValidReservationEntity() {
        ReservationEntity entity = new ReservationEntity();
        entity.setId(reservationId);
        entity.setGuestId(guestId);
        entity.setRoomId(roomId);
        entity.setCheckIn(checkIn);
        entity.setCheckOut(checkOut);
        entity.setGuestsCount(2);
        entity.setNotes("Test reservation");
        return entity;
    }

    private Reservation createValidReservation() {
        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setGuestId(guestId);
        reservation.setRoomId(roomId);
        reservation.setCheckIn(checkIn);
        reservation.setCheckOut(checkOut);
        reservation.setGuestsCount(2);
        reservation.setNotes("Test reservation");
        return reservation;
    }

    private RoomEntity createRoomEntity(Boolean available, Integer maxGuests) {
        RoomEntity room = new RoomEntity();
        room.setId(roomId);
        room.setAvailable(available);
        room.setMaxGuests(maxGuests);
        return room;
    }

    private static UUID anyUUID() {
        return any(UUID.class);
    }
=======
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
>>>>>>> 172dd885fac0a45e017f03dbf7a46e05025ed065
}
