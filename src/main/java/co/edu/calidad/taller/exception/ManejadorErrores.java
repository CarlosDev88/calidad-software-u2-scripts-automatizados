package co.edu.calidad.taller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Convierte las excepciones en respuestas HTTP claras (manejo centralizado de errores).
 */
@RestControllerAdvice
public class ManejadorErrores {

    /** 400: alguna validacion de UsuarioRequest fallo. Devuelve el error de cada campo. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRespuesta> datosInvalidos(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            // Un campo puede fallar varias reglas a la vez (ej: "" falla @NotBlank y @Size).
            // Se muestra un solo mensaje por campo y tiene prioridad el de "obligatorio",
            // asi la respuesta siempre es la misma (las pruebas no dependen del orden).
            boolean esObligatorio = "NotBlank".equals(error.getCode()) || "NotNull".equals(error.getCode());
            if (esObligatorio || !errores.containsKey(error.getField())) {
                errores.put(error.getField(), error.getDefaultMessage());
            }
        }
        return respuesta(HttpStatus.BAD_REQUEST, "Datos invalidos", errores);
    }

    /**
     * 400: el cuerpo no es un JSON valido o un campo tiene un tipo incorrecto
     * (por ejemplo edad 18.9 o "25": ver application.properties, ya no se convierten en silencio).
     */
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorRespuesta> peticionMalFormada(Exception ex) {
        return respuesta(HttpStatus.BAD_REQUEST,
                "La peticion esta mal formada o tiene un tipo de dato invalido (la edad debe ser un numero entero)", Map.of());
    }

    /** 405: metodo HTTP no soportado (ej: DELETE). Antes respondia con el formato por defecto de Spring. */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorRespuesta> metodoNoSoportado(HttpRequestMethodNotSupportedException ex) {
        return respuesta(HttpStatus.METHOD_NOT_ALLOWED, "Metodo " + ex.getMethod() + " no permitido en esta ruta", Map.of());
    }

    /** 415: el cuerpo no es JSON (ej: text/plain). Antes respondia con el formato por defecto de Spring. */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorRespuesta> tipoNoSoportado(HttpMediaTypeNotSupportedException ex) {
        return respuesta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Tipo de contenido no soportado: use application/json", Map.of());
    }

    /** 404: el usuario buscado no existe. */
    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ErrorRespuesta> noEncontrado(UsuarioNoEncontradoException ex) {
        return respuesta(HttpStatus.NOT_FOUND, ex.getMessage(), Map.of());
    }

    /** 409: el email ya esta registrado. */
    @ExceptionHandler(EmailDuplicadoException.class)
    public ResponseEntity<ErrorRespuesta> emailDuplicado(EmailDuplicadoException ex) {
        return respuesta(HttpStatus.CONFLICT, ex.getMessage(), Map.of("email", "Este email ya esta registrado"));
    }

    private ResponseEntity<ErrorRespuesta> respuesta(HttpStatus estado, String mensaje, Map<String, String> errores) {
        return ResponseEntity.status(estado).body(new ErrorRespuesta(estado.value(), mensaje, errores));
    }
}
