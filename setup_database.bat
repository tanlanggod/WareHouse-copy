@echo off
echo ====================================
echo 仓库管理系统 - 数据库初始化脚本
echo ====================================
echo.

echo 正在检查Java环境...
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未找到Java环境，请先安装Java 8或更高版本
    pause
    exit /b 1
)

echo ✅ Java环境检查通过
echo.

echo 正在编译数据库初始化工具...
cd /d "%~dp0backend"

if not exist "target\classes" (
    echo 正在编译项目...
    call mvn compile -q
    if %errorlevel% neq 0 (
        echo 错误: 项目编译失败
        pause
        exit /b 1
    )
)

echo ✅ 编译完成
echo.

echo 运行数据库初始化工具...
echo ----------------------------------------
java -cp "target\classes;target\lib\*" com.warehouse.util.DatabaseInitUtil
echo ----------------------------------------

echo.
echo 初始化完成！
echo.
echo 如果初始化成功，请启动应用：
echo cd backend
echo mvn spring-boot:run
echo.
echo 如果初始化失败，请按照上述提示手动配置数据库
pause