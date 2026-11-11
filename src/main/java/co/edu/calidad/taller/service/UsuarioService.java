package co.edu.calidad.taller.service;

import co.edu.calidad.taller.exception.EmailDuplicadoException;
import co.edu.calidad.taller.exception.UsuarioNoEncontradoException;
import co.edu.calidad.taller.model.Usuario;
import co.edu.calidad.taller.model.UsuarioRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Logica de negocio. Guarda los usuarios en memoria (no se necesita base de datos para el taller).
 */
@Service
public class UsuarioService {

    private final Map<Long, Usuario> usuarios = new ConcurrentHashMap<>();
    private final AtomicLong secuencia = new AtomicLong(0);

    public List<Usuario> listar() {
        return new ArrayList<>(usuarios.values());
    }

    public Usuario buscarPorId(Long id) {
        Usuario usuario = usuarios.get(id);
        if (usuario == null) {
            throw new UsuarioNoEncontradoException(id);
        }
        return usuario;
    }

    public Usuario crear(UsuarioRequest request) {
        // Regla de negocio: no se permiten dos usuarios con el mismo email
        boolean existe = usuarios.values().stream()
                .anyMatch(u -> u.email().equalsIgnoreCase(request.email()));
        if (existe) {
            throw new EmailDuplicadoException(request.email());
        }
        Long id = secuencia.incrementAndGet();
        Usuario nuevo = new Usuario(id, request.nombre(), request.email(), request.edad());
        usuarios.put(id, nuevo);
        return nuevo;
    }

    /** Deja el almacenamiento vacio. Lo usan las pruebas para empezar siempre desde cero. */
    public void limpiar() {
        usuarios.clear();
        secuencia.set(0);
    }
}
