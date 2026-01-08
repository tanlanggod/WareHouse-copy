package com.warehouse.service;


import com.warehouse.common.Result;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import com.aliyun.tea.*;

/**
 * 短信服务
 */
@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Value("${app.sms.enabled:false}")
    private Boolean smsEnabled;

    @Value("${app.sms.test-code:123456}")
    private String testVerificationCode;

    // 阿里云短信配置
    @Value("${app.sms.aliyun.access-key-id:}")
    private String accessKeyId;

    @Value("${app.sms.aliyun.access-key-secret:}")
    private String accessKeySecret;

    @Value("${app.sms.aliyun.region:ap-southeast-1}")
    private String region;

    @Value("${app.sms.aliyun.sign-name:}")
    private String signName;

    @Value("${app.sms.aliyun.template-code:}")
    private String templateCode;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 阿里云客户端
    private final Gson gson = new Gson();

    private static final String SMS_CODE_PREFIX = "sms_code:";
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int CODE_LENGTH = 6;
    private static final int RESEND_INTERVAL_SECONDS = 60;





    /**
     * 发送验证码
     */
    public Result<String> sendVerificationCode(String phone) {
        try {
            // 检查发送间隔
            String intervalKey = SMS_CODE_PREFIX + "interval:" + phone;
            if (redisTemplate.hasKey(intervalKey)) {
                return Result.error("验证码发送过于频繁，请稍后再试");
            }

            String code;
            if (Boolean.TRUE.equals(smsEnabled)) {
                // 生产环境：生成6位随机验证码
                code = generateVerificationCode();
                // TODO: 集成真实短信服务商 (阿里云、腾讯云等)
                sendSms(phone, code);
            } else {
                // 开发/测试环境：使用固定验证码
                code = testVerificationCode;
                System.out.println("【测试】验证码: " + code + "，手机号: " + phone);
            }

            // 缓存验证码
            String codeKey = SMS_CODE_PREFIX + phone;
            redisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);

            // 设置发送间隔限制
            redisTemplate.opsForValue().set(intervalKey, "1", RESEND_INTERVAL_SECONDS, TimeUnit.SECONDS);

            return Result.success("验证码发送成功", Boolean.TRUE.equals(smsEnabled) ? null : code);
        } catch (Exception e) {
            return Result.error("验证码发送失败：" + e.getMessage());
        }
    }

    /**
     * 验证验证码
     */
    public boolean verifyCode(String phone, String inputCode) {
        String codeKey = SMS_CODE_PREFIX + phone;
        String savedCode = (String) redisTemplate.opsForValue().get(codeKey);

        if (savedCode == null) {
            return false;
        }

        boolean valid = savedCode.equals(inputCode);
        if (valid) {
            // 验证成功后删除验证码
            redisTemplate.delete(codeKey);
        }

        return valid;
    }

    /**
     * 生成6位随机验证码
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }

    /**
     * 发送短信
     * 使用阿里云短信服务
     */
    private void sendSms(String phone, String code) {
        try {
            // 使用最新的阿里云SDK方式创建客户端
            com.aliyun.credentials.Client credential = new com.aliyun.credentials.Client();

            com.aliyun.teaopenapi.models.Config config = new com.aliyun.teaopenapi.models.Config()
                    .setCredential(credential)
                    .setAccessKeyId(accessKeyId)
                    .setAccessKeySecret(accessKeySecret)
                    .setEndpoint("dypnsapi.aliyuncs.com");

            com.aliyun.dypnsapi20170525.Client client = new com.aliyun.dypnsapi20170525.Client(config);

            com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest request =
                new com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest()
                    .setPhoneNumber(phone)
                    .setSignName(signName)
                    .setTemplateCode(templateCode)
                    .setTemplateParam("{\"code\":\"" + code + "\",\"min\":\"5\"}");

            com.aliyun.teautil.models.RuntimeOptions runtime = new com.aliyun.teautil.models.RuntimeOptions();
            com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse resp =
                client.sendSmsVerifyCodeWithOptions(request, runtime);
            // 检查发送结果
            if (resp.getBody() != null && "OK".equals(resp.getBody().getCode())) {
                logger.info("阿里云短信发送成功，手机号: {}, 验证码: {}", phone, code);
            } else {
                String errorMsg = resp.getBody() != null ? resp.getBody().getMessage() : "响应为空";
                throw new RuntimeException("阿里云短信发送失败: " + errorMsg);
            }

        } catch (com.aliyun.tea.TeaException e) {
            logger.error("阿里云短信服务异常，手机号: {}, 错误: {}", phone, e.getMessage(), e);
            throw new RuntimeException("短信发送失败: " + e.getMessage());
        } catch (Exception e) {
            logger.error("发送阿里云短信失败，手机号: {}, 错误: {}", phone, e.getMessage(), e);
            throw new RuntimeException("短信发送失败: " + e.getMessage());
        }
    }
}