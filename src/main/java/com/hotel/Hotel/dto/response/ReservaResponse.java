package com.hotel.Hotel.dto.response;

import java.time.LocalDate;
import java.util.UUID;

public record ReservaResponse(
        UUID id,
        String nombreHuesped,
        String habitacionNumero,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        double costoTotal,
        String estado) {
}
