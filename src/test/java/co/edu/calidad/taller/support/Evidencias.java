package co.edu.calidad.taller.support;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Guarda las evidencias de las pruebas (capturas de pantalla y resultados).
 * Carpeta por defecto: ./evidencias  (se puede cambiar con la variable EVIDENCIAS_DIR).
 */
public final class Evidencias {

    private Evidencias() {
    }

    public static Path directorio() {
        Path dir = Path.of(System.getenv().getOrDefault("EVIDENCIAS_DIR", "evidencias"));
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo crear la carpeta de evidencias " + dir, e);
        }
        return dir;
    }

    /** Toma una captura del navegador y la guarda como evidencias/capturas/<nombre>.png */
    public static Path capturar(WebDriver driver, String nombre) {
        try {
            Path carpeta = directorio().resolve("capturas");
            Files.createDirectories(carpeta);
            File temporal = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path destino = carpeta.resolve(nombreSeguro(nombre) + ".png");
            Files.copy(temporal.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);
            return destino;
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo guardar la captura " + nombre, e);
        }
    }

    /** Quita espacios, tildes y simbolos para que el nombre sirva en cualquier sistema operativo. */
    static String nombreSeguro(String nombre) {
        return nombre.trim().replaceAll("[^a-zA-Z0-9_-]+", "_");
    }
}
