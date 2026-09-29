package com.hotel.Hotel.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearHabitacionEstandarRequest(
        @NotBlank(message = "El número es obligatorio") String numero,
        @NotNull(message = "El precio por noche es obligatorio") @Positive(message = "El precio debe ser mayor que 0") Double precioPorNoche,
        @NotNull(message = "La capacidad máxima es obligatoria") @Min(value = 1, message = "La capacidad mínima es 1") Integer capacidadMaxima,
        @NotNull(message = "Las camas individuales son obligatorias") @Min(value = 0, message = "No puede ser negativo") Integer camasIndividuales)
        implements CrearHabitacionRequest {
}
