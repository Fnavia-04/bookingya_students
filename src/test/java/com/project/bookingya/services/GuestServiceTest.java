package com.project.bookingya.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import com.project.bookingya.dtos.GuestDto;
import com.project.bookingya.entities.GuestEntity;
import com.project.bookingya.exceptions.EntityExistsException;
import com.project.bookingya.exceptions.EntityNotExistsException;
import com.project.bookingya.models.Guest;
import com.project.bookingya.repositories.IGuestRepository;
import com.project.bookingya.shared.Constants;

@ExtendWith(MockitoExtension.class)
@DisplayName("GuestService Unit Tests")
class GuestServiceTest {

    @Mock
    private IGuestRepository guestRepository;

    @Mock
    private ModelMapper mapper;

    @InjectMocks
    private GuestService guestService;

    private UUID guestId;
    private GuestEntity guestEntity;
    private GuestDto guestDto;
    private Guest guest;

    @BeforeEach
    void setUp() {
        guestId = UUID.randomUUID();

        guestEntity = new GuestEntity();
        guestEntity.setId(guestId);
        guestEntity.setIdentification("1234567890");
        guestEntity.setName("Juan Pérez");
        guestEntity.setEmail("juan@example.com");

        guestDto = new GuestDto();
        guestDto.setIdentification("1234567890");
        guestDto.setName("Juan Pérez");
        guestDto.setEmail("juan@example.com");

        guest = new Guest();
        guest.setId(guestId);
        guest.setIdentification("1234567890");
        guest.setName("Juan Pérez");
        guest.setEmail("juan@example.com");
    }

    @Nested
    @DisplayName("CREATE - Crear un huésped")
    class CreateGuestTests {

        @Test
        @DisplayName("Debe crear un huésped exitosamente con datos válidos")
        void shouldCreateGuestSuccessfully() {
            // Arrange
            when(guestRepository.existsByIdentification(guestDto.getIdentification()))
                .thenReturn(false);
            when(guestRepository.existsByEmail(guestDto.getEmail()))
                .thenReturn(false);
            when(mapper.map(guestDto, GuestEntity.class))
                .thenReturn(guestEntity);
            when(guestRepository.save(any(GuestEntity.class)))
                .thenReturn(guestEntity);
            when(mapper.map(guestEntity, Guest.class))
                .thenReturn(guest);

            // Act
            Guest result = guestService.create(guestDto);

            // Assert
            assertNotNull(result);
            assertEquals(guestId, result.getId());
            assertEquals("1234567890", result.getIdentification());
            assertEquals("juan@example.com", result.getEmail());
            verify(guestRepository, times(1)).save(any(GuestEntity.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el identification ya existe")
        void shouldThrowExceptionWhenIdentificationExists() {
            // Arrange
            when(guestRepository.existsByIdentification(guestDto.getIdentification()))
                .thenReturn(true);

            // Act & Assert
            assertThrows(EntityExistsException.class, () -> {
                guestService.create(guestDto);
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el email ya existe")
        void shouldThrowExceptionWhenEmailExists() {
            // Arrange
            when(guestRepository.existsByIdentification(guestDto.getIdentification()))
                .thenReturn(false);
            when(guestRepository.existsByEmail(guestDto.getEmail()))
                .thenReturn(true);

            // Act & Assert
            assertThrows(EntityExistsException.class, () -> {
                guestService.create(guestDto);
            });
        }
    }

    @Nested
    @DisplayName("READ - Consultar un huésped")
    class ReadGuestTests {

        @Test
        @DisplayName("Debe obtener un huésped por ID exitosamente")
        void shouldGetGuestByIdSuccessfully() {
            // Arrange
            when(guestRepository.findById(guestId))
                .thenReturn(Optional.of(guestEntity));
            when(mapper.map(guestEntity, Guest.class))
                .thenReturn(guest);

            // Act
            Guest result = guestService.getById(guestId);

            // Assert
            assertNotNull(result);
            assertEquals(guestId, result.getId());
            assertEquals("1234567890", result.getIdentification());
            verify(guestRepository, times(1)).findById(guestId);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el huésped no existe por ID")
        void shouldThrowExceptionWhenGuestNotFoundById() {
            // Arrange
            when(guestRepository.findById(guestId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                guestService.getById(guestId);
            });
        }

        @Test
        @DisplayName("Debe obtener un huésped por identification exitosamente")
        void shouldGetGuestByIdentificationSuccessfully() {
            // Arrange
            when(guestRepository.findByIdentification("1234567890"))
                .thenReturn(Optional.of(guestEntity));
            when(mapper.map(guestEntity, Guest.class))
                .thenReturn(guest);

            // Act
            Guest result = guestService.getByIdentification("1234567890");

            // Assert
            assertNotNull(result);
            assertEquals("1234567890", result.getIdentification());
            verify(guestRepository, times(1)).findByIdentification("1234567890");
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el huésped no existe por identification")
        void shouldThrowExceptionWhenGuestNotFoundByIdentification() {
            // Arrange
            when(guestRepository.findByIdentification("1234567890"))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                guestService.getByIdentification("1234567890");
            });
        }

        @Test
        @DisplayName("Debe obtener todos los huéspedes")
        void shouldGetAllGuestsSuccessfully() {
            // Arrange
            List<GuestEntity> entities = List.of(guestEntity);
            List<Guest> guests = List.of(guest);
            when(guestRepository.findAll()).thenReturn(entities);
            when(mapper.map(eq(entities), any(java.lang.reflect.Type.class)))
                .thenReturn(guests);

            // Act
            List<Guest> result = guestService.getAll();

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            assertEquals(1, result.size());
            verify(guestRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("UPDATE - Actualizar un huésped")
    class UpdateGuestTests {

        @Test
        @DisplayName("Debe actualizar un huésped exitosamente")
        void shouldUpdateGuestSuccessfully() {
            // Arrange
            GuestDto updateDto = new GuestDto();
            updateDto.setIdentification("1234567890"); // Mismo que el existente
            updateDto.setName("Juan Pérez Actualizado");
            updateDto.setEmail("juan@example.com"); // Mismo que el existente

            when(guestRepository.findById(guestId))
                .thenReturn(Optional.of(guestEntity));
            doNothing().when(mapper).map(updateDto, guestEntity);
            when(guestRepository.save(any(GuestEntity.class)))
                .thenReturn(guestEntity);
            when(mapper.map(guestEntity, Guest.class))
                .thenReturn(guest);

            // Act
            Guest result = guestService.update(updateDto, guestId);

            // Assert
            assertNotNull(result);
            verify(guestRepository, times(1)).findById(guestId);
            verify(guestRepository, times(1)).save(any(GuestEntity.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el huésped no existe en actualización")
        void shouldThrowExceptionWhenGuestNotFoundOnUpdate() {
            // Arrange
            when(guestRepository.findById(guestId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                guestService.update(guestDto, guestId);
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando se intenta actualizar con identification duplicada")
        void shouldThrowExceptionWhenUpdatingWithDuplicateIdentification() {
            // Arrange
            GuestDto updateDto = new GuestDto();
            updateDto.setIdentification("9876543210");
            updateDto.setName("Juan");
            updateDto.setEmail("juan@example.com");

            when(guestRepository.findById(guestId))
                .thenReturn(Optional.of(guestEntity));
            when(guestRepository.existsByIdentification(updateDto.getIdentification()))
                .thenReturn(true);

            // Act & Assert
            assertThrows(EntityExistsException.class, () -> {
                guestService.update(updateDto, guestId);
            });
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando se intenta actualizar con email duplicado")
        void shouldThrowExceptionWhenUpdatingWithDuplicateEmail() {
            // Arrange
            GuestDto updateDto = new GuestDto();
            updateDto.setIdentification("1234567890"); // Mismo que el existente (no fallará aquí)
            updateDto.setName("Juan");
            updateDto.setEmail("otro@example.com"); // Email diferente

            when(guestRepository.findById(guestId))
                .thenReturn(Optional.of(guestEntity));
            when(guestRepository.existsByEmail(updateDto.getEmail()))
                .thenReturn(true); // Email ya existe

            // Act & Assert
            assertThrows(EntityExistsException.class, () -> {
                guestService.update(updateDto, guestId);
            });
        }
    }

    @Nested
    @DisplayName("DELETE - Eliminar un huésped")
    class DeleteGuestTests {

        @Test
        @DisplayName("Debe eliminar un huésped exitosamente")
        void shouldDeleteGuestSuccessfully() {
            // Arrange
            when(guestRepository.findById(guestId))
                .thenReturn(Optional.of(guestEntity));
            doNothing().when(guestRepository).delete(guestEntity);

            // Act
            guestService.delete(guestId);

            // Assert
            verify(guestRepository, times(1)).findById(guestId);
            verify(guestRepository, times(1)).delete(guestEntity);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el huésped no existe en eliminación")
        void shouldThrowExceptionWhenGuestNotFoundOnDelete() {
            // Arrange
            when(guestRepository.findById(guestId))
                .thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(EntityNotExistsException.class, () -> {
                guestService.delete(guestId);
            });
        }
    }
}
