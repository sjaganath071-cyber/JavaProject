package com.team11.logistics.dto;

import com.team11.logistics.model.RequestPriority;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public class UpdateLogisticsRequestDto {

    @NotBlank(message = "Requesting unit is required")
    @Size(max = 150, message = "Requesting unit must not exceed 150 characters")
    private String requestingUnit;

    @Size(max = 150, message = "Requester name must not exceed 150 characters")
    private String requesterName;

    @NotBlank(message = "Purpose is required")
    @Size(max = 1000, message = "Purpose must not exceed 1000 characters")
    private String purpose;

    @NotNull(message = "Priority is required")
    private RequestPriority priority;

    private LocalDate requiredFrom;

    private LocalDate requiredUntil;

    @NotEmpty(message = "At least one item is required")
    @Valid
    private List<RequestItemDto> items;

    public UpdateLogisticsRequestDto() {
    }

    @AssertTrue(message = "Required until must not be earlier than required from")
    public boolean isDateRangeValid() {
        if (requiredFrom == null || requiredUntil == null) {
            return true;
        }
        return !requiredUntil.isBefore(requiredFrom);
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
