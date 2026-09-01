package com.ommahajan.product_managment_api.service;

public interface AuditLogService {

    void logProductCreated(Integer productId, String performedBy);
}
