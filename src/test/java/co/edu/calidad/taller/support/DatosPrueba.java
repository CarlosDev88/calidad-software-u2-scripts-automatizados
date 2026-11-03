package co.edu.calidad.taller.support;

import co.edu.calidad.taller.model.UsuarioRequest;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Datos de entrada reutilizables para todas las pruebas (API y formulario).
 * Centralizarlos evita repetir valores "magicos" en cada test.
 */
public final class DatosPrueba {

    public static final String NOMBRE_VALIDO = "Ana Perez";
    public static final int EDAD_VALIDA = 25;

    private static final AtomicInteger contador = new AtomicInteger();

    private DatosPrueba() {
    }

    /** Genera un email distinto en cada llamada para que las pruebas no choquen entre si. */
    public static String emailUnico(String prefijo) {
        return prefijo + "." + contador.incrementAndGet() + "." + System.currentTimeMillis() + "@correo.com";
    }

    /** Usuario que cumple todas las reglas de validacion. */
    public static UsuarioRequest usuarioValido() {
        return new UsuarioRequest(NOMBRE_VALIDO, emailUnico("ana"), EDAD_VALIDA);
    }

    public static UsuarioRequest usuarioValidoConEmail(String email) {
        return new UsuarioRequest(NOMBRE_VALIDO, email, EDAD_VALIDA);
    }
}
