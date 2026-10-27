# 后端镜像：只带 JRE 与 jar，非 root 运行
# 构建上下文是项目根（demo/），因此 jar 路径带 target/ 前缀
# 用前先在本地执行：mvnw clean package（务必带 clean，见部署清单）
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/demo-0.0.1-SNAPSHOT.jar app.jar

# 非 root 运行：即使被攻破也拿不到容器 root
RUN addgroup -S app && adduser -S app -G app
USER app

EXPOSE 8081

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar", "--spring.profiles.active=prod"]
