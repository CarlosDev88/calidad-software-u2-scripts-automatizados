# Imagen con versiones exactas: Maven 3.9.9 + Java 21 (Eclipse Temurin)
FROM maven:3.9.9-eclipse-temurin-21

WORKDIR /app

# 1) Primero solo el pom.xml: las dependencias quedan en cache y no se descargan en cada build
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

# 2) Luego el codigo fuente
COPY src ./src

# Por defecto corre todas las pruebas, guarda la salida de consola y los reportes de JUnit en ./evidencias
CMD ["bash", "-c", "set -o pipefail; mkdir -p evidencias; mvn -B test 2>&1 | tee evidencias/salida-consola.txt; estado=$?; cp -r target/surefire-reports evidencias/ 2>/dev/null; exit $estado"]
