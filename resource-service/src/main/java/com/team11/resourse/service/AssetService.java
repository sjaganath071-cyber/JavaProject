package com.team11.resourse.service;

import com.team11.resourse.dto.AssetCreateRequest;
import com.team11.resourse.dto.AssetResponse;
import com.team11.resourse.dto.AssetUpdateRequest;
import com.team11.resourse.entity.Asset;
import com.team11.resourse.repository.AssetRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public AssetResponse createAsset(AssetCreateRequest request) {
        validateRequest(request);
        String assetTag = normalize(request.assetTag());
        ensureTagAvailable(assetTag);

        Asset asset = new Asset();
        applyFields(asset, request.assetName(), request.category(), request.unitId(), request.location(),
                request.condition(), request.status());
        asset.setAssetTag(assetTag);
        return toResponse(assetRepository.save(asset));
    }

    @Transactional(readOnly = true)
    public List<AssetResponse> getAssets(Long unitId, String category, String location, String status) {
        if (unitId != null && unitId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unit ID must be greater than zero");
        }

        Specification<Asset> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (unitId != null) {
                predicates.add(criteriaBuilder.equal(root.get("unitId"), unitId));
            }
            addTextFilter(predicates, root, criteriaBuilder, "category", category);
            addTextFilter(predicates, root, criteriaBuilder, "location", location);
            addTextFilter(predicates, root, criteriaBuilder, "status", status);

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return assetRepository.findAll(specification).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AssetResponse getAssetById(Long id) {
        return toResponse(findAsset(id));
    }

    public AssetResponse updateAsset(Long id, AssetUpdateRequest request) {
        validateRequest(request);
        Asset asset = findAsset(id);
        String assetTag = normalize(request.assetTag());
        if (!asset.getAssetTag().equalsIgnoreCase(assetTag)) {
            ensureTagAvailable(assetTag);
        }

        asset.setAssetTag(assetTag);
        applyFields(asset, request.assetName(), request.category(), request.unitId(), request.location(),
                request.condition(), request.status());
        return toResponse(assetRepository.save(asset));
    }

    public void deleteAsset(Long id) {
        assetRepository.delete(findAsset(id));
    }

    private void addTextFilter(List<Predicate> predicates, jakarta.persistence.criteria.Root<Asset> root,
                               jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
                               String field, String value) {
        if (!isBlank(value)) {
            predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get(field)), normalize(value).toLowerCase(Locale.ROOT)));
        }
    }

    private void applyFields(Asset asset, String assetName, String category, Long unitId, String location,
                             String condition, String status) {
        asset.setAssetName(normalize(assetName));
        asset.setCategory(normalize(category));
        asset.setUnitId(unitId);
        asset.setLocation(normalize(location));
        asset.setCondition(normalize(condition));
        asset.setStatus(normalize(status));
    }

    private void validateRequest(AssetCreateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset request is required");
        }
        validateRequiredFields(request.assetTag(), request.assetName(), request.category(), request.unitId(),
                request.location(), request.condition(), request.status());
    }

    private void validateRequest(AssetUpdateRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset request is required");
        }
        validateRequiredFields(request.assetTag(), request.assetName(), request.category(), request.unitId(),
                request.location(), request.condition(), request.status());
    }

    private void validateRequiredFields(String assetTag, String assetName, String category, Long unitId,
                                        String location, String condition, String status) {
        if (isBlank(assetTag)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset tag is required");
        }
        if (isBlank(assetName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Asset name is required");
        }
        if (isBlank(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category is required");
        }
        if (unitId == null || unitId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unit ID must be greater than zero");
        }
        if (isBlank(location)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Location is required");
        }
        if (isBlank(condition)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Condition is required");
        }
        if (isBlank(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status is required");
        }
    }

    private void ensureTagAvailable(String assetTag) {
        if (assetRepository.existsByAssetTagIgnoreCase(assetTag)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Asset tag already exists");
        }
    }

    private Asset findAsset(Long id) {
        return assetRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found with ID: " + id));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private AssetResponse toResponse(Asset asset) {
        return new AssetResponse(
                asset.getAssetId(),
                asset.getAssetTag(),
                asset.getAssetName(),
                asset.getCategory(),
                asset.getUnitId(),
                asset.getLocation(),
                asset.getCondition(),
                asset.getStatus(),
                asset.getCreatedAt(),
                asset.getUpdatedAt());
    }
}