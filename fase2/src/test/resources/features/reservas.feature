@reservas
Feature: Gestión de Reservas - BookingYa - FASE 2 BDD
  Como usuario de BookingYa
  Quiero gestionar mis reservas de habitaciones
  Para organizar mis estadías de forma eficiente

  Contexto del sistema:
    - Disponibilidad: http://localhost:8080/api
    - Base de datos: H2 (en memoria para tests)
    - Escenarios: Exitosos y de error

  Background:
    Given que el sistema está disponible en "http://localhost:8080/api"

  # ========== ESCENARIO 1: CREAR RESERVA EXITOSAMENTE ==========
  @crear @bdd @fase2
  Scenario: Crear una reserva exitosa con datos válidos
    """
    Dado que existe una habitación disponible para hospedar
    Y existe un huésped registrado en el sistema
    Cuando se envía una solicitud de reserva con fechas válidas
    Entonces el sistema confirma la creación de la reserva
    Y se asigna un identificador único a la reserva
    """
    Given que existe una habitacion disponible con codigo "HABIT-001" para 3 personas
    And que existe un huesped registrado con cedula "1234567890"
    When creo una reserva de entrada "2027-06-15" salida "2027-06-20" para 2 personas
    Then la reserva se crea correctamente con codigo 200
    And la reserva contiene el ID de habitacion y huesped
    And la reserva contiene la fecha de checkIn "2027-06-15"
    And la reserva contiene la cantidad de 2 huespedes

  # ========== ESCENARIO 2: VALIDAR DISPONIBILIDAD DE HABITACIÓN ==========
  @disponibilidad @bdd @fase2
  Scenario: Verificar la disponibilidad de una habitación en fechas específicas
    """
    Dado que existe una habitación en el sistema
    Y se verifica su disponibilidad para un rango de fechas
    Cuando no hay reservas que se superpongan en esas fechas
    Entonces el sistema indica que la habitación está disponible
    """
    Given que existe una habitacion disponible con codigo "HABIT-DISP" para 4 personas
    And que existe un huesped registrado con cedula "9876543210"
    When verifico la disponibilidad de la habitacion entre "2027-08-01" y "2027-08-10"
    Then el sistema confirma que la habitacion esta disponible
    And la respuesta contiene el estado "true"

  # ========== ESCENARIO 3: RECHAZAR RESERVA CON FECHAS INVÁLIDAS ==========
  @validacion @bdd @fase2
  Scenario: Rechazar una reserva cuando las fechas son inválidas
    """
    Dado que existe una habitación disponible
    Y existe un huésped registrado
    Cuando se intenta crear una reserva con checkOut antes que checkIn
    Entonces el sistema rechaza la solicitud
    Y devuelve un código de error 400
    Y proporciona un mensaje indicando el error de validación
    """
    Given que existe una habitacion disponible con codigo "HABIT-003" para 2 personas
    And que existe un huesped registrado con cedula "1111111111"
    When intento crear una reserva con fechas invalidas "2027-06-20" checkIn y "2027-06-15" checkOut
    Then el sistema rechaza la reserva con error 400
    And el error indica "Rango de fechas inválido"

  # ========== ESCENARIO 4: RECHAZAR POR CAPACIDAD EXCEDIDA ==========
  @capacidad @bdd @fase2
  Scenario: Rechazar una reserva cuando se excede la capacidad de la habitación
    """
    Dado que existe una habitación con capacidad limitada
    Y existe un huésped registrado
    Cuando se intenta crear una reserva para más personas de las que cabe
    Entonces el sistema rechaza la solicitud
    Y devuelve error 400 por capacidad excedida
    """
    Given que existe una habitacion disponible con codigo "HABIT-PEQUENA" para 1 personas
    And que existe un huesped registrado con cedula "0987654321"
    When intento reservar esa habitacion para 4 personas
    Then el sistema rechaza la reserva con error 400
    And el error indica "capacidad" o "capacity"

  # ========== ESCENARIO 5: ELIMINAR RESERVA EXITOSAMENTE ==========
  @eliminar @bdd @fase2
  Scenario: Cancelar una reserva que estaba activa
    """
    Dado que existe una reserva activa en el sistema
    Cuando se solicita la cancelación
    Entonces se elimina la reserva exitosamente
    Y al consultar por su ID, ya no existe
    """
    Given que existe una habitacion disponible con codigo "HABIT-004" para 2 personas
    And que existe un huesped registrado con cedula "2222222222"
    And que tengo una reserva creada desde "2027-07-01" hasta "2027-07-05"
    When cancelo mi reserva
    Then la cancelacion es exitosa con codigo 200
    And cuando consulto esa reserva ya no existe en el sistema

  # ========== ESCENARIO 6: CONSULTAR RESERVA ==========
  @consultar @bdd @fase2
  Scenario: Consultar una reserva que ya existe
    """
    Dado que existe una reserva creada en el sistema
    Cuando se solicita consultar por su identificador
    Entonces se retornan todos los datos de la reserva
    Y se validan las fechas y participantes
    """
    Given que existe una habitacion disponible con codigo "HABIT-005" para 2 personas
    And que existe un huesped registrado con cedula "3333333333"
    And que ya cree una reserva exitosa
    When consulto esa reserva por su identificador
    Then obtengo los datos correctos de la reserva
    And veo el ID de habitacion y huesped
    And el checkIn de la reserva es "2027-06-15"

