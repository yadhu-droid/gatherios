package com.app.dearly.service;

import com.app.dearly.dto.SurpriseRequest;
import com.app.dearly.entity.Surprise;

public interface MetaConversionsService {
    void sendPurchaseAfterPayment(SurpriseRequest request, Surprise saved);
}
