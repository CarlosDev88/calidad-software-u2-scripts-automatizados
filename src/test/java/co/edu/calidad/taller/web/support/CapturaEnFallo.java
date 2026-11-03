package co.edu.calidad.taller.web.support;

import co.edu.calidad.taller.support.Evidencias;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.WebDriver;

/**
 * Extension de JUnit 5: si una prueba de navegador falla, guarda una captura
 * (evidencias/capturas/FALLO_<prueba>.png) ANTES de que se cierre el navegador.
 */
public class CapturaEnFallo implements AfterTestExecutionCallback {

    @Override
    public void afterTestExecution(ExtensionContext contexto) {
        if (contexto.getExecutionException().isEmpty()) {
            return;
        }
        Object prueba = contexto.getRequiredTestInstance();
        if (prueba instanceof ConNavegador conNavegador) {
            WebDriver driver = conNavegador.getDriver();
            if (driver != null) {
                try {
                    Evidencias.capturar(driver, "FALLO_" + contexto.getDisplayName());
                } catch (RuntimeException e) {
                    // La captura es un apoyo: si falla no debe ocultar el error original de la prueba
                    System.out.println("No se pudo guardar la captura del fallo: " + e.getMessage());
                }
            }
        }
    }
}
