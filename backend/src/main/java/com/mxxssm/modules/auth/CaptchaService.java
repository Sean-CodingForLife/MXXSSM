package com.mxxssm.modules.auth;

import com.mxxssm.config.AppProperties;
import jakarta.servlet.http.HttpSession;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;

@Service
public class CaptchaService {

    private static final String SESSION_CODE = "MXXSSM_CAPTCHA_CODE";
    private static final String SESSION_EXPIRES = "MXXSSM_CAPTCHA_EXPIRES";
    private static final char[] ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int CODE_LENGTH = 4;

    private final AppProperties properties;
    private final SecureRandom random = new SecureRandom();

    public CaptchaService(AppProperties properties) {
        this.properties = properties;
    }

    public CaptchaResponse generate(HttpSession session) {
        String code = randomCode();
        int width = properties.getCaptcha().getWidth();
        int height = properties.getCaptcha().getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setColor(new Color(246, 248, 252));
            graphics.fillRect(0, 0, width, height);

            graphics.setStroke(new BasicStroke(1.2f));
            for (int i = 0; i < 6; i++) {
                graphics.setColor(new Color(120 + random.nextInt(100), 140 + random.nextInt(90),
                        180 + random.nextInt(70), 90));
                graphics.drawLine(random.nextInt(width), random.nextInt(height),
                        random.nextInt(width), random.nextInt(height));
            }

            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, height - 14));
            for (int i = 0; i < code.length(); i++) {
                graphics.setColor(new Color(30 + random.nextInt(60), 50 + random.nextInt(70),
                        120 + random.nextInt(90)));
                double angle = (random.nextDouble() - 0.5) * 0.5;
                int x = 14 + i * ((width - 24) / CODE_LENGTH);
                int y = height - 12 + random.nextInt(7);
                graphics.rotate(angle, x, y);
                graphics.drawString(String.valueOf(code.charAt(i)), x, y);
                graphics.rotate(-angle, x, y);
            }

            for (int i = 0; i < 24; i++) {
                graphics.setColor(new Color(random.nextInt(255), random.nextInt(255), random.nextInt(255), 70));
                graphics.fillOval(random.nextInt(width), random.nextInt(height), 2, 2);
            }
        } finally {
            graphics.dispose();
        }

        session.setAttribute(SESSION_CODE, code);
        session.setAttribute(SESSION_EXPIRES,
                System.currentTimeMillis() + properties.getCaptcha().getTtlSeconds() * 1000L);

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            String base64 = Base64.getEncoder().encodeToString(output.toByteArray());
            return new CaptchaResponse("data:image/png;base64," + base64, true,
                    properties.getCaptcha().getTtlSeconds());
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to generate captcha image", exception);
        }
    }

    public boolean verify(HttpSession session, String input) {
        Object expected = session.getAttribute(SESSION_CODE);
        Object expires = session.getAttribute(SESSION_EXPIRES);
        session.removeAttribute(SESSION_CODE);
        session.removeAttribute(SESSION_EXPIRES);
        if (expected == null || expires == null || input == null) {
            return false;
        }
        if (System.currentTimeMillis() > (Long) expires) {
            return false;
        }
        return expected.toString().equalsIgnoreCase(input.trim());
    }

    private String randomCode() {
        StringBuilder builder = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            builder.append(ALPHABET[random.nextInt(ALPHABET.length)]);
        }
        return builder.toString();
    }
}
