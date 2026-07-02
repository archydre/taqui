package com.taqui.backend.modules.audit.service;

import com.taqui.backend.modules.audit.entity.AuditAction;
import com.taqui.backend.modules.audit.entity.AuditEntityType;
import com.taqui.backend.modules.audit.entity.AuditLog;
import com.taqui.backend.modules.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void record(UUID actorId, AuditAction action, AuditEntityType entityType, UUID entityId) {
        record(actorId, action, entityType, entityId, null);
    }

    public void record(UUID actorId, AuditAction action, AuditEntityType entityType, UUID entityId, String details) {
        AuditLog log = new AuditLog();
        log.setActorId(actorId);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
