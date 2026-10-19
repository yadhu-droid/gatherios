package com.app.dearly.service;

import com.app.dearly.dto.ApplyDiscountRequest;
import com.app.dearly.dto.ApplyDiscountResponse;
import com.app.dearly.dto.CreateDiscountRequest;
import com.app.dearly.dto.DiscountCodeDto;

import java.util.List;

public interface DiscountService {
    List<DiscountCodeDto> listAll();

    DiscountCodeDto create(CreateDiscountRequest request);

    DiscountCodeDto update(Long id, CreateDiscountRequest request);

    DiscountCodeDto setActive(Long id, boolean active);

    void delete(Long id);

    ApplyDiscountResponse preview(ApplyDiscountRequest request);

    /** Apply an optional code to an original amount in paise. Blank code leaves the amount unchanged. */
    int applyToPaise(int originalPaise, String code);
}
