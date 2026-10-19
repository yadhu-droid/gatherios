package com.app.dearly.service.impl;

import com.app.dearly.dao.TemplatePreviewStatDao;
import com.app.dearly.entity.TemplatePreviewStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplatePreviewService {

    private static final Logger log = LoggerFactory.getLogger(TemplatePreviewService.class);

    private final TemplatePreviewStatDao dao;

    public TemplatePreviewService(TemplatePreviewStatDao dao) {
        this.dao = dao;
    }

    /** One upsert. Runs off the request thread so the click handler can return immediately. */
    @Async
    public void incrementAsync(int templateId) {
        try {
            dao.increment(templateId);
        } catch (Exception e) {
            log.warn("Template preview increment failed for {}: {}", templateId, e.getMessage());
        }
    }

    public List<TemplatePreviewStat> list() {
        return dao.findAllByOrderByViewCountDesc();
    }
}
