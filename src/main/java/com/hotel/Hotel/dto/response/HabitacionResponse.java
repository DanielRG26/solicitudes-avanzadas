package com.hotel.Hotel.dto.response;

import java.util.UUID;

/**
 * Contrato de salida polimórfico (sealed interface de Java 17).
 * Cada implementación expone su campo "tipo" y sus atributos propios.
 */
public sealed interface HabitacionResponse
        permits HabitacionEstandarResponse, SuitePresidencialResponse {

    TipoHabitacion tipo();

    UUID id();

    String numero();

    int capacidadMaxima();

    double precioPorNoche();

    String estado();
}
