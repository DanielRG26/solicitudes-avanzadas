package com.hotel.Hotel.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

/** Sub-DTO plano: NO referencia de vuelta al cliente (evita ciclos en Jackson). */
public record ReservaItemResponse(
        UUID idReserva,
        String numeroHabitacion,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        String estado,
        double costoTotal) {
}
