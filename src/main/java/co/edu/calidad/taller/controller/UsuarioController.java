package co.edu.calidad.taller.controller;

import co.edu.calidad.taller.model.Usuario;
import co.edu.calidad.taller.model.UsuarioRequest;
import co.edu.calidad.taller.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * Endpoints REST que se prueban con TestRestTemplate y Postman.
 *
 * GET  /api/usuarios       -> 200 lista de usuarios
 * GET  /api/usuarios/{id}  -> 200 usuario | 404 no existe
 * POST /api/usuarios       -> 201 creado | 400 datos invalidos | 409 email duplicado
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    public Usuario buscarPorId(@PathVariable Long id) {
        return usuarioService.buscarPorId(id);
    }

    @PostMapping
    public ResponseEntity<Usuario> crear(@Valid @RequestBody UsuarioRequest request) {
        Usuario creado = usuarioService.crear(request);
        return ResponseEntity.created(URI.create("/api/usuarios/" + creado.id())).body(creado);
    }
}
