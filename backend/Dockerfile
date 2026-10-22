# Imagem da API usada no deploy (Render) e no compose.yaml (perfil "api")

# Etapa 1: compila a API. Os testes ficam com o CI, que já roda antes do deploy.
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build

# Dependências primeiro: enquanto o pom.xml não mudar, o Docker reaproveita esta camada
COPY mvnw pom.xml ./
COPY .mvn/ .mvn/
RUN ./mvnw -B -q dependency:go-offline

COPY src/ src/
RUN ./mvnw -B -q package -DskipTests -Djacoco.skip=true \
    && cp target/dismar-stock-api-*.jar app.jar \
    && java -Djarmode=tools -jar app.jar extract --layers --destination extracted

# Etapa 2: só o JRE e a aplicação extraída, rodando sem root
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S dismar && adduser -S dismar -G dismar
USER dismar

# Camadas da que muda menos para a que muda mais: um deploy só de código reenvia apenas a última
COPY --from=build /build/extracted/dependencies/ ./
COPY --from=build /build/extracted/spring-boot-loader/ ./
COPY --from=build /build/extracted/snapshot-dependencies/ ./
COPY --from=build /build/extracted/application/ ./

# Ajustado para o plano gratuito do Render (512 MB e pouca CPU): heap limitada à memória do container,
# coletor de lixo mais leve e compilação JIT mais simples, que deixa a inicialização bem mais rápida
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC -XX:TieredStopAtLevel=1"

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
