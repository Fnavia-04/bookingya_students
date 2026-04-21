@reservas
Feature: Gestión de Reservas - BookingYa
  Como usuario de BookingYa
  Quiero gestionar mis reservas de habitaciones
  Para organizar mis estadías de forma eficiente

  Background:
    Given que el sistema está disponible en "http://localhost:8080/api"

  # Escenarios de creacion de reservas
  @crear
  Scenario: Crear una reserva exitosa con datos validos
    Given que existe una habitacion disponible con codigo "HABIT-001" para 3 personas
    And que existe un huesped registrado con cedula "1234567890"
    When creo una reserva de entrada "2027-06-15" salida "2027-06-20" para 2 personas
    Then la reserva se crea correctamente con codigo 200
    And la reserva contiene el ID de habitacion y huesped

  @crear
  Scenario: No se permite reservar una habitacion con mas personas de las que cabe
    Given que existe una habitacion disponible con codigo "HABIT-002" para 1 personas
    And que existe un huesped registrado con cedula "0987654321"
    When intento reservar esa habitacion para 4 personas
    Then el sistema rechaza la reserva con error 400
    And el error indica que se excede la capacidad de la habitacion

  # Escenarios de consulta
  @consultar
  Scenario: Consultar una reserva que ya existe
    Given que existe una habitacion disponible con codigo "HABIT-003" para 2 personas
    And que existe un huesped registrado con cedula "1111111111"
    And que ya cree una reserva exitosa
    When consulto esa reserva por su identificador
    Then obtengo los datos correctos de la reserva
    And veo el ID de habitacion y huesped

  # Escenarios de cancelacion
  @eliminar
  Scenario: Cancelar una reserva que estaba activa
    Given que existe una habitacion disponible con codigo "HABIT-004" para 2 personas
    And que existe un huesped registrado con cedula "2222222222"
    And que tengo una reserva creada desde "2027-07-01" hasta "2027-07-05"
    When cancelo mi reserva
    Then la cancelacion es exitosa con codigo 200
    And cuando consulto esa reserva ya no existe en el sistema
