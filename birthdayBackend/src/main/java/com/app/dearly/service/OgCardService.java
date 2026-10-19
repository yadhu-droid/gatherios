package com.app.dearly.service;

import com.app.dearly.dto.SurpriseResponse;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Locale;

/**
 * Builds a 1200×630 Open Graph share card with couple photo (when available),
 * names joined by a heart, invite subtitle, wedding date, and light branding.
 */
@Service
public class OgCardService {

    public static final int WIDTH = 1200;
    public static final int HEIGHT = 630;

    public byte[] renderJpeg(SurpriseResponse surprise) throws Exception {
        BufferedImage canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            BufferedImage photo = decodeFirstPhoto(surprise);
            if (photo != null) {
                drawCoverPhoto(g, photo);
                // Soft dark veil so text stays readable over any photo
                g.setPaint(new GradientPaint(0, HEIGHT * 0.35f, new Color(20, 12, 8, 20),
                        0, HEIGHT, new Color(20, 12, 8, 210)));
                g.fillRect(0, 0, WIDTH, HEIGHT);
            } else {
                drawElegantFallbackBackground(g, surprise.getTemplate());
            }

            // Bottom content panel
            g.setColor(new Color(255, 250, 240, photo != null ? 235 : 245));
            g.fill(new RoundRectangle2D.Float(48, 300, WIDTH - 96, 282, 28, 28));

            // Gold accent line
            g.setColor(new Color(201, 169, 110));
            g.fillRect(48, 300, WIDTH - 96, 4);

            String title = formatCoupleTitle(surprise.getName());
            String subtitle = "You're Invited to Our Wedding";
            String dateLabel = formatDate(surprise.getBirthdate());
            String venue = surprise.getPromptVal() != null ? surprise.getPromptVal().trim() : "";

            // Subtitle
            g.setFont(new Font(Font.SERIF, Font.ITALIC, 28));
            g.setColor(new Color(140, 110, 60));
            drawCentered(g, subtitle, WIDTH / 2, 360);

            // Couple names with drawn heart (emoji fonts are unreliable on servers)
            drawCoupleNamesWithHeart(g, title, WIDTH / 2, 430);

            // Date + venue
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
            g.setColor(new Color(70, 55, 40));
            String meta = dateLabel;
            if (!venue.isEmpty()) {
                meta = meta.isEmpty() ? venue : meta + "  ·  " + venue;
            }
            if (!meta.isEmpty()) {
                drawCentered(g, truncate(meta, 64), WIDTH / 2, 490);
            }

            // Branding
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            g.setColor(new Color(170, 124, 17));
            drawCentered(g, "GATHERIOS", WIDTH / 2, 545);
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
            g.setColor(new Color(150, 130, 100));
            drawCentered(g, "Digital Wedding Invitations", WIDTH / 2, 568);
        } finally {
            g.dispose();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(canvas, "jpg", out);
        return out.toByteArray();
    }

    /** Turns "Bride & Groom" into display title used on the card and in OG text. */
    public static String formatCoupleTitle(String rawName) {
        if (rawName == null || rawName.isBlank()) return "Our Wedding";
        String raw = rawName.trim();
        String[] parts = raw.split("\\s*&\\s*|\\s+and\\s+", 2);
        if (parts.length == 2 && !parts[0].isBlank() && !parts[1].isBlank()) {
            return parts[0].trim() + " ❤️ " + parts[1].trim();
        }
        return raw;
    }

    private void drawCoupleNamesWithHeart(Graphics2D g, String title, int cx, int cy) {
        // Prefer splitting on heart markers; fall back to drawing the whole string
        String left;
        String right;
        if (title.contains(" ❤️ ")) {
            String[] parts = title.split(" ❤️ ", 2);
            left = parts[0];
            right = parts[1];
        } else if (title.contains(" ❤ ")) {
            String[] parts = title.split(" ❤ ", 2);
            left = parts[0];
            right = parts[1];
        } else if (title.contains(" & ")) {
            String[] parts = title.split(" & ", 2);
            left = parts[0];
            right = parts[1];
        } else {
            g.setFont(fitFont(g, title, Font.SERIF, Font.BOLD, 54, WIDTH - 160));
            g.setColor(new Color(74, 40, 30));
            drawCentered(g, title, cx, cy);
            return;
        }

        Font nameFont = fitFont(g, left + "   " + right, Font.SERIF, Font.BOLD, 52, WIDTH - 220);
        g.setFont(nameFont);
        FontMetrics fm = g.getFontMetrics();
        int heartGap = Math.max(36, fm.getHeight() / 2);
        int total = fm.stringWidth(left) + heartGap + fm.stringWidth(right);
        int x = cx - total / 2;
        int baseline = cy;

        g.setColor(new Color(74, 40, 30));
        g.drawString(left, x, baseline);
        int heartX = x + fm.stringWidth(left) + heartGap / 2;
        int heartY = baseline - fm.getAscent() / 2 + 4;
        drawHeart(g, heartX, heartY, Math.max(14, fm.getAscent() / 3));
        g.setColor(new Color(74, 40, 30));
        g.drawString(right, x + fm.stringWidth(left) + heartGap, baseline);
    }

    private void drawHeart(Graphics2D g, int cx, int cy, int size) {
        GeneralPath heart = new GeneralPath();
        heart.moveTo(cx, cy + size * 0.35);
        heart.curveTo(cx, cy, cx - size, cy - size * 0.15, cx - size, cy - size * 0.55);
        heart.curveTo(cx - size, cy - size, cx, cy - size * 0.85, cx, cy - size * 0.45);
        heart.curveTo(cx, cy - size * 0.85, cx + size, cy - size, cx + size, cy - size * 0.55);
        heart.curveTo(cx + size, cy - size * 0.15, cx, cy, cx, cy + size * 0.35);
        heart.closePath();
        g.setColor(new Color(196, 70, 90));
        g.fill(heart);
    }

    private void drawCoverPhoto(Graphics2D g, BufferedImage photo) {
        double scale = Math.max((double) WIDTH / photo.getWidth(), (double) HEIGHT / photo.getHeight());
        int w = (int) Math.round(photo.getWidth() * scale);
        int h = (int) Math.round(photo.getHeight() * scale);
        int x = (WIDTH - w) / 2;
        int y = (HEIGHT - h) / 2;
        g.drawImage(photo, x, y, w, h, null);
    }

    private void drawElegantFallbackBackground(Graphics2D g, int template) {
        Color top;
        Color bottom;
        switch (template) {
            case 18, 15, 16 -> {
                top = new Color(251, 246, 236);
                bottom = new Color(232, 214, 180);
            }
            case 17 -> {
                top = new Color(248, 244, 250);
                bottom = new Color(220, 210, 230);
            }
            default -> {
                top = new Color(250, 240, 245);
                bottom = new Color(230, 200, 210);
            }
        }
        g.setPaint(new GradientPaint(0, 0, top, 0, HEIGHT, bottom));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // Soft decorative orbs
        g.setColor(new Color(201, 169, 110, 40));
        g.fill(new Ellipse2D.Float(-80, -60, 320, 280));
        g.fill(new Ellipse2D.Float(WIDTH - 260, HEIGHT - 240, 340, 300));
        g.setColor(new Color(170, 124, 17, 28));
        g.fill(new Ellipse2D.Float(WIDTH / 2f - 180, 40, 360, 200));
    }

    private BufferedImage decodeFirstPhoto(SurpriseResponse surprise) {
        if (surprise.getPhotos() == null || surprise.getPhotos().isEmpty()) return null;
        String raw = surprise.getPhotos().get(0);
        if (raw == null || raw.isBlank()) return null;
        try {
            String payload = raw.trim();
            if (payload.startsWith("data:")) {
                int comma = payload.indexOf(',');
                if (comma < 0) return null;
                payload = payload.substring(comma + 1);
            }
            byte[] bytes = Base64.getDecoder().decode(payload);
            if (bytes.length == 0 || bytes.length > 8_000_000) return null;
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            return null;
        }
    }

    private static Font fitFont(Graphics2D g, String text, String family, int style, int maxSize, int maxWidth) {
        int size = maxSize;
        while (size >= 22) {
            Font font = new Font(family, style, size);
            if (g.getFontMetrics(font).stringWidth(text) <= maxWidth) {
                return font;
            }
            size -= 2;
        }
        return new Font(family, style, 22);
    }

    private static void drawCentered(Graphics2D g, String text, int cx, int baselineY) {
        FontMetrics fm = g.getFontMetrics();
        int x = cx - fm.stringWidth(text) / 2;
        g.drawString(text, x, baselineY);
    }

    private static String truncate(String value, int max) {
        if (value.length() <= max) return value;
        return value.substring(0, Math.max(0, max - 1)).trim() + "…";
    }

    private static String formatDate(String raw) {
        if (raw == null || raw.isBlank()) return "";
        try {
            LocalDate date = LocalDate.parse(raw.trim());
            return date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH));
        } catch (DateTimeParseException ignored) {
            return raw.trim();
        }
    }
}
