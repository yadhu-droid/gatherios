package com.app.dearly.service.impl;

import com.app.dearly.dao.DiscountCodeDao;
import com.app.dearly.dto.ApplyDiscountRequest;
import com.app.dearly.dto.ApplyDiscountResponse;
import com.app.dearly.dto.CreateDiscountRequest;
import com.app.dearly.dto.DiscountCodeDto;
import com.app.dearly.entity.DiscountCode;
import com.app.dearly.service.DiscountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class DiscountServiceImpl implements DiscountService {

    private final DiscountCodeDao discountCodeDao;

    public DiscountServiceImpl(DiscountCodeDao discountCodeDao) {
        this.discountCodeDao = discountCodeDao;
    }

    @Override
    public List<DiscountCodeDto> listAll() {
        return discountCodeDao.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DiscountCodeDto create(CreateDiscountRequest request) {
        String code = normalizeCode(request.getCode());
        if (code.isEmpty()) {
            throw new IllegalArgumentException("Discount code is required.");
        }
        if (discountCodeDao.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("A discount with this code already exists.");
        }
        String type = normalizeType(request.getType());
        double value = validateValue(type, request.getValue());

        DiscountCode entity = new DiscountCode();
        entity.setCode(code);
        entity.setType(type);
        entity.setValue(value);
        entity.setActive(request.getActive() == null || request.getActive());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return toDto(discountCodeDao.save(entity));
    }

    @Override
    @Transactional
    public DiscountCodeDto update(Long id, CreateDiscountRequest request) {
        DiscountCode entity = discountCodeDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Discount not found."));

        if (request.getCode() != null) {
            String code = normalizeCode(request.getCode());
            if (code.isEmpty()) {
                throw new IllegalArgumentException("Discount code is required.");
            }
            discountCodeDao.findByCodeIgnoreCase(code).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new IllegalArgumentException("A discount with this code already exists.");
                }
            });
            entity.setCode(code);
        }
        if (request.getType() != null) {
            entity.setType(normalizeType(request.getType()));
        }
        if (request.getValue() > 0 || "FLAT".equals(entity.getType())) {
            entity.setValue(validateValue(entity.getType(), request.getValue()));
        }
        if (request.getActive() != null) {
            entity.setActive(request.getActive());
        }
        entity.setUpdatedAt(LocalDateTime.now());
        return toDto(discountCodeDao.save(entity));
    }

    @Override
    @Transactional
    public DiscountCodeDto setActive(Long id, boolean active) {
        DiscountCode entity = discountCodeDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Discount not found."));
        entity.setActive(active);
        entity.setUpdatedAt(LocalDateTime.now());
        return toDto(discountCodeDao.save(entity));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!discountCodeDao.existsById(id)) {
            throw new NoSuchElementException("Discount not found.");
        }
        discountCodeDao.deleteById(id);
    }

    @Override
    public ApplyDiscountResponse preview(ApplyDiscountRequest request) {
        double original = round2(request == null ? 0 : request.getAmount());
        String code = request == null ? "" : normalizeCode(request.getCode());
        return previewInternal(original, code, true);
    }

    @Override
    public int applyToPaise(int originalPaise, String code) {
        String normalized = normalizeCode(code);
        if (normalized.isEmpty()) {
            return Math.max(0, originalPaise);
        }
        double originalRs = originalPaise / 100.0;
        ApplyDiscountResponse applied = previewInternal(originalRs, normalized, false);
        return (int) Math.round(applied.getFinalAmount() * 100);
    }

    private ApplyDiscountResponse previewInternal(double originalRs, String code, boolean allowInvalid) {
        ApplyDiscountResponse res = new ApplyDiscountResponse();
        res.setOriginalAmount(round2(Math.max(0, originalRs)));
        res.setDiscountAmount(0);
        res.setFinalAmount(res.getOriginalAmount());

        if (code.isEmpty()) {
            res.setValid(false);
            res.setMessage("Enter a discount code.");
            if (!allowInvalid) {
                throw new IllegalArgumentException(res.getMessage());
            }
            return res;
        }

        DiscountCode entity = discountCodeDao.findByCodeIgnoreCase(code).orElse(null);
        if (entity == null) {
            res.setValid(false);
            res.setMessage("This discount code is not valid.");
            if (!allowInvalid) {
                throw new IllegalArgumentException(res.getMessage());
            }
            return res;
        }
        if (!entity.isActive()) {
            res.setValid(false);
            res.setMessage("This discount code is not active.");
            if (!allowInvalid) {
                throw new IllegalArgumentException(res.getMessage());
            }
            return res;
        }

        double discount = computeDiscount(entity.getType(), entity.getValue(), res.getOriginalAmount());
        res.setValid(true);
        res.setCode(entity.getCode());
        res.setType(entity.getType());
        res.setValue(entity.getValue());
        res.setDiscountAmount(discount);
        res.setFinalAmount(round2(Math.max(0, res.getOriginalAmount() - discount)));
        res.setMessage("Discount applied.");
        return res;
    }

    private double computeDiscount(String type, double value, double original) {
        if ("PERCENT".equals(type)) {
            return round2(original * Math.min(100, value) / 100.0);
        }
        return round2(Math.min(original, Math.max(0, value)));
    }

    private String normalizeCode(String raw) {
        if (raw == null) return "";
        return raw.trim().replaceAll("\\s+", "").toUpperCase();
    }

    private String normalizeType(String raw) {
        String type = raw == null ? "" : raw.trim().toUpperCase();
        if ("PERCENT".equals(type) || "PERCENTAGE".equals(type) || "PCT".equals(type)) {
            return "PERCENT";
        }
        if ("FLAT".equals(type) || "FIXED".equals(type) || "RUPEES".equals(type)) {
            return "FLAT";
        }
        throw new IllegalArgumentException("Type must be Percentage or Flat.");
    }

    private double validateValue(String type, double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Discount value must be greater than zero.");
        }
        if ("PERCENT".equals(type) && value > 100) {
            throw new IllegalArgumentException("Percentage cannot be more than 100.");
        }
        return round2(value);
    }

    private DiscountCodeDto toDto(DiscountCode entity) {
        return new DiscountCodeDto(
                entity.getId(),
                entity.getCode(),
                entity.getType(),
                entity.getValue(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
