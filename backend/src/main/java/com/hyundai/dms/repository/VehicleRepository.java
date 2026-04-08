package com.hyundai.dms.repository;

import com.hyundai.dms.entity.Vehicle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    @Query("SELECT SUM(v.stock) FROM Vehicle v WHERE v.dealerId = :dealerId")
    Long sumTotalStockByDealerId(@Param("dealerId") Long dealerId);

    @Query("SELECT SUM(v.stock) FROM Vehicle v")
    Long sumTotalStockGlobal();

    long countByStockLessThan(int threshold);

    long countByStockLessThanAndDealerId(int threshold, Long dealerId);

    @Query("SELECT DISTINCT v.modelName FROM Vehicle v WHERE (:dealerId IS NULL OR v.dealerId = :dealerId) ORDER BY v.modelName")
    java.util.List<String> findDistinctModelNamesByDealerId(@Param("dealerId") Long dealerId);

    @Query("SELECT DISTINCT v.variant FROM Vehicle v WHERE (:dealerId IS NULL OR v.dealerId = :dealerId) AND v.modelName = :modelName AND v.variant IS NOT NULL ORDER BY v.variant")
    java.util.List<String> findDistinctVariantsByDealerIdAndModelName(@Param("dealerId") Long dealerId, @Param("modelName") String modelName);

    @Query("SELECT v FROM Vehicle v WHERE v.dealerId = :dealerId AND " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(v.modelName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.variant) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR " +
           "(:status = 'OUT_OF_STOCK' AND v.stock = 0) OR " +
           "(:status = 'LOW_STOCK' AND v.stock > 0 AND v.stock < 3) OR " +
           "(:status = 'AVAILABLE' AND v.stock >= 3)) AND " +
           "(:minPrice IS NULL OR v.basePrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR v.basePrice <= :maxPrice) AND " +
           "(:year IS NULL OR v.year = :year)")
    Page<Vehicle> findWithSearch(
            @Param("dealerId") Long dealerId,
            @Param("search") String search,
            @Param("status") String status,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("year") Integer year,
            Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE " +
           "(:search IS NULL OR :search = '' OR " +
           "LOWER(v.modelName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.brand) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(v.variant) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:status IS NULL OR :status = '' OR " +
           "(:status = 'OUT_OF_STOCK' AND v.stock = 0) OR " +
           "(:status = 'LOW_STOCK' AND v.stock > 0 AND v.stock < 3) OR " +
           "(:status = 'AVAILABLE' AND v.stock >= 3)) AND " +
           "(:minPrice IS NULL OR v.basePrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR v.basePrice <= :maxPrice) AND " +
           "(:year IS NULL OR v.year = :year)")
    Page<Vehicle> findWithSearchAll(
            @Param("search") String search,
            @Param("status") String status,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("year") Integer year,
            Pageable pageable);
}
