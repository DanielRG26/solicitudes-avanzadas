package com.hotel.Hotel.mapper;

import com.hotel.Hotel.domain.Habitacion;
import com.hotel.Hotel.domain.HabitacionEstandar;
import com.hotel.Hotel.domain.SuitePresidencial;
import com.hotel.Hotel.dto.request.CrearHabitacionEstandarRequest;
import com.hotel.Hotel.dto.request.CrearHabitacionRequest;
import com.hotel.Hotel.dto.request.CrearSuitePresidencialRequest;
import com.hotel.Hotel.dto.response.HabitacionEstandarResponse;
import com.hotel.Hotel.dto.response.HabitacionResponse;
import com.hotel.Hotel.dto.response.SuitePresidencialResponse;
import org.hibernate.Hibernate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * Mapper polimórfico: los métodos default resuelven la subclase concreta en
 * tiempo de ejecución y delegan en los métodos generados por MapStruct.
 */
@Mapper(componentModel = "spring")
public interface HabitacionMapper {

    // ---------- Entidad -> Response (polimórfico) ----------

    default HabitacionResponse toResponse(Habitacion habitacion) {
        if (habitacion == null) {
            return null;
        }
        // unproxy: si llega un proxy lazy de Hibernate, instanceof fallaría
        Object real = Hibernate.unproxy(habitacion);
        if (real instanceof HabitacionEstandar estandar) {
            return toEstandarResponse(estandar);
        }
        if (real instanceof SuitePresidencial suite) {
            return toSuiteResponse(suite);
        }
        throw new IllegalArgumentException("Tipo de habitación no soportado: " + real.getClass().getSimpleName());
    }

    List<HabitacionResponse> toResponseList(List<Habitacion> habitaciones);

    @Mapping(target = "tipo", constant = "ESTANDAR")
    HabitacionEstandarResponse toEstandarResponse(HabitacionEstandar habitacion);

    @Mapping(target = "tipo", constant = "SUITE")
    SuitePresidencialResponse toSuiteResponse(SuitePresidencial habitacion);

    // ---------- Request -> Entidad (polimórfico) ----------

    default Habitacion toEntity(CrearHabitacionRequest request) {
        if (request instanceof CrearHabitacionEstandarRequest estandar) {
            return toEstandarEntity(estandar);
        }
        if (request instanceof CrearSuitePresidencialRequest suite) {
            return toSuiteEntity(suite);
        }
        throw new IllegalArgumentException("Tipo de solicitud no soportado");
    }

    // El constructor de la entidad (modelo rico) se usa vía parámetros por nombre;
    // id y estado los asigna el propio dominio.
    HabitacionEstandar toEstandarEntity(CrearHabitacionEstandarRequest request);

    SuitePresidencial toSuiteEntity(CrearSuitePresidencialRequest request);
}
