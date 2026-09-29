package com.hotel.Hotel.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearClienteRequest(
        @NotBlank(message = "El nombre es obligatorio") @Size(max = 100, message = "Máximo 100 caracteres") String nombre,
        @NotBlank(message = "El email es obligatorio") @Email(message = "Email inválido") @Size(max = 120, message = "Máximo 120 caracteres") String email) {
}
