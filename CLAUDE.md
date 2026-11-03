# Plan de ejecución para Claude (consola local)

Contexto: taller académico (Prueba y Calidad de Software, Unidad 2, AA1). El código ya está escrito,
pero **nunca se ha compilado ni ejecutado**: se escribió en un entorno sin acceso a Maven Central ni a Docker Hub.
Tu trabajo es ejecutarlo con Docker en esta máquina (Windows + Docker Desktop), corregir lo que falle,
generar las evidencias y dejar el repo git listo. Lee `README.md` para entender el proyecto.

El usuario (Carlos) **no tiene Java ni Maven instalados**: todo se corre con Docker. Responde en español, breve.

## Reglas

- No cambies el alcance: es una app pequeña de usuarios + sus pruebas. No agregues frameworks ni features.
- Si algo falla, corrige con el cambio **mínimo** y explica en una línea qué corregiste.
- No debilites las pruebas para que pasen (no borres aserciones, no pongas `@Disabled`). Si una aserción
  está mal frente al comportamiento correcto de la app, corrige la aserción y dilo.
- Mantén las versiones del README. Solo cámbialas si una imagen o dependencia no existe; en ese caso usa la
  más cercana y actualiza README, `pom.xml` y `docker-compose.yml` para que coincidan.
- Comandos en PowerShell. Directorio de trabajo: la carpeta de este archivo.

## Pasos

### 1. Verificar Docker
```powershell
docker version
docker compose version
```
Si Docker no responde, pídele a Carlos que abra Docker Desktop y espera.

### 2. Correr las pruebas de API y formulario
```powershell
docker compose up --build --exit-code-from tests
```
- Primera vez: tarda varios minutos (descarga imágenes y dependencias de Maven).
- Esperado: `Tests run: 21, Failures: 0, Errors: 0` aprox. (13 de API + 8 de formulario) y `BUILD SUCCESS`.
- Problemas posibles:
  - `selenium/standalone-chrome:4.25.0-20240922` no existe → busca un tag `4.25.0-AAAAMMDD` válido
    (por ejemplo `docker pull selenium/standalone-chrome:4.25.0`) y actualiza el compose.
  - Error de compilación → corrígelo en el archivo indicado.
  - Selenium no llega a la app (timeout, `ERR_NAME_NOT_RESOLVED`, `ERR_CONNECTION_REFUSED`) → revisa que
    `APP_HOST=tests` y que el servicio se llame `tests` en el compose.
- Repite hasta que todo pase. Al final: `docker compose down`.

### 3. Correr la colección Postman
```powershell
docker compose --profile postman run --rm newman
docker compose --profile demo --profile postman down
```
- Esperado: 7 requests, 0 fallos.
- Si `postman/newman:6.2.1-alpine` no existe, usa `postman/newman:6-alpine` o el tag 6.x más cercano.

### 4. Revisar evidencias
Confirma que existen y no están vacíos:
- `evidencias/resultados-pruebas.txt` (todas en `[OK   ]`)
- `evidencias/salida-consola.txt`
- `evidencias/surefire-reports/`
- `evidencias/capturas/` (unos 14 PNG: `form_01_...`, `form_02_...`, `form_03_...`, `form_04_...`)
- `evidencias/newman-resultados.xml`

No debe quedar ningún `FALLO_*.png`. Si queda de una corrida anterior, bórralo y vuelve a correr.
Abre 2 o 3 capturas y confirma que muestran el formulario con los mensajes esperados.

### 5. Repositorio git
Si no existe `.git`, inicialízalo y haz commits por partes (historial limpio):
```powershell
git init -b main
git add pom.xml .gitignore .gitattributes .dockerignore src/main
git commit -m "feat: app de usuarios (API REST + formulario de registro)"
git add src/test/java/co/edu/calidad/taller/support src/test/java/co/edu/calidad/taller/api
git commit -m "test: pruebas de API REST con TestRestTemplate"
git add src/test/java/co/edu/calidad/taller/web
git commit -m "test: pruebas del formulario web con Selenium WebDriver (Page Object)"
git add postman
git commit -m "test: coleccion Postman para la API"
git add Dockerfile docker-compose.yml README.md CLAUDE.md
git commit -m "chore: entorno Docker con versiones fijas y documentacion"
git add evidencias
git commit -m "docs: evidencias de ejecucion (capturas y resultados)"
```
Si hiciste correcciones, inclúyelas en el commit que corresponda o en uno aparte `fix: ...`.
Si git pide nombre/email, pídeselos a Carlos (no los inventes).

### 6. Subir a GitHub (solo si Carlos lo pide)
Si tiene `gh` autenticado:
```powershell
gh repo create taller-scripts-automatizados --public --source . --push
```
Si no, dale los pasos para crear el repo en github.com y hacer `git remote add origin ...` + `git push -u origin main`.

### 7. Resumen final para Carlos
En pocas líneas:
- cuántas pruebas pasaron (JUnit y Postman),
- qué corregiste (si algo),
- dónde están las evidencias,
- capturas que le sirven para el informe PDF: consola con `BUILD SUCCESS`, `resultados-pruebas.txt`,
  capturas del formulario, salida de Newman, y el navegador en vivo en http://localhost:7900 (clave `secret`)
  si quiere tomar capturas mientras corre.
