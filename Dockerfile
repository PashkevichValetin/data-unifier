# Dockerfile для Data Unifier приложения

# Этап сборки
FROM maven:3.9.6-eclipse-temurin-21 AS builder

WORKDIR /app

# Копирование файлов проекта
COPY pom.xml .
COPY src ./src

# Сборка приложения (пропуская тесты для ускорения сборки)
RUN mvn clean package -DskipTests

# Этап выполнения
FROM eclipse-temurin:21-jre-jammy

# Создание непривилегированного пользователя
RUN groupadd -r dataunifier && useradd -r -g dataunifier dataunifier

WORKDIR /app

# Копирование собранного jar файла из этапа сборки
COPY --from=builder /app/target/*.jar app.jar

# Копирование скрипта для проверки готовности
COPY --chmod=755 <<"EOF" /usr/local/bin/wait-for-it.sh
#!/bin/bash
# wait-for-it.sh - ожидание доступности TCP хоста и порта

TIMEOUT=30
HOST=$1
PORT=$2
shift 2

if [ -z "$HOST" ] || [ -z "$PORT" ]; then
    echo "Usage: $0 host port [timeout]"
    exit 1
fi

if [ -n "$1" ]; then
    TIMEOUT=$1
fi

echo "Waiting for $HOST:$PORT to be available..."

for i in $(seq $TIMEOUT); do
    nc -z "$HOST" "$PORT" > /dev/null 2>&1
    if [ $? -eq 0 ]; then
        echo "$HOST:$PORT is available"
        exit 0
    fi
    echo "Waiting for $HOST:$PORT... $i/$TIMEOUT"
    sleep 1
done

echo "Timeout of $TIMEOUT seconds reached, $HOST:$PORT is not available"
exit 1
EOF

# Настройка директорий для логов
RUN mkdir -p /app/logs && chown -R dataunifier:dataunifier /app

# Переключение на непривилегированного пользователя
USER dataunifier

# Параметры JVM для оптимизации
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UnlockExperimentalVMOptions \
               -XX:+UseCGroupMemoryLimitForHeap \
               -Djava.security.egd=file:/dev/./urandom"

# Порт приложения
EXPOSE 8080

# Проверка здоровья
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Запуск приложения
ENTRYPOINT exec java ${JAVA_OPTS} -jar /app/app.jar