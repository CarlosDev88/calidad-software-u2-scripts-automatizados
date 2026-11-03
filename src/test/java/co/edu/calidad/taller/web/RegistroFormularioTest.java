package co.edu.calidad.taller.web;

import co.edu.calidad.taller.support.DatosPrueba;
import co.edu.calidad.taller.support.Evidencias;
import co.edu.calidad.taller.support.ReporteResultados;
import co.edu.calidad.taller.web.pages.RegistroPage;
import co.edu.calidad.taller.web.support.CapturaEnFallo;
import co.edu.calidad.taller.web.support.ConNavegador;
import co.edu.calidad.taller.web.support.DriverFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.WebDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Script de prueba automatizado para el formulario web de registro usando Selenium WebDriver.
 *
 * Se levanta la app real en un puerto aleatorio y el navegador llena el formulario como lo haria un usuario.
 * Cada caso guarda capturas en evidencias/capturas (datos de entrada y resultado).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith({ReporteResultados.class, CapturaEnFallo.class})
@DisplayName("Formulario web de registro (Selenium)")
class RegistroFormularioTest implements ConNavegador {

    @LocalServerPort
    private int puerto;

    private WebDriver driver;
    private RegistroPage pagina;

    @BeforeEach
    void abrirNavegador() {
        driver = DriverFactory.crear();
        pagina = new RegistroPage(driver, DriverFactory.urlBase(puerto)).abrir();
    }

    @AfterEach
    void cerrarNavegador() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Override
    public WebDriver getDriver() {
        return driver;
    }

    @Test
    @DisplayName("Registro con datos validos muestra mensaje de exito y limpia el formulario")
    void registroExitoso() {
        pagina.llenarFormulario(DatosPrueba.NOMBRE_VALIDO, DatosPrueba.emailUnico("ana"), "25");
        Evidencias.capturar(driver, "form_01_registro_exitoso_datos");

        pagina.enviar();
        String mensaje = pagina.esperarMensajeExito();
        Evidencias.capturar(driver, "form_01_registro_exitoso_resultado");

        assertThat(mensaje).contains("Usuario registrado con exito");
        assertThat(pagina.valorDe("nombre")).isEmpty();
        assertThat(pagina.valorDe("email")).isEmpty();
    }

    /**
     * Datos de entrada invalidos. Columnas: caso, nombre, email, edad, campo con error, texto esperado.
     * '' = campo vacio.
     */
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "Email con formato invalido, Ana Perez, ana-correo,     25,  email,  formato valido",
            "Nombre vacio,               '',        ana@correo.com, 25,  nombre, obligatorio",
            "Nombre muy corto,           Al,        ana@correo.com, 25,  nombre, entre 3 y 60",
            "Edad menor a 18,            Ana Perez, ana@correo.com, 15,  edad,   minima es 18",
            "Edad mayor a 100,           Ana Perez, ana@correo.com, 120, edad,   maxima es 100"
    })
    @DisplayName("Datos invalidos muestran el error en el campo correspondiente")
    void muestraErrorConDatosInvalidos(String caso, String nombre, String email, String edad,
                                       String campoConError, String textoEsperado) {
        pagina.llenarFormulario(nombre, email, edad);
        Evidencias.capturar(driver, "form_02_" + caso + "_datos");

        pagina.enviar();
        String error = pagina.esperarErrorDe(campoConError);
        Evidencias.capturar(driver, "form_02_" + caso + "_resultado");

        assertThat(error).contains(textoEsperado);
        assertThat(pagina.esperarMensajeError()).contains("Datos invalidos");
    }

    @Test
    @DisplayName("Enviar el formulario vacio muestra error en los tres campos")
    void formularioVacio() {
        pagina.enviar();
        pagina.esperarErrorDe("nombre");
        Evidencias.capturar(driver, "form_03_formulario_vacio");

        assertThat(pagina.errorDe("nombre")).contains("obligatorio");
        assertThat(pagina.errorDe("email")).contains("obligatorio");
        assertThat(pagina.errorDe("edad")).contains("obligatoria");
    }

    @Test
    @DisplayName("Registrar un email que ya existe muestra error de duplicado")
    void emailDuplicado() {
        String email = DatosPrueba.emailUnico("repetido");
        pagina.registrar(DatosPrueba.NOMBRE_VALIDO, email, "30");
        pagina.esperarMensajeExito();

        pagina.registrar("Otro Usuario", email, "40");
        String error = pagina.esperarErrorDe("email");
        Evidencias.capturar(driver, "form_04_email_duplicado");

        assertThat(error).contains("ya esta registrado");
    }
}
