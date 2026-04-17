package com.project.bookingya.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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

import com.project.bookingya.dtos.RoomDto;
import com.project.bookingya.entities.RoomEntity;
import com.project.bookingya.exceptions.EntityExistsException;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Room;
import com.project.bookingya.repositories.IRoomRepository;
import com.project.bookingya.shared.Constants;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoomService Unit Tests")
class RoomServiceTest {

    @Mock
    private IRoomRepository roomRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private RoomService roomService;

    private UUID roomId;
    private RoomEntity roomEntity;
    private RoomDto roomDto;
    private Room room;

    @BeforeEach
    void setUp() {
        roomId = UUID.randomUUID();

        roomEntity = new RoomEntity();
        roomEntity.setId(roomId);
        roomEntity.setCode("ROOM-001");
        roomEntity.setName("Habitación Deluxe");
        roomEntity.setCity("Bogotá");
        roomEntity.setMaxGuests(4);
        roomEntity.setNightlyPrice(new BigDecimal("150.00"));
        roomEntity.setAvailable(true);

        roomDto = new RoomDto();
        roomDto.setCode("ROOM-001");
        roomDto.setName("Habitación Deluxe");
        roomDto.setCity("Bogotá");
        roomDto.setMaxGuests(4);
        roomDto.setNightlyPrice(new BigDecimal("150.00"));
        roomDto.setAvailable(true);

        room = new Room();
        room.setId(roomId);
        room.setCode("ROOM-001");
        room.setName("Habitación Deluxe");
        room.setCity("Bogotá");
        room.setMaxGuests(4);
        room.setNightlyPrice(new BigDecimal("150.00"));
        room.setAvailable(true);
    }

    @Nested
    @DisplayName("CREATE - Crear una habitación")
    class CreateRoomTests {

        @Test
        @DisplayName("Debe crear una habitación exitosamente con datos válidos")
        void shouldCreateRoomSuccessfully() {
            // Arrange
            when(roomRepository.existsByCode(roomDto.getCode()))
                .thenReturn(false);
            when(mapper.map(roomDto, RoomEntity.class))
                .thenReturn(roomEntity);
            when(roomRepository.save(any(RoomEntity.class)))
                .thenReturn(roomEntity);
            when(mapper.map(roomEntity, Room.class))
                .thenReturn(room);

            // Act
            Room result = roomService.create(roomDto);

            // Assert
            assertNotNull(result);
            assertEquals(roomId, result.getId());
            assertEquals("ROOM-001", result.getCode());
            assertEquals("Habitación Deluxe", result.getName());
            assertEquals(4, result.getMaxGuests());
            assertTrue(result.getAvailable());
            verify(roomRepository, times(1)).save(any(RoomEntity.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el código de habitación ya existe")
        void shouldThrowExceptionWhenRoomCodeExists() {
            // Arrange
            when(roomRepository.existsByCode(roomDto.getCode()))
                .thenReturn(true);

            // Act & Assert
            assertThrows(EntityExistsException.class, () -> {
                roomService.create(roomDto);
            });
            verify(roomRepository, times(0)).save(any());
        }
    }

    @Nested
    @DisplayName("READ - Consultar una habitación")
    class ReadRoomTests {

        @Test
        @DisplayName("Debe obtener una habitación por ID exitosamente")
        void shouldGetRoomByIdSuccessfully() {
            // Arrange
            when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(roomEntity));
            when(mapper.map(roomEntity, Room.class))
                .thenReturn(room);

            // Act
            Room result = roomService.getById(roomId);

            // Assert
            assertNotNull(result);
            assertEquals(roomId, result.getId());
            assertEquals("ROOM-001", result.getCode());
            verify(roomRepository, times(1)).findById(roomId);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando la habitación no existe por ID")
        void shouldThrowExceptionWhenRoomNotFoundById() {
            // Arrange
            when(roomRepository.findById(roomId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                roomService.getById(roomId);
            });
        }

        @Test
        @DisplayName("Debe obtener una habitación por code exitosamente")
        void shouldGetRoomByCodeSuccessfully() {
            // Arrange
            when(roomRepository.findByCode("ROOM-001"))
                .thenReturn(Optional.of(roomEntity));
            when(mapper.map(roomEntity, Room.class))
                .thenReturn(room);

            // Act
            Room result = roomService.getByCode("ROOM-001");

            // Assert
            assertNotNull(result);
            assertEquals("ROOM-001", result.getCode());
            verify(roomRepository, times(1)).findByCode("ROOM-001");
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando la habitación no existe por code")
        void shouldThrowExceptionWhenRoomNotFoundByCode() {
            // Arrange
            when(roomRepository.findByCode("ROOM-999"))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                roomService.getByCode("ROOM-999");
            });
        }

        @Test
        @DisplayName("Debe obtener todas las habitaciones")
        void shouldGetAllRoomsSuccessfully() {
            // Arrange
            List<RoomEntity> entities = List.of(roomEntity);
            List<Room> rooms = List.of(room);
            when(roomRepository.findAll()).thenReturn(entities);
            when(mapper.map(eq(entities), any(java.lang.reflect.Type.class)))
                .thenReturn(rooms);

            // Act
            List<Room> result = roomService.getAll();

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            verify(roomRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("UPDATE - Actualizar una habitación")
    class UpdateRoomTests {

        @Test
        @DisplayName("Debe actualizar una habitación exitosamente")
        void shouldUpdateRoomSuccessfully() {
            // Arrange
            RoomDto updateDto = new RoomDto();
            updateDto.setCode("ROOM-001"); // Mismo código que el existente
            updateDto.setName("Habitación Deluxe Premium");
            updateDto.setCity("Medellín");
            updateDto.setMaxGuests(6);
            updateDto.setNightlyPrice(new BigDecimal("200.00"));
            updateDto.setAvailable(true);

            when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(roomEntity));
            doNothing().when(mapper).map(updateDto, roomEntity);
            when(roomRepository.save(any(RoomEntity.class)))
                .thenReturn(roomEntity);
            when(mapper.map(roomEntity, Room.class))
                .thenReturn(room);

            // Act
            Room result = roomService.update(updateDto, roomId);

            // Assert
            assertNotNull(result);
            verify(roomRepository, times(1)).findById(roomId);
            verify(roomRepository, times(1)).save(any(RoomEntity.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando la habitación no existe en actualización")
        void shouldThrowExceptionWhenRoomNotFoundOnUpdate() {
            // Arrange
            when(roomRepository.findById(roomId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                roomService.update(roomDto, roomId);
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando se intenta actualizar con código duplicado")
        void shouldThrowExceptionWhenUpdatingWithDuplicateCode() {
            // Arrange
            RoomDto updateDto = new RoomDto();
            updateDto.setCode("ROOM-002");
            updateDto.setName("Habitación");
            updateDto.setCity("Bogotá");
            updateDto.setMaxGuests(2);
            updateDto.setNightlyPrice(new BigDecimal("100.00"));
            updateDto.setAvailable(true);

            when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(roomEntity));
            when(roomRepository.existsByCode(updateDto.getCode()))
                .thenReturn(true);

            // Act & Assert
            assertThrows(EntityExistsException.class, () -> {
                roomService.update(updateDto, roomId);
            });
        }
    }

    @Nested
    @DisplayName("DELETE - Eliminar una habitación")
    class DeleteRoomTests {

        @Test
        @DisplayName("Debe eliminar una habitación exitosamente")
        void shouldDeleteRoomSuccessfully() {
            // Arrange
            when(roomRepository.findById(roomId))
                .thenReturn(Optional.of(roomEntity));
            doNothing().when(roomRepository).delete(roomEntity);

            // Act
            roomService.delete(roomId);

            // Assert
            verify(roomRepository, times(1)).findById(roomId);
            verify(roomRepository, times(1)).delete(roomEntity);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando la habitación no existe en eliminación")
        void shouldThrowExceptionWhenRoomNotFoundOnDelete() {
            // Arrange
            when(roomRepository.findById(roomId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                roomService.delete(roomId);
            });
        }
    }
}
