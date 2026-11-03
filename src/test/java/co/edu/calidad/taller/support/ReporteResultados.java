package co.edu.calidad.taller.support;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Extension de JUnit 5 que registra el resultado de cada prueba (OK / FALLO)
 * en la consola y en evidencias/resultados-pruebas.txt, para usarlo como evidencia en el informe.
 */
public class ReporteResultados implements TestWatcher {

    private static boolean archivoIniciado = false;

    @Override
    public void testSuccessful(ExtensionContext contexto) {
        registrar("OK   ", contexto, null);
    }

    @Override
    public void testFailed(ExtensionContext contexto, Throwable causa) {
        registrar("FALLO", contexto, causa);
    }

    @Override
    public void testAborted(ExtensionContext contexto, Throwable causa) {
        registrar("ABORT", contexto, causa);
    }

    @Override
    public void testDisabled(ExtensionContext contexto, Optional<String> razon) {
        registrar("OMIT ", contexto, null);
    }

    private static synchronized void registrar(String estado, ExtensionContext contexto, Throwable causa) {
        String clase = contexto.getTestClass().map(Class::getSimpleName).orElse("?");
        String linea = String.format("[%s] %-24s | %s", estado, clase, nombrePrueba(contexto));
        if (causa != null) {
            linea += System.lineSeparator() + "         motivo: " + causa.getMessage();
        }
        System.out.println(linea);
        escribir(linea + System.lineSeparator());
    }

    /** En pruebas parametrizadas incluye el nombre del metodo y el caso: "metodo > caso". */
    private static String nombrePrueba(ExtensionContext contexto) {
        Optional<ExtensionContext> padre = contexto.getParent();
        boolean esCasoParametrizado = padre.isPresent() && padre.get().getTestMethod().isPresent();
        return esCasoParametrizado
                ? padre.get().getDisplayName() + " > " + contexto.getDisplayName()
                : contexto.getDisplayName();
    }

    private static void escribir(String texto) {
        Path archivo = Evidencias.directorio().resolve("resultados-pruebas.txt");
        try {
            if (!archivoIniciado) {
                String encabezado = "Resultados de pruebas automatizadas - "
                        + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                        + System.lineSeparator() + "=".repeat(70) + System.lineSeparator();
                Files.writeString(archivo, encabezado, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                archivoIniciado = true;
            }
            Files.writeString(archivo, texto, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo escribir el reporte de resultados", e);
        }
    }
}
