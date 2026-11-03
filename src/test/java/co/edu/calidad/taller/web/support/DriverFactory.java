package co.edu.calidad.taller.web.support;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

/**
 * Crea el navegador para las pruebas.
 *
 * - Si existe la variable SELENIUM_REMOTE_URL (caso Docker) usa el Chrome del contenedor selenium/standalone-chrome.
 * - Si no existe, abre un Chrome local (headless por defecto; HEADLESS=false para verlo).
 */
public final class DriverFactory {

    private static final int INTENTOS_CONEXION = 10;
    private static final long ESPERA_ENTRE_INTENTOS_MS = 3000;

    private DriverFactory() {
    }

    public static WebDriver crear() {
        ChromeOptions opciones = new ChromeOptions();
        opciones.addArguments("--window-size=1280,900", "--no-sandbox", "--disable-dev-shm-usage");

        String urlRemota = System.getenv("SELENIUM_REMOTE_URL");
        if (urlRemota == null || urlRemota.isBlank()) {
            if (!"false".equalsIgnoreCase(System.getenv("HEADLESS"))) {
                opciones.addArguments("--headless=new");
            }
            return new ChromeDriver(opciones);
        }
        return crearRemoto(urlRemota, opciones);
    }

    /** URL base de la app vista desde el navegador. En Docker el host es el nombre del servicio (APP_HOST=tests). */
    public static String urlBase(int puerto) {
        String host = System.getenv().getOrDefault("APP_HOST", "localhost");
        return "http://" + host + ":" + puerto;
    }

    /** El contenedor de Selenium puede tardar unos segundos en estar listo: se reintenta la conexion. */
    private static WebDriver crearRemoto(String urlRemota, ChromeOptions opciones) {
        URL url = convertirUrl(urlRemota);
        WebDriverException ultimoError = null;
        for (int intento = 1; intento <= INTENTOS_CONEXION; intento++) {
            try {
                return new RemoteWebDriver(url, opciones);
            } catch (WebDriverException e) {
                ultimoError = e;
                System.out.printf("Selenium aun no esta listo (intento %d/%d)...%n", intento, INTENTOS_CONEXION);
                dormir();
            }
        }
        throw new IllegalStateException("No se pudo conectar con Selenium en " + urlRemota, ultimoError);
    }

    private static URL convertirUrl(String url) {
        try {
            return URI.create(url).toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new IllegalArgumentException("SELENIUM_REMOTE_URL no es una URL valida: " + url, e);
        }
    }

    private static void dormir() {
        try {
            Thread.sleep(ESPERA_ENTRE_INTENTOS_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
