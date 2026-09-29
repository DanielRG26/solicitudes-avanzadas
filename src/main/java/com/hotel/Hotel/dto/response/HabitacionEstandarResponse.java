package com.hotel.Hotel.dto.response;

import java.util.UUID;

public record HabitacionEstandarResponse(
        TipoHabitacion tipo,
        UUID id,
        String numero,
        int capacidadMaxima,
        double precioPorNoche,
        String estado,
        int camasIndividuales) implements HabitacionResponse {
}
