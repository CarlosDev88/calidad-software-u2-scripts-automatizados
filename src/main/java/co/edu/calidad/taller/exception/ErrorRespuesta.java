package co.edu.calidad.taller.exception;

import java.util.Map;

/**
 * Formato unico de error que devuelve la API.
 * Ejemplo: {"estado":400,"mensaje":"Datos invalidos","errores":{"email":"El email no tiene un formato valido"}}
 */
public record ErrorRespuesta(int estado, String mensaje, Map<String, String> errores) {
}
