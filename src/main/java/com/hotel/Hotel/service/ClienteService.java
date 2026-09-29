package com.hotel.Hotel.service;

import com.hotel.Hotel.domain.Cliente;
import com.hotel.Hotel.dto.request.ActualizarClienteRequest;
import com.hotel.Hotel.dto.request.CrearClienteRequest;
import com.hotel.Hotel.dto.response.ClienteResponse;
import com.hotel.Hotel.dto.response.ClienteResumenResponse;
import com.hotel.Hotel.exception.ConflictoException;
import com.hotel.Hotel.exception.RecursoNoEncontradoException;
import com.hotel.Hotel.mapper.ClienteMapper;
import com.hotel.Hotel.repository.ClienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    public ClienteService(ClienteRepository clienteRepository, ClienteMapper clienteMapper) {
        this.clienteRepository = clienteRepository;
        this.clienteMapper = clienteMapper;
    }

    @Transactional
    public ClienteResponse crear(CrearClienteRequest request) {
        if (clienteRepository.findByEmail(request.email()).isPresent()) {
            throw new ConflictoException("Ya existe un cliente con el email: " + request.email());
        }
        Cliente cliente = new Cliente(request.nombre(), request.email());
        Cliente guardado = clienteRepository.save(cliente);
        return clienteMapper.toResponse(guardado);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        return clienteMapper.toResponseList(clienteRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(UUID id) {
        return clienteMapper.toResponse(buscar(id));
    }

    /** Vista ejecutiva: el mapeo ocurre dentro de la transacción (colecciones lazy). */
    @Transactional(readOnly = true)
    public ClienteResumenResponse obtenerResumen(UUID id) {
        Cliente cliente = clienteRepository.findConReservasById(id)
                .orElseThrow(() -> noEncontrado(id));
        return clienteMapper.toResumenResponse(cliente);
    }

    /** PATCH: cargar -> aplicar cambios no nulos con el mapper -> guardar -> responder. */
    @Transactional
    public ClienteResponse actualizarParcial(UUID id, ActualizarClienteRequest request) {
        Cliente cliente = buscar(id);

        if (request.email() != null) {
            clienteRepository.findByEmail(request.email().trim())
                    .filter(otro -> !otro.getId().equals(id))
                    .ifPresent(otro -> {
                        throw new ConflictoException("Ya existe un cliente con el email: " + request.email());
                    });
        }

        clienteMapper.updateClienteFromDto(request, cliente);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    private Cliente buscar(UUID id) {
        return clienteRepository.findById(id).orElseThrow(() -> noEncontrado(id));
    }

    private RecursoNoEncontradoException noEncontrado(UUID id) {
        return new RecursoNoEncontradoException("Cliente no encontrado con ID: " + id);
    }
}
