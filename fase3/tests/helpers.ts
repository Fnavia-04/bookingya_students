import { APIRequestContext } from '@playwright/test';

/**
 * FASE 3 — ATDD
 * Helpers reutilizables para crear entidades de prueba via API.
 * Cada test de aceptación usa estos helpers para preparar su contexto
 * sin duplicar código.
 */

export interface Room {
  id: string;
  code: string;
  name: string;
  city: string;
  maxGuests: number;
  nightlyPrice: number;
  available: boolean;
}

export interface Guest {
  id: string;
  identification: string;
  name: string;
  email: string;
}

export interface Reservation {
  id: string;
  roomId: string;
  guestId: string;
  checkIn: string;
  checkOut: string;
  guestsCount: number;
  notes?: string;
}

/** Crea una habitación disponible y retorna su modelo */
export async function createRoom(
  request: APIRequestContext,
  code: string,
  maxGuests = 3,
  available = true
): Promise<Room> {
  const res = await request.post('/room', {
    data: {
      code,
      name: `Habitación ${code}`,
      city: 'Bogotá',
      maxGuests,
      nightlyPrice: 180.00,
      available,
    },
  });
  if (!res.ok()) {
    throw new Error(`createRoom falló [${res.status()}]: ${await res.text()}`);
  }
  return res.json();
}

/** Crea un huésped y retorna su modelo */
export async function createGuest(
  request: APIRequestContext,
  identification: string
): Promise<Guest> {
  const email = `${identification.toLowerCase().replace(/-/g, '')}@test.com`;
  const res = await request.post('/guest', {
    data: { identification, name: `Huésped ${identification}`, email },
  });
  if (!res.ok()) {
    throw new Error(`createGuest falló [${res.status()}]: ${await res.text()}`);
  }
  return res.json();
}

/** Crea una reserva y retorna su modelo */
export async function createReservation(
  request: APIRequestContext,
  roomId: string,
  guestId: string,
  checkIn: string,
  checkOut: string,
  guestsCount = 1
): Promise<Reservation> {
  const res = await request.post('/reservation', {
    data: { roomId, guestId, checkIn, checkOut, guestsCount },
  });
  if (!res.ok()) {
    throw new Error(`createReservation falló [${res.status()}]: ${await res.text()}`);
  }
  return res.json();
}

/** Genera una fecha ISO futura a N días desde hoy */
export function futureDate(days: number, hour = 14): string {
  const d = new Date();
  d.setDate(d.getDate() + days);
  d.setHours(hour, 0, 0, 0);
  return d.toISOString().replace('Z', '').split('.')[0];
}
