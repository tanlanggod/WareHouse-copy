# 使用官方的 OpenJDK 8 作为基础镜像
FROM openjdk:8-jdk-slim

# 设置工作目录
WORKDIR /app

# 复制 Maven 配置文件
COPY pom.xml .

# 复制 Maven Wrapper（如果存在）
COPY mvnw .
COPY .mvn .mvn

# 下载依赖（利用 Docker 缓存层）
RUN chmod +x mvnw && ./mvnw dependency:go-offline

# 复制源代码
COPY src ./src

# 构建应用
RUN ./mvnw clean package -DskipTests

# 暴露端口
EXPOSE 8080

# 设置 JVM 参数
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# 启动应用
CMD ["java", "-jar", "target/warehouse-management-1.0.0.jar"]