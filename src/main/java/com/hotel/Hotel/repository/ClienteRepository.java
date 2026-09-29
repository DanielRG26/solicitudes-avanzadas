package com.hotel.Hotel.repository;

import com.hotel.Hotel.domain.Cliente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByEmail(String email);

    /** Carga cliente + reservas + habitación en una sola consulta (evita N+1). */
    @EntityGraph(attributePaths = { "reservas", "reservas.habitacion" })
    Optional<Cliente> findConReservasById(UUID id);
}
