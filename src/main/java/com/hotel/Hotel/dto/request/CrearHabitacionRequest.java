package com.hotel.Hotel.dto.request;

/**
 * Jerarquía sellada de requests para dar de alta habitaciones.
 * Cada subtipo tiene su propio endpoint (/estandar, /suites) pero ambos
 * comparten un único controlador, servicio y mapper polimórfico.
 */
public sealed interface CrearHabitacionRequest
        permits CrearHabitacionEstandarRequest, CrearSuitePresidencialRequest {

    String numero();

    Double precioPorNoche();

    Integer capacidadMaxima();
}
