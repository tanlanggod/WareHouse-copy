package com.warehouse.service;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import com.warehouse.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.FastByteArrayOutputStream;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 */
@Service
public class CaptchaService {

    private static final Logger logger = LoggerFactory.getLogger(CaptchaService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${app.captcha.expire-minutes:5}")
    private int captchaExpireMinutes;

    @Value("${app.captcha.length:4}")
    private int captchaLength;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private final DefaultKaptcha captchaProducer;
    private final Random random = new Random();

    public CaptchaService() {
        // 配置验证码生成器
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", "yes");
        properties.setProperty("kaptcha.border.color", "220,220,220");
        properties.setProperty("kaptcha.textproducer.font.color", "38,29,12");
        properties.setProperty("kaptcha.image.width", "120");
        properties.setProperty("kaptcha.image.height", "40");
        properties.setProperty("kaptcha.textproducer.font.size", "30");
        properties.setProperty("kaptcha.session.key", "code");
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        properties.setProperty("kaptcha.textproducer.char.space", "8");
        properties.setProperty("kaptcha.textproducer.font.names", "Arial,Courier");

        Config config = new Config(properties);
        captchaProducer = new DefaultKaptcha();
        captchaProducer.setConfig(config);
    }

    /**
     * 生成验证码
     * @return 验证码信息（包含图片Base64和验证码ID）
     */
    public Result<Map<String, String>> generateCaptcha() {
        try {
            // 生成验证码文本
            String captchaText = generateCaptchaText();

            // 生成验证码图片
            BufferedImage image = captchaProducer.createImage(captchaText);

            // 生成验证码ID
            String captchaId = UUID.randomUUID().toString();

            // 将验证码存储到Redis
            String key = CAPTCHA_PREFIX + captchaId;
            redisTemplate.opsForValue().set(key, captchaText, captchaExpireMinutes, TimeUnit.MINUTES);

            // 将图片转换为Base64
            String imageBase64 = imageToBase64(image);

            Map<String, String> result = new HashMap<>();
            result.put("captchaId", captchaId);
            result.put("captchaImage", "data:image/png;base64," + imageBase64);

            logger.debug("生成图片验证码成功，ID: {}, 图片验证码: {}", captchaId, captchaText);

            return Result.success("图片验证码生成成功", result);

        } catch (Exception e) {
            logger.error("生成图片验证码失败: {}", e.getMessage(), e);
            return Result.error("图片验证码生成失败：" + e.getMessage());
        }
    }

    /**
     * 验证验证码（不删除，用于中间验证步骤如发送短信）
     * @param captchaId 验证码ID
     * @param userInputCaptcha 用户输入的验证码
     * @return 验证结果
     */
    public boolean validateCaptchaWithoutDelete(String captchaId, String userInputCaptcha) {
        if (captchaId == null || userInputCaptcha == null) {
            return false;
        }

        try {
            String key = CAPTCHA_PREFIX + captchaId;
            String storedCaptcha = (String) redisTemplate.opsForValue().get(key);

            if (storedCaptcha == null) {
                logger.warn("图片验证码已过期或不存在，ID: {}", captchaId);
                return false;
            }

            // 不区分大小写比较，不删除验证码
            boolean isValid = storedCaptcha.equalsIgnoreCase(userInputCaptcha.trim());

            if (isValid) {
                logger.debug("图片验证码验证成功（未删除），ID: {}", captchaId);
            } else {
                logger.warn("图片验证码验证失败，ID: {}, 存储值: {}, 用户输入: {}",
                    captchaId, storedCaptcha, userInputCaptcha);
            }

            return isValid;

        } catch (Exception e) {
            logger.error("图片验证码验证失败，ID: {}, 错误: {}", captchaId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 验证验证码（验证成功后删除，用于最终登录验证）
     * @param captchaId 验证码ID
     * @param userInputCaptcha 用户输入的验证码
     * @return 验证结果
     */
    public boolean validateCaptcha(String captchaId, String userInputCaptcha) {
        if (captchaId == null || userInputCaptcha == null) {
            return false;
        }

        try {
            String key = CAPTCHA_PREFIX + captchaId;
            String storedCaptcha = (String) redisTemplate.opsForValue().get(key);

            if (storedCaptcha == null) {
                logger.warn("图片验证码已过期或不存在，ID: {}", captchaId);
                return false;
            }

            // 不区分大小写比较
            boolean isValid = storedCaptcha.equalsIgnoreCase(userInputCaptcha.trim());

            if (isValid) {
                // 验证成功后删除验证码（一次性使用）
                redisTemplate.delete(key);
                logger.debug("图片验证码验证成功并删除，ID: {}", captchaId);
            } else {
                logger.warn("图片验证码验证失败，ID: {}, 存储值: {}, 用户输入: {}",
                    captchaId, storedCaptcha, userInputCaptcha);
            }

            return isValid;

        } catch (Exception e) {
            logger.error("图片验证码验证失败，ID: {}, 错误: {}", captchaId, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 生成验证码文本
     */
    private String generateCaptchaText() {
        String chars = "0123456789";
        StringBuilder captcha = new StringBuilder();

        for (int i = 0; i < captchaLength; i++) {
            captcha.append(chars.charAt(random.nextInt(chars.length())));
        }

        return captcha.toString();
    }

    /**
     * 图片转Base64
     */
    private String imageToBase64(BufferedImage image) throws IOException {
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        return Base64.getEncoder().encodeToString(os.toByteArray());
    }

    /**
     * 清理过期验证码（可用于定时任务）
     */
    public void cleanExpiredCaptchas() {
        try {
            // 由于Redis会自动过期，这里主要是统计清理
            logger.debug("图片验证码清理任务执行完成");
        } catch (Exception e) {
            logger.error("图片清理过期验证码失败: {}", e.getMessage(), e);
        }
    }
}