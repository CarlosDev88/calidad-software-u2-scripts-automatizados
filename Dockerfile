# Imagen con versiones exactas: Maven 3.9.9 + Java 21 (Eclipse Temurin)
FROM maven:3.9.9-eclipse-temurin-21

WORKDIR /app

# 1) Primero solo el pom.xml: las dependencias quedan en cache y no se descargan en cada build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# 2) Luego el codigo fuente
COPY src ./src

# Por defecto corre TODAS las pruebas (unitarias, API y formulario) + reporte de cobertura JaCoCo.
# Deja en ./evidencias: salida de consola, reportes JUnit y el reporte de cobertura.
CMD ["bash", "-c", "set -o pipefail; mkdir -p evidencias; mvn -B verify 2>&1 | tee evidencias/salida-consola.txt; estado=$?; cp -r target/surefire-reports evidencias/ 2>/dev/null; rm -rf evidencias/cobertura; mkdir -p evidencias/cobertura; cp target/site/jacoco/jacoco.csv target/site/jacoco/jacoco.xml evidencias/cobertura/ 2>/dev/null; cp -r target/site/jacoco evidencias/cobertura/html 2>/dev/null; exit $estado"]
