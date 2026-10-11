package com.team11.defense_logistics.controller;

import com.team11.defense_logistics.dto.AssetDto;
import com.team11.defense_logistics.service.AssetService;
import com.team11.defense_logistics.service.AssetService.AssetServiceException;

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
import java.util.Locale;

@Controller
@RequestMapping("/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public String listAssets(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            Model model) {

        try {
            List<AssetDto> allAssets = assetService.getAllAssets();

            List<AssetDto> filteredAssets = allAssets.stream()
                    .filter(asset -> matchesKeyword(asset, keyword))
                    .filter(asset -> matchesCategory(asset, category))
                    .filter(asset -> matchesStatus(asset, status))
                    .toList();

            model.addAttribute("assetList", filteredAssets);
            model.addAttribute("keyword", keyword == null ? "" : keyword);
            model.addAttribute("category", category == null ? "" : category);
            model.addAttribute("status", status == null ? "" : status);

            model.addAttribute(
                    "categories",
                    allAssets.stream()
                            .map(AssetDto::getCategory)
                            .filter(value -> value != null && !value.isBlank())
                            .distinct()
                            .sorted()
                            .toList()
            );

        } catch (AssetServiceException exception) {
            model.addAttribute("assetList", List.of());
            model.addAttribute("categories", List.of());
            model.addAttribute("keyword", keyword == null ? "" : keyword);
            model.addAttribute("category", category == null ? "" : category);
            model.addAttribute("status", status == null ? "" : status);
            model.addAttribute("errorMessage", exception.getMessage());
        }

        return "assets/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("asset", new AssetDto());
        model.addAttribute("pageTitle", "Add Asset");
        model.addAttribute("formAction", "/assets/new");
        model.addAttribute("formMode", "create");

        return "assets/form";
    }

    @PostMapping("/new")
    public String createAsset(
            @Valid @ModelAttribute("asset") AssetDto asset,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Add Asset");
            model.addAttribute("formAction", "/assets/new");
            model.addAttribute("formMode", "create");

            return "assets/form";
        }

        try {
            assetService.createAsset(asset);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Asset created successfully."
            );

            return "redirect:/assets";

        } catch (AssetServiceException exception) {
            model.addAttribute("pageTitle", "Add Asset");
            model.addAttribute("formAction", "/assets/new");
            model.addAttribute("formMode", "create");
            model.addAttribute("errorMessage", exception.getMessage());

            return "assets/form";
        }
    }

    @GetMapping("/{id}")
    public String viewAsset(
            @PathVariable Long id,
            Model model) {

        try {
            model.addAttribute("asset", assetService.getAssetById(id));

            return "assets/details";

        } catch (AssetServiceException exception) {
            model.addAttribute("errorMessage", exception.getMessage());

            return "assets/details";
        }
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable Long id,
            Model model) {

        try {
            model.addAttribute("asset", assetService.getAssetById(id));
            model.addAttribute("pageTitle", "Edit Asset");
            model.addAttribute("formAction", "/assets/" + id + "/edit");
            model.addAttribute("formMode", "edit");

            return "assets/form";

        } catch (AssetServiceException exception) {
            model.addAttribute("errorMessage", exception.getMessage());

            return "assets/details";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateAsset(
            @PathVariable Long id,
            @Valid @ModelAttribute("asset") AssetDto asset,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("pageTitle", "Edit Asset");
            model.addAttribute("formAction", "/assets/" + id + "/edit");
            model.addAttribute("formMode", "edit");

            return "assets/form";
        }

        try {
            assetService.updateAsset(id, asset);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Asset updated successfully."
            );

            return "redirect:/assets/" + id;

        } catch (AssetServiceException exception) {
            model.addAttribute("pageTitle", "Edit Asset");
            model.addAttribute("formAction", "/assets/" + id + "/edit");
            model.addAttribute("formMode", "edit");
            model.addAttribute("errorMessage", exception.getMessage());

            return "assets/form";
        }
    }

    private boolean matchesKeyword(AssetDto asset, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }

        String search = keyword.toLowerCase(Locale.ROOT);

        return contains(asset.getAssetTag(), search)
                || contains(asset.getName(), search)
                || contains(asset.getCategory(), search)
                || contains(asset.getUnit(), search)
                || contains(asset.getLocation(), search);
    }

    private boolean matchesCategory(AssetDto asset, String category) {
        return category == null
                || category.isBlank()
                || (asset.getCategory() != null
                && asset.getCategory().equalsIgnoreCase(category));
    }

    private boolean matchesStatus(AssetDto asset, String status) {
        return status == null
                || status.isBlank()
                || (asset.getStatus() != null
                && asset.getStatus().equalsIgnoreCase(status));
    }

    private boolean contains(String value, String search) {
        return value != null
                && value.toLowerCase(Locale.ROOT).contains(search);
    }
}
