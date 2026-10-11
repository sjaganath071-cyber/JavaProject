package com.team11.defense_logistics.controller;

import com.team11.defense_logistics.dto.InventoryItemDto;
import com.team11.defense_logistics.service.InventoryService;
import com.team11.defense_logistics.service.InventoryService.InventoryServiceException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public String listItems(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String stockStatus,
            Model model) {

        try {
            List<InventoryItemDto> allItems = inventoryService.getAllItems();

            List<InventoryItemDto> filteredItems = allItems.stream()
                    .filter(item -> matchesKeyword(item, keyword))
                    .filter(item -> matchesCategory(item, category))
                    .filter(item -> matchesStockStatus(item, stockStatus))
                    .collect(Collectors.toList());

            long lowStockCount = allItems.stream()
                    .filter(item -> "LOW_STOCK".equals(item.getStockStatus()))
                    .count();

            long outOfStockCount = allItems.stream()
                    .filter(item -> "OUT_OF_STOCK".equals(item.getStockStatus()))
                    .count();

            model.addAttribute("items", filteredItems);
            model.addAttribute("keyword", keyword == null ? "" : keyword);
            model.addAttribute("selectedCategory", category == null ? "" : category);
            model.addAttribute("selectedStockStatus",
                    stockStatus == null ? "" : stockStatus);
            model.addAttribute("categories", allItems.stream()
                    .map(InventoryItemDto::getCategory)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList()));
            model.addAttribute("totalItems", allItems.size());
            model.addAttribute("lowStockCount", lowStockCount);
            model.addAttribute("outOfStockCount", outOfStockCount);

        } catch (InventoryServiceException exception) {
            model.addAttribute("items", List.of());
            model.addAttribute("keyword", keyword == null ? "" : keyword);
            model.addAttribute("selectedCategory", category == null ? "" : category);
            model.addAttribute("selectedStockStatus",
                    stockStatus == null ? "" : stockStatus);
            model.addAttribute("categories", List.of());
            model.addAttribute("totalItems", 0);
            model.addAttribute("lowStockCount", 0);
            model.addAttribute("outOfStockCount", 0);
            model.addAttribute("errorMessage", exception.getMessage());
        }

        return "inventory/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("item", new InventoryItemDto());
        model.addAttribute("formTitle", "Add Inventory Item");
        model.addAttribute("formAction", "/inventory");
        return "inventory/form";
    }

    @PostMapping
    public String createItem(
            @Valid @ModelAttribute("item") InventoryItemDto item,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "Add Inventory Item");
            model.addAttribute("formAction", "/inventory");
            return "inventory/form";
        }

        try {
            inventoryService.createItem(item);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Inventory item created successfully."
            );
            return "redirect:/inventory";

        } catch (InventoryServiceException exception) {
            model.addAttribute("formTitle", "Add Inventory Item");
            model.addAttribute("formAction", "/inventory");
            model.addAttribute("errorMessage", exception.getMessage());
            return "inventory/form";
        }
    }

    @GetMapping("/{id}")
    public String viewItem(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            model.addAttribute("item", inventoryService.getItemById(id));
            return "inventory/details";

        } catch (InventoryServiceException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
            return "redirect:/inventory";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {
            model.addAttribute("item", inventoryService.getItemById(id));
            model.addAttribute("formTitle", "Edit Inventory Item");
            model.addAttribute("formAction", "/inventory/" + id);
            return "inventory/form";

        } catch (InventoryServiceException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
            return "redirect:/inventory";
        }
    }

    @PostMapping("/{id}")
    public String updateItem(
            @PathVariable Long id,
            @Valid @ModelAttribute("item") InventoryItemDto item,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "Edit Inventory Item");
            model.addAttribute("formAction", "/inventory/" + id);
            return "inventory/form";
        }

        try {
            inventoryService.updateItem(id, item);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Inventory item updated successfully."
            );

            return "redirect:/inventory/" + id;

        } catch (InventoryServiceException exception) {
            model.addAttribute("formTitle", "Edit Inventory Item");
            model.addAttribute("formAction", "/inventory/" + id);
            model.addAttribute("errorMessage", exception.getMessage());
            return "inventory/form";
        }
    }

    private boolean matchesKeyword(InventoryItemDto item, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String search = keyword.trim().toLowerCase();

        return contains(item.getItemCode(), search)
                || contains(item.getName(), search)
                || contains(item.getCategory(), search)
                || contains(item.getStorageLocation(), search);
    }

    private boolean matchesCategory(InventoryItemDto item, String category) {
        return category == null
                || category.isBlank()
                || category.equalsIgnoreCase(item.getCategory());
    }

    private boolean matchesStockStatus(
            InventoryItemDto item,
            String stockStatus) {

        return stockStatus == null
                || stockStatus.isBlank()
                || stockStatus.equalsIgnoreCase(item.getStockStatus());
    }

    private boolean contains(String value, String search) {
        return value != null
                && value.toLowerCase().contains(search);
    }
}
