@echo off
chcp 65001 >nul
echo ==========================================
echo 测试入库报表PDF生成功能
echo ==========================================

set BASE_URL=http://localhost:8080/api
set OUTPUT_DIR=test-reports

if not exist %OUTPUT_DIR% mkdir %OUTPUT_DIR%

echo.
echo 测试1: 生成全部入库记录的PDF报表
echo ------------------------------------------
curl -X GET "%BASE_URL%/reports/inbound/pdf" -H "Accept: application/pdf" --output "%OUTPUT_DIR%/inbound_report_all.pdf" -w "HTTP状态码: %%{http_code}\n"

if exist "%OUTPUT_DIR%/inbound_report_all.pdf" (
    echo ✓ PDF文件生成成功！
    echo 文件位置: %OUTPUT_DIR%\inbound_report_all.pdf
    for %%A in ("%OUTPUT_DIR%\inbound_report_all.pdf") do echo 文件大小: %%~zA 字节
) else (
    echo ✗ PDF文件生成失败
)

echo.
echo 测试2: 生成指定日期范围的PDF报表
echo ------------------------------------------
set "START_DATE=2025-01-01 00:00:00"
set "END_DATE=2025-12-31 23:59:59"

curl -X GET "%BASE_URL%/reports/inbound/pdf?startDate=%START_DATE%&endDate=%END_DATE%" -H "Accept: application/pdf" --output "%OUTPUT_DIR%/inbound_report_2025.pdf" -w "HTTP状态码: %%{http_code}\n"

if exist "%OUTPUT_DIR%/inbound_report_2025.pdf" (
    echo ✓ PDF文件生成成功！
    echo 文件位置: %OUTPUT_DIR%\inbound_report_2025.pdf
    for %%A in ("%OUTPUT_DIR%\inbound_report_2025.pdf") do echo 文件大小: %%~zA 字节
) else (
    echo ✗ PDF文件生成失败
)

echo.
echo 测试3: 使用Postman或浏览器测试
echo ------------------------------------------
echo 在浏览器中打开以下URL:
echo %BASE_URL%/reports/inbound/pdf
echo.

echo ==========================================
echo 测试完成！
echo ==========================================
echo 生成的PDF文件保存在: %OUTPUT_DIR%\
echo.
echo 请检查以下内容:
echo 1. PDF文件是否能正常打开
echo 2. 中文是否正常显示
echo 3. 表格数据是否正确
echo 4. 统计信息是否准确
echo.
pause
