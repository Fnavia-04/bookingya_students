package com.project.bookingya;

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
import com.project.bookingya.services.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * FASE 1 — TDD
 * Pruebas unitarias del servicio ReservationService usando Mockito.
 *
 * Estrategia TDD aplicada:
 *  RED   → Se escribe el test antes de que la lógica exista/pase.
 *  GREEN → El servicio implementa la lógica mínima para que pase.
 *  REFACTOR → Se limpia el código sin romper los tests.
 *
 * Los colaboradores (repositorios, ModelMapper) son mocks; el servicio
 * se prueba en completo aislamiento de la base de datos.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("FASE 1 — TDD: ReservationService (pruebas unitarias)")
class ReservationServiceTDDTest {

    // ── Mocks ────────────────────────────────────────────────────────────────
    @Mock private IReservationRepository reservationRepository;
    @Mock private IRoomRepository        roomRepository;
    @Mock private IGuestRepository       guestRepository;
    @Mock private ModelMapper            mapper;

    @InjectMocks
    private ReservationService reservationService;

    // ── Fixtures reutilizables ───────────────────────────────────────────────
    private UUID roomId;
    private UUID guestId;
    private UUID reservationId;
    private RoomEntity availableRoom;
    private GuestEntity guest;
    private ReservationEntity reservationEntity;
    private Reservation reservationModel;
    private ReservationDto validDto;

    @BeforeEach
    void setUp() {
        roomId        = UUID.randomUUID();
        guestId       = UUID.randomUUID();
        reservationId = UUID.randomUUID();

        availableRoom = new RoomEntity();
        availableRoom.setId(roomId);
        availableRoom.setCode("R-001");
        availableRoom.setName("Suite Premium");
        availableRoom.setCity("Bogotá");
        availableRoom.setMaxGuests(3);
        availableRoom.setNightlyPrice(new BigDecimal("200.00"));
        availableRoom.setAvailable(true);

        guest = new GuestEntity();
        guest.setId(guestId);
        guest.setIdentification("CC-123");
        guest.setName("Carlos Pérez");
        guest.setEmail("carlos@mail.com");

        reservationEntity = new ReservationEntity();
        reservationEntity.setId(reservationId);
        reservationEntity.setRoomId(roomId);
        reservationEntity.setGuestId(guestId);
        reservationEntity.setCheckIn(future(1));
        reservationEntity.setCheckOut(future(5));
        reservationEntity.setGuestsCount(2);

        reservationModel = new Reservation();
        reservationModel.setId(reservationId);
        reservationModel.setRoomId(roomId);
        reservationModel.setGuestId(guestId);
        reservationModel.setCheckIn(future(1));
        reservationModel.setCheckOut(future(5));
        reservationModel.setGuestsCount(2);

        validDto = new ReservationDto();
        validDto.setRoomId(roomId);
        validDto.setGuestId(guestId);
        validDto.setCheckIn(future(1));
        validDto.setCheckOut(future(5));
        validDto.setGuestsCount(2);
    }

    // =========================================================================
    // CREAR RESERVA
    // =========================================================================

    @Test
    @DisplayName("TDD-01 Crear reserva válida retorna el modelo guardado")
    void crear_reserva_valida_retorna_modelo() {
        // Arrange
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(availableRoom));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));
        when(reservationRepository.existsOverlappingReservationForRoom(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(any(), any(), any(), any())).thenReturn(false);
        when(mapper.map(validDto, ReservationEntity.class)).thenReturn(reservationEntity);
        when(reservationRepository.saveAndFlush(reservationEntity)).thenReturn(reservationEntity);
        when(mapper.map(reservationEntity, Reservation.class)).thenReturn(reservationModel);

        // Act
        Reservation resultado = reservationService.create(validDto);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(reservationId);
        assertThat(resultado.getRoomId()).isEqualTo(roomId);
        assertThat(resultado.getGuestsCount()).isEqualTo(2);
        verify(reservationRepository, times(1)).saveAndFlush(reservationEntity);
    }

    @Test
    @DisplayName("TDD-02 Crear reserva falla cuando checkIn >= checkOut")
    void crear_reserva_falla_rango_invalido() {
        // Arrange
        validDto.setCheckIn(future(10));
        validDto.setCheckOut(future(5)); // checkIn > checkOut → inválido

        // Act & Assert
        assertThatThrownBy(() -> reservationService.create(validDto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("checkIn must be before checkOut");

        verifyNoInteractions(reservationRepository);
    }

    @Test
    @DisplayName("TDD-03 Crear reserva falla cuando la habitación no existe")
    void crear_reserva_falla_habitacion_no_existe() {
        // Arrange
        when(roomRepository.findById(roomId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reservationService.create(validDto))
                .isInstanceOf(EntityNotExistsException.class)
                .hasMessage("Room not found");
    }

    @Test
    @DisplayName("TDD-04 Crear reserva falla cuando el huésped no existe")
    void crear_reserva_falla_huesped_no_existe() {
        // Arrange
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(availableRoom));
        when(guestRepository.findById(guestId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reservationService.create(validDto))
                .isInstanceOf(EntityNotExistsException.class)
                .hasMessage("Guest not found");
    }

    @Test
    @DisplayName("TDD-05 Crear reserva falla cuando la habitación no está disponible")
    void crear_reserva_falla_habitacion_no_disponible() {
        // Arrange
        availableRoom.setAvailable(false);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(availableRoom));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));

        // Act & Assert
        assertThatThrownBy(() -> reservationService.create(validDto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Room is not available");
    }

    @Test
    @DisplayName("TDD-06 Crear reserva falla cuando guestsCount supera maxGuests")
    void crear_reserva_falla_capacidad_excedida() {
        // Arrange
        validDto.setGuestsCount(10); // maxGuests = 3
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(availableRoom));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));

        // Act & Assert
        assertThatThrownBy(() -> reservationService.create(validDto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("guestsCount exceeds room capacity");
    }

    @Test
    @DisplayName("TDD-07 Crear reserva falla cuando hay solapamiento en la habitación")
    void crear_reserva_falla_solapamiento_habitacion() {
        // Arrange
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(availableRoom));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));
        when(reservationRepository.existsOverlappingReservationForRoom(any(), any(), any(), any())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> reservationService.create(validDto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("The room already has a reservation in that time range");
    }

    @Test
    @DisplayName("TDD-08 Crear reserva falla cuando el huésped ya tiene reserva solapada")
    void crear_reserva_falla_solapamiento_huesped() {
        // Arrange
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(availableRoom));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));
        when(reservationRepository.existsOverlappingReservationForRoom(any(), any(), any(), any())).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(any(), any(), any(), any())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> reservationService.create(validDto))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("The guest already has a reservation in that time range");
    }

    // =========================================================================
    // CONSULTAR RESERVAS
    // =========================================================================

    @Test
    @DisplayName("TDD-09 Consultar todas las reservas retorna lista mapeada")
    void consultar_todas_las_reservas() {
        // Arrange
        List<ReservationEntity> entities = List.of(reservationEntity);
        Type listType = new TypeToken<List<Reservation>>() {}.getType();
        when(reservationRepository.findAll()).thenReturn(entities);
        when(mapper.map(entities, listType)).thenReturn(List.of(reservationModel));

        // Act
        List<Reservation> resultado = reservationService.getAll();

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getId()).isEqualTo(reservationId);
    }

    @Test
    @DisplayName("TDD-10 Obtener reserva por ID existente retorna el modelo")
    void obtener_reserva_por_id_existente() {
        // Arrange
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservationEntity));
        when(mapper.map(reservationEntity, Reservation.class)).thenReturn(reservationModel);

        // Act
        Reservation resultado = reservationService.getById(reservationId);

        // Assert
        assertThat(resultado).isNotNull();
        assertThat(resultado.getId()).isEqualTo(reservationId);
    }

    @Test
    @DisplayName("TDD-11 Obtener reserva por ID inexistente lanza excepción")
    void obtener_reserva_por_id_inexistente() {
        // Arrange
        UUID idFalso = UUID.randomUUID();
        when(reservationRepository.findById(idFalso)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reservationService.getById(idFalso))
                .isInstanceOf(EntityNotExistsException.class)
                .hasMessage("Reservation not found");
    }

    @Test
    @DisplayName("TDD-12 Consultar reservas por roomId retorna lista filtrada")
    void consultar_reservas_por_room_id() {
        // Arrange
        List<ReservationEntity> entities = List.of(reservationEntity);
        Type listType = new TypeToken<List<Reservation>>() {}.getType();
        when(reservationRepository.findByRoomId(roomId)).thenReturn(entities);
        when(mapper.map(entities, listType)).thenReturn(List.of(reservationModel));

        // Act
        List<Reservation> resultado = reservationService.getByRoomId(roomId);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getRoomId()).isEqualTo(roomId);
    }

    @Test
    @DisplayName("TDD-13 Consultar reservas por guestId retorna lista filtrada")
    void consultar_reservas_por_guest_id() {
        // Arrange
        List<ReservationEntity> entities = List.of(reservationEntity);
        Type listType = new TypeToken<List<Reservation>>() {}.getType();
        when(reservationRepository.findByGuestId(guestId)).thenReturn(entities);
        when(mapper.map(entities, listType)).thenReturn(List.of(reservationModel));

        // Act
        List<Reservation> resultado = reservationService.getByGuestId(guestId);

        // Assert
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getGuestId()).isEqualTo(guestId);
    }

    // =========================================================================
    // ACTUALIZAR RESERVA
    // =========================================================================

    @Test
    @DisplayName("TDD-14 Actualizar reserva válida retorna modelo actualizado")
    void actualizar_reserva_valida() {
        // Arrange
        ReservationDto updateDto = new ReservationDto();
        updateDto.setRoomId(roomId);
        updateDto.setGuestId(guestId);
        updateDto.setCheckIn(future(10));
        updateDto.setCheckOut(future(15));
        updateDto.setGuestsCount(1);

        Reservation updatedModel = new Reservation();
        updatedModel.setId(reservationId);
        updatedModel.setGuestsCount(1);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservationEntity));
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(availableRoom));
        when(guestRepository.findById(guestId)).thenReturn(Optional.of(guest));
        when(reservationRepository.existsOverlappingReservationForRoom(any(), any(), any(), eq(reservationId))).thenReturn(false);
        when(reservationRepository.existsOverlappingReservationForGuest(any(), any(), any(), eq(reservationId))).thenReturn(false);
        when(reservationRepository.saveAndFlush(any())).thenReturn(reservationEntity);
        when(mapper.map(reservationEntity, Reservation.class)).thenReturn(updatedModel);

        // Act
        Reservation resultado = reservationService.update(updateDto, reservationId);

        // Assert
        assertThat(resultado).isNotNull();
        verify(reservationRepository).saveAndFlush(any());
    }

    @Test
    @DisplayName("TDD-15 Actualizar reserva inexistente lanza excepción")
    void actualizar_reserva_inexistente() {
        // Arrange
        UUID idFalso = UUID.randomUUID();
        when(reservationRepository.findById(idFalso)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reservationService.update(validDto, idFalso))
                .isInstanceOf(EntityNotExistsException.class)
                .hasMessage("Reservation not found");
    }

    // =========================================================================
    // ELIMINAR RESERVA
    // =========================================================================

    @Test
    @DisplayName("TDD-16 Eliminar reserva existente invoca delete en el repositorio")
    void eliminar_reserva_existente() {
        // Arrange
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(reservationEntity));

        // Act
        reservationService.delete(reservationId);

        // Assert
        verify(reservationRepository, times(1)).delete(reservationEntity);
        verify(reservationRepository, times(1)).flush();
    }

    @Test
    @DisplayName("TDD-17 Eliminar reserva inexistente lanza excepción")
    void eliminar_reserva_inexistente() {
        // Arrange
        UUID idFalso = UUID.randomUUID();
        when(reservationRepository.findById(idFalso)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reservationService.delete(idFalso))
                .isInstanceOf(EntityNotExistsException.class)
                .hasMessage("Reservation not found");

        verify(reservationRepository, never()).delete(any());
    }

    // =========================================================================
    // DISPONIBILIDAD
    // =========================================================================

    @Test
    @DisplayName("TDD-18 Consultar disponibilidad retorna true cuando no hay solapamiento")
    void consultar_disponibilidad_disponible() {
        // Arrange
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(availableRoom));
        when(reservationRepository.existsOverlappingReservationForRoom(eq(roomId), any(), any(), isNull())).thenReturn(false);

        // Act
        boolean disponible = reservationService.isRoomAvailable(roomId, future(1), future(5));

        // Assert
        assertThat(disponible).isTrue();
    }

    @Test
    @DisplayName("TDD-19 Consultar disponibilidad retorna false cuando hay solapamiento")
    void consultar_disponibilidad_no_disponible() {
        // Arrange
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(availableRoom));
        when(reservationRepository.existsOverlappingReservationForRoom(eq(roomId), any(), any(), isNull())).thenReturn(true);

        // Act
        boolean disponible = reservationService.isRoomAvailable(roomId, future(1), future(5));

        // Assert
        assertThat(disponible).isFalse();
    }

    @Test
    @DisplayName("TDD-20 Consultar disponibilidad con rango inválido lanza excepción")
    void consultar_disponibilidad_rango_invalido() {
        assertThatThrownBy(() -> reservationService.isRoomAvailable(roomId, future(10), future(5)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("checkIn must be before checkOut");
    }

    // ── Helper ───────────────────────────────────────────────────────────────
    private LocalDateTime future(int days) {
        return LocalDateTime.now().plusDays(days).withHour(14).withMinute(0).withSecond(0).withNano(0);
    }
}
