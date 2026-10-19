package com.app.dearly.dao;

import com.app.dearly.entity.TemplatePreviewStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TemplatePreviewStatDao extends JpaRepository<TemplatePreviewStat, Integer> {

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO template_preview_stats (template_id, view_count, updated_at)
            VALUES (:templateId, 1, NOW())
            ON CONFLICT (template_id)
            DO UPDATE SET view_count = template_preview_stats.view_count + 1,
                          updated_at = NOW()
            """, nativeQuery = true)
    void increment(@Param("templateId") int templateId);

    List<TemplatePreviewStat> findAllByOrderByViewCountDesc();
}
