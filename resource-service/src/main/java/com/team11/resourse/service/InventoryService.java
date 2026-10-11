package com.team11.resourse.service;

import com.team11.resourse.dto.InventoryItemCreateRequest;
import com.team11.resourse.dto.InventoryItemResponse;
import com.team11.resourse.dto.InventoryItemUpdateRequest;
import com.team11.resourse.dto.InventoryTransactionResponse;
import com.team11.resourse.dto.StockTransactionRequest;
import com.team11.resourse.entity.InventoryItem;
import com.team11.resourse.entity.InventoryTransaction;
import com.team11.resourse.repository.InventoryItemRepository;
import com.team11.resourse.repository.InventoryTransactionRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class InventoryService {

    private final InventoryItemRepository itemRepository;
    private final InventoryTransactionRepository transactionRepository;

    public InventoryService(InventoryItemRepository itemRepository,
                            InventoryTransactionRepository transactionRepository) {
        this.itemRepository = itemRepository;
        this.transactionRepository = transactionRepository;
    }

    public InventoryItemResponse createItem(InventoryItemCreateRequest request) {
        validateCreateRequest(request);
        String code = normalize(request.itemCode());
        ensureCodeAvailable(code);

        BigDecimal onHand = zeroIfNull(request.quantityOnHand());
        BigDecimal reserved = zeroIfNull(request.quantityReserved());
        BigDecimal threshold = zeroIfNull(request.reorderThreshold());
        if (reserved.compareTo(onHand) > 0) {
            throw badRequest("Quantity reserved cannot exceed quantity on hand");
        }

        InventoryItem item = new InventoryItem();
        item.setItemCode(code);
        item.setItemName(normalize(request.itemName()));
        item.setCategory(normalize(request.category()));
        item.setUnitOfMeasure(normalize(request.unitOfMeasure()));
        item.setUnitId(request.unitId());
        item.setLocation(normalize(request.location()));
        item.setQuantityOnHand(onHand);
        item.setQuantityReserved(reserved);
        item.setReorderThreshold(threshold);
        return toItemResponse(itemRepository.save(item));
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getItems(Long unitId, String category, String location, String name) {
        if (unitId != null && unitId <= 0) {
            throw badRequest("Unit ID must be greater than zero");
        }

        Specification<InventoryItem> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (unitId != null) {
                predicates.add(criteriaBuilder.equal(root.get("unitId"), unitId));
            }
            addTextFilter(predicates, root, criteriaBuilder, "category", category);
            addTextFilter(predicates, root, criteriaBuilder, "location", location);
            if (!isBlank(name)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("itemName")),
                        "%" + normalize(name).toLowerCase(Locale.ROOT) + "%"));
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return itemRepository.findAll(specification).stream().map(this::toItemResponse).toList();
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getItem(Long itemId) {
        return toItemResponse(findItem(itemId));
    }

    public InventoryItemResponse updateItem(Long itemId, InventoryItemUpdateRequest request) {
        validateUpdateRequest(request);
        InventoryItem item = findItem(itemId);
        String code = normalize(request.itemCode());
        if (!item.getItemCode().equalsIgnoreCase(code)) {
            ensureCodeAvailable(code);
        }

        item.setItemCode(code);
        item.setItemName(normalize(request.itemName()));
        item.setCategory(normalize(request.category()));
        item.setUnitOfMeasure(normalize(request.unitOfMeasure()));
        item.setUnitId(request.unitId());
        item.setLocation(normalize(request.location()));
        item.setReorderThreshold(request.reorderThreshold());
        return toItemResponse(itemRepository.save(item));
    }

    public void deleteItem(Long itemId) {
        InventoryItem item = lockItem(itemId);
        if (transactionRepository.existsByItemItemId(itemId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Inventory item cannot be deleted because transaction history exists");
        }
        itemRepository.delete(item);
    }

    public InventoryItemResponse receiveStock(Long itemId, StockTransactionRequest request) {
        validateStockRequest(request, false);
        InventoryItem item = lockItem(itemId);
        item.setQuantityOnHand(item.getQuantityOnHand().add(request.quantity()));
        itemRepository.save(item);
        saveTransaction(item, "RECEIVE", request.quantity(), request.reason());
        return toItemResponse(item);
    }

    public InventoryItemResponse issueStock(Long itemId, StockTransactionRequest request) {
        validateStockRequest(request, false);
        InventoryItem item = lockItem(itemId);
        if (request.quantity().compareTo(item.getQuantityAvailable()) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Issue quantity exceeds available stock");
        }
        item.setQuantityOnHand(item.getQuantityOnHand().subtract(request.quantity()));
        itemRepository.save(item);
        saveTransaction(item, "ISSUE", request.quantity(), request.reason());
        return toItemResponse(item);
    }

    public InventoryItemResponse adjustStock(Long itemId, StockTransactionRequest request) {
        validateStockRequest(request, true);
        InventoryItem item = lockItem(itemId);
        String direction = normalize(request.direction()).toUpperCase(Locale.ROOT);

        if (direction.equals("INCREASE")) {
            item.setQuantityOnHand(item.getQuantityOnHand().add(request.quantity()));
        } else {
            BigDecimal adjustedOnHand = item.getQuantityOnHand().subtract(request.quantity());
            if (adjustedOnHand.compareTo(item.getQuantityReserved()) < 0) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Adjustment cannot reduce stock below reserved quantity");
            }
            item.setQuantityOnHand(adjustedOnHand);
        }

        itemRepository.save(item);
        String reason = direction + ": " + normalize(request.reason());
        saveTransaction(item, "ADJUST", request.quantity(), reason);
        return toItemResponse(item);
    }

    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getItemTransactions(Long itemId) {
        findItem(itemId);
        return transactionRepository.findByItemItemIdOrderByCreatedAtDesc(itemId)
                .stream().map(this::toTransactionResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> getTransactions(Long itemId) {
        List<InventoryTransaction> transactions;
        if (itemId == null) {
            transactions = transactionRepository.findAllByOrderByCreatedAtDesc();
        } else {
            findItem(itemId);
            transactions = transactionRepository.findByItemItemIdOrderByCreatedAtDesc(itemId);
        }
        return transactions.stream().map(this::toTransactionResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getLowStockItems() {
        return itemRepository.findLowStockItems().stream().map(this::toItemResponse).toList();
    }

    private InventoryItem findItem(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Inventory item not found with ID: " + itemId));
    }

    private InventoryItem lockItem(Long itemId) {
        return itemRepository.findByIdForUpdate(itemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Inventory item not found with ID: " + itemId));
    }

    private void saveTransaction(InventoryItem item, String type, BigDecimal quantity, String reason) {
        InventoryTransaction transaction = new InventoryTransaction();
        transaction.setItem(item);
        transaction.setTransactionType(type);
        transaction.setQuantity(quantity);
        transaction.setReason(normalize(reason));
        transactionRepository.save(transaction);
    }

    private void ensureCodeAvailable(String code) {
        if (itemRepository.existsByItemCodeIgnoreCase(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Item code already exists");
        }
    }

    private void validateCreateRequest(InventoryItemCreateRequest request) {
        if (request == null) {
            throw badRequest("Inventory item request is required");
        }
        validateCommonFields(request.itemCode(), request.itemName(), request.category(), request.unitOfMeasure(),
                request.unitId(), request.location());
        requireNonNegative(request.quantityOnHand(), "Quantity on hand");
        requireNonNegative(request.quantityReserved(), "Quantity reserved");
        requireNonNegative(request.reorderThreshold(), "Reorder threshold");
    }

    private void validateUpdateRequest(InventoryItemUpdateRequest request) {
        if (request == null) {
            throw badRequest("Inventory item request is required");
        }
        validateCommonFields(request.itemCode(), request.itemName(), request.category(), request.unitOfMeasure(),
                request.unitId(), request.location());
        requireNonNegative(request.reorderThreshold(), "Reorder threshold");
    }

    private void validateCommonFields(String code, String name, String category, String unitOfMeasure,
                                      Long unitId, String location) {
        requireText(code, "Item code");
        requireText(name, "Item name");
        requireText(category, "Category");
        requireText(unitOfMeasure, "Unit of measure");
        if (unitId == null || unitId <= 0) {
            throw badRequest("Unit ID must be greater than zero");
        }
        requireText(location, "Location");
    }

    private void validateStockRequest(StockTransactionRequest request, boolean requireDirection) {
        if (request == null) {
            throw badRequest("Stock transaction request is required");
        }
        if (request.quantity() == null || request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw badRequest("Quantity must be greater than zero");
        }
        requireText(request.reason(), "Reason");
        if (requireDirection && (isBlank(request.direction())
                || !(request.direction().equalsIgnoreCase("INCREASE")
                || request.direction().equalsIgnoreCase("DECREASE")))) {
            throw badRequest("Direction must be INCREASE or DECREASE");
        }
    }

    private void requireNonNegative(BigDecimal value, String label) {
        if (value != null && value.compareTo(BigDecimal.ZERO) < 0) {
            throw badRequest(label + " cannot be negative");
        }
    }

    private void requireText(String value, String label) {
        if (isBlank(value)) {
            throw badRequest(label + " is required");
        }
    }

    private void addTextFilter(List<Predicate> predicates, jakarta.persistence.criteria.Root<InventoryItem> root,
                               jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
                               String field, String value) {
        if (!isBlank(value)) {
            predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get(field)),
                    normalize(value).toLowerCase(Locale.ROOT)));
        }
    }

    private ResponseStatusException badRequest(String reason) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, reason);
    }

    private BigDecimal zeroIfNull(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private InventoryItemResponse toItemResponse(InventoryItem item) {
        return new InventoryItemResponse(item.getItemId(), item.getItemCode(), item.getItemName(), item.getCategory(),
                item.getUnitOfMeasure(), item.getUnitId(), item.getLocation(), item.getQuantityOnHand(),
                item.getQuantityReserved(), item.getQuantityAvailable(), item.getReorderThreshold(),
                item.getCreatedAt(), item.getUpdatedAt());
    }

    private InventoryTransactionResponse toTransactionResponse(InventoryTransaction transaction) {
        return new InventoryTransactionResponse(transaction.getTransactionId(), transaction.getItem().getItemId(),
                transaction.getTransactionType(), transaction.getQuantity(), transaction.getReason(),
                transaction.getCreatedAt());
    }
}