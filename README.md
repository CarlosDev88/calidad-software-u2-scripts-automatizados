# Pruebas automatizadas: JUnit, Selenium y Postman

**Asignatura:** Prueba y Calidad de Software, Unidad 2
**Estudiante:** Carlos Arturo Rueda Calier

## Evolución del proyecto

El repositorio creció en dos actividades. El historial de git conserva cada etapa.

| Etapa | Actividad | Qué se agregó | Informe |
|---|---|---|---|
| 1 | AA1: Diseño de scripts para servicios REST y formularios web | App de usuarios, pruebas de API (TestRestTemplate), formulario (Selenium), colección Postman, Docker | `Taller_Scripts_Automatizados_CarlosRueda.pdf` |
| 2 | AA2: Automatización de pruebas con JUnit, Selenium o Postman | Pruebas unitarias (JUnit 5 + Mockito), valores límite, etiquetas por nivel, cobertura JaCoCo con mínimo del 80 %, integración continua con GitHub Actions, guía paso a paso | `Taller_Automatizacion_CarlosRueda.pdf` |

Suite de pruebas automatizadas para una aplicación pequeña de registro de usuarios: una API REST y un formulario web. Cubre los tres niveles de prueba y queda integrada en un pipeline de integración continua.

| Nivel | Herramienta | Qué prueba | Clases |
|---|---|---|---|
| Unitaria | JUnit 5 | Lógica de negocio y reglas de validación (valores límite y particiones) | `unit/UsuarioServiceTest`, `unit/UsuarioRequestValidacionTest` |
| Unitaria (aislada) | JUnit 5 + **Mockito** + MockMvc | Controlador REST con el servicio simulado | `unit/UsuarioControllerTest` |
| Integración / API | JUnit 5 + **TestRestTemplate** | Endpoints REST reales | `api/UsuarioApiTest` |
| Integración / API | **Postman** + Newman | Endpoints REST desde fuera de la app | `postman/usuarios.postman_collection.json` |
| Sistema / UI | JUnit 5 + **Selenium WebDriver** | Formulario web en Chrome | `web/RegistroFormularioTest` |
| Cobertura | **JaCoCo** | % de código ejecutado por las pruebas (mínimo 80 %) | `pom.xml` |
| CI | **GitHub Actions** | Corre todo en cada push | `.github/workflows/pruebas.yml` |

La guía detallada del proceso está en **[GUIA_PASO_A_PASO.md](GUIA_PASO_A_PASO.md)**.

## Versiones

| Componente | Versión |
|---|---|
| Java | 21 (Eclipse Temurin) |
| Maven | 3.9.9 |
| Spring Boot | 3.3.5 (incluye JUnit 5, Mockito, AssertJ) |
| Selenium | 4.25.0 |
| Chrome (Docker) | `selenium/standalone-chrome:4.25.0-20240922` |
| JaCoCo | 0.8.12 |
| Newman | `postman/newman:6.1.3-alpine` |

## Cómo correrlo

**Con Docker** (solo requiere Docker Desktop):

```bash
# Todas las pruebas JUnit (unitarias + API + Selenium) y la cobertura
docker compose up --build --exit-code-from tests

# Colección Postman con Newman
docker compose --profile postman run --rm newman

# Usar la app a mano: http://localhost:8080/registro.html
docker compose --profile demo up --build app

# Apagar todo
docker compose --profile demo --profile postman down
```

**Sin Docker** (Java 21, Maven y Chrome instalados):

```bash
mvn verify                  # todo + cobertura (falla si baja del 80 %)
mvn test -Dgroups=unit      # solo unitarias (segundos)
mvn test -Dgroups=api       # solo API
mvn test -Dgroups=ui        # solo Selenium
```

Mientras corre Selenium en Docker, el navegador se ve en vivo en http://localhost:7900 (clave `secret`).

## Evidencias (`evidencias/`)

| Archivo | Contenido |
|---|---|
| `resultados-pruebas.txt` | Cada prueba con su resultado (OK / FALLO) |
| `salida-consola.txt` | Salida completa de `mvn verify` |
| `salida-newman.txt` | Salida de consola de Newman |
| `surefire-reports/` | Reportes JUnit (txt y xml, con tiempos) |
| `cobertura/` | Cobertura JaCoCo: `jacoco.csv`, `jacoco.xml` y el reporte HTML en `cobertura/html/index.html` |
| `defectos-antes-de-corregir.txt` | Pruebas que fallaron antes de corregir tres defectos encontrados (ver la guía, sección A7) |
| `capturas-proceso/` | Capturas de pantalla de la ejecución en GitHub Actions y del reporte de cobertura |
| `capturas/*.png` | Capturas de Selenium: datos de entrada y resultado de cada caso |
| `newman-resultados.xml` | Resultado de la colección Postman |

## Aplicación bajo prueba

| Método | Endpoint | Respuestas |
|---|---|---|
| GET | `/api/usuarios` | 200 lista |
| GET | `/api/usuarios/{id}` | 200 · 404 no existe · 400 id no numérico |
| Otros métodos / otro Content-Type | `/api/usuarios...` | 405 · 415 (mismo formato de error) |
| POST | `/api/usuarios` | 201 creado · 400 datos inválidos / JSON mal formado · 409 email duplicado |

Reglas: nombre obligatorio de 3 a 60 caracteres (sin contar espacios al inicio o al final), email obligatorio y válido, edad obligatoria, entera, entre 18 y 100, email único.

## Estructura

```
├── .github/workflows/pruebas.yml        CI
├── Dockerfile / docker-compose.yml      Entorno reproducible
├── postman/                             Colección Postman
├── src/main/java/co/edu/calidad/taller  Aplicación (controller, service, model, exception)
├── src/main/resources/static/           Formulario registro.html
└── src/test/java/co/edu/calidad/taller
    ├── unit/      Unitarias (JUnit 5, Mockito)
    ├── api/       API con TestRestTemplate
    ├── web/       Selenium (Page Object en web/pages, utilidades en web/support)
    └── support/   Datos de prueba, evidencias y reporte de resultados
```
