# Taller: scripts de prueba automatizados para servicios REST y formularios web

**Asignatura:** Prueba y Calidad de Software — Unidad 2, AA1
**Estudiante:** Carlos Rueda

Proyecto con una aplicación pequeña (API REST de usuarios + formulario web de registro) y sus scripts de prueba automatizados:

| Área | Herramienta | Archivo principal |
|---|---|---|
| API REST | JUnit 5 + **TestRestTemplate** | `src/test/java/.../api/UsuarioApiTest.java` |
| API REST | **Postman** (se corre con Newman) | `postman/usuarios.postman_collection.json` |
| Formulario web | JUnit 5 + **Selenium WebDriver** | `src/test/java/.../web/RegistroFormularioTest.java` |

Todo corre en **Docker** con versiones fijas, así funciona igual en cualquier máquina sin instalar Java, Maven ni Chrome.

## Versiones

| Componente | Versión |
|---|---|
| Java | 21 (Eclipse Temurin) |
| Maven | 3.9.9 |
| Spring Boot | 3.3.5 |
| JUnit | 5 (incluido en Spring Boot) |
| Selenium | 4.25.0 |
| Chrome | imagen `selenium/standalone-chrome:4.25.0-20240922` |
| Newman | 6.1.3 (imagen `postman/newman:6.1.3-alpine`) |

## Requisito

Solo **Docker Desktop** encendido.

## Cómo correrlo

```bash
# 1. Pruebas de API (TestRestTemplate) y formulario (Selenium)
docker compose up --build --exit-code-from tests

# 2. Pruebas de la colección Postman (Newman)
docker compose --profile postman run --rm newman

# 3. (Opcional) Usar la app a mano: http://localhost:8080/registro.html
docker compose --profile demo up --build app

# Apagar todo
docker compose --profile demo --profile postman down
```

Mientras corren las pruebas de Selenium se puede ver el navegador en vivo en **http://localhost:7900** (clave: `secret`).

### Sin Docker (opcional)

Con Java 21, Maven y Chrome instalados: `mvn test`. Para ver el navegador: `HEADLESS=false mvn test`.

## Evidencias generadas

Al terminar, la carpeta `evidencias/` contiene:

| Archivo | Contenido |
|---|---|
| `resultados-pruebas.txt` | Lista de cada prueba con su resultado (OK / FALLO) |
| `salida-consola.txt` | Salida completa de Maven |
| `surefire-reports/` | Reportes de JUnit (txt y xml) |
| `capturas/*.png` | Capturas del formulario: datos de entrada y resultado de cada caso |
| `newman-resultados.xml` | Resultado de la colección Postman |

## La aplicación bajo prueba

| Método | Endpoint | Respuestas |
|---|---|---|
| GET | `/api/usuarios` | 200 lista |
| GET | `/api/usuarios/{id}` | 200 usuario · 404 no existe |
| POST | `/api/usuarios` | 201 creado · 400 datos inválidos · 409 email duplicado |

Reglas de validación: nombre obligatorio (3 a 60 caracteres), email obligatorio con formato válido, edad obligatoria entre 18 y 100, email único.

Formulario: `http://localhost:8080/registro.html`. Envía los datos a la API y muestra los errores debajo de cada campo.

## Casos de prueba

### API — `UsuarioApiTest` (TestRestTemplate)

| # | Caso | Entrada | Resultado esperado |
|---|---|---|---|
| 1 | Crear usuario válido | nombre, email, edad válidos | 201, id generado, header `Location` |
| 2 | Listar usuarios | 2 usuarios creados | 200, lista de 2 |
| 3 | Buscar por id | id existente | 200, mismo usuario |
| 4 | Buscar inexistente | id 999 | 404 |
| 5–11 | Datos inválidos (parametrizado) | nombre vacío / corto, email inválido / vacío, edad < 18 / > 100 / nula | 400 con el error en el campo correcto, nada guardado |
| 12 | Email duplicado | mismo email dos veces | 409 |
| 13 | JSON mal formado | cuerpo JSON roto | 400 |

### API — Colección Postman

7 peticiones en orden (crear → consultar → listar → errores 400 → 409 → 404), con pruebas en cada una y una prueba común a toda la colección (tiempo de respuesta < 2 s y respuesta JSON).

### Formulario — `RegistroFormularioTest` (Selenium)

| # | Caso | Entrada | Resultado esperado |
|---|---|---|---|
| 1 | Registro exitoso | datos válidos | mensaje de éxito y formulario limpio |
| 2–6 | Datos inválidos (parametrizado) | email inválido, nombre vacío, nombre corto, edad 15, edad 120 | mensaje de error bajo el campo correcto |
| 7 | Formulario vacío | nada | error en los 3 campos |
| 8 | Email duplicado | mismo email dos veces | "Este email ya está registrado" |

## Buenas prácticas aplicadas

- **Page Object Model** (`RegistroPage`): los localizadores y acciones del formulario están en un solo lugar.
- **Datos de prueba centralizados** (`DatosPrueba`) y **pruebas parametrizadas** (`@ParameterizedTest`) para reutilizar un mismo script con muchos datos.
- **Patrón AAA** (preparar, ejecutar, verificar) y nombres descriptivos con `@DisplayName`.
- **Pruebas independientes**: cada prueba limpia los datos y usa emails únicos.
- **Esperas explícitas** (`WebDriverWait`), sin `Thread.sleep`.
- **Manejo de errores**: la API responde con un formato de error único; si una prueba de Selenium falla se guarda una captura automática (`CapturaEnFallo`); la conexión con Selenium se reintenta.
- **Entorno reproducible** con Docker y versiones fijas.

## Estructura

```
├── Dockerfile / docker-compose.yml
├── postman/usuarios.postman_collection.json
├── src/main/java/co/edu/calidad/taller/
│   ├── controller/UsuarioController.java
│   ├── service/UsuarioService.java
│   ├── model/ (Usuario, UsuarioRequest)
│   └── exception/ (ManejadorErrores, ErrorRespuesta, ...)
├── src/main/resources/static/registro.html
└── src/test/java/co/edu/calidad/taller/
    ├── api/UsuarioApiTest.java
    ├── web/RegistroFormularioTest.java
    ├── web/pages/RegistroPage.java
    ├── web/support/ (DriverFactory, CapturaEnFallo, ConNavegador)
    └── support/ (DatosPrueba, Evidencias, ReporteResultados)
```
