FROM gradle:8.5-jdk17 AS builder
WORKDIR /build
COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon
COPY . .
RUN ./gradlew build -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/* \
 && useradd --system --uid 1001 --create-home --shell /usr/sbin/nologin appuser \
 && mkdir -p /data/files \
 && chown -R appuser:appuser /data
ENV FILE_UPLOAD_DIR=/data/files
# 컨테이너는 노드 타임존을 물려받지 않아 기본이 UTC다. 서비스 기준 시간을 한국으로 고정한다.
ENV TZ=Asia/Seoul
COPY --chown=appuser:appuser --from=builder /build/build/libs/*.jar /app/app.jar
USER appuser
EXPOSE 8080 7070
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
