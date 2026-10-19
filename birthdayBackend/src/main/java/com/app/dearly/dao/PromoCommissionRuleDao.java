package com.app.dearly.dao;

import com.app.dearly.entity.PromoCommissionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromoCommissionRuleDao extends JpaRepository<PromoCommissionRule, Long> {
    List<PromoCommissionRule> findByScopeOrderByTemplateIdAsc(String scope);

    List<PromoCommissionRule> findByScopeAndPromoLinkIdOrderByTemplateIdAsc(String scope, UUID promoLinkId);

    Optional<PromoCommissionRule> findByScopeAndPromoLinkIdAndTemplateId(String scope, UUID promoLinkId, int templateId);

    Optional<PromoCommissionRule> findByScopeAndTemplateIdAndPromoLinkIdIsNull(String scope, int templateId);

    List<PromoCommissionRule> findByScopeAndPromoLinkIdIsNull(String scope);
}
