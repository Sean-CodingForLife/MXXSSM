# syntax=docker/dockerfile:1

# --- Frontend build ---------------------------------------------------------
FROM node:22-alpine AS web

WORKDIR /workspace
RUN corepack enable

COPY package.json pnpm-workspace.yaml pnpm-lock.yaml ./
COPY frontend/package.json frontend/package.json
RUN pnpm install --frozen-lockfile

COPY frontend frontend
RUN pnpm --dir frontend build

# --- Backend build ----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS api

WORKDIR /workspace
COPY backend/pom.xml backend/pom.xml
RUN mvn -f backend/pom.xml -B --no-transfer-progress dependency:go-offline

COPY backend backend
COPY --from=web /workspace/frontend/dist frontend/dist
RUN mvn -f backend/pom.xml -B --no-transfer-progress clean package -DskipTests

# --- Runtime ----------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app \
    && mkdir -p /app/logs && chown -R app:app /app

WORKDIR /app
COPY --from=api /workspace/backend/target/mxxssm.jar /app/app.jar

USER app
EXPOSE 8081

ENV TZ=Asia/Shanghai

HEALTHCHECK --interval=30s --timeout=5s --start-period=45s --retries=3 \
    CMD wget -qO- http://127.0.0.1:8081/actuator/health | grep -q UP || exit 1

ENTRYPOINT ["java", "-Duser.timezone=Asia/Shanghai", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
