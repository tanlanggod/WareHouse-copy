package com.warehouse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.entity.WarehouseLocation;

/**
 * 简单实体测试
 */
public class SimpleEntityTest {

    public static void main(String[] args) {
        SimpleEntityTest test = new SimpleEntityTest();
        test.testEntity();
    }

    public void testEntity() {
        System.out.println("=== 简单实体测试 ===");

        try {
            // 1. 创建实例
            WarehouseLocation location = new WarehouseLocation();

            // 2. 测试 setter 方法

            location.setRackNumber("A1");
            location.setShelfLevel("1");
            location.setBinNumber("1");

            System.out.println("设置后的值:");
            System.out.println("rackNumber: " + location.getRackNumber());
            System.out.println("shelfLevel: " + location.getShelfLevel());
            System.out.println("binNumber: " + location.getBinNumber());

            // 3. 测试 JSON 映射
            String json = "{\"warehouseId\":456,\"rackNumber\":\"B2\"}";
            System.out.println("测试JSON: " + json);

            ObjectMapper mapper = new ObjectMapper();
            WarehouseLocation mappedLocation = mapper.readValue(json, WarehouseLocation.class);

            System.out.println("映射结果:");
            System.out.println("rackNumber: " + mappedLocation.getRackNumber());

            // 4. 测试输出 JSON
            String outputJson = mapper.writeValueAsString(mappedLocation);
            System.out.println("输出JSON: " + outputJson);

            System.out.println("=== 测试完成 ===");

        } catch (Exception e) {
            System.err.println("测试异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}