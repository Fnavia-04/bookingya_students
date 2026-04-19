package com.project.bookingya.steps;

import io.cucumber.java.en.*;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.hamcrest.Matchers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * FASE 2 — BDD
 * Step Definitions de Cucumber con Serenity REST (RestAssured integrado).
 *
 * Los steps usan SerenityRest para que cada llamada HTTP quede registrada
 * automáticamente en el reporte HTML de Serenity.
 *
 * Base URL configurada en el step "Background" del feature file.
 */
public class ReservationSteps {

    private String baseUrl;
    private UUID   roomId;
    private UUID   guestId;
    private UUID   lastReservationId;
    private Response lastResponse;

    // ─── Background ─────────────────────────────────────────────────────────

    @Given("que el sistema BookingYa está disponible en {string}")
    public void sistemaDisponible(String url) {
        this.baseUrl = url;
        SerenityRest.setDefaultBasePath(url);
    }

    // ─── Given: Setup de datos ───────────────────────────────────────────────

    @Given("que existe una habitación disponible con código {string} y capacidad para {int} huéspedes")
    public void crearHabitacionDisponible(String code, int maxGuests) {
        Map<String, Object> body = roomBody(code, maxGuests, true);
        Response res = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .when().post("/room");
        roomId = UUID.fromString(res.jsonPath().getString("id"));
    }

    @Given("que existe una habitación NO disponible con código {string} y capacidad para {int} huéspedes")
    public void crearHabitacionNoDisponible(String code, int maxGuests) {
        Map<String, Object> body = roomBody(code, maxGuests, false);
        Response res = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .when().post("/room");
        roomId = UUID.fromString(res.jsonPath().getString("id"));
    }

    @Given("que existe un huésped registrado con identificación {string}")
    public void crearHuesped(String identification) {
        Map<String, String> body = new HashMap<>();
        body.put("identification", identification);
        body.put("name", "Huésped " + identification);
        body.put("email", identification.toLowerCase().replace("-", "") + "@mail.com");

        Response res = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .when().post("/guest");
        guestId = UUID.fromString(res.jsonPath().getString("id"));
    }

    @Given("ya existe una reserva en esa habitación del {string} al {string}")
    public void crearReservaPrevia(String checkIn, String checkOut) {
        Map<String, Object> body = reservationBody(roomId, guestId, checkIn, checkOut, 1);
        SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .when().post("/reservation")
                .then().statusCode(200);
    }

    // ─── When: Acciones ──────────────────────────────────────────────────────

    @When("el usuario crea una reserva con checkIn {string} y checkOut {string} para {int} huésped")
    public void crearReserva(String checkIn, String checkOut, int guestsCount) {
        crearReservaConN(checkIn, checkOut, guestsCount);
    }

    @When("el usuario crea una reserva con checkIn {string} y checkOut {string} para {int} huéspedes")
    public void crearReservaPlural(String checkIn, String checkOut, int guestsCount) {
        crearReservaConN(checkIn, checkOut, guestsCount);
    }

    private void crearReservaConN(String checkIn, String checkOut, int guestsCount) {
        Map<String, Object> body = reservationBody(roomId, guestId, checkIn, checkOut, guestsCount);
        lastResponse = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .when().post("/reservation");

        if (lastResponse.statusCode() == 200) {
            lastReservationId = UUID.fromString(lastResponse.jsonPath().getString("id"));
        }
    }

    @When("el usuario con identificación {string} intenta reservar la misma habitación del {string} al {string} para {int} huésped")
    public void crearReservaSolapada(String identification, String checkIn, String checkOut, int guestsCount) {
        // Buscar el guestId por identificación
        Response guestRes = SerenityRest.given()
                .when().get("/guest/identification/" + identification);
        UUID secondGuestId = UUID.fromString(guestRes.jsonPath().getString("id"));

        Map<String, Object> body = reservationBody(roomId, secondGuestId, checkIn, checkOut, guestsCount);
        lastResponse = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .when().post("/reservation");
    }

    @When("el usuario consulta todas las reservas")
    public void consultarTodas() {
        lastResponse = SerenityRest.given().when().get("/reservation");
    }

    @When("el usuario consulta la reserva por su ID")
    public void consultarPorId() {
        lastResponse = SerenityRest.given().when().get("/reservation/" + lastReservationId);
    }

    @When("el usuario consulta la reserva con ID {string}")
    public void consultarPorIdEspecifico(String id) {
        lastResponse = SerenityRest.given().when().get("/reservation/" + id);
    }

    @When("el usuario consulta la disponibilidad de esa habitación del {string} al {string}")
    public void consultarDisponibilidad(String checkIn, String checkOut) {
        lastResponse = SerenityRest.given()
                .queryParam("checkIn", checkIn)
                .queryParam("checkOut", checkOut)
                .when().get("/reservation/availability/room/" + roomId);
    }

    @When("el usuario actualiza la reserva con checkIn {string} y checkOut {string} para {int} huéspedes")
    public void actualizarReserva(String checkIn, String checkOut, int guestsCount) {
        Map<String, Object> body = reservationBody(roomId, guestId, checkIn, checkOut, guestsCount);
        lastResponse = SerenityRest.given()
                .contentType("application/json")
                .body(body)
                .when().put("/reservation/" + lastReservationId);
    }

    @When("el usuario cancela la reserva")
    public void cancelarReserva() {
        lastResponse = SerenityRest.given().when().delete("/reservation/" + lastReservationId);
    }

    @When("el usuario cancela la reserva con ID {string}")
    public void cancelarReservaPorId(String id) {
        lastResponse = SerenityRest.given().when().delete("/reservation/" + id);
    }

    // ─── Then: Aserciones ────────────────────────────────────────────────────

    @Then("la reserva es creada exitosamente con código de estado {int}")
    public void reservaCreadaExitosamente(int statusCode) {
        assertThat(lastResponse.statusCode(), is(statusCode));
        assertThat(lastResponse.jsonPath().getString("id"), notNullValue());
    }

    @Then("la reserva retornada contiene el roomId y guestId correctos")
    public void reservaContieneIds() {
        assertThat(lastResponse.jsonPath().getString("roomId"), is(roomId.toString()));
        assertThat(lastResponse.jsonPath().getString("guestId"), is(guestId.toString()));
    }

    @Then("el sistema responde con código de estado {int}")
    public void verificarStatusCode(int statusCode) {
        assertThat(lastResponse.statusCode(), is(statusCode));
    }

    @Then("el mensaje de error es {string}")
    public void verificarMensajeError(String mensaje) {
        assertThat(lastResponse.jsonPath().getString("error"), is(mensaje));
    }

    @Then("la respuesta es una lista JSON")
    public void respuestaEsLista() {
        assertThat(lastResponse.jsonPath().getList("$"), notNullValue());
    }

    @Then("la disponibilidad retornada es true")
    public void disponibleTrue() {
        assertThat(lastResponse.statusCode(), is(200));
        assertThat(lastResponse.jsonPath().getBoolean("available"), is(true));
    }

    @Then("la reserva es actualizada exitosamente con código de estado {int}")
    public void reservaActualizadaExitosamente(int statusCode) {
        assertThat(lastResponse.statusCode(), is(statusCode));
    }

    @Then("la reserva es cancelada con código de estado {int}")
    public void reservaCancelada(int statusCode) {
        assertThat(lastResponse.statusCode(), is(statusCode));
    }

    @Then("al consultar esa reserva el sistema responde con código de estado {int}")
    public void consultarReservaCancelada(int statusCode) {
        Response res = SerenityRest.given().when().get("/reservation/" + lastReservationId);
        assertThat(res.statusCode(), is(statusCode));
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Map<String, Object> roomBody(String code, int maxGuests, boolean available) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", code);
        body.put("name", "Habitación " + code);
        body.put("city", "Bogotá");
        body.put("maxGuests", maxGuests);
        body.put("nightlyPrice", 150.00);
        body.put("available", available);
        return body;
    }

    private Map<String, Object> reservationBody(UUID roomId, UUID guestId,
                                                  String checkIn, String checkOut, int guestsCount) {
        Map<String, Object> body = new HashMap<>();
        body.put("roomId", roomId.toString());
        body.put("guestId", guestId.toString());
        body.put("checkIn", checkIn);
        body.put("checkOut", checkOut);
        body.put("guestsCount", guestsCount);
        return body;
    }
}
