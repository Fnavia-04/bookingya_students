# Pruebas Unitarias - Servicio de Reservas (TDD)

## Resumen Ejecutivo
Se han implementado **23 pruebas unitarias** siguiendo el enfoque **Test-Driven Development (TDD)** para el servicio de reservas de la plataforma Bookingya. Todas las pruebas han pasado exitosamente (100% de cobertura de los casos de uso).

## Estructura de Pruebas

### 1. **Pruebas de Creación de Reserva** (CreateReservationTests) - 9 pruebas
Validaciones implementadas:
- ✅ Creación exitosa con datos válidos
- ✅ Rango de fechas inválido (check-in >= check-out)
- ✅ Cantidad de huéspedes inválida (≤ 0)
- ✅ Habitación inexistente
- ✅ Huésped inexistente
- ✅ Habitación no disponible
- ✅ Exceso de capacidad de la habitación
- ✅ Solapamiento de reservas para habitación
- ✅ Solapamiento de reservas para huésped

### 2. **Pruebas de Consulta por ID** (GetReservationByIdTests) - 3 pruebas
Validaciones implementadas:
- ✅ Recuperación exitosa por ID
- ✅ Excepción cuando la reserva no existe
- ✅ Retorno de datos correctos en la reserva

### 3. **Pruebas de Actualización** (UpdateReservationTests) - 4 pruebas
Validaciones implementadas:
- ✅ Actualización exitosa con datos válidos
- ✅ Excepción cuando la reserva no existe
- ✅ Rango de fechas inválido en actualización
- ✅ Exclusión de reserva actual del verificador de solapamientos

### 4. **Pruebas de Eliminación** (DeleteReservationTests) - 3 pruebas
Validaciones implementadas:
- ✅ Eliminación exitosa
- ✅ Excepción cuando la reserva no existe
- ✅ Verificación de flush después de eliminación

### 5. **Pruebas de Consulta de Todas las Reservas** (GetAllReservationsTests) - 2 pruebas
Validaciones implementadas:
- ✅ Recuperación exitosa de todas las reservas
- ✅ Retorno de lista vacía cuando no hay reservas

### 6. **Pruebas de Consulta por ID de Huésped** (GetReservationsByGuestIdTests) - 1 prueba
- ✅ Recuperación exitosa de reservas por huésped

### 7. **Pruebas de Consulta por ID de Habitación** (GetReservationsByRoomIdTests) - 1 prueba
- ✅ Recuperación exitosa de reservas por habitación

## Herramientas y Marcos Utilizados

### Dependencias de Testing
- **JUnit 5**: Framework de pruebas (incluido en spring-boot-starter-test)
- **Mockito**: Para crear mocks y verificar comportamientos
- **Spring Test**: Para integración con Spring Boot
- **H2 Database**: Base de datos en memoria para pruebas

### Anotaciones Utilizadas
- `@ExtendWith(MockitoExtension.class)`: Integración con Mockito
- `@BeforeEach`: Inicialización antes de cada prueba
- `@DisplayName`: Nombres descriptivos para cada prueba
- `@Nested`: Agrupación lógica de pruebas relacionadas
- `@Test`: Marca métodos como casos de prueba

## Patrones de Testing Implementados

### 1. **AAA Pattern (Arrange-Act-Assert)**
```java
// Arrange: Preparar datos y mocks
ReservationDto reservationDto = createValidReservationDto();
when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

// Act: Ejecutar el método bajo prueba
Reservation result = reservationService.create(reservationDto);

// Assert: Verificar resultados
assertNotNull(result);
assertEquals(guestId, result.getGuestId());
```

### 2. **Mocking de Dependencias**
- Se mockean los repositorios (`IReservationRepository`, `IRoomRepository`, `IGuestRepository`)
- Se mockea `ModelMapper` para mapeos
- Se verifica el comportamiento correcto de cada dependencia

### 3. **Validación de Excepciones**
```java
assertThrows(BusinessRuleException.class, () -> {
    reservationService.create(invalidDto);
});
```

### 4. **Verificación de Métodos**
```java
verify(reservationRepository, times(1)).saveAndFlush(any(ReservationEntity.class));
verify(reservationRepository, never()).delete(any());
```

## Casos de Prueba Cubiertos

### Casos de Éxito (Happy Path)
- [x] Crear reserva con datos válidos
- [x] Obtener reserva por ID válido
- [x] Actualizar reserva con datos válidos
- [x] Eliminar reserva existente
- [x] Listar todas las reservas
- [x] Listar reservas por huésped
- [x] Listar reservas por habitación

### Casos de Error (Error Paths)
- [x] Fechas inválidas (check-in ≥ check-out)
- [x] Cantidad de huéspedes inválida
- [x] Recursos no encontrados (habitación, huésped, reserva)
- [x] Reglas de negocio (capacidad, disponibilidad, solapamientos)
- [x] Conflictos de reserva (múltiples validaciones)

## Métricas de Cobertura

| Clase | Métodos | Cubiertos | % |
|-------|---------|-----------|---|
| `create()` | 1 | 1 | 100% |
| `getById()` | 1 | 1 | 100% |
| `update()` | 1 | 1 | 100% |
| `delete()` | 1 | 1 | 100% |
| `getAll()` | 1 | 1 | 100% |
| `getByGuestId()` | 1 | 1 | 100% |
| `getByRoomId()` | 1 | 1 | 100% |
| **Total** | **7** | **7** | **100%** |

## Ejecución de Pruebas

### Comando para ejecutar todas las pruebas
```bash
mvn clean test -Dtest=ReservationServiceTest
```

### Comando para ejecutar un test específico
```bash
mvn clean test -Dtest=ReservationServiceTest#shouldCreateReservationSuccessfully
```

### Comando para ejecutar una clase anidada
```bash
mvn clean test -Dtest=ReservationServiceTest$CreateReservationTests
```

### Resultado de la ejecución
```
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running com.project.bookingya.services.ReservationServiceTest
[INFO] Tests run: 23, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

## Buenas Prácticas Implementadas

1. **Nombres Descriptivos**: Cada test tiene un nombre que describe claramente qué se prueba
2. **Pruebas Independientes**: Cada prueba es completamente independiente de las otras
3. **Datos Reutilizables**: Métodos helper para crear datos de prueba (`createValidReservationDto()`, etc.)
4. **Cobertura Completa**: Se prueban todos los caminos posibles (éxito y error)
5. **Organización**: Tests agrupados por funcionalidad usando `@Nested`
6. **Mocking Apropiado**: Solo se mockean las dependencias externas
7. **Verificación**: Se verifica no solo el resultado sino también que se llamen los métodos correctos

## Próximos Pasos (FASE 2 y 3)

Para completar la actividad conforme al cronograma:

### FASE 2: Pruebas de Integración (Integration Testing)
- [ ] Configurar TestContainers para bases de datos
- [ ] Crear tests de integración con la base de datos real
- [ ] Validar endpoints REST completos

### FASE 3: Pruebas de Aceptación (Acceptance Testing - BDD/ATDD)
- [ ] Implementar tests de aceptación con Cucumber/Gherkin
- [ ] Escribir scenarios en lenguaje Gherkin
- [ ] Crear step definitions para BDD

## Notas Adicionales

- Todas las pruebas están localizadas en: `src/test/java/com/project/bookingya/services/ReservationServiceTest.java`
- Las pruebas no requieren una base de datos real (usan mocks)
- La velocidad de ejecución es rápida (< 5 segundos para 23 pruebas)
- El proyecto está listo para CI/CD (Jenkins, GitLab CI, GitHub Actions, etc.)

---

**Autor**: Actividad 2 - Pruebas Unitarias con TDD
**Fecha**: 2024
**Versión**: 1.0
