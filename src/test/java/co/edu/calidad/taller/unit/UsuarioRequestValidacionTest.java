package co.edu.calidad.taller.unit;

import co.edu.calidad.taller.model.UsuarioRequest;
import co.edu.calidad.taller.support.ReporteResultados;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas unitarias de las reglas de validacion de UsuarioRequest.
 *
 * Se aplica la tecnica de caja negra "analisis de valores limite": para cada regla se prueba
 * el valor justo en el limite y el inmediatamente afuera (17/18, 100/101, 2/3 y 60/61 caracteres),
 * que es donde mas se equivocan los programadores (errores de < vs <=).
 */
@Tag("unit")
@ExtendWith(ReporteResultados.class)
@DisplayName("Unitarias - Reglas de validacion (valores limite)")
class UsuarioRequestValidacionTest {

    private static ValidatorFactory fabrica;
    private static Validator validador;

    @BeforeAll
    static void crearValidador() {
        fabrica = Validation.buildDefaultValidatorFactory();
        validador = fabrica.getValidator();
    }

    @AfterAll
    static void cerrarValidador() {
        fabrica.close();
    }

    /** Devuelve los nombres de los campos que fallaron la validacion. */
    private Set<String> camposInvalidos(UsuarioRequest request) {
        return validador.validate(request).stream()
                .map(ConstraintViolation::getPropertyPath)
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    @Test
    @DisplayName("Un usuario con todos los datos correctos no tiene errores")
    void usuarioValidoSinErrores() {
        assertThat(camposInvalidos(new UsuarioRequest("Ana Perez", "ana@correo.com", 25))).isEmpty();
    }

    @ParameterizedTest(name = "edad {0} -> valida: {1}")
    @CsvSource({
            "-1,  false",   // particion: edad negativa
            "17,  false",   // justo debajo del minimo
            "18,  true",    // minimo
            "100, true",    // maximo
            "101, false"    // justo encima del maximo
    })
    @DisplayName("Limites de la edad (18 a 100)")
    void limitesEdad(int edad, boolean esValida) {
        Set<String> errores = camposInvalidos(new UsuarioRequest("Ana Perez", "ana@correo.com", edad));

        assertThat(errores.contains("edad")).isEqualTo(!esValida);
    }

    @ParameterizedTest(name = "nombre de {0} caracteres -> valido: {1}")
    @CsvSource({
            "2,  false",
            "3,  true",
            "60, true",
            "61, false"
    })
    @DisplayName("Limites del largo del nombre (3 a 60 caracteres)")
    void limitesNombre(int largo, boolean esValido) {
        String nombre = "a".repeat(largo);

        Set<String> errores = camposInvalidos(new UsuarioRequest(nombre, "ana@correo.com", 25));

        assertThat(errores.contains("nombre")).isEqualTo(!esValido);
    }

    @ParameterizedTest(name = "email \"{0}\" -> valido: {1}")
    @CsvSource({
            "ana@correo.com,      true",
            "ana.perez+1@uni.edu.co, true",
            "ana-correo,          false",
            "ana@,                false",
            "@correo.com,         false",
            "ana@correo,          false"
    })
    @DisplayName("Particiones de equivalencia del email")
    void formatosEmail(String email, boolean esValido) {
        Set<String> errores = camposInvalidos(new UsuarioRequest("Ana Perez", email, 25));

        assertThat(errores.contains("email")).isEqualTo(!esValido);
    }

    @ParameterizedTest(name = "nombre \"{0}\" -> invalido")
    @ValueSource(strings = {"", "   ", "  A  ", " Al "})
    @DisplayName("Nombre vacio, solo espacios o corto rodeado de espacios es invalido")
    void nombreConEspaciosSeValidaRecortado(String nombre) {
        assertThat(camposInvalidos(new UsuarioRequest(nombre, "ana@correo.com", 25))).contains("nombre");
    }

    @Test
    @DisplayName("Los espacios al inicio y al final se quitan antes de validar")
    void recortaEspaciosAntesDeValidar() {
        UsuarioRequest request = new UsuarioRequest("  Ana Perez  ", " ana@correo.com ", 25);

        assertThat(request.nombre()).isEqualTo("Ana Perez");
        assertThat(request.email()).isEqualTo("ana@correo.com");
        assertThat(camposInvalidos(request)).isEmpty();
    }

    @Test
    @DisplayName("Campos nulos generan error de obligatorio en los tres campos")
    void camposNulos() {
        assertThat(camposInvalidos(new UsuarioRequest(null, null, null)))
                .containsExactlyInAnyOrder("nombre", "email", "edad");
    }
}
