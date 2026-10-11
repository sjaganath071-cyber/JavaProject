package com.team11.logistics.dto;

public class DashboardSummaryResponse {

    private long totalRequests;
    private long pendingRequests;
    private long awaitingApproval;
    private long approvedUnallocatedRequests;
    private long activeAllocations;
    private long activeMovements;
    private long completedRequests;
    private long cancelledRequests;
    private Long totalResources;
    private Long lowStockResources;
    private boolean resourceServiceAvailable;
    private String resourceServiceMessage;

    public DashboardSummaryResponse() {
    }

    public long getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(long totalRequests) {
        this.totalRequests = totalRequests;
    }

    public long getPendingRequests() {
        return pendingRequests;
    }

    public void setPendingRequests(long pendingRequests) {
        this.pendingRequests = pendingRequests;
    }

    public long getAwaitingApproval() {
        return awaitingApproval;
    }

    public void setAwaitingApproval(long awaitingApproval) {
        this.awaitingApproval = awaitingApproval;
    }

    public long getApprovedUnallocatedRequests() {
        return approvedUnallocatedRequests;
    }

    public void setApprovedUnallocatedRequests(long approvedUnallocatedRequests) {
        this.approvedUnallocatedRequests = approvedUnallocatedRequests;
    }

    public long getActiveAllocations() {
        return activeAllocations;
    }

    public void setActiveAllocations(long activeAllocations) {
        this.activeAllocations = activeAllocations;
    }

    public long getActiveMovements() {
        return activeMovements;
    }

    public void setActiveMovements(long activeMovements) {
        this.activeMovements = activeMovements;
    }

    public long getCompletedRequests() {
        return completedRequests;
    }

    public void setCompletedRequests(long completedRequests) {
        this.completedRequests = completedRequests;
    }

    public long getCancelledRequests() {
        return cancelledRequests;
    }

    public void setCancelledRequests(long cancelledRequests) {
        this.cancelledRequests = cancelledRequests;
    }

    public Long getTotalResources() {
        return totalResources;
    }

    public void setTotalResources(Long totalResources) {
        this.totalResources = totalResources;
    }

    public Long getLowStockResources() {
        return lowStockResources;
    }

    public void setLowStockResources(Long lowStockResources) {
        this.lowStockResources = lowStockResources;
    }

    public boolean isResourceServiceAvailable() {
        return resourceServiceAvailable;
    }

    public void setResourceServiceAvailable(boolean resourceServiceAvailable) {
        this.resourceServiceAvailable = resourceServiceAvailable;
    }

    public String getResourceServiceMessage() {
        return resourceServiceMessage;
    }

    public void setResourceServiceMessage(String resourceServiceMessage) {
        this.resourceServiceMessage = resourceServiceMessage;
    }
}
