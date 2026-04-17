package com.project.bookingya.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

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

    private ReservationService reservationService;

    private UUID reservationId;
    private UUID guestId;
    private UUID roomId;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;

    @BeforeEach
    void setUp() {
        reservationService = new ReservationService(
            reservationRepository,
            roomRepository,
            guestRepository,
            mapper
        );

        reservationId = UUID.randomUUID();
        guestId = UUID.randomUUID();
        roomId = UUID.randomUUID();
        checkIn = LocalDateTime.now().plusDays(1);
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

            // Act
            Reservation result = reservationService.create(reservationDto);

            // Assert
            assertNotNull(result);
            assertEquals(guestId, result.getGuestId());
            assertEquals(roomId, result.getRoomId());
            assertEquals(2, result.getGuestsCount());
            verify(reservationRepository, times(1)).saveAndFlush(any(ReservationEntity.class));
        }

        @Test
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
            reservationDto.setGuestsCount(0);

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
            verify(reservationRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should throw EntityNotExistsException when room does not exist")
        void shouldThrowExceptionWhenRoomNotExists() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.create(reservationDto);
            });
            verify(reservationRepository, never()).saveAndFlush(any());
        }

        @Test
        @DisplayName("Should throw EntityNotExistsException when guest does not exist")
        void shouldThrowExceptionWhenGuestNotExists() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
            RoomEntity room = createRoomEntity(true, 5);

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
            when(guestRepository.findById(guestId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.create(reservationDto);
            });
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

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
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

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
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

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
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

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
            verify(reservationRepository, never()).saveAndFlush(any());
        }
    }

    @Nested
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

            // Act
            Reservation result = reservationService.getById(reservationId);

            // Assert
            assertNotNull(result);
            assertEquals(guestId, result.getGuestId());
            assertEquals(roomId, result.getRoomId());
            verify(reservationRepository, times(1)).findById(reservationId);
        }

        @Test
        @DisplayName("Should throw EntityNotExistsException when reservation does not exist")
        void shouldThrowExceptionWhenReservationNotExists() {
            // Arrange
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.getById(reservationId);
            });
        }

        @Test
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
        }
    }

    @Nested
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
            verify(reservationRepository, times(1)).saveAndFlush(any(ReservationEntity.class));
        }

        @Test
        @DisplayName("Should throw EntityNotExistsException when reservation to update does not exist")
        void shouldThrowExceptionWhenReservationNotExists() {
            // Arrange
            ReservationDto reservationDto = createValidReservationDto();
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.update(reservationDto, reservationId);
            });
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
        }
    }

    @Nested
    @DisplayName("Delete Reservation Tests")
    class DeleteReservationTests {

        @Test
        @DisplayName("Should delete reservation successfully")
        void shouldDeleteReservationSuccessfully() {
            // Arrange
            ReservationEntity reservationEntity = createValidReservationEntity();
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));

            // Act
            reservationService.delete(reservationId);

            // Assert
            verify(reservationRepository, times(1)).delete(reservationEntity);
            verify(reservationRepository, times(1)).flush();
        }

        @Test
        @DisplayName("Should throw EntityNotExistsException when reservation to delete does not exist")
        void shouldThrowExceptionWhenReservationNotExists() {
            // Arrange
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.delete(reservationId);
            });
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
        }
    }

    @Nested
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
}
