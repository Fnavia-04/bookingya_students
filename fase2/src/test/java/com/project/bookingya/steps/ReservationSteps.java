package com.project.bookingya.steps;

import io.cucumber.java.es.*;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * FASE 2 - BDD con Serenity + Cucumber
 * 
 * Pasos para validar el comportamiento de las reservas en BookingYa.
 * Los pasos usan SerenityRest que registra automaticamente en los reportes.
 */
public class ReservationSteps {

    private String baseUrl;
    private UUID roomId;
    private UUID guestId;
    private UUID reservationId;
    private Response response;

    // Background
    @Dado("que el sistema está disponible en {string}")
    public void sistemaDisponible(String url) {
        this.baseUrl = url;
        SerenityRest.setDefaultBasePath(url);
    }

    // Given: Preparacion de datos
    @Dado("que existe una habitacion disponible con codigo {string} para {int} personas")
    public void crearHabitacion(String code, int capacity) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("name", "Habitacion " + code);
        body.put("city", "Bogota");
        body.put("maxGuests", capacity);
        body.put("nightlyPrice", 120.0);
        body.put("available", true);

        response = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .when().post("/room");

        roomId = UUID.fromString(response.jsonPath().getString("id"));
    }

    @Dado("que existe un huesped registrado con cedula {string}")
    public void crearHuesped(String cedula) {
        Map<String, String> body = new HashMap<>();
        body.put("identification", cedula);
        body.put("name", "Usuario " + cedula);
        body.put("email", "usuario" + cedula + "@mail.com");

        response = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .when().post("/guest");

        guestId = UUID.fromString(response.jsonPath().getString("id"));
    }

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
                .when().post("/reservation");

        if (response.statusCode() == 200) {
            reservationId = UUID.fromString(response.jsonPath().getString("id"));
        }
    }

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
                .when().post("/reservation");

        if (response.statusCode() == 200) {
            reservationId = UUID.fromString(response.jsonPath().getString("id"));
        }
    }

    // When: Acciones
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
                .when().post("/reservation");

        if (response.statusCode() == 200) {
            reservationId = UUID.fromString(response.jsonPath().getString("id"));
        }
    }

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
                .when().post("/reservation");
    }

    @Cuando("consulto esa reserva por su identificador")
    public void consultarReserva() {
        response = SerenityRest.given()
                .when().get("/reservation/" + reservationId);
    }

    @Cuando("cancelo mi reserva")
    public void cancelarReserva() {
        response = SerenityRest.given()
                .when().delete("/reservation/" + reservationId);
    }

    // Then: Validaciones
    @Entonces("la reserva se crea correctamente con codigo {int}")
    public void validarCreacionExitosa(int statusCode) {
        assertThat(response.statusCode(), is(statusCode));
        assertThat(response.jsonPath().getString("id"), notNullValue());
    }

    @Entonces("la reserva contiene el ID de habitacion y huesped")
    public void validarReservaIds() {
        assertThat(response.jsonPath().getString("roomId"), is(roomId.toString()));
        assertThat(response.jsonPath().getString("guestId"), is(guestId.toString()));
    }

    @Entonces("el sistema rechaza la reserva con error {int}")
    public void validarErrorCreacion(int statusCode) {
        assertThat(response.statusCode(), is(statusCode));
    }

    @Entonces("el error indica que se excede la capacidad de la habitacion")
    public void validarMensajeCapacidad() {
        String error = response.jsonPath().getString("error");
        assertThat(error, containsString("capacity"));
    }

    @Entonces("obtengo los datos correctos de la reserva")
    public void validarConsultaExitosa() {
        assertThat(response.statusCode(), is(200));
        assertThat(response.jsonPath().getString("id"), notNullValue());
    }

    @Entonces("veo el ID de habitacion y huesped")
    public void validarConsultaIds() {
        assertThat(response.jsonPath().getString("roomId"), is(roomId.toString()));
        assertThat(response.jsonPath().getString("guestId"), is(guestId.toString()));
    }

    @Entonces("la cancelacion es exitosa con codigo {int}")
    public void validarCancelacionExitosa(int statusCode) {
        assertThat(response.statusCode(), is(statusCode));
    }

    @Entonces("cuando consulto esa reserva ya no existe en el sistema")
    public void validarReservaCancelada() {
        Response checkResponse = SerenityRest.given()
                .when().get("/reservation/" + reservationId);
        assertThat(checkResponse.statusCode(), is(404));
    }
}
