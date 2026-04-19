# language: es
@reservas
Feature: Gestión de reservas en BookingYa
  Como usuario de la plataforma BookingYa
  Quiero poder crear, consultar, actualizar y cancelar reservas
  Para gestionar eficientemente mi alojamiento

  Background:
    Given que el sistema BookingYa está disponible en "http://localhost:8080/api"

  # ─── CREAR ──────────────────────────────────────────────────────────────────

  @crear @exitoso
  Scenario: Crear una reserva válida exitosamente
    Given que existe una habitación disponible con código "R-BDD-01" y capacidad para 3 huéspedes
    And que existe un huésped registrado con identificación "CC-BDD-01"
    When el usuario crea una reserva con checkIn "2027-06-01T14:00:00" y checkOut "2027-06-05T11:00:00" para 2 huéspedes
    Then la reserva es creada exitosamente con código de estado 200
    And la reserva retornada contiene el roomId y guestId correctos

  @crear @error
  Scenario: Crear una reserva con fechas inválidas falla con error 400
    Given que existe una habitación disponible con código "R-BDD-02" y capacidad para 2 huéspedes
    And que existe un huésped registrado con identificación "CC-BDD-02"
    When el usuario crea una reserva con checkIn "2027-06-10T14:00:00" y checkOut "2027-06-05T11:00:00" para 1 huésped
    Then el sistema responde con código de estado 400
    And el mensaje de error es "checkIn must be before checkOut"

  @crear @error
  Scenario: Crear una reserva que excede la capacidad máxima falla con error 400
    Given que existe una habitación disponible con código "R-BDD-03" y capacidad para 1 huésped
    And que existe un huésped registrado con identificación "CC-BDD-03"
    When el usuario crea una reserva con checkIn "2027-07-01T14:00:00" y checkOut "2027-07-03T11:00:00" para 5 huéspedes
    Then el sistema responde con código de estado 400
    And el mensaje de error es "guestsCount exceeds room capacity"

  @crear @error
  Scenario: Crear una reserva en habitación no disponible falla con error 400
    Given que existe una habitación NO disponible con código "R-BDD-04" y capacidad para 2 huéspedes
    And que existe un huésped registrado con identificación "CC-BDD-04"
    When el usuario crea una reserva con checkIn "2027-08-01T14:00:00" y checkOut "2027-08-05T11:00:00" para 1 huésped
    Then el sistema responde con código de estado 400
    And el mensaje de error es "Room is not available"

  @crear @error
  Scenario: Crear una reserva solapada en la misma habitación falla con error 400
    Given que existe una habitación disponible con código "R-BDD-05" y capacidad para 3 huéspedes
    And que existe un huésped registrado con identificación "CC-BDD-05"
    And que existe un huésped registrado con identificación "CC-BDD-05B"
    And ya existe una reserva en esa habitación del "2027-09-01T14:00:00" al "2027-09-07T11:00:00"
    When el usuario con identificación "CC-BDD-05B" intenta reservar la misma habitación del "2027-09-04T14:00:00" al "2027-09-10T11:00:00" para 1 huésped
    Then el sistema responde con código de estado 400
    And el mensaje de error es "The room already has a reservation in that time range"

  # ─── CONSULTAR ──────────────────────────────────────────────────────────────

  @consultar
  Scenario: Consultar todas las reservas retorna lista con código 200
    When el usuario consulta todas las reservas
    Then el sistema responde con código de estado 200
    And la respuesta es una lista JSON

  @consultar
  Scenario: Consultar una reserva por ID existente retorna sus datos
    Given que existe una habitación disponible con código "R-BDD-06" y capacidad para 2 huéspedes
    And que existe un huésped registrado con identificación "CC-BDD-06"
    And el usuario crea una reserva con checkIn "2027-10-01T14:00:00" y checkOut "2027-10-05T11:00:00" para 1 huésped
    When el usuario consulta la reserva por su ID
    Then la reserva retornada contiene el roomId y guestId correctos

  @consultar
  Scenario: Consultar una reserva con ID inexistente retorna 404
    When el usuario consulta la reserva con ID "00000000-0000-0000-0000-000000000000"
    Then el sistema responde con código de estado 404
    And el mensaje de error es "Reservation not found"

  @consultar
  Scenario: Consultar disponibilidad de una habitación sin reservas retorna disponible
    Given que existe una habitación disponible con código "R-BDD-07" y capacidad para 2 huéspedes
    When el usuario consulta la disponibilidad de esa habitación del "2028-01-01T14:00:00" al "2028-01-05T11:00:00"
    Then la disponibilidad retornada es true

  # ─── ACTUALIZAR ─────────────────────────────────────────────────────────────

  @actualizar
  Scenario: Actualizar una reserva existente con datos válidos retorna 200
    Given que existe una habitación disponible con código "R-BDD-08" y capacidad para 3 huéspedes
    And que existe un huésped registrado con identificación "CC-BDD-08"
    And el usuario crea una reserva con checkIn "2027-11-01T14:00:00" y checkOut "2027-11-05T11:00:00" para 1 huésped
    When el usuario actualiza la reserva con checkIn "2027-11-10T14:00:00" y checkOut "2027-11-15T11:00:00" para 2 huéspedes
    Then la reserva es actualizada exitosamente con código de estado 200

  # ─── ELIMINAR ───────────────────────────────────────────────────────────────

  @eliminar
  Scenario: Cancelar una reserva existente retorna 200
    Given que existe una habitación disponible con código "R-BDD-09" y capacidad para 2 huéspedes
    And que existe un huésped registrado con identificación "CC-BDD-09"
    And el usuario crea una reserva con checkIn "2027-12-01T14:00:00" y checkOut "2027-12-05T11:00:00" para 1 huésped
    When el usuario cancela la reserva
    Then la reserva es cancelada con código de estado 200
    And al consultar esa reserva el sistema responde con código de estado 404

  @eliminar @error
  Scenario: Cancelar una reserva inexistente retorna 404
    When el usuario cancela la reserva con ID "00000000-0000-0000-0000-000000000000"
    Then el sistema responde con código de estado 404
    And el mensaje de error es "Reservation not found"
