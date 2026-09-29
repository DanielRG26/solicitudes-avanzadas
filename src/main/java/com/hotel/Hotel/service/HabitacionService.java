package com.hotel.Hotel.service;

import com.hotel.Hotel.domain.Habitacion;
import com.hotel.Hotel.dto.request.CrearHabitacionRequest;
import com.hotel.Hotel.dto.response.HabitacionResponse;
import com.hotel.Hotel.exception.ConflictoException;
import com.hotel.Hotel.exception.RecursoNoEncontradoException;
import com.hotel.Hotel.mapper.HabitacionMapper;
import com.hotel.Hotel.repository.HabitacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class HabitacionService {

    private final HabitacionRepository habitacionRepository;
    private final HabitacionMapper habitacionMapper;

    public HabitacionService(HabitacionRepository habitacionRepository, HabitacionMapper habitacionMapper) {
        this.habitacionRepository = habitacionRepository;
        this.habitacionMapper = habitacionMapper;
    }

    /** Un único método de alta para cualquier subtipo de habitación. */
    @Transactional
    public HabitacionResponse crear(CrearHabitacionRequest request) {
        if (habitacionRepository.findByNumero(request.numero()).isPresent()) {
            throw new ConflictoException("Ya existe una habitación con el número: " + request.numero());
        }
        Habitacion habitacion = habitacionMapper.toEntity(request);
        return habitacionMapper.toResponse(habitacionRepository.save(habitacion));
    }

    @Transactional(readOnly = true)
    public List<HabitacionResponse> listarTodas() {
        return habitacionMapper.toResponseList(habitacionRepository.findAll());
    }

    @Transactional(readOnly = true)
    public HabitacionResponse obtenerPorId(UUID id) {
        Habitacion habitacion = habitacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Habitación no encontrada con ID: " + id));
        return habitacionMapper.toResponse(habitacion);
    }
}
