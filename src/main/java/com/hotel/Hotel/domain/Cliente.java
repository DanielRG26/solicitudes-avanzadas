package com.hotel.Hotel.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private boolean activo;

    @Column(nullable = false)
    private int penalizaciones;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reserva> reservas = new ArrayList<>();

    protected Cliente() {
    }

    public Cliente(String nombre, String email) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
        this.id = UUID.randomUUID();
        this.nombre = nombre;
        this.email = email;
        this.activo = true;
        this.penalizaciones = 0;
    }

    public void registrarPenalizacion() {
        this.penalizaciones++;
        if (this.penalizaciones >= 3) {
            this.activo = false;
        }
    }

    public void reactivar() {
        this.activo = true;
        this.penalizaciones = 0;
    }

    public boolean puedeRealizarReservas() {
        return this.activo;
    }

    /**
     * Setters solo para los datos editables por el propio cliente (PATCH).
     * Conservan las invariantes del modelo rico. id, activo, penalizaciones y
     * reservas NO tienen setter: solo cambian mediante métodos de dominio.
     */
    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }
        this.nombre = nombre.trim();
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacío");
        }
        this.email = email.trim();
    }

    public UUID getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActivo() {
        return activo;
    }

    public int getPenalizaciones() {
        return penalizaciones;
    }

    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }
}
