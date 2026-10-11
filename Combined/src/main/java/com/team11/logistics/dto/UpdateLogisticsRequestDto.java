package com.team11.logistics.dto;

import java.time.LocalDate;
import java.util.List;

import com.team11.logistics.model.RequestPriority;

public class UpdateLogisticsRequestDto {

    private String requestingUnit;
    private String requesterName;
    private String purpose;
    private RequestPriority priority;
    private LocalDate requiredFrom;
    private LocalDate requiredUntil;
    private List<RequestItemDto> items;

    public UpdateLogisticsRequestDto() {
    }

    public String getRequestingUnit() {
        return requestingUnit;
    }

    public void setRequestingUnit(String requestingUnit) {
        this.requestingUnit = requestingUnit;
    }

    public String getRequesterName() {
        return requesterName;
    }

    public void setRequesterName(String requesterName) {
        this.requesterName = requesterName;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public RequestPriority getPriority() {
        return priority;
    }

    public void setPriority(RequestPriority priority) {
        this.priority = priority;
    }

    public LocalDate getRequiredFrom() {
        return requiredFrom;
    }

    public void setRequiredFrom(LocalDate requiredFrom) {
        this.requiredFrom = requiredFrom;
    }

    public LocalDate getRequiredUntil() {
        return requiredUntil;
    }

    public void setRequiredUntil(LocalDate requiredUntil) {
        this.requiredUntil = requiredUntil;
    }

    public List<RequestItemDto> getItems() {
        return items;
    }

    public void setItems(List<RequestItemDto> items) {
        this.items = items;
    }
}
