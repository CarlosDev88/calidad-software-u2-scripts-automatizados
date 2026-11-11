package co.edu.calidad.taller.unit;

import co.edu.calidad.taller.controller.UsuarioController;
import co.edu.calidad.taller.exception.EmailDuplicadoException;
import co.edu.calidad.taller.exception.UsuarioNoEncontradoException;
import co.edu.calidad.taller.model.Usuario;
import co.edu.calidad.taller.model.UsuarioRequest;
import co.edu.calidad.taller.service.UsuarioService;
import co.edu.calidad.taller.support.ReporteResultados;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas unitarias del controlador REST aislado con Mockito.
 *
 * @WebMvcTest levanta solo la capa web (sin servidor real) y el servicio se reemplaza por un
 * "mock" de Mockito. Asi se prueba unicamente el controlador: que llame al servicio cuando debe,
 * que NO lo llame si los datos son invalidos y que traduzca las excepciones al codigo HTTP correcto.
 */
@Tag("unit")
@ExtendWith(ReporteResultados.class)
@WebMvcTest(UsuarioController.class)
@DisplayName("Unitarias - UsuarioController con Mockito")
class UsuarioControllerTest {

    private static final String JSON_VALIDO = """
            {"nombre": "Ana Perez", "email": "ana@correo.com", "edad": 25}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService servicio;

    @Test
    @DisplayName("POST valido llama al servicio una vez y responde 201 con Location")
    void crearValido() throws Exception {
        when(servicio.crear(any(UsuarioRequest.class)))
                .thenReturn(new Usuario(7L, "Ana Perez", "ana@correo.com", 25));

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/usuarios/7"))
                .andExpect(jsonPath("$.id").value(7));

        verify(servicio).crear(any(UsuarioRequest.class));
    }

    @Test
    @DisplayName("POST invalido responde 400 y NUNCA llama al servicio")
    void crearInvalidoNoLlamaAlServicio() throws Exception {
        String jsonInvalido = """
                {"nombre": "", "email": "ana-correo", "edad": 15}
                """;

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(jsonInvalido))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errores.nombre").value("El nombre es obligatorio"))
                .andExpect(jsonPath("$.errores.email").exists())
                .andExpect(jsonPath("$.errores.edad").value("La edad minima es 18"));

        verify(servicio, never()).crear(any());
    }

    @Test
    @DisplayName("Si el servicio lanza EmailDuplicadoException responde 409")
    void emailDuplicadoResponde409() throws Exception {
        when(servicio.crear(any(UsuarioRequest.class))).thenThrow(new EmailDuplicadoException("ana@correo.com"));

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errores.email").value("Este email ya esta registrado"));
    }

    @Test
    @DisplayName("Si el servicio lanza UsuarioNoEncontradoException responde 404")
    void usuarioInexistenteResponde404() throws Exception {
        when(servicio.buscarPorId(5L)).thenThrow(new UsuarioNoEncontradoException(5L));

        mockMvc.perform(get("/api/usuarios/5"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No existe un usuario con id 5"));
    }

    @Test
    @DisplayName("GET lista devuelve lo que entrega el servicio")
    void listar() throws Exception {
        when(servicio.listar()).thenReturn(List.of(
                new Usuario(1L, "Ana Perez", "ana@correo.com", 25),
                new Usuario(2L, "Luis Gomez", "luis@correo.com", 30)));

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].nombre").value("Luis Gomez"));
    }

    @Test
    @DisplayName("GET con id no numerico responde 400")
    void idNoNumerico() throws Exception {
        mockMvc.perform(get("/api/usuarios/abc"))
                .andExpect(status().isBadRequest());

        verify(servicio, never()).buscarPorId(any());
    }
}
