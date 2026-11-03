# Guía para el agente que hace el informe (Codex)

Contexto: taller individual de Prueba y Calidad de Software, Unidad 2, AA1
"Diseño de scripts de prueba automatizados para servicios REST y formularios web".
Estudiante: Carlos Rueda. El código y las pruebas ya están hechos. **No modifiques el código** (`src/`, `postman/`,
`Dockerfile`, `docker-compose.yml`). Tu trabajo es el **informe en PDF**.

Archivos para leer primero:
- `README.md`: qué es el proyecto, versiones, casos de prueba y buenas prácticas.
- `../RAA1_U2_Prueba_calidad_software.docx.pdf`: la rúbrica.
- `evidencias/`: resultados reales de la ejecución.

## 1. Verificar o generar evidencias

Si `evidencias/resultados-pruebas.txt` no existe, o tiene líneas `[FALLO]`, córrelo (Windows + Docker Desktop):
```powershell
docker compose up --build --exit-code-from tests
docker compose --profile postman run --rm newman
docker compose --profile demo --profile postman down
```
Usa **solo resultados reales** de `evidencias/`. Nunca inventes números, tiempos ni capturas.
Si algo falla, no lo arregles tú: repórtaselo a Carlos.

Qué hay en `evidencias/`:
- `resultados-pruebas.txt`: cada prueba con OK/FALLO (API + formulario).
- `salida-consola.txt`: salida de Maven (buscar `Tests run:` y `BUILD SUCCESS`).
- `surefire-reports/`: reportes JUnit.
- `capturas/form_*.png`: capturas de Selenium. Por caso hay `_datos` (datos de entrada) y `_resultado` (resultado).
- `newman-resultados.xml`: resultado de la colección Postman.

## 2. El informe

- Formato: **PDF**, en español, tono académico pero claro y sencillo.
- Nombre: `Taller_Scripts_Automatizados_CarlosRueda.pdf`, en esta carpeta.
- Portada: asignatura, actividad, estudiante, fecha.

### Estructura (amarrada a la rúbrica)

1. **Introducción y objetivo** (corta).
2. **Herramientas y entorno.** Tabla de versiones del README. Por qué Docker: mismo resultado en cualquier máquina,
   el profesor lo corre con un comando.
3. **Aplicación bajo prueba.** API `/api/usuarios` (endpoints y códigos 200/201/400/404/409), reglas de validación,
   formulario `registro.html`. Incluye la captura del formulario.
4. **Diseño de los scripts** (criterio 1: organización, reutilización, funciones, validaciones, manejo de errores,
   código comentado). Explica con fragmentos cortos de código reales:
   - Page Object `RegistroPage` (localizadores y acciones en un solo lugar).
   - `DatosPrueba` + `@ParameterizedTest` (un script, muchos datos).
   - Patrón AAA y `@DisplayName`.
   - Esperas explícitas (`WebDriverWait`).
   - Manejo de errores: `ManejadorErrores` (formato único de error), `CapturaEnFallo` (captura automática si falla),
     reintento de conexión en `DriverFactory`.
   - Pruebas independientes (`limpiar()`, emails únicos).
5. **Script de API** (criterio 2):
   - TestRestTemplate (`UsuarioApiTest`): tabla de casos con entrada, aserción y resultado real.
   - Postman/Newman: las 7 peticiones y sus pruebas, con el resultado de `newman-resultados.xml`.
6. **Script de formulario web** (criterio 2): `RegistroFormularioTest` con Selenium WebDriver. Tabla de casos con
   entrada, aserción y resultado. Incluye las capturas `_datos` y `_resultado` de varios casos (mínimo: registro
   exitoso, email inválido, formulario vacío, email duplicado).
7. **Flujo seguido paso a paso:** diseño de casos → app → scripts → ejecución en Docker → evidencias.
8. **Resultados.** Resumen con totales reales (pruebas ejecutadas / pasadas, JUnit y Postman).
9. **Justificación de decisiones:** Java + Spring Boot (TestRestTemplate lo pide la rúbrica), Selenium con Chrome en
   contenedor, Postman como segunda forma de probar la API, app propia en vez de una web externa (no depende de
   terceros, resultados repetibles).
10. **Reflexión final** sobre la utilidad de las pruebas automatizadas: detectan regresiones, son repetibles, sirven
    de documentación, se pueden integrar en CI. Mencionar una limitación: no reemplazan las pruebas exploratorias.
    Escríbela en primera persona y sencilla; Carlos puede ajustarla.
11. **Trabajo en equipo.** La actividad es individual, así que en una línea indica que el trabajo fue individual.
    No inventes compañeros ni participación grupal.
12. **Anexo:** estructura del repositorio y comandos para ejecutarlo.

### Capturas

- Usa las de `evidencias/capturas/`.
- Capturas de terminal o de Postman: si no puedes tomarlas, deja un recuadro con el texto
  `[CAPTURA PENDIENTE: ...]` describiendo cuál va (ej. "consola con BUILD SUCCESS",
  "Postman Runner con 7/7 requests") y avísale a Carlos al final cuáles faltan.
  Puedes incluir el texto real de `salida-consola.txt` o la salida de Newman como bloque de código.

### Estilo

- Directo, sin relleno, sin frases de venta.
- Explicaciones simples sin perder el detalle técnico.
- Cada captura con un pie de figura numerado.

## 3. Al terminar

Dile a Carlos en pocas líneas: dónde quedó el PDF, cuántas páginas tiene y qué capturas quedaron pendientes.
Si el repo git existe, haz commit del PDF y de este archivo: `git add AGENTS.md Taller_Scripts_Automatizados_CarlosRueda.pdf`
y `git commit -m "docs: informe del taller"`.
