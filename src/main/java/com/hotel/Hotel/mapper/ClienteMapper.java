package com.hotel.Hotel.mapper;

import com.hotel.Hotel.domain.Cliente;
import com.hotel.Hotel.domain.EstadoReserva;
import com.hotel.Hotel.domain.Reserva;
import com.hotel.Hotel.dto.request.ActualizarClienteRequest;
import com.hotel.Hotel.dto.request.CrearClienteRequest;
import com.hotel.Hotel.dto.response.ClienteResponse;
import com.hotel.Hotel.dto.response.ClienteResumenResponse;
import com.hotel.Hotel.dto.response.ReservaItemResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ClienteMapper {

    ClienteResponse toResponse(Cliente cliente);

    List<ClienteResponse> toResponseList(List<Cliente> clientes);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "penalizaciones", ignore = true)
    @Mapping(target = "reservas", ignore = true)
    Cliente toEntity(CrearClienteRequest request);

    // ---------- Tarea 3: actualización parcial segura (PATCH) ----------

    /** Solo copia los campos NO nulos; nunca toca id, activo, penalizaciones ni reservas. */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activo", ignore = true)
    @Mapping(target = "penalizaciones", ignore = true)
    @Mapping(target = "reservas", ignore = true)
    void updateClienteFromDto(ActualizarClienteRequest dto, @MappingTarget Cliente entity);

    // ---------- Tarea 2: resumen agregado con proyección anidada ----------

    @Mapping(target = "totalReservasRealizadas", source = "reservas", qualifiedByName = "contarReservas")
    @Mapping(target = "montoTotalGastado", source = "reservas", qualifiedByName = "sumarMontoGastado")
    @Mapping(target = "reservasRecientes", source = "reservas", qualifiedByName = "reservasMasRecientesPrimero")
    ClienteResumenResponse toResumenResponse(Cliente cliente);

    @Mapping(target = "idReserva", source = "id")
    @Mapping(target = "numeroHabitacion", source = "habitacion.numero")
    @Mapping(target = "fechaInicio", expression = "java(reserva.getPeriodo().getFechaInicio())")
    @Mapping(target = "fechaFin", expression = "java(reserva.getPeriodo().getFechaFin())")
    ReservaItemResponse toReservaItem(Reserva reserva);

    @Named("contarReservas")
    default int contarReservas(List<Reserva> reservas) {
        return reservas == null ? 0 : reservas.size();
    }

    /** Suma con Streams; las reservas CANCELADAS no cuentan como dinero gastado. */
    @Named("sumarMontoGastado")
    default double sumarMontoGastado(List<Reserva> reservas) {
        if (reservas == null) {
            return 0.0;
        }
        return reservas.stream()
                .filter(r -> r.getEstado() != EstadoReserva.CANCELADA)
                .mapToDouble(Reserva::getCostoTotal)
                .sum();
    }

    @Named("reservasMasRecientesPrimero")
    default List<ReservaItemResponse> reservasMasRecientesPrimero(List<Reserva> reservas) {
        if (reservas == null) {
            return List.of();
        }
        return reservas.stream()
                .sorted(Comparator.comparing((Reserva r) -> r.getPeriodo().getFechaInicio()).reversed())
                .map(this::toReservaItem)
                .toList();
    }
}
