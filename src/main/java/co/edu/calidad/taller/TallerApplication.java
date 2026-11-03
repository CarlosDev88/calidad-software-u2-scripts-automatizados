package co.edu.calidad.taller;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicacion bajo prueba: API REST de usuarios + formulario web de registro.
 * El formulario queda disponible en http://localhost:8080/registro.html
 */
@SpringBootApplication
public class TallerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TallerApplication.class, args);
    }
}
