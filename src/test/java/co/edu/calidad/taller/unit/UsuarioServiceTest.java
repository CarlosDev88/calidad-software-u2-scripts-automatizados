package co.edu.calidad.taller.unit;

import co.edu.calidad.taller.exception.EmailDuplicadoException;
import co.edu.calidad.taller.exception.UsuarioNoEncontradoException;
import co.edu.calidad.taller.model.Usuario;
import co.edu.calidad.taller.model.UsuarioRequest;
import co.edu.calidad.taller.service.UsuarioService;
import co.edu.calidad.taller.support.ReporteResultados;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas unitarias de la logica de negocio (UsuarioService) con JUnit 5 "puro".
 *
 * No se levanta Spring ni servidor: se crea el servicio con "new", por eso son las pruebas
 * mas rapidas de la suite (milisegundos). Se agrupan por comportamiento con @Nested.
 */
@Tag("unit")
@ExtendWith(ReporteResultados.class)
@DisplayName("Unitarias - UsuarioService")
class UsuarioServiceTest {

    private UsuarioService servicio;

    @BeforeEach
    void crearServicioNuevo() {
        servicio = new UsuarioService();
    }

    @Nested
    @DisplayName("Al crear un usuario")
    class Crear {

        @Test
        @DisplayName("asigna ids consecutivos empezando en 1")
        void asignaIdsConsecutivos() {
            Usuario primero = servicio.crear(new UsuarioRequest("Ana Perez", "ana@correo.com", 25));
            Usuario segundo = servicio.crear(new UsuarioRequest("Luis Gomez", "luis@correo.com", 30));

            assertThat(primero.id()).isEqualTo(1L);
            assertThat(segundo.id()).isEqualTo(2L);
        }

        @Test
        @DisplayName("rechaza un email repetido aunque cambie mayusculas/minusculas")
        void rechazaEmailDuplicadoSinImportarMayusculas() {
            servicio.crear(new UsuarioRequest("Ana Perez", "ana@correo.com", 25));

            assertThatThrownBy(() -> servicio.crear(new UsuarioRequest("Otra Ana", "ANA@Correo.com", 40)))
                    .isInstanceOf(EmailDuplicadoException.class)
                    .hasMessageContaining("ANA@Correo.com");
            assertThat(servicio.listar()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Al consultar usuarios")
    class Consultar {

        @Test
        @DisplayName("devuelve una lista vacia cuando no hay usuarios")
        void listaVacia() {
            assertThat(servicio.listar()).isEmpty();
        }

        @Test
        @DisplayName("encuentra un usuario existente por su id")
        void buscaPorId() {
            Usuario creado = servicio.crear(new UsuarioRequest("Ana Perez", "ana@correo.com", 25));

            assertThat(servicio.buscarPorId(creado.id())).isEqualTo(creado);
        }

        @Test
        @DisplayName("lanza UsuarioNoEncontradoException si el id no existe")
        void idInexistente() {
            assertThatThrownBy(() -> servicio.buscarPorId(99L))
                    .isInstanceOf(UsuarioNoEncontradoException.class)
                    .hasMessageContaining("99");
        }
    }

    @Test
    @DisplayName("limpiar() borra los usuarios y reinicia la secuencia de ids")
    void limpiarReiniciaTodo() {
        servicio.crear(new UsuarioRequest("Ana Perez", "ana@correo.com", 25));

        servicio.limpiar();
        Usuario nuevo = servicio.crear(new UsuarioRequest("Luis Gomez", "luis@correo.com", 30));

        assertThat(servicio.listar()).hasSize(1);
        assertThat(nuevo.id()).isEqualTo(1L);
    }
}
