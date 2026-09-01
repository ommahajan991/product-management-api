package com.ommahajan.product_managment_api.service.implementation;

import com.ommahajan.product_managment_api.entity.AuditLog;
import com.ommahajan.product_managment_api.repository.AuditLogRepository;
import com.ommahajan.product_managment_api.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Async("auditExecutor")
    public void logProductCreated(Integer productId, String performedBy) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction("PRODUCT_CREATED");
        auditLog.setEntityType("PRODUCT");
        auditLog.setEntityId(productId);
        auditLog.setPerformedBy(performedBy);
        auditLog.setTimestamp(Instant.now());

        auditLogRepository.save(auditLog);

        log.info("Audit log written for product {} on thread {}",
                productId, Thread.currentThread().getName());
    }
}