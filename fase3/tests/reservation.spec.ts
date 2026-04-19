import { test, expect } from '@playwright/test';
import {
  createRoom, createGuest, createReservation, futureDate,
  type Reservation
} from './helpers';

/**
 * FASE 3 — ATDD
 * Pruebas de aceptación con Playwright + TypeScript.
 *
 * Criterios de aceptación del cliente/usuario final:
 *  AC-01  El sistema permite crear una reserva válida.
 *  AC-02  El sistema rechaza una reserva con fechas inválidas.
 *  AC-03  El sistema rechaza una reserva que excede la capacidad.
 *  AC-04  El sistema rechaza una reserva en habitación no disponible.
 *  AC-05  El sistema rechaza reservas solapadas en la misma habitación.
 *  AC-06  El sistema rechaza reservas solapadas para el mismo huésped.
 *  AC-07  El sistema permite consultar todas las reservas.
 *  AC-08  El sistema permite consultar una reserva por ID.
 *  AC-09  El sistema retorna 404 al consultar una reserva inexistente.
 *  AC-10  El sistema permite consultar disponibilidad de una habitación.
 *  AC-11  El sistema permite actualizar una reserva existente.
 *  AC-12  El sistema permite cancelar (eliminar) una reserva.
 *  AC-13  El sistema retorna 404 al cancelar una reserva inexistente.
 */

// ─────────────────────────────────────────────────────────────────────────────
// AC-01: Crear reserva válida
// ─────────────────────────────────────────────────────────────────────────────
test('AC-01 — Crear una reserva válida retorna 200 con los datos correctos', async ({ request }) => {
  const room  = await createRoom(request, 'PW-R01', 3, true);
  const guest = await createGuest(request, 'PW-G01');

  const res = await request.post('/reservation', {
    data: {
      roomId:      room.id,
      guestId:     guest.id,
      checkIn:     futureDate(10),
      checkOut:    futureDate(15),
      guestsCount: 2,
    },
  });

  expect(res.status()).toBe(200);
  const body: Reservation = await res.json();
  expect(body.id).toBeTruthy();
  expect(body.roomId).toBe(room.id);
  expect(body.guestId).toBe(guest.id);
  expect(body.guestsCount).toBe(2);
});

// ─────────────────────────────────────────────────────────────────────────────
// AC-02: Rechazar reserva con fechas inválidas
// ─────────────────────────────────────────────────────────────────────────────
test('AC-02 — Crear reserva con checkIn posterior a checkOut retorna 400', async ({ request }) => {
  const room  = await createRoom(request, 'PW-R02', 2, true);
  const guest = await createGuest(request, 'PW-G02');

  const res = await request.post('/reservation', {
    data: {
      roomId:      room.id,
      guestId:     guest.id,
      checkIn:     futureDate(20),   // checkIn > checkOut
      checkOut:    futureDate(10),
      guestsCount: 1,
    },
  });

  expect(res.status()).toBe(400);
  const body = await res.json();
  expect(body.error).toBe('checkIn must be before checkOut');
});

// ─────────────────────────────────────────────────────────────────────────────
// AC-03: Rechazar reserva que excede la capacidad
// ─────────────────────────────────────────────────────────────────────────────
test('AC-03 — Crear reserva con guestsCount mayor a maxGuests retorna 400', async ({ request }) => {
  const room  = await createRoom(request, 'PW-R03', 1, true); // maxGuests=1
  const guest = await createGuest(request, 'PW-G03');

  const res = await request.post('/reservation', {
    data: {
      roomId:      room.id,
      guestId:     guest.id,
      checkIn:     futureDate(30),
      checkOut:    futureDate(35),
      guestsCount: 5,  // excede maxGuests
    },
  });

  expect(res.status()).toBe(400);
  const body = await res.json();
  expect(body.error).toBe('guestsCount exceeds room capacity');
});

// ─────────────────────────────────────────────────────────────────────────────
// AC-04: Rechazar reserva en habitación no disponible
// ─────────────────────────────────────────────────────────────────────────────
test('AC-04 — Crear reserva en habitación no disponible retorna 400', async ({ request }) => {
  const room  = await createRoom(request, 'PW-R04', 2, false); // available=false
  const guest = await createGuest(request, 'PW-G04');

  const res = await request.post('/reservation', {
    data: {
      roomId:      room.id,
      guestId:     guest.id,
      checkIn:     futureDate(40),
      checkOut:    futureDate(45),
      guestsCount: 1,
    },
  });

  expect(res.status()).toBe(400);
  const body = await res.json();
  expect(body.error).toBe('Room is not available');
});

// ─────────────────────────────────────────────────────────────────────────────
// AC-05: Rechazar reservas solapadas en la misma habitación
// ─────────────────────────────────────────────────────────────────────────────
test('AC-05 — Crear reserva solapada en la misma habitación retorna 400', async ({ request }) => {
  const room   = await createRoom(request, 'PW-R05', 3, true);
  const guestA = await createGuest(request, 'PW-G05A');
  const guestB = await createGuest(request, 'PW-G05B');

  // Primera reserva: días 50-60
  await createReservation(request, room.id, guestA.id, futureDate(50), futureDate(60));

  // Segunda reserva solapada en la misma habitación: días 55-65
  const res = await request.post('/reservation', {
    data: {
      roomId:      room.id,
      guestId:     guestB.id,
      checkIn:     futureDate(55),
      checkOut:    futureDate(65),
      guestsCount: 1,
    },
  });

  expect(res.status()).toBe(400);
  const body = await res.json();
  expect(body.error).toBe('The room already has a reservation in that time range');
});

// ─────────────────────────────────────────────────────────────────────────────
// AC-06: Rechazar reservas solapadas para el mismo huésped
// ─────────────────────────────────────────────────────────────────────────────
test('AC-06 — El mismo huésped no puede tener dos reservas solapadas', async ({ request }) => {
  const room1 = await createRoom(request, 'PW-R06A', 2, true);
  const room2 = await createRoom(request, 'PW-R06B', 2, true);
  const guest = await createGuest(request, 'PW-G06');

  // Primera reserva del huésped
  await createReservation(request, room1.id, guest.id, futureDate(70), futureDate(80));

  // Segunda reserva del mismo huésped en fechas solapadas
  const res = await request.post('/reservation', {
    data: {
      roomId:      room2.id,
      guestId:     guest.id,
      checkIn:     futureDate(75),
      checkOut:    futureDate(85),
      guestsCount: 1,
    },
  });

  expect(res.status()).toBe(400);
  const body = await res.json();
  expect(body.error).toBe('The guest already has a reservation in that time range');
});

// ─────────────────────────────────────────────────────────────────────────────
// AC-07: Consultar todas las reservas
// ─────────────────────────────────────────────────────────────────────────────
test('AC-07 — Consultar todas las reservas retorna 200 y un arreglo JSON', async ({ request }) => {
  const res = await request.get('/reservation');

  expect(res.status()).toBe(200);
  const body = await res.json();
  expect(Array.isArray(body)).toBeTruthy();
});

// ─────────────────────────────────────────────────────────────────────────────
// AC-08: Consultar reserva por ID
// ─────────────────────────────────────────────────────────────────────────────
test('AC-08 — Consultar una reserva por su ID retorna 200 con los datos correctos', async ({ request }) => {
  const room      = await createRoom(request, 'PW-R08', 2, true);
  const guest     = await createGuest(request, 'PW-G08');
  const created   = await createReservation(request, room.id, guest.id, futureDate(90), futureDate(95));

  const res = await request.get(`/reservation/${created.id}`);

  expect(res.status()).toBe(200);
  const body: Reservation = await res.json();
  expect(body.id).toBe(created.id);
  expect(body.roomId).toBe(room.id);
  expect(body.guestId).toBe(guest.id);
});

// ─────────────────────────────────────────────────────────────────────────────
// AC-09: 404 al consultar reserva inexistente
// ─────────────────────────────────────────────────────────────────────────────
test('AC-09 — Consultar una reserva inexistente retorna 404', async ({ request }) => {
  const res = await request.get('/reservation/00000000-0000-0000-0000-000000000000');

  expect(res.status()).toBe(404);
  const body = await res.json();
  expect(body.error).toBe('Reservation not found');
});

// ─────────────────────────────────────────────────────────────────────────────
// AC-10: Consultar disponibilidad
// ─────────────────────────────────────────────────────────────────────────────
test('AC-10a — Habitación sin reservas aparece disponible', async ({ request }) => {
  const room = await createRoom(request, 'PW-R10', 2, true);

  const res = await request.get(`/reservation/availability/room/${room.id}`, {
    params: { checkIn: futureDate(200), checkOut: futureDate(210) },
  });

  expect(res.status()).toBe(200);
  const body = await res.json();
  expect(body.available).toBe(true);
});

test('AC-10b — Habitación con reserva solapada aparece no disponible', async ({ request }) => {
  const room  = await createRoom(request, 'PW-R10B', 2, true);
  const guest = await createGuest(request, 'PW-G10B');

  await createReservation(request, room.id, guest.id, futureDate(100), futureDate(110));

  const res = await request.get(`/reservation/availability/room/${room.id}`, {
    params: { checkIn: futureDate(105), checkOut: futureDate(115) },
  });

  expect(res.status()).toBe(200);
  const body = await res.json();
  expect(body.available).toBe(false);
});

// ─────────────────────────────────────────────────────────────────────────────
// AC-11: Actualizar reserva
// ─────────────────────────────────────────────────────────────────────────────
test('AC-11 — Actualizar una reserva existente retorna 200 con datos actualizados', async ({ request }) => {
  const room    = await createRoom(request, 'PW-R11', 3, true);
  const guest   = await createGuest(request, 'PW-G11');
  const created = await createReservation(request, room.id, guest.id, futureDate(120), futureDate(125));

  const res = await request.put(`/reservation/${created.id}`, {
    data: {
      roomId:      room.id,
      guestId:     guest.id,
      checkIn:     futureDate(130),
      checkOut:    futureDate(140),
      guestsCount: 3,
      notes:       'Actualizado por ATDD',
    },
  });

  expect(res.status()).toBe(200);
  const body: Reservation = await res.json();
  expect(body.guestsCount).toBe(3);
  expect(body.notes).toBe('Actualizado por ATDD');
});

// ─────────────────────────────────────────────────────────────────────────────
// AC-12: Cancelar (eliminar) reserva
// ─────────────────────────────────────────────────────────────────────────────
test('AC-12 — Cancelar una reserva retorna 200 y luego no puede consultarse', async ({ request }) => {
  const room    = await createRoom(request, 'PW-R12', 2, true);
  const guest   = await createGuest(request, 'PW-G12');
  const created = await createReservation(request, room.id, guest.id, futureDate(150), futureDate(155));

  // Cancelar
  const deleteRes = await request.delete(`/reservation/${created.id}`);
  expect(deleteRes.status()).toBe(200);

  // Verificar que ya no existe
  const getRes = await request.get(`/reservation/${created.id}`);
  expect(getRes.status()).toBe(404);
});

// ─────────────────────────────────────────────────────────────────────────────
// AC-13: 404 al cancelar reserva inexistente
// ─────────────────────────────────────────────────────────────────────────────
test('AC-13 — Cancelar una reserva inexistente retorna 404', async ({ request }) => {
  const res = await request.delete('/reservation/00000000-0000-0000-0000-000000000000');

  expect(res.status()).toBe(404);
  const body = await res.json();
  expect(body.error).toBe('Reservation not found');
});
