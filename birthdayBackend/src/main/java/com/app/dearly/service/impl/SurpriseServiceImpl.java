package com.app.dearly.service.impl;

import com.app.dearly.dao.SurpriseDao;
import com.app.dearly.dao.RsvpDao;
import com.app.dearly.dao.GuestWishDao;
import com.app.dearly.dao.PaymentDao;
import com.app.dearly.dto.*;
import com.app.dearly.entity.Surprise;
import com.app.dearly.entity.Rsvp;
import com.app.dearly.entity.GuestWish;
import com.app.dearly.entity.Payment;
import com.app.dearly.service.EmailService;
import com.app.dearly.service.MetaConversionsService;
import com.app.dearly.service.PromotionService;
import com.app.dearly.service.SurpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class SurpriseServiceImpl implements SurpriseService {

    private final SurpriseDao surpriseDao;
    private final RsvpDao rsvpDao;
    private final GuestWishDao guestWishDao;
    private final PaymentDao paymentDao;
    private final PromotionService promotionService;
    private final MetaConversionsService metaConversionsService;

    @Autowired(required = false)
    private EmailService emailService;

    @Value("${razorpay.key.secret:}")
    private String razorpayKeySecret;

    @Autowired
    public SurpriseServiceImpl(
            SurpriseDao surpriseDao,
            RsvpDao rsvpDao,
            GuestWishDao guestWishDao,
            PaymentDao paymentDao,
            PromotionService promotionService,
            MetaConversionsService metaConversionsService
    ) {
        this.surpriseDao = surpriseDao;
        this.rsvpDao = rsvpDao;
        this.guestWishDao = guestWishDao;
        this.paymentDao = paymentDao;
        this.promotionService = promotionService;
        this.metaConversionsService = metaConversionsService;
    }

    private String generate6CharPassword() {
        String chars = "123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Override
    public SurpriseResponse createSurprise(SurpriseRequest request) {
        Surprise surprise = new Surprise();
        surprise.setRecipient(request.getRecipient());
        surprise.setOccasion(request.getOccasion());
        surprise.setName(request.getName());
        surprise.setBirthdate(request.getBirthdate());
        surprise.setPromptVal(request.getPromptVal());
        surprise.setMessage(request.getMessage());
        surprise.setTemplate(request.getTemplate());
        surprise.setUsername(request.getUsername());
        
        String pwd = request.getPassword();
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            Optional<Surprise> existingSurprise = surpriseDao.findFirstByUsernameOrderByCreatedAtDesc(request.getUsername().trim());
            if (existingSurprise.isPresent() && existingSurprise.get().getPassword() != null && !existingSurprise.get().getPassword().trim().isEmpty()) {
                pwd = existingSurprise.get().getPassword().trim();
            }
        }
        if (pwd == null || pwd.trim().isEmpty()) {
            pwd = generate6CharPassword();
        }
        surprise.setPassword(pwd);
        surprise.setRazorpayOrderId(request.getRazorpayOrderId());
        surprise.setRazorpayPaymentId(request.getRazorpayPaymentId());
        surprise.setShowWishWall(request.isShowWishWall());
        applyProFeatures(surprise, request);
        applyMusic(surprise, request, true);

        List<String> reqPhotos = request.getPhotos() != null ? request.getPhotos() : Collections.emptyList();
        surprise.setPhoto1(reqPhotos.size() > 0 ? reqPhotos.get(0) : null);
        surprise.setPhoto2(reqPhotos.size() > 1 ? reqPhotos.get(1) : null);
        surprise.setPhoto3(reqPhotos.size() > 2 ? reqPhotos.get(2) : null);
        surprise.setPhoto4(reqPhotos.size() > 3 ? reqPhotos.get(3) : null);
        surprise.setPhoto5(reqPhotos.size() > 4 ? reqPhotos.get(4) : null);
        surprise.setPhoto6(reqPhotos.size() > 5 ? reqPhotos.get(5) : null);
        surprise.setPhoto7(reqPhotos.size() > 6 ? reqPhotos.get(6) : null);
        surprise.setPhoto8(reqPhotos.size() > 7 ? reqPhotos.get(7) : null);
        surprise.setPhoto9(reqPhotos.size() > 8 ? reqPhotos.get(8) : null);
        surprise.setPhoto10(reqPhotos.size() > 9 ? reqPhotos.get(9) : null);

        surprise.setCreatedAt(LocalDateTime.now());
        surprise.setViews(0);
        surprise.setAccepted(false);

        Surprise saved = surpriseDao.save(surprise);

        if (emailService != null && saved.getUsername() != null && !saved.getUsername().trim().isEmpty()) {
            emailService.sendDashboardCredentialsEmail(saved.getUsername(), saved.getPassword(), saved.getName(), saved.getId().toString());
        }

        try {
            promotionService.recordConversionIfEligible(
                    saved.getId(),
                    saved.getTemplate(),
                    request.getPromoCode(),
                    saved.getRazorpayOrderId(),
                    request.getSaleAmount()
            );
        } catch (Exception ignored) {
            // Never block invitation creation on promo attribution failures
        }

        try {
            metaConversionsService.sendPurchaseAfterPayment(request, saved);
        } catch (Exception ignored) {
            // Never block invitation creation on ads reporting failures
        }

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public SurpriseResponse getSurpriseById(UUID id) {
        Surprise surprise = surpriseDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Surprise not found with ID: " + id));
        return mapToResponse(surprise);
    }

    @Override
    public void incrementViews(UUID id) {
        Surprise surprise = surpriseDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Surprise not found with ID: " + id));
        surprise.setViews(surprise.getViews() + 1);
        surpriseDao.save(surprise);
    }

    @Override
    public void markAccepted(UUID id) {
        Surprise surprise = surpriseDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Surprise not found with ID: " + id));
        if (!surprise.isAccepted()) {
            surprise.setAccepted(true);
            surprise.setAcceptedAt(LocalDateTime.now());
            surpriseDao.save(surprise);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics() {
        List<Object[]> rows = surpriseDao.findAnalyticsRows();

        Map<String, Double> paidByOrderId = paymentDao.findAll().stream()
                .filter(p -> "PAID".equals(p.getStatus()) && p.getRazorpayOrderId() != null)
                .collect(Collectors.toMap(
                        Payment::getRazorpayOrderId,
                        Payment::getAmount,
                        (a, b) -> a
                ));

        List<SurpriseResponse> responses = new ArrayList<>(rows.size());
        long totalViews = 0;
        long totalAccepted = 0;
        double totalGiftPrice = 0;
        double totalGiftDebt = 0;
        Map<String, Long> occasionBreakdown = new HashMap<>();
        Map<Integer, Long> templateBreakdown = new HashMap<>();

        for (String occasion : Arrays.asList("proposal", "birthday", "other", "wedding")) {
            occasionBreakdown.put(occasion, 0L);
        }
        for (int i = 1; i <= 20; i++) {
            templateBreakdown.put(i, 0L);
        }

        for (Object[] row : rows) {
            SurpriseResponse response = mapAnalyticsRow(row, paidByOrderId);
            responses.add(response);

            totalViews += response.getViews();
            if (response.isAccepted()) totalAccepted++;
            totalGiftPrice += response.getGiftPrice() != null ? response.getGiftPrice() : 0;
            totalGiftDebt += response.getGiftDebt() != null ? response.getGiftDebt() : 0;

            String occasion = response.getOccasion() != null ? response.getOccasion() : "other";
            occasionBreakdown.put(occasion, occasionBreakdown.getOrDefault(occasion, 0L) + 1);
            templateBreakdown.put(response.getTemplate(), templateBreakdown.getOrDefault(response.getTemplate(), 0L) + 1);
        }

        long totalSurprises = responses.size();
        double averageViews = totalSurprises > 0 ? (double) totalViews / totalSurprises : 0.0;
        double acceptanceRate = totalSurprises > 0 ? ((double) totalAccepted / totalSurprises) * 100.0 : 0.0;
        double totalPayouts = paidByOrderId.values().stream().mapToDouble(Double::doubleValue).sum();

        AnalyticsResponse analytics = new AnalyticsResponse();
        analytics.setTotalSurprises(totalSurprises);
        analytics.setTotalViews(totalViews);
        analytics.setAverageViews(averageViews);
        analytics.setTotalAccepted(totalAccepted);
        analytics.setAcceptanceRate(acceptanceRate);
        analytics.setOccasionBreakdown(occasionBreakdown);
        analytics.setTemplateBreakdown(templateBreakdown);
        analytics.setSurprisesList(responses);
        analytics.setTotalPayouts(totalPayouts);
        analytics.setTotalGiftPrice(totalGiftPrice);
        analytics.setTotalGiftDebt(totalGiftDebt);
        return analytics;
    }

    private SurpriseResponse mapAnalyticsRow(Object[] row, Map<String, Double> paidByOrderId) {
        SurpriseResponse response = new SurpriseResponse();
        response.setId((UUID) row[0]);
        response.setRecipient((String) row[1]);
        response.setOccasion((String) row[2]);
        response.setName((String) row[3]);
        response.setTemplate(row[4] != null ? ((Number) row[4]).intValue() : 0);
        response.setCreatedAt((LocalDateTime) row[5]);
        response.setViews(row[6] != null ? ((Number) row[6]).intValue() : 0);
        response.setAccepted(Boolean.TRUE.equals(row[7]));
        response.setAcceptedAt((LocalDateTime) row[8]);
        response.setUsername((String) row[9]);
        response.setPassword((String) row[10]);
        response.setRazorpayOrderId((String) row[11]);
        response.setRazorpayPaymentId((String) row[12]);
        response.setMoneyGiftEnabled(Boolean.TRUE.equals(row[13]));
        response.setMoneyGiftPlan((String) row[14]);
        response.setMoneyGiftUpi((String) row[15]);
        response.setMoneyGiftPlatformFeePercent(row[16] != null ? ((Number) row[16]).doubleValue() : 0.0);
        response.setGiftPrice(row[17] != null ? ((Number) row[17]).doubleValue() : 0.0);
        response.setGiftDebt(row[18] != null ? ((Number) row[18]).doubleValue() : 0.0);
        response.setCustomMusicEnabled(Boolean.TRUE.equals(row[19]));
        response.setShowWishWall(row[20] == null || Boolean.TRUE.equals(row[20]));

        String orderId = response.getRazorpayOrderId();
        if (orderId != null && paidByOrderId.containsKey(orderId)) {
            response.setPayoutAmount(paidByOrderId.get(orderId));
        }

        // Keep payload tiny for admin table — open invitation details fetches full record.
        response.setPhotos(Collections.emptyList());
        response.setRsvps(Collections.emptyList());
        response.setGuestWishes(Collections.emptyList());
        response.setCustomMusicData(null);
        response.setMessage(null);
        return response;
    }

    private SurpriseResponse mapToResponse(Surprise entity) {
        SurpriseResponse response = new SurpriseResponse();
        response.setId(entity.getId());
        response.setRecipient(entity.getRecipient());
        response.setOccasion(entity.getOccasion());
        response.setName(entity.getName());
        response.setBirthdate(entity.getBirthdate());
        response.setPromptVal(entity.getPromptVal());
        response.setMessage(entity.getMessage());
        response.setTemplate(entity.getTemplate());
        response.setCreatedAt(entity.getCreatedAt());
        response.setViews(entity.getViews());
        response.setAccepted(entity.isAccepted());
        response.setAcceptedAt(entity.getAcceptedAt());
        response.setUsername(entity.getUsername());
        response.setPassword(entity.getPassword());
        response.setRazorpayOrderId(entity.getRazorpayOrderId());
        response.setRazorpayPaymentId(entity.getRazorpayPaymentId());
        response.setShowWishWall(entity.isShowWishWall());
        response.setCustomMusicEnabled(entity.isCustomMusicEnabled());
        response.setCustomMusicData(entity.getCustomMusicData());
        response.setCustomMusicName(entity.getCustomMusicName());
        response.setMoneyGiftEnabled(entity.isMoneyGiftEnabled());
        response.setMoneyGiftPlan(entity.getMoneyGiftPlan());
        response.setMoneyGiftUpi(entity.getMoneyGiftUpi());
        response.setMoneyGiftPlatformFeePercent(entity.getMoneyGiftPlatformFeePercent());
        response.setGiftPrice(entity.getGiftPrice());
        response.setGiftDebt(entity.getGiftDebt());

        if (entity.getRazorpayOrderId() != null) {
            paymentDao.findByRazorpayOrderId(entity.getRazorpayOrderId()).ifPresent(p -> {
                if ("PAID".equals(p.getStatus())) {
                    response.setPayoutAmount(p.getAmount());
                }
            });
        }

        List<String> photosList = new ArrayList<>();
        if (entity.getPhoto1() != null && !entity.getPhoto1().trim().isEmpty()) photosList.add(entity.getPhoto1());
        if (entity.getPhoto2() != null && !entity.getPhoto2().trim().isEmpty()) photosList.add(entity.getPhoto2());
        if (entity.getPhoto3() != null && !entity.getPhoto3().trim().isEmpty()) photosList.add(entity.getPhoto3());
        if (entity.getPhoto4() != null && !entity.getPhoto4().trim().isEmpty()) photosList.add(entity.getPhoto4());
        if (entity.getPhoto5() != null && !entity.getPhoto5().trim().isEmpty()) photosList.add(entity.getPhoto5());
        if (entity.getPhoto6() != null && !entity.getPhoto6().trim().isEmpty()) photosList.add(entity.getPhoto6());
        if (entity.getPhoto7() != null && !entity.getPhoto7().trim().isEmpty()) photosList.add(entity.getPhoto7());
        if (entity.getPhoto8() != null && !entity.getPhoto8().trim().isEmpty()) photosList.add(entity.getPhoto8());
        if (entity.getPhoto9() != null && !entity.getPhoto9().trim().isEmpty()) photosList.add(entity.getPhoto9());
        if (entity.getPhoto10() != null && !entity.getPhoto10().trim().isEmpty()) photosList.add(entity.getPhoto10());
        response.setPhotos(photosList);

        List<Rsvp> rsvps = rsvpDao.findBySurpriseId(entity.getId());
        List<RsvpResponse> rsvpResponses = rsvps.stream().map(r -> {
            RsvpResponse resp = new RsvpResponse();
            resp.setId(r.getId());
            resp.setName(r.getName());
            resp.setGuestCount(r.getGuestCount());
            resp.setWishes(r.getWishes());
            resp.setCreatedAt(r.getCreatedAt());
            return resp;
        }).collect(Collectors.toList());
        response.setRsvps(rsvpResponses);

        List<GuestWish> wishes = guestWishDao.findBySurpriseId(entity.getId());
        List<GuestWishResponse> wishResponses = wishes.stream().map(w -> {
            GuestWishResponse resp = new GuestWishResponse();
            resp.setId(w.getId());
            resp.setName(w.getName());
            resp.setWishes(w.getWishes());
            resp.setPhotoBase64(w.getPhotoBase64());
            resp.setAudioBase64(w.getAudioBase64());
            resp.setVideoBase64(w.getVideoBase64());
            resp.setApproved(w.isApproved());
            resp.setCreatedAt(w.getCreatedAt());
            return resp;
        }).collect(Collectors.toList());
        response.setGuestWishes(wishResponses);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalyticsForSurprise(UUID id) {
        Surprise surprise = surpriseDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Surprise not found with ID: " + id));

        List<Surprise> userSurprises;
        if (surprise.getUsername() != null && !surprise.getUsername().trim().isEmpty()) {
            userSurprises = surpriseDao.findByUsernameOrderByCreatedAtDesc(surprise.getUsername().trim());
            if (userSurprises.isEmpty()) {
                userSurprises = List.of(surprise);
            }
        } else {
            userSurprises = List.of(surprise);
        }

        long totalSurprises = userSurprises.size();
        long totalViews = userSurprises.stream().mapToLong(Surprise::getViews).sum();
        double averageViews = totalSurprises > 0 ? (double) totalViews / totalSurprises : 0.0;
        long totalAccepted = userSurprises.stream().filter(Surprise::isAccepted).count();
        double acceptanceRate = totalSurprises > 0 ? ((double) totalAccepted / totalSurprises) * 100.0 : 0.0;

        Map<String, Long> occasionBreakdown = new HashMap<>();
        Map<Integer, Long> templateBreakdown = new HashMap<>();

        // Initialize breaks to avoid empty widgets
        for (String occasion : Arrays.asList("proposal", "birthday", "other")) {
            occasionBreakdown.put(occasion, 0L);
        }
        for (int i = 1; i <= 9; i++) {
            templateBreakdown.put(i, 0L);
        }

        userSurprises.forEach(s -> {
            occasionBreakdown.put(s.getOccasion(), occasionBreakdown.getOrDefault(s.getOccasion(), 0L) + 1);
            templateBreakdown.put(s.getTemplate(), templateBreakdown.getOrDefault(s.getTemplate(), 0L) + 1);
        });

        List<SurpriseResponse> responseDtos = userSurprises.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        AnalyticsResponse analytics = new AnalyticsResponse();
        analytics.setTotalSurprises(totalSurprises);
        analytics.setTotalViews(totalViews);
        analytics.setAverageViews(averageViews);
        analytics.setTotalAccepted(totalAccepted);
        analytics.setAcceptanceRate(acceptanceRate);
        analytics.setOccasionBreakdown(occasionBreakdown);
        analytics.setTemplateBreakdown(templateBreakdown);
        analytics.setSurprisesList(responseDtos);

        return analytics;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<SurpriseResponse> getSurpriseByCredentials(String username, String password) {
        return surpriseDao.findFirstByUsernameAndPasswordOrderByCreatedAtDesc(username, password)
                .map(this::mapToResponse);
    }

    @Override
    public void submitRsvp(UUID id, RsvpRequest request) {
        Surprise surprise = surpriseDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Surprise not found with ID: " + id));

        Rsvp rsvp = new Rsvp();
        rsvp.setSurpriseId(id);
        rsvp.setName(request.getName());
        rsvp.setGuestCount(request.getGuestCount());
        rsvp.setWishes(request.getWishes());
        rsvp.setCreatedAt(LocalDateTime.now());
        rsvpDao.save(rsvp);

        // Also mark surprise as accepted
        if (!surprise.isAccepted()) {
            surprise.setAccepted(true);
            surprise.setAcceptedAt(LocalDateTime.now());
            surpriseDao.save(surprise);
        }
    }

    @Override
    public SurpriseResponse updateSurprise(UUID id, SurpriseRequest request) {
        Surprise surprise = surpriseDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Surprise not found with ID: " + id));

        surprise.setRecipient(request.getRecipient());
        surprise.setOccasion(request.getOccasion());
        surprise.setName(request.getName());
        surprise.setBirthdate(request.getBirthdate());
        surprise.setPromptVal(request.getPromptVal());
        surprise.setMessage(request.getMessage());
        surprise.setTemplate(request.getTemplate());
        surprise.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            surprise.setPassword(request.getPassword());
        }
        if (request.getRazorpayOrderId() != null) {
            surprise.setRazorpayOrderId(request.getRazorpayOrderId());
        }
        if (request.getRazorpayPaymentId() != null) {
            surprise.setRazorpayPaymentId(request.getRazorpayPaymentId());
        }
        surprise.setShowWishWall(request.isShowWishWall());

        String lockedPlan = surprise.getMoneyGiftPlan();
        boolean lockedGiftOn = surprise.isMoneyGiftEnabled();
        Double lockedFee = surprise.getMoneyGiftPlatformFeePercent();
        String lockedUpi = surprise.getMoneyGiftUpi();
        applyProFeatures(surprise, request);
        surprise.setMoneyGiftPlan(lockedPlan);
        surprise.setMoneyGiftEnabled(lockedGiftOn);
        surprise.setMoneyGiftPlatformFeePercent(lockedFee);
        if (lockedGiftOn) {
            String upi = request.getMoneyGiftUpi();
            surprise.setMoneyGiftUpi(upi != null && !upi.trim().isEmpty() ? upi.trim() : lockedUpi);
        } else {
            surprise.setMoneyGiftUpi(lockedUpi);
        }
        applyMusic(surprise, request, false);

        List<String> reqPhotos = request.getPhotos() != null ? request.getPhotos() : Collections.emptyList();
        surprise.setPhoto1(reqPhotos.size() > 0 ? reqPhotos.get(0) : null);
        surprise.setPhoto2(reqPhotos.size() > 1 ? reqPhotos.get(1) : null);
        surprise.setPhoto3(reqPhotos.size() > 2 ? reqPhotos.get(2) : null);
        surprise.setPhoto4(reqPhotos.size() > 3 ? reqPhotos.get(3) : null);
        surprise.setPhoto5(reqPhotos.size() > 4 ? reqPhotos.get(4) : null);
        surprise.setPhoto6(reqPhotos.size() > 5 ? reqPhotos.get(5) : null);
        surprise.setPhoto7(reqPhotos.size() > 6 ? reqPhotos.get(6) : null);
        surprise.setPhoto8(reqPhotos.size() > 7 ? reqPhotos.get(7) : null);
        surprise.setPhoto9(reqPhotos.size() > 8 ? reqPhotos.get(8) : null);
        surprise.setPhoto10(reqPhotos.size() > 9 ? reqPhotos.get(9) : null);

        Surprise saved = surpriseDao.save(surprise);
        return mapToResponse(saved);
    }

    @Override
    public SurpriseResponse unlockCustomMusic(UUID id, UnlockCustomMusicRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment details are required");
        }
        Surprise surprise = surpriseDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Surprise not found with ID: " + id));
        if (surprise.isCustomMusicEnabled()) {
            applyUnlockedTrack(surprise, request.getCustomMusicData(), request.getCustomMusicName());
            return mapToResponse(surpriseDao.save(surprise));
        }

        String orderId = trimToNull(request.getRazorpayOrderId());
        String paymentId = trimToNull(request.getRazorpayPaymentId());
        String signature = trimToNull(request.getRazorpaySignature());
        if (orderId == null || paymentId == null || signature == null) {
            throw new IllegalArgumentException("Razorpay payment details are required");
        }
        if (!razorpaySignatureValid(orderId, paymentId, signature)) {
            throw new IllegalArgumentException("Payment verification failed");
        }

        Payment payment = paymentDao.findByRazorpayOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Payment record not found"));
        if (payment.getAddonSurpriseId() != null && !payment.getAddonSurpriseId().equals(id)) {
            throw new IllegalArgumentException("This payment was already used");
        }
        if (Math.abs(payment.getAmount() - 500.0) > 0.05) {
            throw new IllegalArgumentException("This payment is not a soundtrack unlock");
        }
        String purpose = payment.getPurpose() == null ? "" : payment.getPurpose().trim();
        if (!purpose.isEmpty() && !"CUSTOM_MUSIC".equalsIgnoreCase(purpose)) {
            throw new IllegalArgumentException("This payment is not a soundtrack unlock");
        }

        payment.setRazorpayPaymentId(paymentId);
        payment.setRazorpaySignature(signature);
        payment.setStatus("PAID");
        payment.setPurpose("CUSTOM_MUSIC");
        payment.setAddonSurpriseId(id);
        payment.setUpdatedAt(LocalDateTime.now());
        paymentDao.save(payment);

        surprise.setCustomMusicEnabled(true);
        applyUnlockedTrack(surprise, request.getCustomMusicData(), request.getCustomMusicName());
        return mapToResponse(surpriseDao.save(surprise));
    }

    private void applyUnlockedTrack(Surprise surprise, String data, String name) {
        if (data != null && !data.isBlank()) {
            surprise.setCustomMusicData(data);
            surprise.setCustomMusicName(name);
        }
    }

    private boolean razorpaySignatureValid(String orderId, String paymentId, String signature) {
        if (razorpayKeySecret == null || razorpayKeySecret.isBlank()) return false;
        try {
            String data = orderId + "|" + paymentId;
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(
                    razorpayKeySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                String part = Integer.toHexString(0xff & b);
                if (part.length() == 1) hex.append('0');
                hex.append(part);
            }
            return hex.toString().equals(signature);
        } catch (Exception e) {
            return false;
        }
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void applyProFeatures(Surprise surprise, SurpriseRequest request) {
        String plan = request.getMoneyGiftPlan() != null ? request.getMoneyGiftPlan().trim().toUpperCase() : "OFF";
        if (!plan.equals("PREMIUM") && !plan.equals("SHARE")) {
            plan = "OFF";
        }
        boolean giftOn = !"OFF".equals(plan);
        surprise.setMoneyGiftEnabled(giftOn);
        surprise.setMoneyGiftPlan(plan);
        surprise.setMoneyGiftUpi(giftOn ? request.getMoneyGiftUpi() : null);
        surprise.setMoneyGiftPlatformFeePercent(plan.equals("SHARE") ? 20.0 : 0.0);
    }

    private void applyMusic(Surprise surprise, SurpriseRequest request, boolean mayEnable) {
        if (surprise.isCustomMusicEnabled()) {
            surprise.setCustomMusicEnabled(true);
            if (request.getCustomMusicData() != null && !request.getCustomMusicData().isBlank()) {
                surprise.setCustomMusicData(request.getCustomMusicData());
                surprise.setCustomMusicName(request.getCustomMusicName());
            } else if (request.getCustomMusicName() == null || request.getCustomMusicName().isBlank()) {
                surprise.setCustomMusicData(null);
                surprise.setCustomMusicName(null);
            }
            return;
        }
        if (mayEnable && request.isCustomMusicEnabled()) {
            surprise.setCustomMusicEnabled(true);
            surprise.setCustomMusicName(request.getCustomMusicName());
            if (request.getCustomMusicData() != null && !request.getCustomMusicData().isBlank()) {
                surprise.setCustomMusicData(request.getCustomMusicData());
            }
            return;
        }
        surprise.setCustomMusicEnabled(false);
        surprise.setCustomMusicData(null);
        surprise.setCustomMusicName(null);
    }

    @Override
    public void submitWish(UUID id, GuestWishRequest request) {
        surpriseDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Surprise not found with ID: " + id));

        GuestWish gw = new GuestWish();
        gw.setSurpriseId(id);
        gw.setName(request.getName());
        gw.setWishes(request.getWishes());
        gw.setPhotoBase64(request.getPhotoBase64());
        gw.setAudioBase64(request.getAudioBase64());
        gw.setVideoBase64(request.getVideoBase64());
        gw.setApproved(true); // Auto-approved by default as moderation is not needed
        gw.setCreatedAt(LocalDateTime.now());
        
        guestWishDao.save(gw);
    }

    @Override
    public void toggleWishApproval(Long wishId) {
        GuestWish gw = guestWishDao.findById(wishId)
                .orElseThrow(() -> new NoSuchElementException("GuestWish not found with ID: " + wishId));
        gw.setApproved(!gw.isApproved());
        guestWishDao.save(gw);
    }

    @Override
    public void toggleWishWallVisibility(UUID id, boolean visible) {
        Surprise surprise = surpriseDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Surprise not found with ID: " + id));
        surprise.setShowWishWall(visible);
        surpriseDao.save(surprise);
    }
}
