package com.app.dearly.service.impl;

import com.app.dearly.dao.*;
import com.app.dearly.dto.*;
import com.app.dearly.entity.*;
import com.app.dearly.service.PromotionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class PromotionServiceImpl implements PromotionService {

    private static final Logger log = LoggerFactory.getLogger(PromotionServiceImpl.class);

    private static final String SCOPE_GLOBAL = "GLOBAL";
    private static final String SCOPE_LINK = "LINK";
    private static final String TYPE_PERCENT = "PERCENT";
    private static final String TYPE_FLAT = "FLAT";
    /** One commission rate applies to every template. */
    private static final int ALL_TEMPLATES = 0;

    private final InfluencerDao influencerDao;
    private final PromoLinkDao promoLinkDao;
    private final PromoCommissionRuleDao ruleDao;
    private final PromoConversionDao conversionDao;
    private final PaymentDao paymentDao;
    private final InfluencerBalanceDao balanceDao;
    private final InfluencerPayoutDao payoutDao;

    public PromotionServiceImpl(
            InfluencerDao influencerDao,
            PromoLinkDao promoLinkDao,
            PromoCommissionRuleDao ruleDao,
            PromoConversionDao conversionDao,
            PaymentDao paymentDao,
            InfluencerBalanceDao balanceDao,
            InfluencerPayoutDao payoutDao
    ) {
        this.influencerDao = influencerDao;
        this.promoLinkDao = promoLinkDao;
        this.ruleDao = ruleDao;
        this.conversionDao = conversionDao;
        this.paymentDao = paymentDao;
        this.balanceDao = balanceDao;
        this.payoutDao = payoutDao;
    }

    @Override
    public List<InfluencerResponse> listInfluencers() {
        return influencerDao.findAll().stream()
                .sorted(Comparator.comparing(Influencer::getCreatedAt).reversed())
                .map(this::toInfluencerResponse)
                .collect(Collectors.toList());
    }

    @Override
    public InfluencerResponse createInfluencer(CreateInfluencerRequest request) {
        String email = requireText(request.getEmail(), "Email").trim().toLowerCase();
        String name = requireText(request.getName(), "Name").trim();
        String password = requireText(request.getPassword(), "Password").trim();

        Optional<Influencer> existingOpt = influencerDao.findByEmailIgnoreCase(email);
        if (existingOpt.isPresent()) {
            Influencer existing = existingOpt.get();
            existing.setName(name);
            existing.setPassword(password);
            if (request.getNotes() != null) {
                existing.setNotes(trimToNull(request.getNotes()));
            }
            if (request.getUpiId() != null) {
                existing.setUpiId(trimToNull(request.getUpiId()));
            }
            existing.setActive(true);
            influencerDao.save(existing);

            promoLinkDao.findByInfluencerId(existing.getId()).ifPresentOrElse(link -> {
                if (!link.isActive()) {
                    link.setActive(true);
                    promoLinkDao.save(link);
                }
            }, () -> {
                String code = normalizeCode(request.getPromoCode());
                if (code == null || code.isBlank()) {
                    code = generateCode(name);
                }
                if (promoLinkDao.existsByCodeIgnoreCase(code)) {
                    throw new IllegalArgumentException("Promo code already in use: " + code);
                }
                PromoLink link = new PromoLink();
                link.setInfluencerId(existing.getId());
                link.setCode(code.toUpperCase());
                link.setActive(true);
                promoLinkDao.save(link);
            });

            InfluencerResponse response = toInfluencerResponse(existing);
            response.setAlreadyExisted(true);
            ensureInfluencerBalance(existing.getId());
            return response;
        }

        String code = normalizeCode(request.getPromoCode());
        if (code == null || code.isBlank()) {
            code = generateCode(name);
        }
        if (promoLinkDao.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Promo code already in use: " + code);
        }

        Influencer influencer = new Influencer();
        influencer.setName(name);
        influencer.setEmail(email);
        influencer.setPassword(password);
        influencer.setNotes(trimToNull(request.getNotes()));
        influencer.setUpiId(trimToNull(request.getUpiId()));
        influencer.setActive(true);
        influencer = influencerDao.save(influencer);

        PromoLink link = new PromoLink();
        link.setInfluencerId(influencer.getId());
        link.setCode(code.toUpperCase());
        link.setActive(true);
        promoLinkDao.save(link);

        ensureInfluencerBalance(influencer.getId());

        InfluencerResponse response = toInfluencerResponse(influencer);
        response.setAlreadyExisted(false);
        return response;
    }

    @Override
    public InfluencerResponse updateInfluencerProfile(UUID id, UpdateInfluencerProfileRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Update details are required");
        }
        Influencer influencer = influencerDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Influencer not found"));
        String name = requireText(request.getName(), "Name").trim();
        influencer.setName(name);
        influencer.setUpiId(trimToNull(request.getUpiId()));
        influencer.setNotes(trimToNull(request.getNotes()));
        influencerDao.save(influencer);
        return toInfluencerResponse(influencer);
    }

    @Override
    public InfluencerResponse updateInfluencerStatus(UUID id, boolean active) {
        Influencer influencer = influencerDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Influencer not found"));
        influencer.setActive(active);
        influencerDao.save(influencer);
        if (!active) {
            promoLinkDao.findByInfluencerId(id).ifPresent(link -> {
                link.setActive(false);
                promoLinkDao.save(link);
            });
        }
        return toInfluencerResponse(influencer);
    }

    @Override
    public InfluencerResponse updatePromoLinkStatus(UUID influencerId, boolean active) {
        Influencer influencer = influencerDao.findById(influencerId)
                .orElseThrow(() -> new NoSuchElementException("Influencer not found"));
        PromoLink link = promoLinkDao.findByInfluencerId(influencerId)
                .orElseThrow(() -> new NoSuchElementException("Promo link not found"));
        if (active && !influencer.isActive()) {
            throw new IllegalArgumentException("Activate the influencer before enabling their link");
        }
        link.setActive(active);
        promoLinkDao.save(link);
        return toInfluencerResponse(influencer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionRuleDto> listGlobalRules() {
        return findSingleGlobalRule()
                .map(r -> List.of(toRuleDto(r)))
                .orElse(List.of());
    }

    @Override
    public CommissionRuleDto upsertGlobalRule(UpsertCommissionRuleRequest request) {
        validateRule(request.getType(), request.getValue());
        List<PromoCommissionRule> existing = ruleDao.findByScopeAndPromoLinkIdIsNull(SCOPE_GLOBAL);
        PromoCommissionRule rule = existing.stream()
                .filter(r -> r.getTemplateId() == ALL_TEMPLATES)
                .findFirst()
                .orElseGet(() -> existing.isEmpty() ? new PromoCommissionRule() : existing.get(0));
        rule.setScope(SCOPE_GLOBAL);
        rule.setPromoLinkId(null);
        rule.setTemplateId(ALL_TEMPLATES);
        rule.setType(normalizeType(request.getType()));
        rule.setValue(request.getValue());
        rule.setUpdatedAt(LocalDateTime.now());
        PromoCommissionRule saved = ruleDao.save(rule);
        existing.stream()
                .filter(r -> !Objects.equals(r.getId(), saved.getId()))
                .forEach(ruleDao::delete);
        return toRuleDto(saved);
    }

    @Override
    public void deleteGlobalRule() {
        ruleDao.findByScopeAndPromoLinkIdIsNull(SCOPE_GLOBAL).forEach(ruleDao::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommissionRuleDto> listLinkOverrides(UUID influencerId) {
        PromoLink link = promoLinkDao.findByInfluencerId(influencerId)
                .orElseThrow(() -> new NoSuchElementException("Promo link not found"));
        return findSingleLinkRule(link.getId())
                .map(r -> List.of(toRuleDto(r)))
                .orElse(List.of());
    }

    @Override
    public CommissionRuleDto upsertLinkOverride(UUID influencerId, UpsertCommissionRuleRequest request) {
        validateRule(request.getType(), request.getValue());
        PromoLink link = promoLinkDao.findByInfluencerId(influencerId)
                .orElseThrow(() -> new NoSuchElementException("Promo link not found"));
        List<PromoCommissionRule> existing =
                ruleDao.findByScopeAndPromoLinkIdOrderByTemplateIdAsc(SCOPE_LINK, link.getId());
        PromoCommissionRule rule = existing.stream()
                .filter(r -> r.getTemplateId() == ALL_TEMPLATES)
                .findFirst()
                .orElseGet(() -> existing.isEmpty() ? new PromoCommissionRule() : existing.get(0));
        rule.setScope(SCOPE_LINK);
        rule.setPromoLinkId(link.getId());
        rule.setTemplateId(ALL_TEMPLATES);
        rule.setType(normalizeType(request.getType()));
        rule.setValue(request.getValue());
        rule.setUpdatedAt(LocalDateTime.now());
        PromoCommissionRule saved = ruleDao.save(rule);
        existing.stream()
                .filter(r -> !Objects.equals(r.getId(), saved.getId()))
                .forEach(ruleDao::delete);
        return toRuleDto(saved);
    }

    @Override
    public void deleteLinkOverride(UUID influencerId) {
        PromoLink link = promoLinkDao.findByInfluencerId(influencerId)
                .orElseThrow(() -> new NoSuchElementException("Promo link not found"));
        ruleDao.findByScopeAndPromoLinkIdOrderByTemplateIdAsc(SCOPE_LINK, link.getId())
                .forEach(ruleDao::delete);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<InfluencerResponse> loginInfluencer(String email, String password) {
        if (email == null || password == null) return Optional.empty();
        return influencerDao.findByEmailIgnoreCase(email.trim())
                .filter(Influencer::isActive)
                .filter(i -> password.equals(i.getPassword()))
                .map(this::toInfluencerResponse);
    }

    @Override
    public InfluencerDashboardResponse getInfluencerDashboard(UUID influencerId) {
        Influencer influencer = influencerDao.findById(influencerId)
                .orElseThrow(() -> new NoSuchElementException("Influencer not found"));
        PromoLink link = promoLinkDao.findByInfluencerId(influencerId).orElse(null);
        List<PromoConversion> conversions = conversionDao.findByInfluencerIdOrderByCreatedAtDesc(influencerId);

        InfluencerDashboardResponse res = new InfluencerDashboardResponse();
        res.setInfluencerId(influencer.getId());
        res.setName(influencer.getName());
        res.setEmail(influencer.getEmail());
        res.setActive(influencer.isActive());
        if (link != null) {
            res.setPromoCode(link.getCode());
            res.setSharePath("/wedding?ref=" + link.getCode());
        }
        res.setTotalPurchases(conversions.size());
        double total = conversions.stream().mapToDouble(PromoConversion::getCommissionAmount).sum();
        InfluencerBalance balance = ensureInfluencerBalance(influencerId);
        res.setTotalEarnings(round2(total));
        res.setUnpaidEarnings(round2(balance.getUnpaidAmount()));
        res.setTotalPaid(round2(balance.getTotalPaidAmount()));
        res.setPayouts(
                payoutDao.findByInfluencerIdOrderByCreatedAtDesc(influencerId).stream()
                        .map(p -> toPayoutDto(p, influencer))
                        .collect(Collectors.toList())
        );

        List<InfluencerDashboardResponse.ConversionDto> rows = conversions.stream().map(c -> {
            InfluencerDashboardResponse.ConversionDto dto = new InfluencerDashboardResponse.ConversionDto();
            dto.setId(c.getId());
            dto.setSurpriseId(c.getSurpriseId());
            dto.setTemplateId(c.getTemplateId());
            dto.setSaleAmount(c.getSaleAmount());
            dto.setCommissionType(c.getCommissionType());
            dto.setCommissionValue(c.getCommissionValue());
            dto.setCommissionAmount(c.getCommissionAmount());
            dto.setPaidOut(c.isPaidOut());
            dto.setCreatedAt(c.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
        res.setConversions(rows);
        return res;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordConversionIfEligible(UUID surpriseId, int templateId, String promoCode, String razorpayOrderId, Double saleAmountHint) {
        // Own transaction so a promo failure cannot roll back the invitation.
        String code = resolvePromoCode(promoCode, razorpayOrderId);
        if (surpriseId == null || code == null || code.isBlank()) return;
        if (conversionDao.existsBySurpriseId(surpriseId)) return;

        Optional<PromoLink> linkOpt = promoLinkDao.findByCodeIgnoreCase(code);
        if (linkOpt.isEmpty()) {
            log.warn("Promo {} was not registered: no matching link", code);
            return;
        }
        PromoLink link = linkOpt.get();
        if (!link.isActive()) {
            log.warn("Promo {} was not registered: link is off", code);
            return;
        }

        Influencer influencer = influencerDao.findById(link.getInfluencerId()).orElse(null);
        if (influencer == null || !influencer.isActive()) {
            log.warn("Promo {} was not registered: influencer is missing or off", code);
            return;
        }

        PromoCommissionRule rule = findSingleLinkRule(link.getId())
                .or(() -> findSingleGlobalRule())
                .orElse(null);
        if (rule == null) {
            log.warn("Promo {} was not registered: no commission rule", code);
            return;
        }

        String type = normalizeType(rule.getType());
        double value = rule.getValue();
        double saleAmount = resolveSaleAmountInr(razorpayOrderId, saleAmountHint);
        double commissionAmount = computeCommissionAmount(type, value, saleAmount);
        if (commissionAmount <= 0 && value > 0 && TYPE_PERCENT.equals(type) && saleAmount <= 0) {
            log.warn("Promo {} was not registered: percent commission but sale amount was 0", code);
            return;
        }

        PromoConversion conversion = new PromoConversion();
        conversion.setInfluencerId(influencer.getId());
        conversion.setPromoLinkId(link.getId());
        conversion.setSurpriseId(surpriseId);
        conversion.setRazorpayOrderId(razorpayOrderId);
        conversion.setTemplateId(templateId);
        conversion.setSaleAmount(round2(saleAmount));
        conversion.setCommissionType(type);
        conversion.setCommissionValue(value);
        conversion.setCommissionAmount(round2(commissionAmount));
        conversion.setPaidOut(false);
        conversionDao.save(conversion);
        creditUnpaidBalance(influencer.getId(), round2(commissionAmount));
    }

    @Override
    public List<InfluencerResponse> listInfluencerPaymentBalances() {
        return influencerDao.findAll().stream()
                .sorted(Comparator.comparing(Influencer::getName, String.CASE_INSENSITIVE_ORDER))
                .map(inf -> {
                    ensureInfluencerBalance(inf.getId());
                    return toInfluencerResponse(inf);
                })
                .collect(Collectors.toList());
    }

    @Override
    public InfluencerPayoutDto recordInfluencerPayout(UUID influencerId, RecordInfluencerPayoutRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payout details are required");
        }
        Influencer influencer = influencerDao.findById(influencerId)
                .orElseThrow(() -> new NoSuchElementException("Influencer not found"));

        double amount = round2(request.getAmount());
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        ensureInfluencerBalance(influencerId);
        InfluencerBalance balance = balanceDao.findByInfluencerIdForUpdate(influencerId)
                .orElseThrow(() -> new IllegalStateException("Influencer balance missing"));

        double unpaid = round2(balance.getUnpaidAmount());
        if (amount > unpaid + 0.001) {
            throw new IllegalArgumentException(
                    "Payment amount ₹" + amount + " exceeds unpaid balance ₹" + unpaid
            );
        }

        double remaining = round2(Math.max(0, unpaid - amount));
        balance.setUnpaidAmount(remaining);
        balance.setTotalPaidAmount(round2(balance.getTotalPaidAmount() + amount));
        balance.setUpdatedAt(LocalDateTime.now());
        balanceDao.save(balance);

        InfluencerPayout payout = new InfluencerPayout();
        payout.setInfluencerId(influencerId);
        payout.setAmount(amount);
        payout.setBalanceAfter(remaining);
        payout.setNote(trimToNull(request.getNote()));
        payout.setCreatedAt(LocalDateTime.now());
        payout = payoutDao.save(payout);

        return toPayoutDto(payout, influencer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InfluencerPayoutDto> listInfluencerPayouts(UUID influencerId) {
        Influencer influencer = influencerDao.findById(influencerId)
                .orElseThrow(() -> new NoSuchElementException("Influencer not found"));
        return payoutDao.findByInfluencerIdOrderByCreatedAtDesc(influencerId).stream()
                .map(p -> toPayoutDto(p, influencer))
                .collect(Collectors.toList());
    }

    /**
     * Creates unpaid balance row once per influencer. If it already exists, leave it untouched.
     * New rows are seeded from existing conversion totals so legacy earnings are not lost.
     */
    private InfluencerBalance ensureInfluencerBalance(UUID influencerId) {
        Optional<InfluencerBalance> existing = balanceDao.findByInfluencerId(influencerId);
        if (existing.isPresent()) {
            return existing.get();
        }
        InfluencerBalance balance = new InfluencerBalance();
        balance.setInfluencerId(influencerId);
        double earned = round2(conversionDao.sumCommissionByInfluencerId(influencerId));
        balance.setUnpaidAmount(Math.max(0, earned));
        balance.setTotalPaidAmount(0);
        balance.setUpdatedAt(LocalDateTime.now());
        try {
            return balanceDao.save(balance);
        } catch (Exception race) {
            // Concurrent create — use the winner row
            return balanceDao.findByInfluencerId(influencerId)
                    .orElseThrow(() -> new IllegalStateException("Could not create influencer balance"));
        }
    }

    private void creditUnpaidBalance(UUID influencerId, double commissionAmount) {
        if (commissionAmount <= 0) return;
        // First-time balance is seeded from all conversions (including this one) — do not add again.
        if (!balanceDao.existsByInfluencerId(influencerId)) {
            ensureInfluencerBalance(influencerId);
            return;
        }
        InfluencerBalance balance = balanceDao.findByInfluencerIdForUpdate(influencerId)
                .orElseThrow(() -> new IllegalStateException("Influencer balance missing"));
        balance.setUnpaidAmount(round2(balance.getUnpaidAmount() + commissionAmount));
        balance.setUpdatedAt(LocalDateTime.now());
        balanceDao.save(balance);
    }

    private InfluencerPayoutDto toPayoutDto(InfluencerPayout payout, Influencer influencer) {
        InfluencerPayoutDto dto = new InfluencerPayoutDto();
        dto.setId(payout.getId());
        dto.setInfluencerId(payout.getInfluencerId());
        dto.setInfluencerName(influencer.getName());
        dto.setInfluencerEmail(influencer.getEmail());
        dto.setAmount(payout.getAmount());
        dto.setBalanceAfter(payout.getBalanceAfter());
        dto.setNote(payout.getNote());
        dto.setCreatedAt(payout.getCreatedAt());
        return dto;
    }

    private double computeCommissionAmount(String type, double value, double saleAmountInr) {
        if (TYPE_FLAT.equals(type)) {
            return Math.max(0, value);
        }
        return Math.max(0, saleAmountInr * (value / 100.0));
    }

    private String resolvePromoCode(String promoCode, String razorpayOrderId) {
        String fromRequest = normalizeCode(promoCode);
        if (fromRequest != null && !fromRequest.isBlank()) return fromRequest;
        if (razorpayOrderId == null || razorpayOrderId.isBlank()) return null;
        return paymentDao.findByRazorpayOrderId(razorpayOrderId)
                .map(Payment::getPromoCode)
                .map(this::normalizeCode)
                .filter(code -> !code.isBlank())
                .orElse(null);
    }

    private double resolveSaleAmountInr(String razorpayOrderId, Double saleAmountHint) {
        double fromPayment = 0;
        if (razorpayOrderId != null && !razorpayOrderId.isBlank()) {
            fromPayment = paymentDao.findByRazorpayOrderId(razorpayOrderId)
                    .map(Payment::getAmount)
                    .orElse(0.0);
        }
        if (fromPayment > 0) return fromPayment;
        if (saleAmountHint != null && saleAmountHint > 0) return saleAmountHint;
        return 0;
    }

    private InfluencerResponse toInfluencerResponse(Influencer influencer) {
        InfluencerResponse res = new InfluencerResponse();
        res.setId(influencer.getId());
        res.setName(influencer.getName());
        res.setEmail(influencer.getEmail());
        res.setPassword(influencer.getPassword());
        res.setActive(influencer.isActive());
        res.setNotes(influencer.getNotes());
        res.setUpiId(influencer.getUpiId());
        res.setCreatedAt(influencer.getCreatedAt());

        promoLinkDao.findByInfluencerId(influencer.getId()).ifPresent(link -> {
            res.setPromoLinkId(link.getId());
            res.setPromoCode(link.getCode());
            res.setPromoActive(link.isActive());
            res.setSharePath("/wedding?ref=" + link.getCode());
            res.setLinkOverrides(
                    findSingleLinkRule(link.getId())
                            .map(r -> List.of(toRuleDto(r)))
                            .orElse(List.of())
            );
        });

        List<PromoConversion> conversions = conversionDao.findByInfluencerIdOrderByCreatedAtDesc(influencer.getId());
        res.setConversionCount(conversions.size());
        res.setTotalEarnings(round2(conversions.stream().mapToDouble(PromoConversion::getCommissionAmount).sum()));
        InfluencerBalance balance = ensureInfluencerBalance(influencer.getId());
        res.setUnpaidEarnings(round2(balance.getUnpaidAmount()));
        res.setTotalPaid(round2(balance.getTotalPaidAmount()));
        return res;
    }

    private Optional<PromoCommissionRule> findSingleGlobalRule() {
        List<PromoCommissionRule> rules = ruleDao.findByScopeAndPromoLinkIdIsNull(SCOPE_GLOBAL);
        if (rules.isEmpty()) return Optional.empty();
        return rules.stream()
                .filter(r -> r.getTemplateId() == ALL_TEMPLATES)
                .findFirst()
                .or(() -> Optional.of(rules.get(0)));
    }

    private Optional<PromoCommissionRule> findSingleLinkRule(UUID promoLinkId) {
        List<PromoCommissionRule> rules =
                ruleDao.findByScopeAndPromoLinkIdOrderByTemplateIdAsc(SCOPE_LINK, promoLinkId);
        if (rules.isEmpty()) return Optional.empty();
        return rules.stream()
                .filter(r -> r.getTemplateId() == ALL_TEMPLATES)
                .findFirst()
                .or(() -> Optional.of(rules.get(0)));
    }

    private CommissionRuleDto toRuleDto(PromoCommissionRule rule) {
        return new CommissionRuleDto(
                rule.getId(),
                rule.getScope(),
                rule.getPromoLinkId(),
                ALL_TEMPLATES,
                rule.getType(),
                rule.getValue()
        );
    }

    private void validateRule(String type, double value) {
        String normalized = normalizeType(type);
        if (!TYPE_PERCENT.equals(normalized) && !TYPE_FLAT.equals(normalized)) {
            throw new IllegalArgumentException("Commission type must be PERCENT or FLAT");
        }
        if (value < 0) {
            throw new IllegalArgumentException("Commission value cannot be negative");
        }
        if (TYPE_PERCENT.equals(normalized) && value > 100) {
            throw new IllegalArgumentException("PERCENT commission cannot exceed 100");
        }
    }

    private String normalizeType(String type) {
        return type == null ? TYPE_PERCENT : type.trim().toUpperCase();
    }

    private String normalizeCode(String code) {
        if (code == null) return null;
        return code.trim().replaceAll("[^A-Za-z0-9_-]", "").toUpperCase();
    }

    private String generateCode(String name) {
        String base = name.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (base.length() > 8) base = base.substring(0, 8);
        if (base.isBlank()) base = "PROMO";
        SecureRandom random = new SecureRandom();
        String candidate;
        do {
            candidate = base + (100 + random.nextInt(900));
        } while (promoLinkDao.existsByCodeIgnoreCase(candidate));
        return candidate;
    }

    private String requireText(String value, String label) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(label + " is required");
        }
        return value;
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
