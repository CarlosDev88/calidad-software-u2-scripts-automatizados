package co.edu.calidad.taller.api;

import co.edu.calidad.taller.exception.ErrorRespuesta;
import co.edu.calidad.taller.model.Usuario;
import co.edu.calidad.taller.model.UsuarioRequest;
import co.edu.calidad.taller.service.UsuarioService;
import co.edu.calidad.taller.support.DatosPrueba;
import co.edu.calidad.taller.support.ReporteResultados;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Script de prueba automatizado para la API REST /api/usuarios usando TestRestTemplate.
 *
 * Cada prueba sigue el patron AAA: Arrange (datos de entrada), Act (llamada HTTP), Assert (aserciones).
 * La app se levanta en un puerto aleatorio para que las pruebas sean independientes del entorno.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("api")
@ExtendWith(ReporteResultados.class)
@DisplayName("API REST /api/usuarios")
class UsuarioApiTest {

    private static final String URL = "/api/usuarios";

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private UsuarioService usuarioService;

    @BeforeEach
    void empezarSinDatos() {
        // Cada prueba arranca con el almacenamiento vacio -> pruebas independientes y repetibles
        usuarioService.limpiar();
    }

    // ---------- Metodos reutilizables ----------

    private ResponseEntity<Usuario> crearUsuario(UsuarioRequest request) {
        return rest.postForEntity(URL, request, Usuario.class);
    }

    private ResponseEntity<ErrorRespuesta> crearUsuarioEsperandoError(UsuarioRequest request) {
        return rest.postForEntity(URL, request, ErrorRespuesta.class);
    }

    // ---------- Casos exitosos ----------

    @Test
    @DisplayName("POST con datos validos crea el usuario y responde 201")
    void crearUsuarioValido() {
        UsuarioRequest request = DatosPrueba.usuarioValido();

        ResponseEntity<Usuario> respuesta = crearUsuario(request);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().id()).isNotNull();
        assertThat(respuesta.getBody().nombre()).isEqualTo(request.nombre());
        assertThat(respuesta.getBody().email()).isEqualTo(request.email());
        assertThat(respuesta.getHeaders().getLocation())
                .hasToString(URL + "/" + respuesta.getBody().id());
    }

    @Test
    @DisplayName("GET lista todos los usuarios y responde 200")
    void listarUsuarios() {
        crearUsuario(DatosPrueba.usuarioValido());
        crearUsuario(DatosPrueba.usuarioValido());

        ResponseEntity<Usuario[]> respuesta = rest.getForEntity(URL, Usuario[].class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).hasSize(2);
    }

    @Test
    @DisplayName("GET por id devuelve el usuario creado y responde 200")
    void buscarUsuarioPorId() {
        Usuario creado = crearUsuario(DatosPrueba.usuarioValido()).getBody();
        assertThat(creado).isNotNull();

        ResponseEntity<Usuario> respuesta = rest.getForEntity(URL + "/" + creado.id(), Usuario.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(respuesta.getBody()).isEqualTo(creado);
    }

    // ---------- Casos de error ----------

    @Test
    @DisplayName("GET por id inexistente responde 404")
    void buscarUsuarioInexistente() {
        ResponseEntity<ErrorRespuesta> respuesta = rest.getForEntity(URL + "/999", ErrorRespuesta.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().mensaje()).contains("999");
    }

    /** Datos de entrada invalidos: (descripcion del caso, request, campo que debe reportar el error). */
    static Stream<Arguments> datosInvalidos() {
        return Stream.of(
                Arguments.of("Nombre vacio", new UsuarioRequest("", DatosPrueba.emailUnico("a"), 25), "nombre"),
                Arguments.of("Nombre muy corto", new UsuarioRequest("Al", DatosPrueba.emailUnico("a"), 25), "nombre"),
                Arguments.of("Nombre corto rodeado de espacios", new UsuarioRequest("  A  ", DatosPrueba.emailUnico("a"), 25), "nombre"),
                Arguments.of("Nombre solo con espacios", new UsuarioRequest("   ", DatosPrueba.emailUnico("a"), 25), "nombre"),
                Arguments.of("Email con formato invalido", new UsuarioRequest("Ana Perez", "ana-correo", 25), "email"),
                Arguments.of("Email vacio", new UsuarioRequest("Ana Perez", "", 25), "email"),
                Arguments.of("Edad menor a 18", new UsuarioRequest("Ana Perez", DatosPrueba.emailUnico("a"), 15), "edad"),
                Arguments.of("Edad mayor a 100", new UsuarioRequest("Ana Perez", DatosPrueba.emailUnico("a"), 120), "edad"),
                Arguments.of("Edad nula", new UsuarioRequest("Ana Perez", DatosPrueba.emailUnico("a"), null), "edad"),
                Arguments.of("Edad negativa", new UsuarioRequest("Ana Perez", DatosPrueba.emailUnico("a"), -1), "edad")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("datosInvalidos")
    @DisplayName("POST con datos invalidos responde 400 indicando el campo")
    void rechazarDatosInvalidos(String caso, UsuarioRequest request, String campoConError) {
        ResponseEntity<ErrorRespuesta> respuesta = crearUsuarioEsperandoError(request);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().errores()).containsKey(campoConError);
        // El usuario invalido no debe quedar guardado
        assertThat(usuarioService.listar()).isEmpty();
    }

    @Test
    @DisplayName("POST con email ya registrado responde 409")
    void rechazarEmailDuplicado() {
        String email = DatosPrueba.emailUnico("duplicado");
        crearUsuario(DatosPrueba.usuarioValidoConEmail(email));

        ResponseEntity<ErrorRespuesta> respuesta = crearUsuarioEsperandoError(DatosPrueba.usuarioValidoConEmail(email));

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().errores()).containsKey("email");
        assertThat(usuarioService.listar()).hasSize(1);
    }

    /** Edad con un tipo de dato que no es un entero: no se debe aceptar ni redondear en silencio. */
    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "Edad decimal (18.9)        | {\"nombre\": \"Ana Perez\", \"email\": \"ana.decimal@correo.com\", \"edad\": 18.9}",
            "Edad como texto (\"25\")   | {\"nombre\": \"Ana Perez\", \"email\": \"ana.texto@correo.com\", \"edad\": \"25\"}"
    })
    @DisplayName("POST con edad que no es un numero entero responde 400")
    void rechazarEdadNoEntera(String caso, String json) {
        ResponseEntity<ErrorRespuesta> respuesta = rest.postForEntity(URL, jsonConCabecera(json), ErrorRespuesta.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(usuarioService.listar()).isEmpty();
    }

    @Test
    @DisplayName("Metodo HTTP no soportado responde 405 con el formato de error de la API")
    void metodoNoSoportado() {
        ResponseEntity<ErrorRespuesta> respuesta =
                rest.exchange(URL + "/1", HttpMethod.DELETE, null, ErrorRespuesta.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().estado()).isEqualTo(405);
        assertThat(respuesta.getBody().mensaje()).contains("DELETE");
    }

    @Test
    @DisplayName("Content-Type distinto de JSON responde 415 con el formato de error de la API")
    void tipoDeContenidoNoSoportado() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        ResponseEntity<ErrorRespuesta> respuesta =
                rest.postForEntity(URL, new HttpEntity<>("nombre=Ana", headers), ErrorRespuesta.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().estado()).isEqualTo(415);
    }

    private HttpEntity<String> jsonConCabecera(String json) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(json, headers);
    }

    @Test
    @DisplayName("POST con JSON mal formado responde 400")
    void rechazarJsonMalFormado() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> peticion = new HttpEntity<>("{\"nombre\": \"Ana\", ", headers);

        ResponseEntity<ErrorRespuesta> respuesta = rest.postForEntity(URL, peticion, ErrorRespuesta.class);

        assertThat(respuesta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(respuesta.getBody()).isNotNull();
        assertThat(respuesta.getBody().mensaje()).contains("mal formada");
    }
}
