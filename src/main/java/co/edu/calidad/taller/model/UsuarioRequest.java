package co.edu.calidad.taller.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Datos de entrada para registrar un usuario.
 * Las reglas de validacion son las que verifican las pruebas automatizadas.
 */
public record UsuarioRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, max = 60, message = "El nombre debe tener entre 3 y 60 caracteres")
        String nombre,

        @NotBlank(message = "El email es obligatorio")
        @Email(regexp = "^[\\w.+-]+@[\\w-]+\\.[\\w.]+$", message = "El email no tiene un formato valido")
        String email,

        @NotNull(message = "La edad es obligatoria")
        @Min(value = 18, message = "La edad minima es 18")
        @Max(value = 100, message = "La edad maxima es 100")
        Integer edad
) {
}
