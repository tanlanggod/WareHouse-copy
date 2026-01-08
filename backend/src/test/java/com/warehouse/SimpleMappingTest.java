package com.warehouse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.entity.WarehouseLocation;

/**
 * 简化映射测试
 */
public class SimpleMappingTest {

    public static void main(String[] args) {
        SimpleMappingTest test = new SimpleMappingTest();
        test.runTests();
    }

    public void runTests() {
        System.out.println("=== 简化映射测试 ===");

        // 1. 测试基础字段设置
        testBasicFieldSetting();

        // 2. 测试JSON映射
        testJsonMapping();

        System.out.println("=== 测试完成 ===");
    }

    private void testBasicFieldSetting() {
        System.out.println("1. 基础字段设置测试");

        try {
            WarehouseLocation location = new WarehouseLocation();

            // 直接调用 setter
            location.setWarehouseIdFromJson(123);
            location.setRackNumber("A1");
            location.setShelfLevel("1");
            location.setBinNumber("1");

            System.out.println("设置后检查:");
            System.out.println("  incomingWarehouseId: " + location.getIncomingWarehouseId());
            System.out.println("  rackNumber: " + location.getRackNumber());
            System.out.println("  shelfLevel: " + location.getShelfLevel());
            System.out.println("  binNumber: " + location.getBinNumber());

            if (location.getIncomingWarehouseId() != null && location.getIncomingWarehouseId().equals(123)) {
                System.out.println("  ✅ incomingWarehouseId 设置成功");
            } else {
                System.out.println("  ❌ incomingWarehouseId 设置失败");
            }

        } catch (Exception e) {
            System.err.println("  ❌ 基础字段设置异常: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void testJsonMapping() {
        System.out.println("2. JSON映射测试");

        String json = "{\"warehouseId\":456,\"rackNumber\":\"B2\",\"shelfLevel\":\"2\",\"binNumber\":\"2\"}";
        System.out.println("输入JSON: " + json);

        try {
            ObjectMapper mapper = new ObjectMapper();
            WarehouseLocation location = mapper.readValue(json, WarehouseLocation.class);

            System.out.println("映射结果:");
            System.out.println("  incomingWarehouseId: " + location.getIncomingWarehouseId());
            System.out.println("  rackNumber: " + location.getRackNumber());
            System.out.println("  shelfLevel: " + location.getShelfLevel());
            System.out.println("  binNumber: " + location.getBinNumber());

            if (location.getIncomingWarehouseId() != null && location.getIncomingWarehouseId().equals(456)) {
                System.out.println("  ✅ JSON映射成功");
            } else {
                System.out.println("  ❌ JSON映射失败");
            }

            // 测试反向序列化
            String outputJson = mapper.writeValueAsString(location);
            System.out.println("  输出JSON: " + outputJson);

            if (!outputJson.contains("incomingWarehouseId")) {
                System.out.println("  ✅ @Transient 注解生效，不包含 transient 字段");
            } else {
                System.out.println("  ❌ @Transient 注解未生效，包含 transient 字段");
            }

        } catch (JsonProcessingException e) {
            System.err.println("  ❌ JSON处理异常: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("  ❌ 其他异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}