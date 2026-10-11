package com.team11.resourse.controller;

import com.team11.resourse.dto.AssetCreateRequest;
import com.team11.resourse.dto.AssetResponse;
import com.team11.resourse.dto.AssetUpdateRequest;
import com.team11.resourse.service.AssetService;
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
@RequestMapping("/api/v1/assets")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AssetResponse createAsset(@Valid @RequestBody AssetCreateRequest request) {
        return assetService.createAsset(request);
    }

    @GetMapping
    public List<AssetResponse> getAssets(
            @RequestParam(required = false) Long unitId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String status) {
        return assetService.getAssets(unitId, category, location, status);
    }

    @GetMapping("/{id}")
    public AssetResponse getAssetById(@PathVariable Long id) {
        return assetService.getAssetById(id);
    }

    @PutMapping("/{id}")
    public AssetResponse updateAsset(@PathVariable Long id, @Valid @RequestBody AssetUpdateRequest request) {
        return assetService.updateAsset(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAsset(@PathVariable Long id) {
        assetService.deleteAsset(id);
    }
}