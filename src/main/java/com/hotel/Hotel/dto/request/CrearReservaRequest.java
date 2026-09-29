package com.hotel.Hotel.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record CrearReservaRequest(
        @NotNull(message = "clienteId es obligatorio") UUID clienteId,
        @NotNull(message = "habitacionId es obligatorio") UUID habitacionId,
        @NotNull(message = "fechaInicio es obligatoria") LocalDateTime fechaInicio,
        @NotNull(message = "fechaFin es obligatoria") LocalDateTime fechaFin) {
}
