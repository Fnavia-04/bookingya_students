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
 * âœ“ CreaciÃ³n de una reserva con validaciÃ³n de reglas de negocio
 * âœ“ Consulta de una reserva por ID
 * âœ“ ActualizaciÃ³n de una reserva existente
 * âœ“ EliminaciÃ³n de una reserva
 * âœ“ ObtenciÃ³n de reservas por diferentes criterios (ID, habitaciÃ³n, huÃ©sped, todas)
 * âœ“ VerificaciÃ³n de disponibilidad de habitaciones
 * 
 * Patrones aplicados:
 * - TDD (Test-Driven Development)
 * - PatrÃ³n AAA (Arrange, Act, Assert)
 * - Pruebas anidadas (@Nested) por funcionalidad
 * - Mockito para aislar la lÃ³gica del servicio
 * - @DisplayName para descripciones legibles
 * - Datos de prueba reutilizables
 * 
 * @author Equipo de QA - Fase 1
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings("unchecked")`n@DisplayName("ReservationService - Pruebas Unitarias FASE 1 TDD")
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

    // ==================== PRUEBAS DE CREACIÃ“N DE RESERVA ====================
    @Nested
    @DisplayName("âœ“ CREAR RESERVA - Pruebas de funcionalidad y validaciÃ³n")
    class CreateReservationTests {

        @Test
        @DisplayName("Debe crear una reserva exitosamente con datos vÃ¡lidos")
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
            assertEquals(guestId, result.getGuestId(), "El ID del huÃ©sped debe coincidir");
            assertEquals(roomId, result.getRoomId(), "El ID de la habitaciÃ³n debe coincidir");
            assertEquals(2, result.getGuestsCount(), "La cantidad de huÃ©spedes debe ser 2");
            assertEquals(checkIn, result.getCheckIn(), "El checkIn debe coincidir");
            assertEquals(checkOut, result.getCheckOut(), "El checkOut debe coincidir");
            
            // Verificar que los mÃ©todos fueron invocados correctamente
            verify(roomRepository, times(1)).findById(roomId);
            verify(guestRepository, times(1)).findById(guestId);
            verify(reservationRepository, times(1)).saveAndFlush(any(ReservationEntity.class));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando checkOut es antes que checkIn")
        void shouldThrowExceptionWhenInvalidDateRange() {
            // Arrange: crear DTO con fechas invÃ¡lidas (checkOut < checkIn)
            ReservationDto invalidDto = new ReservationDto();
            invalidDto.setGuestId(guestId);
            invalidDto.setRoomId(roomId);
            invalidDto.setCheckIn(checkOut);
            invalidDto.setCheckOut(checkIn); // Invertido intencionalmente
            invalidDto.setGuestsCount(2);

            // Act & Assert: verificar que se lance excepciÃ³n BusinessRuleException
            BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(invalidDto),
                "Debe lanzar BusinessRuleException por rango de fechas invÃ¡lido"
            );
            assertTrue(exception.getMessage().contains(Constants.INVALID_RESERVATION_RANGE));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando guestsCount es <= 0")
        void shouldThrowExceptionWhenInvalidGuestsCount() {
            // Arrange: crear DTO con cantidad de huÃ©spedes invÃ¡lida
            ReservationDto invalidDto = new ReservationDto();
            invalidDto.setGuestId(guestId);
            invalidDto.setRoomId(roomId);
            invalidDto.setCheckIn(checkIn);
            invalidDto.setCheckOut(checkOut);
            invalidDto.setGuestsCount(0); // InvÃ¡lido: 0 huÃ©spedes

            // Act & Assert
            BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(invalidDto),
                "Debe lanzar excepciÃ³n cuando guestsCount <= 0"
            );
            assertTrue(exception.getMessage().contains(Constants.INVALID_GUESTS_COUNT));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando la habitaciÃ³n no existe")
        void shouldThrowExceptionWhenRoomNotExists() {
            // Arrange: mock retorna empty para habitaciÃ³n inexistente
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotExistsException exception = assertThrows(
                EntityNotExistsException.class,
                () -> reservationService.create(reservationDto),
                "Debe lanzar EntityNotExistsException cuando la habitaciÃ³n no existe"
            );
            assertTrue(exception.getMessage().contains(Constants.ROOM_NOT_FOUND));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando el huÃ©sped no existe")
        void shouldThrowExceptionWhenGuestNotExists() {
            // Arrange: habitaciÃ³n existe pero huÃ©sped no
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotExistsException exception = assertThrows(
                EntityNotExistsException.class,
                () -> reservationService.create(reservationDto),
                "Debe lanzar EntityNotExistsException cuando el huÃ©sped no existe"
            );
            assertTrue(exception.getMessage().contains(Constants.GUEST_NOT_FOUND));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando la habitaciÃ³n no estÃ¡ disponible")
        void shouldThrowExceptionWhenRoomNotAvailable() {
            // Arrange: habitaciÃ³n con available = false
            roomEntity.setAvailable(false);
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));

            // Act & Assert
            BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(reservationDto),
                "Debe lanzar excepciÃ³n cuando la habitaciÃ³n no estÃ¡ disponible"
            );
            assertTrue(exception.getMessage().contains(Constants.ROOM_NOT_AVAILABLE));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando la cantidad de huÃ©spedes excede la capacidad")
        void shouldThrowExceptionWhenGuestsExceedCapacity() {
            // Arrange: solicitar mÃ¡s huÃ©spedes de los que la habitaciÃ³n puede alojar
            ReservationDto invalidDto = new ReservationDto();
            invalidDto.setGuestId(guestId);
            invalidDto.setRoomId(roomId);
            invalidDto.setCheckIn(checkIn);
            invalidDto.setCheckOut(checkOut);
            invalidDto.setGuestsCount(5); // HabitaciÃ³n solo permite 4 (ver setUp)

            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));

            // Act & Assert
            BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(invalidDto),
                "Debe lanzar excepciÃ³n cuando guestsCount > maxGuests"
            );
            assertTrue(exception.getMessage().contains(Constants.ROOM_CAPACITY_EXCEEDED));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando hay superposiciÃ³n de reservas en la habitaciÃ³n")
        void shouldThrowExceptionWhenRoomOverlaps() {
            // Arrange: existe una reserva que se superpone en la habitaciÃ³n
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(guestRepository.findById(guestId)).thenReturn(Optional.of(guestEntity));
            when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(true);

            // Act & Assert
            BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.create(reservationDto),
                "Debe lanzar excepciÃ³n cuando hay superposiciÃ³n de reservas en la habitaciÃ³n"
            );
            assertTrue(exception.getMessage().contains(Constants.RESERVATION_OVERLAP_ROOM));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando hay superposiciÃ³n de reservas para el huÃ©sped")
        void shouldThrowExceptionWhenGuestOverlaps() {
            // Arrange: el huÃ©sped ya tiene una reserva que se superpone
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
                "Debe lanzar excepciÃ³n cuando hay superposiciÃ³n de reservas para el huÃ©sped"
            );
            assertTrue(exception.getMessage().contains(Constants.RESERVATION_OVERLAP_GUEST));
        }
    }

    // ==================== PRUEBAS DE CONSULTA DE RESERVA POR ID ====================
    @Nested
    @DisplayName("âœ“ OBTENER RESERVA POR ID - Pruebas de bÃºsqueda")
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

            // Assert: verificar que se retornÃ³ la reserva correctamente
            assertNotNull(result, "La reserva no debe ser nula");
            assertEquals(reservationId, result.getId(), "El ID debe coincidir");
            assertEquals(guestId, result.getGuestId(), "El ID del huÃ©sped debe coincidir");
            assertEquals(roomId, result.getRoomId(), "El ID de la habitaciÃ³n debe coincidir");
            assertEquals(checkIn, result.getCheckIn(), "El checkIn debe coincidir");
            assertEquals(checkOut, result.getCheckOut(), "El checkOut debe coincidir");
            
            verify(reservationRepository, times(1)).findById(reservationId);
            verify(mapper, times(1)).map(reservationEntity, Reservation.class);
        }

        @Test
        @DisplayName("Falla: Debe lanzar excepciÃ³n cuando la reserva no existe")
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

    // ==================== PRUEBAS DE ACTUALIZACIÃ“N DE RESERVA ====================
    @Nested
    @DisplayName("âœ“ ACTUALIZAR RESERVA - Pruebas de modificaciÃ³n")
    class UpdateReservationTests {

        @Test
        @DisplayName("Debe actualizar una reserva exitosamente con datos vÃ¡lidos")
        void shouldUpdateReservationSuccessfully() {
            // Arrange: preparar datos para la actualizaciÃ³n
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
        @DisplayName("Falla: Debe rechazar cuando la actualizaciÃ³n tiene fechas invÃ¡lidas")
        void shouldThrowExceptionWhenUpdatingWithInvalidDateRange() {
            // Arrange: crear DTO con fechas invÃ¡lidas
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
                "Debe lanzar excepciÃ³n cuando las fechas son invÃ¡lidas"
            );
            assertTrue(exception.getMessage().contains(Constants.INVALID_RESERVATION_RANGE));
        }

        @Test
        @DisplayName("Falla: Debe rechazar cuando la habitaciÃ³n no estÃ¡ disponible durante la actualizaciÃ³n")
        void shouldThrowExceptionWhenUpdatingWithUnavailableRoom() {
            // Arrange: habitaciÃ³n no disponible
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
                "Debe lanzar excepciÃ³n cuando la habitaciÃ³n no estÃ¡ disponible"
            );
            assertTrue(exception.getMessage().contains(Constants.ROOM_NOT_AVAILABLE));
        }
    }

    // ==================== PRUEBAS DE ELIMINACIÃ“N DE RESERVA ====================
    @Nested
    @DisplayName("âœ“ ELIMINAR RESERVA - Pruebas de borrado")
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
                "No debe lanzar excepciÃ³n al eliminar una reserva existente"
            );

            // Assert: verificar que se invocaron los mÃ©todos correctos
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

    // ==================== PRUEBAS DE CONSULTA Y RECUPERACIÃ“N DE RESERVAS ====================
    @Nested
    @DisplayName("âœ“ CONSULTAR RESERVAS - Pruebas de bÃºsqueda y filtrado")
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
        @DisplayName("Debe obtener reservas por ID de habitaciÃ³n")
        void shouldGetReservationsByRoomIdSuccessfully() {
            // Arrange
            List<ReservationEntity> entities = Arrays.asList(reservationEntity);
            List<Reservation> reservations = Arrays.asList(reservation);

            when(reservationRepository.findByRoomId(roomId)).thenReturn(entities);
            when(mapper.map(entities, new TypeToken<List<Reservation>>() {}.getType()))
                .thenReturn(reservations);

            // Act: obtener reservas de una habitaciÃ³n
            List<Reservation> result = reservationService.getByRoomId(roomId);

            // Assert
            assertNotNull(result, "La lista no debe ser nula");
            assertEquals(1, result.size(), "Debe retornar 1 reserva");
            verify(reservationRepository, times(1)).findByRoomId(roomId);
        }

        @Test
        @DisplayName("Debe obtener reservas por ID de huÃ©sped")
        void shouldGetReservationsByGuestIdSuccessfully() {
            // Arrange
            List<ReservationEntity> entities = Arrays.asList(reservationEntity);
            List<Reservation> reservations = Arrays.asList(reservation);

            when(reservationRepository.findByGuestId(guestId)).thenReturn(entities);
            when(mapper.map(entities, new TypeToken<List<Reservation>>() {}.getType()))
                .thenReturn(reservations);

            // Act: obtener reservas de un huÃ©sped
            List<Reservation> result = reservationService.getByGuestId(guestId);

            // Assert
            assertNotNull(result, "La lista no debe ser nula");
            assertEquals(1, result.size(), "Debe retornar 1 reserva");
            verify(reservationRepository, times(1)).findByGuestId(guestId);
        }

        @Test
        @DisplayName("Debe retornar una lista vacÃ­a cuando no existen reservas")
        void shouldReturnEmptyListWhenNoReservations() {
            // Arrange
            when(reservationRepository.findAll()).thenReturn(Arrays.asList());
            when(mapper.map(Arrays.asList(), new TypeToken<List<Reservation>>() {}.getType()))
                .thenReturn(Arrays.asList());

            // Act: obtener todas las reservas (cuando no hay)
            List<Reservation> result = reservationService.getAll();

            // Assert
            assertNotNull(result, "La lista no debe ser nula");
            assertEquals(0, result.size(), "La lista debe estar vacÃ­a");
        }
    }

    // ==================== PRUEBAS DE DISPONIBILIDAD DE HABITACIÃ“N ====================
    @Nested
    @DisplayName("âœ“ DISPONIBILIDAD DE HABITACIÃ“N - Pruebas de validaciÃ³n")
    class RoomAvailabilityTests {

        @Test
        @DisplayName("Debe confirmar que una habitaciÃ³n estÃ¡ disponible para las fechas especificadas")
        void shouldReturnTrueWhenRoomIsAvailable() {
            // Arrange: configurar mocks para indicar disponibilidad
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(false);

            // Act: verificar disponibilidad
            boolean result = reservationService.isRoomAvailable(roomId, checkIn, checkOut);

            // Assert
            assertTrue(result, "La habitaciÃ³n debe estar disponible");
            verify(roomRepository, times(1)).findById(roomId);
            verify(reservationRepository, times(1)).existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null);
        }

        @Test
        @DisplayName("Debe confirmar que una habitaciÃ³n NO estÃ¡ disponible cuando hay superposiciÃ³n")
        void shouldReturnFalseWhenRoomNotAvailable() {
            // Arrange: configurar mocks para indicar NO disponibilidad
            when(roomRepository.findById(roomId)).thenReturn(Optional.of(roomEntity));
            when(reservationRepository.existsOverlappingReservationForRoom(
                roomId, checkIn, checkOut, null)).thenReturn(true);

            // Act: verificar disponibilidad
            boolean result = reservationService.isRoomAvailable(roomId, checkIn, checkOut);

            // Assert
            assertFalse(result, "La habitaciÃ³n no debe estar disponible por superposiciÃ³n");
        }

        @Test
        @DisplayName("Falla: Debe lanzar excepciÃ³n si la habitaciÃ³n no existe")
        void shouldThrowExceptionWhenCheckingAvailabilityOfNonExistentRoom() {
            // Arrange: habitaciÃ³n no existe
            when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

            // Act & Assert
            EntityNotExistsException exception = assertThrows(
                EntityNotExistsException.class,
                () -> reservationService.isRoomAvailable(roomId, checkIn, checkOut),
                "Debe lanzar EntityNotExistsException si la habitaciÃ³n no existe"
            );
            assertTrue(exception.getMessage().contains(Constants.ROOM_NOT_FOUND));
        }

        @Test
        @DisplayName("Falla: Debe lanzar excepciÃ³n si el rango de fechas es invÃ¡lido")
        void shouldThrowExceptionWhenCheckingAvailabilityWithInvalidDateRange() {
            // Act & Assert: no se debe ni verificar existencia de habitaciÃ³n
            BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> reservationService.isRoomAvailable(roomId, checkOut, checkIn),
                "Debe lanzar excepciÃ³n cuando las fechas son invÃ¡lidas"
            );
            assertTrue(exception.getMessage().contains(Constants.INVALID_RESERVATION_RANGE));
        }
    }
}


