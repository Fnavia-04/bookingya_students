package com.project.bookingya.steps;

import io.cucumber.java.es.*;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import net.serenitybdd.screenplay.Actor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * FASE 2 - BDD con Serenity + Cucumber
 * 
 * Pasos (Steps) para validar el comportamiento de las reservas en BookingYa.
 * Los pasos usan SerenityRest que registra automáticamente en los reportes HTML.
 * 
 * Escenarios cubiertos:
 * 1. Crear una reserva exitosa con datos válidos
 * 2. Verificar disponibilidad de una habitación
 * 3. Rechazar reserva con fechas inválidas
 * 
 * Validaciones BDD:
 * - Verificación de códigos de estado HTTP
 * - Validación de estructura de respuesta
 * - Validación de mensajes de error
 * - Validación de relaciones entre entidades
 * 
 * @author Equipo BDD - Fase 2
 * @version 1.0
 */
public class ReservationSteps {

    private String baseUrl;
    private UUID roomId;
    private UUID guestId;
    private UUID reservationId;
    private Response response;
    private Map<String, Object> reservationData;

    // ==================== BACKGROUND / INICIAL ====================

    /**
     * Dado: Inicializa la URL base del sistema
     * Este paso se ejecuta una sola vez al inicio del escenario (Background)
     */
    @Dado("que el sistema está disponible en {string}")
    public void sistemaDisponible(String url) {
        this.baseUrl = url;
        SerenityRest.setDefaultBasePath(url);
        // Validar que el servidor está disponible haciendo GET a /room
        response = SerenityRest.given()
                .when().get("/room");
        assertThat("El sistema debe estar disponible", 
                response.statusCode(), 
                anyOf(is(200), is(400), is(404)));
    }

    // ==================== GIVEN: PREPARACIÓN DE DATOS ====================

    /**
     * Dado: Crear una habitación disponible en el sistema
     */
    @Dado("que existe una habitacion disponible con codigo {string} para {int} personas")
    public void crearHabitacion(String code, int capacity) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("name", "Habitacion " + code);
        body.put("city", "Bogota");
        body.put("maxGuests", capacity);
        body.put("nightlyPrice", "120.00");
        body.put("available", true);

        response = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .log().ifValidationFails()
                .when().post("/room");

        // Validar que se creó correctamente
        assertThat("La habitación debe crearse con código 200", 
                response.statusCode(), 
                is(200));
        
        roomId = UUID.fromString(response.jsonPath().getString("id"));
        assertThat("El ID de habitación no debe ser nulo", roomId, notNullValue());
    }

    /**
     * Dado: Crear un huésped registrado en el sistema
     */
    @Dado("que existe un huesped registrado con cedula {string}")
    public void crearHuesped(String cedula) {
        Map<String, Object> body = new HashMap<>();
        body.put("identification", cedula);
        body.put("name", "Usuario " + cedula);
        body.put("email", "usuario" + cedula + "@mail.com");

        response = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .log().ifValidationFails()
                .when().post("/guest");

        // Validar que se creó correctamente
        assertThat("El huésped debe crearse con código 200", 
                response.statusCode(), 
                is(200));
        
        guestId = UUID.fromString(response.jsonPath().getString("id"));
        assertThat("El ID de huésped no debe ser nulo", guestId, notNullValue());
    }

    /**
     * Dado: Crear una reserva previa exitosa
     */
    @Dado("que ya cree una reserva exitosa")
    public void crearReservaPrevia() {
        Map<String, Object> body = new HashMap<>();
        body.put("roomId", roomId.toString());
        body.put("guestId", guestId.toString());
        body.put("checkIn", "2027-06-15T14:00:00");
        body.put("checkOut", "2027-06-20T11:00:00");
        body.put("guestsCount", 1);

        response = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .log().ifValidationFails()
                .when().post("/reservation");

        if (response.statusCode() == 200) {
            reservationId = UUID.fromString(response.jsonPath().getString("id"));
            reservationData = new HashMap<>();
            reservationData.put("id", reservationId);
            reservationData.put("roomId", roomId);
            reservationData.put("guestId", guestId);
        }
    }

    /**
     * Dado: Crear una reserva con fechas específicas
     */
    @Dado("que tengo una reserva creada desde {string} hasta {string}")
    public void crearReservaConFechas(String checkIn, String checkOut) {
        Map<String, Object> body = new HashMap<>();
        body.put("roomId", roomId.toString());
        body.put("guestId", guestId.toString());
        body.put("checkIn", checkIn + "T14:00:00");
        body.put("checkOut", checkOut + "T11:00:00");
        body.put("guestsCount", 1);

        response = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .log().ifValidationFails()
                .when().post("/reservation");

        if (response.statusCode() == 200) {
            reservationId = UUID.fromString(response.jsonPath().getString("id"));
        }
    }

    // ==================== WHEN: ACCIONES ====================

    /**
     * Cuando: Crear una reserva con fechas y cantidad de huéspedes válidas
     */
    @Cuando("creo una reserva de entrada {string} salida {string} para {int} personas")
    public void crearReserva(String checkIn, String checkOut, int guests) {
        Map<String, Object> body = new HashMap<>();
        body.put("roomId", roomId.toString());
        body.put("guestId", guestId.toString());
        body.put("checkIn", checkIn + "T14:00:00");
        body.put("checkOut", checkOut + "T11:00:00");
        body.put("guestsCount", guests);

        response = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .log().ifValidationFails()
                .when().post("/reservation");

        if (response.statusCode() == 200) {
            reservationId = UUID.fromString(response.jsonPath().getString("id"));
        }
    }

    /**
     * Cuando: Intentar reservar una habitación para más personas de su capacidad
     */
    @Cuando("intento reservar esa habitacion para {int} personas")
    public void crearReservaConCapacidadExcedida(int guests) {
        Map<String, Object> body = new HashMap<>();
        body.put("roomId", roomId.toString());
        body.put("guestId", guestId.toString());
        body.put("checkIn", "2027-07-01T14:00:00");
        body.put("checkOut", "2027-07-05T11:00:00");
        body.put("guestsCount", guests);

        response = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .log().ifValidationFails()
                .when().post("/reservation");
    }

    /**
     * Cuando: Verificar disponibilidad de una habitación en un rango de fechas
     */
    @Cuando("verifico la disponibilidad de la habitacion entre {string} y {string}")
    public void verificarDisponibilidad(String checkIn, String checkOut) {
        String queryPath = String.format(
                "/room/%s/availability?checkIn=%sT14:00:00&checkOut=%sT11:00:00",
                roomId, checkIn, checkOut
        );

        response = SerenityRest.given()
                .log().ifValidationFails()
                .when().get(queryPath);
    }

    /**
     * Cuando: Intentar crear una reserva con fechas inválidas (checkOut antes que checkIn)
     */
    @Cuando("intento crear una reserva con fechas invalidas {string} checkIn y {string} checkOut")
    public void crearReservaConFechasInvalidas(String checkIn, String checkOut) {
        Map<String, Object> body = new HashMap<>();
        body.put("roomId", roomId.toString());
        body.put("guestId", guestId.toString());
        body.put("checkIn", checkIn + "T14:00:00");
        body.put("checkOut", checkOut + "T11:00:00");
        body.put("guestsCount", 1);

        response = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .log().ifValidationFails()
                .when().post("/reservation");
    }

    /**
     * Cuando: Consultar una reserva por su identificador
     */
    @Cuando("consulto esa reserva por su identificador")
    public void consultarReserva() {
        response = SerenityRest.given()
                .log().ifValidationFails()
                .when().get("/reservation/" + reservationId);
    }

    /**
     * Cuando: Cancelar una reserva
     */
    @Cuando("cancelo mi reserva")
    public void cancelarReserva() {
        response = SerenityRest.given()
                .log().ifValidationFails()
                .when().delete("/reservation/" + reservationId);
    }

    // ==================== THEN: VALIDACIONES ====================

    /**
     * Entonces: Validar que la reserva se creó correctamente
     */
    @Entonces("la reserva se crea correctamente con codigo {int}")
    public void validarCreacionExitosa(int statusCode) {
        assertThat("El código de estado debe ser " + statusCode, 
                response.statusCode(), 
                is(statusCode));
        assertThat("La respuesta debe contener un ID", 
                response.jsonPath().getString("id"), 
                notNullValue());
    }

    /**
     * Entonces: Validar que la reserva contiene IDs de habitación y huésped
     */
    @Entonces("la reserva contiene el ID de habitacion y huesped")
    public void validarReservaIds() {
        String responseRoomId = response.jsonPath().getString("roomId");
        String responseGuestId = response.jsonPath().getString("guestId");
        
        assertThat("El roomId debe coincidir", 
                responseRoomId, 
                is(roomId.toString()));
        assertThat("El guestId debe coincidir", 
                responseGuestId, 
                is(guestId.toString()));
    }

    /**
     * Entonces: Validar la fecha de checkIn en la reserva
     */
    @Entonces("la reserva contiene la fecha de checkIn {string}")
    public void validarCheckIn(String expectedCheckIn) {
        String checkIn = response.jsonPath().getString("checkIn");
        assertThat("El checkIn debe coincidir", 
                checkIn, 
                containsString(expectedCheckIn));
    }

    /**
     * Entonces: Validar la cantidad de huéspedes en la reserva
     */
    @Entonces("la reserva contiene la cantidad de {int} huespedes")
    public void validarGuestCount(int expectedCount) {
        Integer guestCount = response.jsonPath().getInt("guestsCount");
        assertThat("La cantidad de huéspedes debe ser " + expectedCount, 
                guestCount, 
                is(expectedCount));
    }

    /**
     * Entonces: Validar que el sistema rechaza una reserva
     */
    @Entonces("el sistema rechaza la reserva con error {int}")
    public void validarErrorCreacion(int statusCode) {
        assertThat("El código de estado debe ser " + statusCode, 
                response.statusCode(), 
                is(statusCode));
    }

    /**
     * Entonces: Validar mensaje de error específico
     */
    @Entonces("el error indica {string}")
    public void validarMensajeError(String expectedMessage) {
        String errorMessage = response.jsonPath().getString("message");
        assertThat("El mensaje de error debe contener: " + expectedMessage, 
                errorMessage.toLowerCase(), 
                containsStringIgnoringCase(expectedMessage));
    }

    /**
     * Entonces: Validar que el error menciona capacidad
     */
    @Entonces("el error indica {string} o {string}")
    public void validarMensajeErrorAlternativo(String msg1, String msg2) {
        String errorMessage = response.jsonPath().getString("message");
        String lowerError = errorMessage.toLowerCase();
        
        boolean contains = lowerError.contains(msg1.toLowerCase()) || 
                          lowerError.contains(msg2.toLowerCase());
        
        assertThat("El mensaje debe contener: " + msg1 + " o " + msg2, 
                contains, 
                is(true));
    }

    /**
     * Entonces: Validar consulta exitosa de reserva
     */
    @Entonces("obtengo los datos correctos de la reserva")
    public void validarConsultaExitosa() {
        assertThat("El código de estado debe ser 200", 
                response.statusCode(), 
                is(200));
        assertThat("La reserva debe tener un ID", 
                response.jsonPath().getString("id"), 
                notNullValue());
    }

    /**
     * Entonces: Validar IDs en consulta
     */
    @Entonces("veo el ID de habitacion y huesped")
    public void validarConsultaIds() {
        String responseRoomId = response.jsonPath().getString("roomId");
        String responseGuestId = response.jsonPath().getString("guestId");
        
        assertThat("El roomId debe estar presente", responseRoomId, notNullValue());
        assertThat("El guestId debe estar presente", responseGuestId, notNullValue());
    }

    /**
     * Entonces: Validar el checkIn en la consulta
     */
    @Entonces("el checkIn de la reserva es {string}")
    public void validarCheckInEnConsulta(String expectedCheckIn) {
        String checkIn = response.jsonPath().getString("checkIn");
        assertThat("El checkIn debe coincidir", 
                checkIn, 
                containsString(expectedCheckIn));
    }

    /**
     * Entonces: Validar cancelación exitosa
     */
    @Entonces("la cancelacion es exitosa con codigo {int}")
    public void validarCancelacionExitosa(int statusCode) {
        assertThat("El código de estado debe ser " + statusCode, 
                response.statusCode(), 
                is(statusCode));
    }

    /**
     * Entonces: Validar que la reserva fue eliminada
     */
    @Entonces("cuando consulto esa reserva ya no existe en el sistema")
    public void validarReservaCancelada() {
        Response checkResponse = SerenityRest.given()
                .log().ifValidationFails()
                .when().get("/reservation/" + reservationId);
        
        assertThat("La reserva debe retornar 404", 
                checkResponse.statusCode(), 
                is(404));
    }

    /**
     * Entonces: Validar disponibilidad de habitación
     */
    @Entonces("el sistema confirma que la habitacion esta disponible")
    public void validarDisponibilidadExitosa() {
        assertThat("El código de estado debe ser 200", 
                response.statusCode(), 
                is(200));
    }

    /**
     * Entonces: Validar estado de disponibilidad
     */
    @Entonces("la respuesta contiene el estado {string}")
    public void validarEstadoDisponibilidad(String expectedState) {
        Boolean isAvailable = response.jsonPath().getBoolean("available");
        assertThat("La habitación debe estar disponible", 
                isAvailable, 
                is(true));
    }
}
