package com.team11.defense_logistics.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;

import com.team11.defense_logistics.dto.LogisticsRequestDto;
import com.team11.defense_logistics.model.Approval;

@Service
public class ApprovalService {

    private final Map<Long, Approval> records = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);
    private final LogisticsRequestService requestService;

    public ApprovalService(LogisticsRequestService requestService) {
        this.requestService = requestService;
    }

    public synchronized List<Approval> findAll() {
        return new ArrayList<>(records.values());
    }

    public synchronized Optional<Approval> findById(Long id) {
        return Optional.ofNullable(records.get(id));
    }

    public synchronized List<Approval> findByRequestId(Long requestId) {
        return records.values().stream()
                .filter(approval -> Objects.equals(approval.getRequestId(), requestId))
                .toList();
    }

    public synchronized Approval create(Approval approval) {
        if (approval == null || approval.getRequestId() == null || approval.getRequestId() <= 0) {
            throw new IllegalArgumentException("Positive request ID is required.");
        }

        if (approval.getApproverName() == null || approval.getApproverName().isBlank()) {
            throw new IllegalArgumentException("Approver name is required.");
        }

        if (approval.getDecision() == null
                || !approval.getDecision().matches("(?i)^(Approved|Rejected)$")) {
            throw new IllegalArgumentException("Decision must be Approved or Rejected.");
        }

        LogisticsRequestDto request = requestService.getRequestById(approval.getRequestId());
        if (request == null) {
            throw new IllegalArgumentException("Logistics request not found.");
        }

        if (!"PENDING".equalsIgnoreCase(request.getStatus())) {
            throw new IllegalArgumentException("Only pending requests can be approved or rejected.");
        }

        if (!findByRequestId(approval.getRequestId()).isEmpty()) {
            throw new IllegalArgumentException("A decision already exists for this request.");
        }

        String decision = approval.getDecision();
        String normalizedDecision = decision.substring(0, 1).toUpperCase(Locale.ROOT)
                + decision.substring(1).toLowerCase(Locale.ROOT);

        Approval record = new Approval();
        record.setRequestId(request.getId());
        record.setApproverName(approval.getApproverName().trim());
        record.setDecision(normalizedDecision);
        record.setComments(approval.getComments() == null || approval.getComments().isBlank()
                ? null : approval.getComments().trim());
        record.setId(sequence.getAndIncrement());
        record.setDecisionDate(LocalDateTime.now());

        request.setStatus(normalizedDecision.toUpperCase(Locale.ROOT));
        requestService.updateRequest(request.getId(), request);
        records.put(record.getId(), record);

        return record;
    }
}
