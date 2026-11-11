package co.edu.calidad.taller.support;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

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
            // Se escriben los bytes directamente: copiar el archivo temporal de Selenium conservaba
            // permisos de solo-dueno (600) y en GitHub Actions no se podian subir como artefacto.
            byte[] imagen = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Path destino = carpeta.resolve(nombreSeguro(nombre) + ".png");
            Files.write(destino, imagen);
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
