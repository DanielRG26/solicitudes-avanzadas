package com.hotel.Hotel.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Ambos campos son opcionales: null = "no cambiar". */
public record ActualizarClienteRequest(
        @Size(max = 100, message = "Máximo 100 caracteres") @Pattern(regexp = ".*\\S.*", message = "El nombre no puede estar vacío") String nombre,
        @Size(max = 120, message = "Máximo 120 caracteres") @Email(message = "Email inválido") String email) {
}
