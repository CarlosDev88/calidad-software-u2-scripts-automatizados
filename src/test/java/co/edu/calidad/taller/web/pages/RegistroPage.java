package co.edu.calidad.taller.web.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Page Object del formulario de registro.
 *
 * Toda la interaccion con la pagina (localizadores, clics, esperas) vive aqui.
 * Las pruebas solo llaman metodos con nombres de negocio, asi si cambia el HTML
 * se corrige en un solo lugar (reutilizacion y mantenibilidad).
 */
public class RegistroPage {

    private static final By CAMPO_NOMBRE = By.id("nombre");
    private static final By CAMPO_EMAIL = By.id("email");
    private static final By CAMPO_EDAD = By.id("edad");
    private static final By BOTON_REGISTRAR = By.id("btn-registrar");
    private static final By MENSAJE_EXITO = By.id("mensaje-exito");
    private static final By MENSAJE_ERROR = By.id("mensaje-error");

    private final WebDriver driver;
    private final WebDriverWait espera;
    private final String urlBase;

    public RegistroPage(WebDriver driver, String urlBase) {
        this.driver = driver;
        this.urlBase = urlBase;
        // Esperas explicitas: nunca Thread.sleep fijos en las pruebas
        this.espera = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    public RegistroPage abrir() {
        driver.get(urlBase + "/registro.html");
        espera.until(ExpectedConditions.elementToBeClickable(BOTON_REGISTRAR));
        return this;
    }

    public RegistroPage llenarFormulario(String nombre, String email, String edad) {
        escribir(CAMPO_NOMBRE, nombre);
        escribir(CAMPO_EMAIL, email);
        escribir(CAMPO_EDAD, edad);
        return this;
    }

    public RegistroPage enviar() {
        driver.findElement(BOTON_REGISTRAR).click();
        return this;
    }

    public RegistroPage registrar(String nombre, String email, String edad) {
        return llenarFormulario(nombre, email, edad).enviar();
    }

    /** Espera a que aparezca el mensaje verde de exito y devuelve su texto. */
    public String esperarMensajeExito() {
        return espera.until(ExpectedConditions.visibilityOfElementLocated(MENSAJE_EXITO)).getText();
    }

    /** Espera a que aparezca el mensaje rojo general de error y devuelve su texto. */
    public String esperarMensajeError() {
        return espera.until(ExpectedConditions.visibilityOfElementLocated(MENSAJE_ERROR)).getText();
    }

    /** Espera a que el campo indicado (nombre, email o edad) muestre su mensaje de error. */
    public String esperarErrorDe(String campo) {
        By errorDelCampo = By.id("error-" + campo);
        espera.until(d -> !d.findElement(errorDelCampo).getText().isBlank());
        return driver.findElement(errorDelCampo).getText();
    }

    /** Texto del error de un campo sin esperar (puede estar vacio). */
    public String errorDe(String campo) {
        return driver.findElement(By.id("error-" + campo)).getText();
    }

    public String valorDe(String campo) {
        return driver.findElement(By.id(campo)).getDomProperty("value");
    }

    private void escribir(By localizador, String valor) {
        WebElement campo = driver.findElement(localizador);
        campo.clear();
        if (valor != null && !valor.isEmpty()) {
            campo.sendKeys(valor);
        }
    }
}
