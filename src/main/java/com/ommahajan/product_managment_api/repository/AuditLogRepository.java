package com.ommahajan.product_managment_api.repository;

import com.ommahajan.product_managment_api.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Integer> {
}