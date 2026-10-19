package com.app.dearly.controller;

import com.app.dearly.service.impl.TemplatePreviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/template-previews")
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS
})
public class TemplatePreviewController {

    private final TemplatePreviewService templatePreviewService;

    public TemplatePreviewController(TemplatePreviewService templatePreviewService) {
        this.templatePreviewService = templatePreviewService;
    }

    private boolean isAdmin(String authHeader) {
        return authHeader != null && authHeader.equals("Bearer mock-admin-token-12345");
    }

    /** Public, fire-and-forget. Returns 202 before the counter is written. */
    @PostMapping
    public ResponseEntity<Void> record(@RequestBody(required = false) Map<String, Object> body) {
        Integer templateId = readTemplateId(body);
        if (templateId != null) {
            templatePreviewService.incrementAsync(templateId);
        }
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestHeader(value = "Authorization", required = false) String auth) {
        if (!isAdmin(auth)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        return ResponseEntity.ok(templatePreviewService.list());
    }

    private Integer readTemplateId(Map<String, Object> body) {
        if (body == null || body.get("templateId") == null) return null;
        Object raw = body.get("templateId");
        int id;
        if (raw instanceof Number number) {
            id = number.intValue();
        } else {
            try {
                id = Integer.parseInt(String.valueOf(raw).trim());
            } catch (NumberFormatException e) {
                return null;
            }
        }
        if (id < 1 || id > 99) return null;
        return id;
    }
}
