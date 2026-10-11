package com.team11.resourse.repository;

import com.team11.resourse.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long>, JpaSpecificationExecutor<Asset> {

    Optional<Asset> findByAssetTagIgnoreCase(String assetTag);

    boolean existsByAssetTagIgnoreCase(String assetTag);

    List<Asset> findByUnitId(Long unitId);

    List<Asset> findByCategoryIgnoreCase(String category);

    List<Asset> findByLocationIgnoreCase(String location);

    List<Asset> findByStatusIgnoreCase(String status);
}