package com.project.bookingya.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Type;
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
import org.mockito.InjectMocks;
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

/**
 * Pruebas unitarias para el servicio de reservas (ReservationService).
 * 
 * FASE 1 - Test-Driven Development (TDD):
 * Este test suite automatiza pruebas unitarias que validan:
 * ✓ Creación de una reserva con validación de reglas de negocio
 * ✓ Consulta de una reserva por ID
 * ✓ Actualización de una reserva existente
 * ✓ Eliminación de una reserva
 * ✓ Obtención de reservas por diferentes criterios (ID, habitación, huésped, todas)
 * ✓ Verificación de disponibilidad de habitaciones
 * 
 * Patrones aplicados:
 * - TDD (Test-Driven Development)
 * - Patrón AAA (Arrange, Act, Assert)
 * - Pruebas anidadas (@Nested) por funcionalidad
 * - Mockito para aislar la lógica del servicio
 * - @DisplayName para descripciones legibles
 * - Datos de prueba reutilizables
 * 
 * @author Equipo de QA - Fase 1
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationService - Pruebas Unitarias FASE 1 TDD")
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

    // ==================== DATOS DE PRUEBA ====================
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

    // ==================== PRUEBAS DE CREACIÓN DE RESERVA ====================
    @Nested
    @DisplayName("✓ CREAR RESERVA - Pruebas de funcionalidad y validación")
    class CreateReservationTests {

        @Test
        @DisplayName("Debe crear una reserva exitosamente con datos válidos")
        void shouldCreateReservationSuccessfully() {
            // Arrange: configurar mocks para un flujo exitoso
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

            // Act: crear la reserva
            Reservation result = reservationService.create(reservationDto);

            // Assert: verificar que la reserva fue creada correctamente
            assertNotNull(result, "La reserva no debe ser nula");
            assertEquals(reservationId, result.getId(), "El ID debe coincidir");
            assertEquals(guestId, result.getGuestId(), "El ID del huésped debe coincidir");
            assertEquals(roomId, result.getRoomId(), "El ID de la habitación debe coincidir");
            assertEquals(2, result.getGuestsCount(), "La cantidad de huéspedes debe ser 2");
            assertEquals(checkIn, result.getCheckIn(), "El checkIn debe coincidir");
            assertEquals(checkOut, result.getCheckOut(), "El checkOut debe coincidir");
            
            // Verificar que los métodos fueron invocados correctamente
            verify(roomRepository, times(1)).findById(roomId);
            verify(guestRepository, times(1)).findById(guestId);
            verify(reservationRepository, times(1)).saveAndFlush(any(ReservationEntity.class));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando checkOut es antes que checkIn")
        void shouldThrowExceptionWhenInvalidDateRange() {
            // Arrange: crear DTO con fechas inválidas (checkOut < checkIn)
            ReservationDto invalidDto = new ReservationDto();
            invalidDto.setGuestId(guestId);
            invalidDto.setRoomId(roomId);
            invalidDto.setCheckIn(checkOut);
            invalidDto.setCheckOut(checkIn); // Invertido intencionalmente
            invalidDto.setGuestsCount(2);

            // Act & Assert: verificar que se lance excepción BusinessRuleException
            BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(invalidDto),
                "Debe lanzar BusinessRuleException por rango de fechas inválido"
            );
            assertTrue(exception.getMessage().contains(Constants.INVALID_RESERVATION_RANGE));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando guestsCount es <= 0")
        void shouldThrowExceptionWhenInvalidGuestsCount() {
            // Arrange: crear DTO con cantidad de huéspedes inválida
            ReservationDto invalidDto = new ReservationDto();
            invalidDto.setGuestId(guestId);
            invalidDto.setRoomId(roomId);
            invalidDto.setCheckIn(checkIn);
            invalidDto.setCheckOut(checkOut);
            invalidDto.setGuestsCount(0); // Inválido: 0 huéspedes

            // Act & Assert
            BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(invalidDto),
                "Debe lanzar excepción cuando guestsCount <= 0"
            );
            assertTrue(exception.getMessage().contains(Constants.INVALID_GUESTS_COUNT));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando la habitación no existe")
        void shouldThrowExceptionWhenRoomNotExists() {
            // Arrange: mock retorna empty para habitación inexistente
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotExistsException exception = assertThrows(
                EntityNotExistsException.class,
                () -> reservationService.create(reservationDto),
                "Debe lanzar EntityNotExistsException cuando la habitación no existe"
            );
            assertTrue(exception.getMessage().contains(Constants.ROOM_NOT_FOUND));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando el huésped no existe")
        void shouldThrowExceptionWhenGuestNotExists() {
            // Arrange: habitación existe pero huésped no
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotExistsException exception = assertThrows(
                EntityNotExistsException.class,
                () -> reservationService.create(reservationDto),
                "Debe lanzar EntityNotExistsException cuando el huésped no existe"
            );
            assertTrue(exception.getMessage().contains(Constants.GUEST_NOT_FOUND));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando la habitación no está disponible")
        void shouldThrowExceptionWhenRoomNotAvailable() {
            // Arrange: habitación con available = false
            roomEntity.setAvailable(false);
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));

            // Act & Assert
            BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(reservationDto),
                "Debe lanzar excepción cuando la habitación no está disponible"
            );
            assertTrue(exception.getMessage().contains(Constants.ROOM_NOT_AVAILABLE));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando la cantidad de huéspedes excede la capacidad")
        void shouldThrowExceptionWhenGuestsExceedCapacity() {
            // Arrange: solicitar más huéspedes de los que la habitación puede alojar
            ReservationDto invalidDto = new ReservationDto();
            invalidDto.setGuestId(guestId);
            invalidDto.setRoomId(roomId);
            invalidDto.setCheckIn(checkIn);
            invalidDto.setCheckOut(checkOut);
            invalidDto.setGuestsCount(5); // Habitación solo permite 4 (ver setUp)

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));

            // Act & Assert
            BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(invalidDto),
                "Debe lanzar excepción cuando guestsCount > maxGuests"
            );
            assertTrue(exception.getMessage().contains(Constants.ROOM_CAPACITY_EXCEEDED));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando hay superposición de reservas en la habitación")
        void shouldThrowExceptionWhenRoomOverlaps() {
            // Arrange: existe una reserva que se superpone en la habitación
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));
            when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(true);

            // Act & Assert
            BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(reservationDto),
                "Debe lanzar excepción cuando hay superposición de reservas en la habitación"
            );
            assertTrue(exception.getMessage().contains(Constants.RESERVATION_OVERLAP_ROOM));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando hay superposición de reservas para el huésped")
        void shouldThrowExceptionWhenGuestOverlaps() {
            // Arrange: el huésped ya tiene una reserva que se superpone
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));
            when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(false);
            when(reservationRepository.existsOverlappingReservationForGuest(
                guestId, checkIn, checkOut, null)).thenReturn(true);

            // Act & Assert
            BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(reservationDto),
                "Debe lanzar excepción cuando hay superposición de reservas para el huésped"
            );
            assertTrue(exception.getMessage().contains(Constants.RESERVATION_OVERLAP_GUEST));
        }
    }

    // ==================== PRUEBAS DE CONSULTA DE RESERVA POR ID ====================
    @Nested
    @DisplayName("✓ OBTENER RESERVA POR ID - Pruebas de búsqueda")
    class GetReservationByIdTests {

        @Test
        @DisplayName("Debe obtener una reserva existente por su ID")
        void shouldGetReservationByIdSuccessfully() {
            // Arrange: configurar mock para retornar una reserva existente
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));
            when(mapper.map(reservationEntity, Reservation.class))
                .thenReturn(reservation);

            // Act: obtener la reserva por ID
            Reservation result = reservationService.getById(reservationId);

            // Assert: verificar que se retornó la reserva correctamente
            assertNotNull(result, "La reserva no debe ser nula");
            assertEquals(reservationId, result.getId(), "El ID debe coincidir");
            assertEquals(guestId, result.getGuestId(), "El ID del huésped debe coincidir");
            assertEquals(roomId, result.getRoomId(), "El ID de la habitación debe coincidir");
            assertEquals(checkIn, result.getCheckIn(), "El checkIn debe coincidir");
            assertEquals(checkOut, result.getCheckOut(), "El checkOut debe coincidir");
            
            verify(reservationRepository, times(1)).findById(reservationId);
            verify(mapper, times(1)).map(reservationEntity, Reservation.class);
        }

        @Test
        @DisplayName("Falla: Debe lanzar excepción cuando la reserva no existe")
        void shouldThrowExceptionWhenReservationNotExists() {
            // Arrange: mock retorna empty para reserva inexistente
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

            // Act & Assert
            EntityNotExistsException exception = assertThrows(
                EntityNotExistsException.class,
                () -> reservationService.getById(reservationId),
                "Debe lanzar EntityNotExistsException cuando la reserva no existe"
            );
            assertTrue(exception.getMessage().contains(Constants.RESERVATION_NOT_FOUND));
            verify(reservationRepository, times(1)).findById(reservationId);
        }
    }

    // ==================== PRUEBAS DE ACTUALIZACIÓN DE RESERVA ====================
    @Nested
    @DisplayName("✓ ACTUALIZAR RESERVA - Pruebas de modificación")
    class UpdateReservationTests {

        @Test
        @DisplayName("Debe actualizar una reserva exitosamente con datos válidos")
        void shouldUpdateReservationSuccessfully() {
            // Arrange: preparar datos para la actualización
            Reservation updatedReservation = new Reservation();
            updatedReservation.setId(reservationId);
            updatedReservation.setGuestId(guestId);
            updatedReservation.setRoomId(roomId);
            updatedReservation.setCheckIn(checkIn);
            updatedReservation.setCheckOut(checkOut);
            updatedReservation.setGuestsCount(2);
            updatedReservation.setNotes("Test reservation");

            // Configurar mocks
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));
            when(reservationRepository.existsOverlappingReservationForRoom(
                any(), any(), any(), any())).thenReturn(false);
            when(reservationRepository.existsOverlappingReservationForGuest(
                any(), any(), any(), any())).thenReturn(false);
            // Donothing para el mapper.map que modifica el objeto existente
            doNothing().when(mapper).map(any(ReservationDto.class), any(ReservationEntity.class));
            when(reservationRepository.saveAndFlush(any(ReservationEntity.class)))
                .thenReturn(reservationEntity);
            when(mapper.map(reservationEntity, Reservation.class))
                .thenReturn(updatedReservation);

            // Act: actualizar la reserva
            Reservation result = reservationService.update(reservationDto, reservationId);

            // Assert
            assertNotNull(result, "La reserva actualizada no debe ser nula");
            assertEquals(reservationId, result.getId(), "El ID debe coincidir");
            
            verify(reservationRepository, times(1)).findById(reservationId);
            verify(reservationRepository, times(1)).saveAndFlush(any(ReservationEntity.class));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando intenta actualizar una reserva inexistente")
        void shouldThrowExceptionWhenUpdatingNonExistentReservation() {
            // Arrange: reserva no existe
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

            // Act & Assert
            EntityNotExistsException exception = assertThrows(
                EntityNotExistsException.class,
                () -> reservationService.update(reservationDto, reservationId),
                "Debe lanzar EntityNotExistsException al intentar actualizar una reserva inexistente"
            );
            assertTrue(exception.getMessage().contains(Constants.RESERVATION_NOT_FOUND));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando la actualización tiene fechas inválidas")
        void shouldThrowExceptionWhenUpdatingWithInvalidDateRange() {
            // Arrange: crear DTO con fechas inválidas
            ReservationDto invalidDto = new ReservationDto();
            invalidDto.setGuestId(guestId);
            invalidDto.setRoomId(roomId);
            invalidDto.setCheckIn(checkOut);
            invalidDto.setCheckOut(checkIn); // Invertido
            invalidDto.setGuestsCount(2);

            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));

            // Act & Assert
            BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.update(invalidDto, reservationId),
                "Debe lanzar excepción cuando las fechas son inválidas"
            );
            assertTrue(exception.getMessage().contains(Constants.INVALID_RESERVATION_RANGE));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando la habitación no está disponible durante la actualización")
        void shouldThrowExceptionWhenUpdatingWithUnavailableRoom() {
            // Arrange: habitación no disponible
            RoomEntity unavailableRoom = new RoomEntity();
            unavailableRoom.setAvailable(false);
            
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(unavailableRoom));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));

            // Act & Assert
            BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.update(reservationDto, reservationId),
                "Debe lanzar excepción cuando la habitación no está disponible"
            );
            assertTrue(exception.getMessage().contains(Constants.ROOM_NOT_AVAILABLE));
        }
    }

    // ==================== PRUEBAS DE ELIMINACIÓN DE RESERVA ====================
    @Nested
    @DisplayName("✓ ELIMINAR RESERVA - Pruebas de borrado")
    class DeleteReservationTests {

        @Test
        @DisplayName("Debe eliminar una reserva exitosamente")
        void shouldDeleteReservationSuccessfully() {
            // Arrange: configurar mock para retornar una reserva existente
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.of(reservationEntity));

            // Act: eliminar la reserva
            assertDoesNotThrow(
                () -> reservationService.delete(reservationId),
                "No debe lanzar excepción al eliminar una reserva existente"
            );

            // Assert: verificar que se invocaron los métodos correctos
            verify(reservationRepository, times(1)).findById(reservationId);
            verify(reservationRepository, times(1)).delete(reservationEntity);
            verify(reservationRepository, times(1)).flush();
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando intenta eliminar una reserva inexistente")
        void shouldThrowExceptionWhenDeletingNonExistentReservation() {
            // Arrange: reserva no existe
            when(reservationRepository.findById(reservationId))
                .thenReturn(Optional.empty());

            // Act & Assert
            EntityNotExistsException exception = assertThrows(
                EntityNotExistsException.class,
                () -> reservationService.delete(reservationId),
                "Debe lanzar EntityNotExistsException al intentar eliminar una reserva inexistente"
            );
            assertTrue(exception.getMessage().contains(Constants.RESERVATION_NOT_FOUND));
            
            // Verificar que delete nunca fue invocado
            verify(reservationRepository, never()).delete(any());
        }
    }

    // ==================== PRUEBAS DE CONSULTA Y RECUPERACIÓN DE RESERVAS ====================
    @Nested
    @DisplayName("✓ CONSULTAR RESERVAS - Pruebas de búsqueda y filtrado")
    class QueryReservationTests {

        @Test
        @DisplayName("Debe obtener todas las reservas exitosamente")
        void shouldGetAllReservationsSuccessfully() {
            // Arrange: preparar datos
            List<ReservationEntity> entities = Arrays.asList(
                reservationEntity,
                new ReservationEntity()
            );
            List<Reservation> reservations = Arrays.asList(
                reservation,
                new Reservation()
            );

            when(reservationRepository.findAll()).thenReturn(entities);
            when(mapper.map(entities, new TypeToken<List<Reservation>>() {}.getType()))
                .thenReturn(reservations);

            // Act: obtener todas las reservas
            List<Reservation> result = reservationService.getAll();

            // Assert
            assertNotNull(result, "La lista de reservas no debe ser nula");
            assertEquals(2, result.size(), "Debe retornar 2 reservas");
            verify(reservationRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Debe obtener reservas por ID de habitación")
        void shouldGetReservationsByRoomIdSuccessfully() {
            // Arrange
            List<ReservationEntity> entities = Arrays.asList(reservationEntity);
            List<Reservation> reservations = Arrays.asList(reservation);

            when(reservationRepository.findByRoomId(roomId)).thenReturn(entities);
            when(mapper.map(entities, new TypeToken<List<Reservation>>() {}.getType()))
                .thenReturn(reservations);

            // Act: obtener reservas de una habitación
            List<Reservation> result = reservationService.getByRoomId(roomId);

            // Assert
            assertNotNull(result, "La lista no debe ser nula");
            assertEquals(1, result.size(), "Debe retornar 1 reserva");
            verify(reservationRepository, times(1)).findByRoomId(roomId);
        }

        @Test
        @DisplayName("Debe obtener reservas por ID de huésped")
        void shouldGetReservationsByGuestIdSuccessfully() {
            // Arrange
            List<ReservationEntity> entities = Arrays.asList(reservationEntity);
            List<Reservation> reservations = Arrays.asList(reservation);

            when(reservationRepository.findByGuestId(guestId)).thenReturn(entities);
            when(mapper.map(entities, new TypeToken<List<Reservation>>() {}.getType()))
                .thenReturn(reservations);

            // Act: obtener reservas de un huésped
            List<Reservation> result = reservationService.getByGuestId(guestId);

            // Assert
            assertNotNull(result, "La lista no debe ser nula");
            assertEquals(1, result.size(), "Debe retornar 1 reserva");
            verify(reservationRepository, times(1)).findByGuestId(guestId);
        }

        @Test
        @DisplayName("Debe retornar una lista vacía cuando no existen reservas")
        void shouldReturnEmptyListWhenNoReservations() {
            // Arrange
            when(reservationRepository.findAll()).thenReturn(Arrays.asList());
            when(mapper.map(Arrays.asList(), new TypeToken<List<Reservation>>() {}.getType()))
                .thenReturn(Arrays.asList());

            // Act: obtener todas las reservas (cuando no hay)
            List<Reservation> result = reservationService.getAll();

            // Assert
            assertNotNull(result, "La lista no debe ser nula");
            assertEquals(0, result.size(), "La lista debe estar vacía");
        }
    }

    // ==================== PRUEBAS DE DISPONIBILIDAD DE HABITACIÓN ====================
    @Nested
    @DisplayName("✓ DISPONIBILIDAD DE HABITACIÓN - Pruebas de validación")
    class RoomAvailabilityTests {

        @Test
        @DisplayName("Debe confirmar que una habitación está disponible para las fechas especificadas")
        void shouldReturnTrueWhenRoomIsAvailable() {
            // Arrange: configurar mocks para indicar disponibilidad
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(false);

            // Act: verificar disponibilidad
            boolean result = reservationService.isRoomAvailable(roomId, checkIn, checkOut);

            // Assert
            assertTrue(result, "La habitación debe estar disponible");
            verify(roomRepository, times(1)).findById(roomId);
            verify(reservationRepository, times(1)).existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null);
        }

        @Test
        @DisplayName("Debe confirmar que una habitación NO está disponible cuando hay superposición")
        void shouldReturnFalseWhenRoomNotAvailable() {
            // Arrange: configurar mocks para indicar NO disponibilidad
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(true);

            // Act: verificar disponibilidad
            boolean result = reservationService.isRoomAvailable(roomId, checkIn, checkOut);

            // Assert
            assertFalse(result, "La habitación no debe estar disponible por superposición");
        }

        @Test
        @DisplayName("Falla: Debe lanzar excepción si la habitación no existe")
        void shouldThrowExceptionWhenCheckingAvailabilityOfNonExistentRoom() {
            // Arrange: habitación no existe
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotExistsException exception = assertThrows(
                EntityNotExistsException.class,
                () -> reservationService.isRoomAvailable(roomId, checkIn, checkOut),
                "Debe lanzar EntityNotExistsException si la habitación no existe"
            );
            assertTrue(exception.getMessage().contains(Constants.ROOM_NOT_FOUND));
        }

        @Test
        @DisplayName("Falla: Debe lanzar excepción si el rango de fechas es inválido")
        void shouldThrowExceptionWhenCheckingAvailabilityWithInvalidDateRange() {
            // Act & Assert: no se debe ni verificar existencia de habitación
            BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.isRoomAvailable(roomId, checkOut, checkIn),
                "Debe lanzar excepción cuando las fechas son inválidas"
            );
            assertTrue(exception.getMessage().contains(Constants.INVALID_RESERVATION_RANGE));
        }
    }
}
