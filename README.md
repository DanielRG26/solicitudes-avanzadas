# Hotel Spring Boot – Taller: Contratos Polimórficos, Proyecciones Anidadas y PATCH seguro

Java 17 · Spring Boot 3.3.4 · JPA/Hibernate · PostgreSQL · MapStruct 1.5.5

## Ejecutar (macOS)

```bash
# 1) Requisitos (una sola vez)
brew install openjdk@17
brew install --cask docker          # solo si usarás PostgreSQL con Docker
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# 2a) Con PostgreSQL (configuración original del proyecto)
docker compose up -d
./mvnw spring-boot:run

# 2b) Sin PostgreSQL (H2 en memoria, para probar rápido)
./mvnw spring-boot:run -Dspring-boot.run.profiles=h2

# Pruebas automáticas (usan H2, no necesitan PostgreSQL)
./mvnw test
```

API en `http://localhost:8080`. Las 4 evidencias están en **`solicitudes-avanzadas.http`**
(extensión *REST Client* de VS Code; ejecutar los bloques en orden).

## Endpoints nuevos

| Verbo | Ruta | Respuesta |
|---|---|---|
| POST | `/api/habitaciones/estandar` | 201 + `Location` + habitación creada |
| POST | `/api/habitaciones/suites` | 201 + `Location` + suite creada |
| GET | `/api/habitaciones` | 200, lista polimórfica con `"tipo": "ESTANDAR" \| "SUITE"` |
| GET | `/api/habitaciones/{id}` | 200 / 404 |
| GET | `/api/clientes/{id}/resumen` | 200 `ClienteResumenResponse` |
| PATCH | `/api/clientes/{id}` | 200 `ClienteResponse` |
| PATCH | `/api/reservas/{id}/confirmar` · `/cancelar` | 200 |

## Decisiones de diseño (rúbrica)

- **Polimorfismo (30 %)**: `CrearHabitacionRequest` y `HabitacionResponse` son *sealed interfaces* de Java 17
  con `record`s por subtipo. `HabitacionMapper.toResponse(Habitacion)` resuelve la subclase en tiempo de
  ejecución (`Hibernate.unproxy` + `instanceof`) y delega en métodos MapStruct que conservan
  `camasIndividuales` / `jacuzziPrivado` / `incluyeMayordomo`. Un solo `HabitacionController` y un solo `HabitacionService`.
- **Agregados (25 %)**: `ClienteMapper.toResumenResponse` usa métodos `@Named` con Streams para
  `totalReservasRealizadas` y `montoTotalGastado`. `ReservaItemResponse` es plano (sin cliente) → no hay
  recursión en Jackson. Se carga con `@EntityGraph` (una consulta, sin N+1). Las reservas salen ordenadas de la más reciente a la más antigua.
- **PATCH (25 %)**: `updateClienteFromDto` con `@MappingTarget` y
  `NullValuePropertyMappingStrategy.IGNORE`; `id`, `activo`, `penalizaciones` y `reservas` ignorados
  explícitamente (y `Cliente` solo tiene setters de `nombre` y `email`, con validación de dominio).
- **Contratos HTTP (20 %)**: 201/200/404/400/409 con `ProblemDetail`; ninguna `@Entity` en la capa web.

## Notas

- `montoTotalGastado` **no suma reservas CANCELADAS** (ese dinero no se gastó).
- Se corrigió `ReservaMapper`: `nombreHuesped`, `habitacionNumero` y las fechas salían `null` porque MapStruct no
  las podía deducir; ahora se mapean explícitamente.
- Se renombraron los parámetros del constructor de `SuitePresidencial` a `incluyeMayordomo` / `jacuzziPrivado`
  para que MapStruct los relacione con el request.
