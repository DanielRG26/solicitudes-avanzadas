package com.hotel.Hotel;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Verifica las 4 evidencias del taller de punta a punta (controlador -> servicio -> mapper -> BD H2). */
@SpringBootTest
@AutoConfigureMockMvc
class ApiHotelAvanzadaTest {

    @Autowired
    private MockMvc mockMvc;

    private static String sufijo() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String postYObtenerId(String url, String json) throws Exception {
        MvcResult resultado = mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(resultado.getResponse().getContentAsString(StandardCharsets.UTF_8), "$.id");
    }

    private String crearCliente(String nombre) throws Exception {
        return postYObtenerId("/api/clientes",
                "{\"nombre\":\"" + nombre + "\",\"email\":\"" + sufijo() + "@correo.com\"}");
    }

    private String crearEstandar(double precio) throws Exception {
        return postYObtenerId("/api/habitaciones/estandar",
                "{\"numero\":\"E-" + sufijo() + "\",\"precioPorNoche\":" + precio
                        + ",\"capacidadMaxima\":2,\"camasIndividuales\":2}");
    }

    private String crearSuite(double precio) throws Exception {
        return postYObtenerId("/api/habitaciones/suites",
                "{\"numero\":\"P-" + sufijo() + "\",\"precioPorNoche\":" + precio
                        + ",\"capacidadMaxima\":2,\"jacuzziPrivado\":true,\"incluyeMayordomo\":true}");
    }

    private String crearReserva(String clienteId, String habitacionId, String inicio, String fin) throws Exception {
        return postYObtenerId("/api/reservas",
                "{\"clienteId\":\"" + clienteId + "\",\"habitacionId\":\"" + habitacionId
                        + "\",\"fechaInicio\":\"" + inicio + "\",\"fechaFin\":\"" + fin + "\"}");
    }

    // ---------- Prueba 1: creación de suite presidencial ----------
    @Test
    void creaSuitePresidencialConCreated() throws Exception {
        String numero = "P05-" + sufijo();
        String cuerpo = "{\"numero\":\"" + numero + "\",\"precioPorNoche\":350.0,\"capacidadMaxima\":2,"
                + "\"jacuzziPrivado\":true,\"incluyeMayordomo\":true}";

        mockMvc.perform(post("/api/habitaciones/suites").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("SUITE"))
                .andExpect(jsonPath("$.jacuzziPrivado").value(true))
                .andExpect(jsonPath("$.incluyeMayordomo").value(true));

        // número duplicado -> 409
        mockMvc.perform(post("/api/habitaciones/suites").contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isConflict());
    }

    // ---------- Prueba 2: consulta polimórfica ----------
    @Test
    void listaHabitacionesPolimorficas() throws Exception {
        crearEstandar(120.0);
        crearSuite(350.0);

        mockMvc.perform(get("/api/habitaciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.tipo=='ESTANDAR')].camasIndividuales").isNotEmpty())
                .andExpect(jsonPath("$[?(@.tipo=='SUITE')].jacuzziPrivado").isNotEmpty())
                .andExpect(jsonPath("$[?(@.tipo=='SUITE')].incluyeMayordomo").isNotEmpty());
    }

    // ---------- Prueba 3: resumen ejecutivo del cliente ----------
    @Test
    void resumenDelClienteCalculaTotalesSinCiclos() throws Exception {
        String clienteId = crearCliente("Ana Torres");
        String estandar = crearEstandar(100.0);
        String suite = crearSuite(350.0);

        crearReserva(clienteId, estandar, "2026-10-01T14:00:00", "2026-10-03T14:00:00"); // 2 noches = 200
        crearReserva(clienteId, suite, "2026-11-10T14:00:00", "2026-11-13T14:00:00"); // 3 noches = 1050
        String cancelada = crearReserva(clienteId, estandar, "2026-12-01T14:00:00", "2026-12-02T14:00:00"); // 100
        mockMvc.perform(patch("/api/reservas/" + cancelada + "/cancelar")).andExpect(status().isOk());

        mockMvc.perform(get("/api/clientes/" + clienteId + "/resumen"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clienteId))
                .andExpect(jsonPath("$.totalReservasRealizadas").value(3))
                .andExpect(jsonPath("$.montoTotalGastado").value(1250.0))
                .andExpect(jsonPath("$.reservasRecientes.length()").value(3))
                .andExpect(jsonPath("$.reservasRecientes[0].estado").value("CANCELADA"))
                .andExpect(jsonPath("$.reservasRecientes[0].numeroHabitacion").isNotEmpty())
                .andExpect(jsonPath("$.reservasRecientes[0].cliente").doesNotExist());
    }

    @Test
    void resumenDeClienteInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/api/clientes/" + UUID.randomUUID() + "/resumen"))
                .andExpect(status().isNotFound());
    }

    // ---------- Prueba 4: PATCH selectivo ----------
    @Test
    void patchSoloCambiaElNombreYProtegeInvariantes() throws Exception {
        String nombreOriginal = "Cliente Original";
        String clienteId = crearCliente(nombreOriginal);
        String emailOriginal = JsonPath.read(
                mockMvc.perform(get("/api/clientes/" + clienteId)).andReturn().getResponse()
                        .getContentAsString(StandardCharsets.UTF_8),
                "$.email");

        // además del nombre se intenta colar activo y penalizaciones (Mass Assignment)
        mockMvc.perform(patch("/api/clientes/" + clienteId).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Nuevo Nombre Modificado\",\"activo\":false,\"penalizaciones\":9}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Nuevo Nombre Modificado"))
                .andExpect(jsonPath("$.email").value(emailOriginal))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.penalizaciones").value(0));

        // se confirma lo persistido en BD
        mockMvc.perform(get("/api/clientes/" + clienteId))
                .andExpect(jsonPath("$.nombre").value("Nuevo Nombre Modificado"))
                .andExpect(jsonPath("$.email").value(emailOriginal))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.penalizaciones").value(0));
    }

    @Test
    void validacionesRetornan400() throws Exception {
        mockMvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"\",\"email\":\"no-es-email\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/habitaciones/suites").contentType(MediaType.APPLICATION_JSON)
                .content("{\"numero\":\"X-1\"}"))
                .andExpect(status().isBadRequest());
    }
}
