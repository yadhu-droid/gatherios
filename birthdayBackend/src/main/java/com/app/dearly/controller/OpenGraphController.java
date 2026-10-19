package com.app.dearly.controller;

import com.app.dearly.dto.SurpriseResponse;
import com.app.dearly.service.OgCardService;
import com.app.dearly.service.SurpriseService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Serves a lightweight Open Graph / Twitter Card HTML document for invitation IDs.
 * Useful when a reverse proxy routes social crawlers here, or for debugging previews.
 * Humans are soft-redirected to the Angular wedding viewer.
 */
@RestController
@RequestMapping("/api")
public class OpenGraphController {

    private static final String SITE_NAME = "Gatherios";
    private static final String DEFAULT_ORIGIN = "https://gatherios.com";

    private final SurpriseService surpriseService;

    public OpenGraphController(SurpriseService surpriseService) {
        this.surpriseService = surpriseService;
    }

    @GetMapping(value = "/surprises/{id}/open-graph", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> openGraphCard(@PathVariable UUID id) {
        try {
            SurpriseResponse surprise = surpriseService.getSurpriseById(id);
            String origin = DEFAULT_ORIGIN;
            String pageUrl = origin + "/wedding-view?id=" + id;
            String apiBase = origin + "/api";

            String names = OgCardService.formatCoupleTitle(surprise.getName());
            String dateLabel = formatDate(surprise.getBirthdate());
            String venue = surprise.getPromptVal() != null ? surprise.getPromptVal().trim() : "";
            String inviteLine = extractInviteLine(surprise.getMessage());

            StringBuilder desc = new StringBuilder("You're Invited to Our Wedding");
            if (!dateLabel.isEmpty()) desc.append(" · ").append(dateLabel);
            if (!venue.isEmpty()) desc.append(" · ").append(venue);
            desc.append(". ");
            if (!inviteLine.isEmpty()) {
                desc.append(inviteLine);
            } else {
                desc.append("Join ").append(names)
                        .append(" for a celebration of love. Open this invitation to view every detail.");
            }
            desc.append(" — ").append(SITE_NAME);
            String description = desc.length() > 300 ? desc.substring(0, 300) : desc.toString();

            // Personalized card always includes image + heart names
            String image = apiBase + "/surprises/" + id + "/og-card";

            String html = buildHtml(names, description, image, pageUrl);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=600")
                    .body(html);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    private static String extractInviteLine(String message) {
        if (message == null || message.isBlank()) return "";
        return message.split("\\|\\|\\|", 2)[0].trim();
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

    private static String esc(String value) {
        if (value == null) return "";
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String buildHtml(String title, String description, String image, String url) {
        String t = esc(title);
        String d = esc(description);
        String img = esc(image);
        String u = esc(url);
        return "<!DOCTYPE html>\n"
                + "<html lang=\"en\"><head>\n"
                + "<meta charset=\"utf-8\">\n"
                + "<title>" + t + " | " + SITE_NAME + "</title>\n"
                + "<meta name=\"description\" content=\"" + d + "\">\n"
                + "<link rel=\"canonical\" href=\"" + u + "\">\n"
                + "<meta property=\"og:type\" content=\"website\">\n"
                + "<meta property=\"og:site_name\" content=\"" + SITE_NAME + "\">\n"
                + "<meta property=\"og:title\" content=\"" + t + "\">\n"
                + "<meta property=\"og:description\" content=\"" + d + "\">\n"
                + "<meta property=\"og:url\" content=\"" + u + "\">\n"
                + "<meta property=\"og:image\" content=\"" + img + "\">\n"
                + "<meta property=\"og:image:secure_url\" content=\"" + img + "\">\n"
                + "<meta property=\"og:image:type\" content=\"image/jpeg\">\n"
                + "<meta property=\"og:image:width\" content=\"1200\">\n"
                + "<meta property=\"og:image:height\" content=\"630\">\n"
                + "<meta property=\"og:image:alt\" content=\"" + t + " — Wedding Invitation\">\n"
                + "<meta name=\"twitter:card\" content=\"summary_large_image\">\n"
                + "<meta name=\"twitter:title\" content=\"" + t + "\">\n"
                + "<meta name=\"twitter:description\" content=\"" + d + "\">\n"
                + "<meta name=\"twitter:image\" content=\"" + img + "\">\n"
                + "<meta http-equiv=\"refresh\" content=\"0;url=" + u + "\">\n"
                + "</head><body>\n"
                + "<img src=\"" + img + "\" alt=\"" + t + "\" width=\"1200\" height=\"630\">\n"
                + "<p><a href=\"" + u + "\">Open wedding invitation</a></p>\n"
                + "<script>location.replace(" + jsonString(url) + ");</script>\n"
                + "</body></html>";
    }

    private static String jsonString(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
