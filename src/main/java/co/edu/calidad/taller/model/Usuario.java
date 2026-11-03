package co.edu.calidad.taller.model;

/**
 * Usuario registrado (lo que responde la API).
 */
public record Usuario(Long id, String nombre, String email, Integer edad) {
}
