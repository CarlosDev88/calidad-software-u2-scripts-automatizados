package co.edu.calidad.taller.web.support;

import org.openqa.selenium.WebDriver;

/**
 * La implementan las clases de prueba que usan navegador,
 * para que la extension CapturaEnFallo pueda tomar la captura.
 */
public interface ConNavegador {

    WebDriver getDriver();
}
