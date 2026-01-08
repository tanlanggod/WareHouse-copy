@echo off
REM 仓库管理系统一键部署脚本 (Windows版)
REM 需要 Docker Desktop 和 Git Bash

echo ==========================================
echo 🚀 仓库管理系统一键部署脚本 (Windows版)
echo ==========================================
echo.

REM 检查 Docker Desktop 是否运行
echo [STEP] 检查 Docker Desktop...
docker --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] 未找到 Docker，请先安装 Docker Desktop
    echo 下载地址: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)
echo [INFO] Docker 已安装

REM 检查 docker-compose
docker-compose --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] 未找到 docker-compose
    pause
    exit /b 1
)
echo [INFO] docker-compose 已安装

REM 创建必要的目录
echo [STEP] 创建必要的目录结构...
if not exist "logs" mkdir logs
if not exist "logs\nginx" mkdir logs\nginx
if not exist "logs\nginx\logs" mkdir logs\nginx\logs
if not exist "uploads" mkdir uploads
if not exist "mysql\conf" mkdir mysql\conf
if not exist "nginx\ssl" mkdir nginx\ssl
echo [INFO] 目录结构创建完成

REM 配置环境变量
echo [STEP] 配置环境变量...
if not exist ".env" (
    if exist ".env.example" (
        copy ".env.example" ".env"
        echo [INFO] 已从 .env.example 创建 .env 文件
        echo [WARN] 请根据实际情况修改 .env 文件中的配置
    ) else (
        echo [WARN] 未找到 .env.example，创建默认 .env 文件
        (
            echo # 数据库配置
            echo MYSQL_ROOT_PASSWORD=warehouse_secure_2024
            echo MYSQL_USER=warehouse_user
            echo MYSQL_PASSWORD=warehouse_pass_2024
            echo DATABASE_URL=jdbc:mysql://mysql:3306/warehouse_management?useUnicode=true^&characterEncoding=utf8^&useSSL=false^&serverTimezone=Asia/Shanghai^&allowPublicKeyRetrieval=true
            echo.
            echo # JWT配置
            echo JWT_SECRET=warehouse_management_jwt_secret_2024_secure_key
            echo JWT_EXPIRATION=86400000
            echo.
            echo # 应用配置
            echo SPRING_PROFILES_ACTIVE=docker
        ) > .env
    )
) else (
    echo [INFO] .env 文件已存在，跳过创建
)

REM 停止现有容器
echo [STEP] 停止现有容器...
docker-compose down

REM 构建镜像
echo [STEP] 构建 Docker 镜像...
docker-compose build --no-cache

REM 启动服务
echo [STEP] 启动服务...
docker-compose up -d

REM 等待服务启动
echo [INFO] 等待服务启动...
timeout /t 30 /nobreak >nul

REM 显示访问信息
echo.
echo =====================================
echo 🎉 仓库管理系统部署完成！
echo =====================================
echo.
echo 📱 访问地址：
echo    本地访问: http://localhost
echo    网络访问: http://[您的服务器IP]
echo.
echo 🔧 管理命令：
echo    查看日志: docker-compose logs -f [frontend^|backend^|mysql]
echo    重启服务: docker-compose restart [frontend^|backend^|mysql]
echo    停止服务: docker-compose down
echo    查看状态: docker-compose ps
echo.
echo 📊 服务状态：
docker-compose ps
echo.
echo ⚠️  重要提醒：
echo    1. 请及时修改 .env 文件中的密码
echo    2. 建议配置 HTTPS 证书
echo    3. 定期备份数据库
echo    4. 监控系统资源使用情况
echo.
echo =====================================

pause