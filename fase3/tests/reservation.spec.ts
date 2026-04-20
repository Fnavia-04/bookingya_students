import { test, expect } from '@playwright/test';
import {
  createRoom, createGuest, createReservation, futureDate,
  type Reservation
} from './helpers';

/**
 * FASE 3 — ATDD (Acceptance Test Driven Development)
 * 
 * Pruebas de aceptacion con Playwright + TypeScript.
 * 
 * Criterios de aceptacion del usuario final de BookingYa:
 * - El usuario debe poder crear una reserva con todos los datos
 * - El usuario no debe poder reservar mas personas de las que caben
 * - El usuario debe poder consultar su reserva y cancelarla
 */

// Test 1: Usuario crea una reserva exitosa
test('El usuario puede crear una reserva con datos validos', async ({ request }) => {
  // Preparacion: existe una habitacion disponible
  const room = await createRoom(request, 'H-001', 3, true);
  // Y existe un huesped registrado
  const guest = await createGuest(request, 'C-001');

  // Accion: el usuario crea una reserva
  const res = await request.post('/reservation', {
    data: {
      roomId: room.id,
      guestId: guest.id,
      checkIn: futureDate(10),
      checkOut: futureDate(15),
      guestsCount: 2,
    },
  });

  // Validacion: la reserva se debe crear correctamente
  expect(res.status()).toBe(200);
  const body: Reservation = await res.json();
  expect(body.id).toBeTruthy();
  expect(body.roomId).toBe(room.id);
  expect(body.guestId).toBe(guest.id);
});

// Test 2: Usuario intenta exceder capacidad de la habitacion
test('El usuario no puede reservar mas personas de las que caben', async ({ request }) => {
  // Preparacion: existe una habitacion solo para 1 persona
  const room = await createRoom(request, 'H-002', 1, true);
  const guest = await createGuest(request, 'C-002');

  // Accion: el usuario intenta reservar para 5 personas
  const res = await request.post('/reservation', {
    data: {
      roomId: room.id,
      guestId: guest.id,
      checkIn: futureDate(20),
      checkOut: futureDate(25),
      guestsCount: 5, // excede la capacidad
    },
  });

  // Validacion: el sistema rechaza la reserva
  expect(res.status()).toBe(400);
  const body = await res.json();
  expect(body.error).toContain('capacity');
});

// Test 3: Usuario consulta y cancela su reserva
test('El usuario puede consultar y luego cancelar su reserva', async ({ request }) => {
  // Preparacion: existe una habitacion y huesped
  const room = await createRoom(request, 'H-003', 2, true);
  const guest = await createGuest(request, 'C-003');
  
  // El usuario crea una reserva
  const created = await createReservation(request, room.id, guest.id, futureDate(30), futureDate(35));

  // Accion: el usuario consulta su reserva
  const getRes = await request.get(`/reservation/${created.id}`);
  
  // Validacion: la reserva existe y tiene sus datos
  expect(getRes.status()).toBe(200);
  const reservation: Reservation = await getRes.json();
  expect(reservation.id).toBe(created.id);
  expect(reservation.roomId).toBe(room.id);

  // Accion: el usuario cancela su reserva
  const deleteRes = await request.delete(`/reservation/${created.id}`);
  
  // Validacion: la cancelacion fue exitosa
  expect(deleteRes.status()).toBe(200);
  
  // Accion: intenta consultar la reserva cancelada
  const checkRes = await request.get(`/reservation/${created.id}`);
  
  // Validacion: ya no existe
  expect(checkRes.status()).toBe(404);
});
