# Etapa 1: Build (Compilação)
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Compila o projeto e pula os testes (para ser mais rápido no deploy)
RUN mvn clean package -DskipTests

# Etapa 2: Runtime (Execução)
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
# Copia o .jar gerado na etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta 8080 (padrão do Spring)
EXPOSE 8080

# Comando para rodar o app
ENTRYPOINT ["java", "-jar", "app.jar"]