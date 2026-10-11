package com.team11.resourse.controller;

import com.team11.resourse.dto.InventoryItemCreateRequest;
import com.team11.resourse.dto.InventoryItemResponse;
import com.team11.resourse.dto.InventoryItemUpdateRequest;
import com.team11.resourse.dto.InventoryTransactionResponse;
import com.team11.resourse.dto.StockTransactionRequest;
import com.team11.resourse.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public InventoryItemResponse createItem(@Valid @RequestBody InventoryItemCreateRequest request) {
        return inventoryService.createItem(request);
    }

    @GetMapping("/items")
    public List<InventoryItemResponse> getItems(
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false, name = "name") String itemName) {
        return inventoryService.getItems(unitId, category, location, itemName);
    }

    @GetMapping("/items/{id}")
    public InventoryItemResponse getItem(@PathVariable Long id) {
        return inventoryService.getItem(id);
    }

    @PutMapping("/items/{id}")
    public InventoryItemResponse updateItem(
            @PathVariable Long id,
            @Valid @RequestBody InventoryItemUpdateRequest request) {
        return inventoryService.updateItem(id, request);
    }

    @DeleteMapping("/items/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable Long id) {
        inventoryService.deleteItem(id);
    }

    @PostMapping("/items/{id}/receive")
    public InventoryItemResponse receiveStock(
            @PathVariable Long id,
            @Valid @RequestBody StockTransactionRequest request) {
        return inventoryService.receiveStock(id, request);
    }

    @PostMapping("/items/{id}/issue")
    public InventoryItemResponse issueStock(
            @PathVariable Long id,
            @Valid @RequestBody StockTransactionRequest request) {
        return inventoryService.issueStock(id, request);
    }

    @PostMapping("/items/{id}/adjust")
    public InventoryItemResponse adjustStock(
            @PathVariable Long id,
            @Valid @RequestBody StockTransactionRequest request) {
        return inventoryService.adjustStock(id, request);
    }

    @GetMapping("/items/{id}/transactions")
    public List<InventoryTransactionResponse> getItemTransactions(@PathVariable Long id) {
        return inventoryService.getItemTransactions(id);
    }

    @GetMapping("/transactions")
    public List<InventoryTransactionResponse> getTransactions(
            @RequestParam(required = false) Long itemId) {
        return inventoryService.getTransactions(itemId);
    }

    @GetMapping("/low-stock")
    public List<InventoryItemResponse> getLowStockItems() {
        return inventoryService.getLowStockItems();
    }
}