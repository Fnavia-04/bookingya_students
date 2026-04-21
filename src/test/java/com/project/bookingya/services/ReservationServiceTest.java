package com.project.bookingya.services;

/**
 * NOTA: Los tests unitarios reales se encuentran en fase1/src/test/java/com/project/bookingya/ReservationServiceTDDTest.java
 * Este archivo está vacío en el root del proyecto ya que los tests se ejecutan en fase1.
 */
class ReservationServiceTest {
}
package com.project.bookingya.services;

/**
 * NOTA: Los tests unitarios reales se encuentran en fase1/src/test/java/com/project/bookingya/ReservationServiceTDDTest.java
 * Este archivo está vacío en el root del proyecto ya que los tests se ejecutan en fase1.
 */
class ReservationServiceTest {
}
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import com.project.bookingya.dtos.ReservationDto;
import com.project.bookingya.entities.ReservationEntity;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.entities.GuestEntity;
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
    private GuestEntity guestEntity;

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

        guestEntity = new GuestEntity();
        guestEntity.setId(guestId);
    }

    @Nested
    @DisplayName("Create Reservation Tests")
    class CreateReservationTests {

        @Test
        @DisplayName("Should create a reservation successfully with valid data")
        void shouldCreateReservationSuccessfully() {
            // Arrange
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));
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
        @DisplayName("Should throw BusinessRuleException when check-out is before check-in")
        void shouldThrowExceptionWhenInvalidDateRange() {
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
        @DisplayName("Should throw BusinessRuleException when guests count is invalid")
        void shouldThrowExceptionWhenInvalidGuestsCount() {
            // Arrange
            ReservationDto invalidDto = new ReservationDto();
            invalidDto.setGuestId(guestId);
            invalidDto.setRoomId(roomId);
            invalidDto.setCheckIn(checkIn);
            invalidDto.setCheckOut(checkOut);
            invalidDto.setGuestsCount(0);

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(invalidDto);
            });
        }

        @Test
        @DisplayName("Should throw EntityNotExistsException when room does not exist")
        void shouldThrowExceptionWhenRoomNotExists() {
            // Arrange
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.create(reservationDto);
            });
        }

        @Test
        @DisplayName("Should throw EntityNotExistsException when guest does not exist")
        void shouldThrowExceptionWhenGuestNotExists() {
            // Arrange
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.create(reservationDto);
            });
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when room is not available")
        void shouldThrowExceptionWhenRoomNotAvailable() {
            // Arrange
            roomEntity.setAvailable(false);
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when guests exceed room capacity")
        void shouldThrowExceptionWhenGuestsExceedCapacity() {
            // Arrange
            ReservationDto invalidDto = new ReservationDto();
            invalidDto.setGuestId(guestId);
            invalidDto.setRoomId(roomId);
            invalidDto.setCheckIn(checkIn);
            invalidDto.setCheckOut(checkOut);
            invalidDto.setGuestsCount(5); // Room has max 4 guests

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(invalidDto);
            });
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when room has overlapping reservations")
        void shouldThrowExceptionWhenRoomOverlaps() {
            // Arrange
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));
            when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(true);

            // Act & Assert
            assertThrows(BusinessRuleException.class, () -> {
                reservationService.create(reservationDto);
            });
        }

        @Test
        @DisplayName("Should throw BusinessRuleException when guest has overlapping reservations")
        void shouldThrowExceptionWhenGuestOverlaps() {
            // Arrange
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));
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
    @DisplayName("Get Reservation by ID Tests")
    class GetReservationByIdTests {

        @Test
        @DisplayName("Should retrieve reservation by ID successfully")
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
    }

    @Nested
    @DisplayName("Update Reservation Tests")
    class UpdateReservationTests {

        @Test
        @DisplayName("Should update a reservation successfully")
        void shouldUpdateReservationSuccessfully() {
            // Arrange
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));
            when(reservationRepository.existsOverlappingReservationForRoom(
                any(), any(), any(), any()))
                .thenReturn(false);
            when(reservationRepository.existsOverlappingReservationForGuest(
                any(), any(), any(), any()))
                .thenReturn(false);
            doReturn(reservationEntity).when(mapper).map(any(), any());
            when(reservationRepository.saveAndFlush(any(ReservationEntity.class)))
                .thenReturn(reservationEntity);
            when(mapper.map(reservationEntity, Reservation.class))
                .thenReturn(reservation);

            // Act
            Reservation result = reservationService.update(reservationDto, reservationId);

            // Assert
            assertNotNull(result);
            assertEquals(reservationId, result.getId());
            verify(reservationRepository, times(1)).findById(reservationId);
            verify(reservationRepository, times(1)).saveAndFlush(any(ReservationEntity.class));
        }

        @Test
        @DisplayName("Should throw EntityNotExistsException when trying to update non-existent reservation")
        void shouldThrowExceptionWhenUpdatingNonExistentReservation() {
            // Arrange
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                reservationService.update(reservationDto, reservationId);
            });
        }
    }

    @Nested
    @DisplayName("Delete Reservation Tests")
    class DeleteReservationTests {

        @Test
        @DisplayName("Should delete a reservation successfully")
        void shouldDeleteReservationSuccessfully() {
            // Arrange
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));

            // Act
            reservationService.delete(reservationId);

            // Assert
            verify(reservationRepository, times(1)).findById(reservationId);
            verify(reservationRepository, times(1)).deleteById(reservationId);
        }

        @Test
        @DisplayName("Should throw EntityNotExistsException when trying to delete non-existent reservation")
        void shouldThrowExceptionWhenDeletingNonExistentReservation() {
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
    @DisplayName("Get All Reservations Tests")
    class GetAllReservationsTests {

        @Test
        @DisplayName("Should retrieve all reservations successfully")
        void shouldGetAllReservationsSuccessfully() {
            // Arrange
            List<ReservationEntity> entities = List.of(reservationEntity);

            when(reservationRepository.findAll()).thenReturn(entities);
            when(mapper.map(eq(entities), any()))
                .thenReturn(List.of(reservation));

            // Act
            List<Reservation> result = reservationService.getAll();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            verify(reservationRepository, times(1)).findAll();
        }
    }
}
