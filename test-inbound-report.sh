#!/bin/bash

# 测试入库报表PDF生成功能
# 使用方法: bash test-inbound-report.sh

echo "=========================================="
echo "测试入库报表PDF生成功能"
echo "=========================================="

# 后端API地址
BASE_URL="http://localhost:8080/api"

# 输出目录
OUTPUT_DIR="./test-reports"
mkdir -p $OUTPUT_DIR

echo ""
echo "测试1: 生成全部入库记录的PDF报表（不传日期参数）"
echo "------------------------------------------"
curl -X GET "${BASE_URL}/reports/inbound/pdf" \
  -H "Accept: application/pdf" \
  --output "${OUTPUT_DIR}/inbound_report_all.pdf" \
  -w "\nHTTP状态码: %{http_code}\n" \
  -v

if [ -f "${OUTPUT_DIR}/inbound_report_all.pdf" ]; then
  FILE_SIZE=$(stat -f%z "${OUTPUT_DIR}/inbound_report_all.pdf" 2>/dev/null || stat -c%s "${OUTPUT_DIR}/inbound_report_all.pdf" 2>/dev/null)
  echo "✅ PDF文件生成成功！文件大小: ${FILE_SIZE} 字节"
  echo "📁 文件位置: ${OUTPUT_DIR}/inbound_report_all.pdf"
else
  echo "❌ PDF文件生成失败"
fi

echo ""
echo "测试2: 生成指定日期范围的PDF报表"
echo "------------------------------------------"
START_DATE="2025-01-01 00:00:00"
END_DATE="2025-12-31 23:59:59"

curl -X GET "${BASE_URL}/reports/inbound/pdf?startDate=${START_DATE}&endDate=${END_DATE}" \
  -H "Accept: application/pdf" \
  --output "${OUTPUT_DIR}/inbound_report_2025.pdf" \
  -w "\nHTTP状态码: %{http_code}\n" \
  -v

if [ -f "${OUTPUT_DIR}/inbound_report_2025.pdf" ]; then
  FILE_SIZE=$(stat -f%z "${OUTPUT_DIR}/inbound_report_2025.pdf" 2>/dev/null || stat -c%s "${OUTPUT_DIR}/inbound_report_2025.pdf" 2>/dev/null)
  echo "✅ PDF文件生成成功！文件大小: ${FILE_SIZE} 字节"
  echo "📁 文件位置: ${OUTPUT_DIR}/inbound_report_2025.pdf"
else
  echo "❌ PDF文件生成失败"
fi

echo ""
echo "测试3: 生成近30天的PDF报表"
echo "------------------------------------------"
# 计算日期（Mac/Linux兼容）
if [[ "$OSTYPE" == "darwin"* ]]; then
  # Mac OS
  START_DATE=$(date -v-30d "+%Y-%m-%d 00:00:00")
  END_DATE=$(date "+%Y-%m-%d 23:59:59")
else
  # Linux
  START_DATE=$(date -d "30 days ago" "+%Y-%m-%d 00:00:00")
  END_DATE=$(date "+%Y-%m-%d 23:59:59")
fi

echo "日期范围: ${START_DATE} 至 ${END_DATE}"

curl -X GET "${BASE_URL}/reports/inbound/pdf?startDate=${START_DATE}&endDate=${END_DATE}" \
  -H "Accept: application/pdf" \
  --output "${OUTPUT_DIR}/inbound_report_last30days.pdf" \
  -w "\nHTTP状态码: %{http_code}\n"

if [ -f "${OUTPUT_DIR}/inbound_report_last30days.pdf" ]; then
  FILE_SIZE=$(stat -f%z "${OUTPUT_DIR}/inbound_report_last30days.pdf" 2>/dev/null || stat -c%s "${OUTPUT_DIR}/inbound_report_last30days.pdf" 2>/dev/null)
  echo "✅ PDF文件生成成功！文件大小: ${FILE_SIZE} 字节"
  echo "📁 文件位置: ${OUTPUT_DIR}/inbound_report_last30days.pdf"
else
  echo "❌ PDF文件生成失败"
fi

echo ""
echo "=========================================="
echo "测试完成！"
echo "=========================================="
echo "生成的PDF文件保存在: ${OUTPUT_DIR}/"
echo ""
echo "请检查以下内容："
echo "1. PDF文件是否能正常打开"
echo "2. 中文是否正常显示"
echo "3. 表格数据是否正确"
echo "4. 统计信息是否准确"
echo ""
