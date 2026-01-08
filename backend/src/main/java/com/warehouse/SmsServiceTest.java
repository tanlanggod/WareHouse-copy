package com.warehouse;

import com.warehouse.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 短信服务测试
 */
@Component
public class SmsServiceTest implements CommandLineRunner {

    @Autowired
    private SmsService smsService;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("=== 阿里云短信服务集成测试 ===");

        // 测试短信发送功能
//        try {
//            String testPhone = "15716610706"; // 测试手机号（请替换为实际手机号）
//            System.out.println("正在测试发送验证码到: " + testPhone);
//
//            var result = smsService.sendVerificationCode(testPhone);
//
//            if (result.getCode() == 200) {
//                System.out.println("✅ 短信服务集成成功！");
//                System.out.println("响应消息: " + result.getMessage());
//                if (result.getData() != null) {
//                    System.out.println("测试环境验证码: " + result.getData());
//                }
//            } else {
//                System.out.println("❌ 短信发送失败: " + result.getMessage());
//            }
//        } catch (Exception e) {
//            System.out.println("❌ 短信服务测试失败: " + e.getMessage());
//            e.printStackTrace();
//        }

        System.out.println("=== 测试完成 ===");
    }
}