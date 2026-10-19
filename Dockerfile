# ---------- estágio 1: build + testes unitários ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# baixa as dependências antes de copiar o código, assim o cache do Docker
# só é invalidado quando o pom.xml muda
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# "package" roda os testes unitários (surefire). Se algum falhar, a imagem não é gerada.
# Os testes de integração (*IT) ficam na esteira, porque precisam de Docker pra subir o Postgres.
RUN mvn -B package

# ---------- estágio 2: só o necessário pra rodar ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S mesadj && adduser -S mesadj -G mesadj
COPY --from=build /app/target/mesadj.jar app.jar
USER mesadj

EXPOSE 8080
HEALTHCHECK --interval=15s --timeout=3s --start-period=40s --retries=5 \
  CMD wget -qO- "http://localhost:${PORT:-8080}/actuator/health" | grep -q '"UP"' || exit 1

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
