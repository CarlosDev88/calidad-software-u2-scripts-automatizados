# Guía paso a paso del proceso de automatización

Esta guía explica cómo se construyó la suite de pruebas, qué comandos se usaron y por qué se tomó cada decisión.

## Paso 1. Definir qué se va a probar

La aplicación bajo prueba es un registro de usuarios con dos entradas:

- **API REST** `/api/usuarios`: crear, listar y buscar usuarios.
- **Formulario web** `registro.html`: envía los datos a la API y muestra los errores debajo de cada campo.

Primero se escribieron las reglas de negocio, porque de ellas salen los casos de prueba:

| Regla | Válido | Inválido |
|---|---|---|
| Nombre | 3 a 60 caracteres | vacío, 2 o 61 caracteres |
| Email | formato `usuario@dominio.ext` y único | sin `@`, sin dominio, repetido |
| Edad | 18 a 100 | vacía, 17, 101 |

**Decisión:** se construyó una aplicación propia en lugar de probar una página pública. Así las pruebas no dependen de un sitio externo que puede cambiar o caerse, y los resultados son repetibles.

## Paso 2. Elegir herramientas por nivel de prueba

Se siguió la **pirámide de pruebas**: muchas pruebas unitarias rápidas abajo, menos de integración en el medio y pocas de interfaz arriba, porque son las más lentas y frágiles.

| Nivel | Herramienta | Por qué |
|---|---|---|
| Unitaria | JUnit 5 + Mockito | Estándar en Java. Mockito permite aislar el controlador del servicio. |
| API | TestRestTemplate | Viene con Spring Boot y prueba la API con un servidor real en un puerto aleatorio. |
| API (externa) | Postman + Newman | La herramienta más usada para APIs. Newman la corre desde consola y en CI. |
| Interfaz | Selenium WebDriver | Controla un navegador real como lo haría un usuario. |
| Cobertura | JaCoCo | Mide qué porcentaje del código ejecutan las pruebas. |
| CI | GitHub Actions | Corre todo automáticamente en cada push, gratis para repos públicos. |

## Paso 3. Preparar el entorno

Estructura del entorno:

```
Máquina (Windows / Linux / Mac)
└── Docker Desktop
    ├── contenedor "chrome"  -> selenium/standalone-chrome (navegador + Selenium Grid)
    ├── contenedor "tests"   -> Maven + Java 21: levanta la app y corre las pruebas
    ├── contenedor "app"     -> la app en el puerto 8080 (para Postman o uso manual)
    └── contenedor "newman"  -> corre la colección Postman contra "app"
```

**Decisión:** todo corre en Docker con **versiones fijas** (Java 21, Maven 3.9.9, Selenium 4.25.0, Chrome de la imagen 4.25.0). Así el resultado es igual en cualquier computador y no hace falta instalar Java ni Chrome. Esto evita el clásico "en mi máquina sí funciona".

Comandos:

```bash
git clone <url-del-repo>
cd taller-automatizacion
docker compose up --build --exit-code-from tests
```

## Paso 4. Pruebas unitarias (JUnit 5)

Archivos en `src/test/java/.../unit/`.

1. **`UsuarioServiceTest`**: prueba la lógica del servicio creándolo con `new`, sin Spring. Son las pruebas más rápidas. Se agrupan con `@Nested` por comportamiento ("Al crear", "Al consultar").
2. **`UsuarioRequestValidacionTest`**: aplica dos técnicas de caja negra vistas en la Unidad 1:
   - **Análisis de valores límite**: edad 17/18/100/101 y nombre de 2/3/60/61 caracteres.
   - **Particiones de equivalencia**: emails válidos e inválidos.
   Con `@ParameterizedTest` un solo método prueba muchos datos.
3. **`UsuarioControllerTest`**: con `@WebMvcTest` se levanta solo la capa web y el servicio se reemplaza por un **mock de Mockito**. Así se verifica, por ejemplo, que con datos inválidos el controlador responde 400 y **nunca** llama al servicio (`verify(servicio, never()).crear(any())`).

```bash
mvn test -Dgroups=unit
```

## Paso 5. Pruebas de API

**TestRestTemplate** (`api/UsuarioApiTest`): `@SpringBootTest(webEnvironment = RANDOM_PORT)` levanta la app real y se hacen peticiones HTTP verdaderas. Cubre: 201 creado, 200 lista y consulta, 404 no existe, 400 en siete casos de datos inválidos (parametrizado), 409 email duplicado y 400 con JSON roto.

**Postman** (`postman/usuarios.postman_collection.json`): siete peticiones en orden. La primera guarda el id creado en una variable que usan las siguientes. Cada petición tiene sus aserciones (`pm.test`), y hay una prueba común a toda la colección: tiempo de respuesta menor a 2 s y respuesta en JSON.

```bash
docker compose --profile postman run --rm newman
```

## Paso 6. Pruebas del formulario web (Selenium)

`web/RegistroFormularioTest` abre Chrome, llena el formulario y verifica los mensajes en pantalla.

- **Page Object** (`web/pages/RegistroPage`): los localizadores (`By.id(...)`) y las acciones están en una sola clase. Si cambia el HTML, se corrige en un solo lugar.
- **Esperas explícitas** (`WebDriverWait`) en lugar de `Thread.sleep`, para que las pruebas no fallen por lentitud ni pierdan tiempo.
- **Capturas automáticas**: cada caso guarda una captura con los datos de entrada y otra con el resultado. Si una prueba falla, `CapturaEnFallo` guarda una captura antes de cerrar el navegador.
- **`DriverFactory`**: usa el Chrome del contenedor (`SELENIUM_REMOTE_URL`) o un Chrome local en modo headless, sin cambiar el código de las pruebas.

## Paso 7. Cobertura (JaCoCo)

`mvn verify` genera `target/site/jacoco/index.html`. El build **falla si la cobertura de líneas baja del 80 %**. Así, si alguien agrega código sin pruebas, el pipeline lo detecta.

## Paso 8. Integración continua (GitHub Actions)

`.github/workflows/pruebas.yml` tiene dos trabajos que corren en cada push a `main`:

1. **JUnit + Selenium + cobertura**: instala Java 21 y corre `mvn verify` (el runner ya trae Chrome).
2. **Postman (Newman)**: levanta la app en Docker y corre la colección.

Las evidencias quedan como *artifacts* descargables en cada ejecución. En la pestaña **Actions** del repositorio se ve el resultado en verde o rojo.

## Paso 9. Revisar evidencias

Al terminar, `evidencias/` contiene los resultados de cada prueba, la salida de consola, los reportes JUnit, el reporte de cobertura, las capturas de Selenium y el resultado de Newman.

## Buenas prácticas aplicadas

- Patrón **AAA** (preparar, ejecutar, verificar) y nombres descriptivos con `@DisplayName`.
- **Pruebas independientes**: cada una parte de cero (`limpiar()`) y usa emails únicos.
- **Etiquetas** `@Tag("unit" | "api" | "ui")` para correr solo una parte de la suite.
- **Datos de prueba centralizados** (`DatosPrueba`).
- **Manejo de errores**: un formato único de error en la API (`ManejadorErrores`), reintento de conexión con Selenium y captura en fallos.
