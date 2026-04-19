# Guía de Pruebas de Integración - Próximas Fases (FASE 2 y 3)

## FASE 2: Pruebas de Integración (Integration Testing)

### Objetivo
Validar que los componentes del sistema funcionen correctamente cuando se integran con:
- Base de datos real
- Controladores REST
- Servicios completos

### Estructura Recomendada

#### 1. Pruebas de Repositorio (Layer 1)
```java
@DataJpaTest
@ExtendWith(SpringExtension.class)
class ReservationRepositoryTest {
    
    @Autowired
    private IReservationRepository reservationRepository;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    void shouldFindReservationsByRoomId() {
        // Arrange
        RoomEntity room = new RoomEntity();
        room.setCode("ROOM001");
        room.setName("Suite Premium");
        room.setCity("Bogotá");
        room.setMaxGuests(4);
        room.setAvailable(true);
        entityManager.persistAndFlush(room);
        
        ReservationEntity reservation = new ReservationEntity();
        reservation.setRoomId(room.getId());
        reservation.setGuestId(UUID.randomUUID());
        reservation.setCheckIn(LocalDateTime.now().plusDays(1));
        reservation.setCheckOut(LocalDateTime.now().plusDays(3));
        reservation.setGuestsCount(2);
        entityManager.persistAndFlush(reservation);
        
        // Act
        List<ReservationEntity> result = reservationRepository.findByRoomId(room.getId());
        
        // Assert
        assertEquals(1, result.size());
        assertEquals(room.getId(), result.get(0).getRoomId());
    }
}
```

#### 2. Pruebas de Controlador (Layer 2)
```java
@WebMvcTest(ReservationController.class)
class ReservationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private ReservationService reservationService;
    
    @Test
    void shouldCreateReservationViaPost() throws Exception {
        // Arrange
        ReservationDto reservationDto = new ReservationDto();
        reservationDto.setGuestId(UUID.randomUUID());
        reservationDto.setRoomId(UUID.randomUUID());
        reservationDto.setCheckIn(LocalDateTime.now().plusDays(1));
        reservationDto.setCheckOut(LocalDateTime.now().plusDays(3));
        reservationDto.setGuestsCount(2);
        
        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID());
        
        when(reservationService.create(any(ReservationDto.class)))
            .thenReturn(reservation);
        
        // Act & Assert
        mockMvc.perform(post("/api/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(reservationDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists());
    }
}
```

#### 3. Pruebas de Integración Completa (Layer 3)
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = Replace.ANY)
class ReservationIntegrationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private String baseUrl;
    
    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port;
    }
    
    @Test
    void shouldCreateAndRetrieveReservation() {
        // Arrange
        ReservationDto reservationDto = new ReservationDto();
        // ... configurar datos
        
        // Act - Create
        ResponseEntity<Reservation> createResponse = 
            restTemplate.postForEntity(
                baseUrl + "/api/reservations",
                reservationDto,
                Reservation.class);
        
        // Assert - Created
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode());
        Reservation created = createResponse.getBody();
        
        // Act - Retrieve
        ResponseEntity<Reservation> getResponse = 
            restTemplate.getForEntity(
                baseUrl + "/api/reservations/" + created.getId(),
                Reservation.class);
        
        // Assert - Retrieved
        assertEquals(HttpStatus.OK, getResponse.getStatusCode());
        assertEquals(created.getId(), getResponse.getBody().getId());
    }
}
```

## FASE 3: Pruebas de Aceptación (BDD/ATDD)

### Definición de Escenarios en Gherkin

Archivo: `src/test/resources/features/reservations.feature`

```gherkin
# language: es
Característica: Gestión de Reservas
  Como cliente de Bookingya
  Quiero gestionar mis reservas
  Para organizar mis hospedajes

  Escenario: Crear una reserva válida
    Dado que exista una habitación disponible
    Y que exista un huésped registrado
    Cuando creo una reserva con datos válidos
    Entonces la reserva debe ser creada exitosamente
    Y debo recibir el ID de la reserva

  Escenario: Obtener una reserva por ID
    Dado que existe una reserva creada
    Cuando solicito los detalles de la reserva
    Entonces debo recibir la información de la reserva
    Y los datos deben coincidir con los almacenados

  Escenario: Actualizar una reserva
    Dado que existe una reserva
    Cuando actualizo las fechas de la reserva
    Entonces la reserva debe ser actualizada
    Y las nuevas fechas deben reflejarse en el sistema

  Escenario: Cancelar una reserva
    Dado que existe una reserva confirmada
    Cuando cancelo la reserva
    Entonces la reserva debe ser eliminada
    Y la habitación debe quedar disponible nuevamente

  Escenario: Validar solapamiento de reservas
    Dado que existe una reserva en la habitación para las fechas 1-5 de junio
    Cuando intento crear una reserva para las fechas 3-8 de junio
    Entonces la creación debe fallar
    Y debo recibir un error de solapamiento

  Escenario: Validar capacidad de la habitación
    Dado que una habitación tiene capacidad máxima de 2 personas
    Cuando intento crear una reserva para 5 personas
    Entonces la creación debe fallar
    Y debo recibir un error de capacidad excedida
```

### Implementación de Steps (Cucumber)

```java
@CucumberContextConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ReservationStepDefinitions {
    
    @LocalServerPort
    private int port;
    
    private RestTemplate restTemplate = new RestTemplate();
    private String baseUrl;
    
    private ReservationDto currentReservationDto;
    private ResponseEntity<?> lastResponse;
    private Exception lastException;
    private ReservationEntity createdReservation;
    
    @Before
    public void setUp() {
        baseUrl = "http://localhost:" + port;
    }
    
    // ===== Dado (Given) =====
    
    @Given("que exista una habitación disponible")
    public void roomExists() {
        // Crear habitación en BD
        RoomEntity room = new RoomEntity();
        room.setCode("ROOM001");
        room.setName("Suite");
        room.setCity("Bogotá");
        room.setMaxGuests(4);
        room.setAvailable(true);
        // persistir...
    }
    
    @Given("que exista un huésped registrado")
    public void guestExists() {
        // Crear huésped en BD
        GuestEntity guest = new GuestEntity();
        guest.setIdentification("12345678");
        guest.setName("Juan Pérez");
        guest.setEmail("juan@example.com");
        // persistir...
    }
    
    // ===== Cuando (When) =====
    
    @When("creo una reserva con datos válidos")
    public void createReservation() {
        currentReservationDto = new ReservationDto();
        currentReservationDto.setCheckIn(LocalDateTime.now().plusDays(1));
        currentReservationDto.setCheckOut(LocalDateTime.now().plusDays(3));
        currentReservationDto.setGuestsCount(2);
        
        try {
            lastResponse = restTemplate.postForEntity(
                baseUrl + "/api/reservations",
                currentReservationDto,
                Reservation.class);
        } catch (Exception e) {
            lastException = e;
        }
    }
    
    // ===== Entonces (Then) =====
    
    @Then("la reserva debe ser creada exitosamente")
    public void reservationCreated() {
        assertEquals(HttpStatus.CREATED, lastResponse.getStatusCode());
    }
    
    @And("debo recibir el ID de la reserva")
    public void receivedReservationId() {
        Reservation created = (Reservation) lastResponse.getBody();
        assertNotNull(created.getId());
    }
}
```

## Dependencias para FASE 2 y 3

### Agregar al pom.xml

```xml
<!-- Para Pruebas de Integración -->
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.17.6</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.17.6</version>
    <scope>test</scope>
</dependency>

<!-- Para BDD/ATDD con Cucumber -->
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>7.12.0</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-spring</artifactId>
    <version>7.12.0</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-junit-platform-engine</artifactId>
    <version>7.12.0</version>
    <scope>test</scope>
</dependency>
```

## Matriz de Pruebas (Test Matrix)

| Tipo de Prueba | Nivel | Framework | Velocidad | Cobertura |
|---|---|---|---|---|
| Unitaria | Servicio | JUnit + Mockito | Muy Rápida | Lógica |
| Integración | Repositorio | DataJpaTest | Rápida | DB Access |
| Integración | Controlador | WebMvcTest | Rápida | HTTP |
| E2E | Aplicación | @SpringBootTest | Normal | Flujo Completo |
| BDD/ATDD | Negocio | Cucumber | Normal | Requisitos |

## Estrategia de Testing Recomendada

```
       Pruebas
        Unitarias
        (23 tests)
            ↓
      Pruebas de
      Integración
        (10-15 tests)
            ↓
      Pruebas E2E
      (5-8 tests)
            ↓
      Pruebas BDD
      (6-8 scenarios)
```

## Comandos de Ejecución

```bash
# Ejecutar solo pruebas unitarias
mvn test -Dtest=ReservationServiceTest

# Ejecutar solo pruebas de integración
mvn test -Dtest=*IntegrationTest

# Ejecutar pruebas Cucumber
mvn test -Dtest=RunCucumberTests

# Ejecutar todas las pruebas
mvn test

# Ejecutar con reporte de cobertura
mvn test jacoco:report
```

## Consideraciones de Desempeño

- Las pruebas unitarias deben ejecutarse en < 100ms cada una
- Las pruebas de integración pueden tomar 500ms - 1s
- Las pruebas E2E pueden tomar 2-5 segundos
- El tiempo total de suite de pruebas no debe exceder 5 minutos

## Checklist para Completar la Actividad

### FASE 1 - COMPLETADA ✅
- [x] Crear pruebas unitarias para crear reserva
- [x] Crear pruebas unitarias para consultar reserva
- [x] Crear pruebas unitarias para actualizar reserva
- [x] Crear pruebas unitarias para eliminar reserva
- [x] Crear pruebas unitarias para obtener reserva por ID
- [x] Todas las pruebas pasando

### FASE 2 - POR HACER
- [ ] Crear pruebas de integración de repositorio
- [ ] Crear pruebas de integración de controlador
- [ ] Configurar TestContainers para PostgreSQL
- [ ] Crear pruebas E2E
- [ ] Validar cobertura > 80%

### FASE 3 - POR HACER
- [ ] Crear archivo .feature con escenarios Gherkin
- [ ] Implementar step definitions con Cucumber
- [ ] Ejecutar pruebas BDD
- [ ] Documentar resultados de BDD

---

**Siguiente Paso**: Consultar con tu instructor sobre cuándo proceder con FASE 2 y 3.
