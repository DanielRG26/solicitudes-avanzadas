package com.hotel.Hotel.dto.response;

import java.util.UUID;

public record SuitePresidencialResponse(
        TipoHabitacion tipo,
        UUID id,
        String numero,
        int capacidadMaxima,
        double precioPorNoche,
        String estado,
        boolean incluyeMayordomo,
        boolean jacuzziPrivado) implements HabitacionResponse {
}
