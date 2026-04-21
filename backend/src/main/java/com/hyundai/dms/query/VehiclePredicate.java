package com.hyundai.dms.query;

import com.hyundai.dms.entity.QVehicle;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import java.math.BigDecimal;

/**
 * QueryDSL predicate builder for Vehicle dynamic filtering.
 */
public class VehiclePredicate {

    private VehiclePredicate() {}

    /**
     * @param dealerId  null = admin (all dealers), non-null = dealer-scoped
     * @param search    text search on modelName, brand, variant
     * @param status    AVAILABLE | LOW_STOCK | OUT_OF_STOCK
     * @param minPrice  minimum base price
     * @param maxPrice  maximum base price
     * @param year      exact year
     */
    public static Predicate build(Long dealerId, String search, String status,
                                  BigDecimal minPrice, BigDecimal maxPrice, Integer year) {
        QVehicle v = QVehicle.vehicle;
        BooleanBuilder builder = new BooleanBuilder();

        if (dealerId != null) {
            builder.and(v.dealerId.eq(dealerId));
        }

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            builder.and(
                v.modelName.lower().like(pattern)
                .or(v.brand.lower().like(pattern))
                .or(v.variant.lower().like(pattern))
            );
        }

        if (status != null && !status.isBlank()) {
            switch (status.toUpperCase()) {
                case "OUT_OF_STOCK" -> builder.and(v.stock.eq(0));
                case "LOW_STOCK"    -> builder.and(v.stock.gt(0).and(v.stock.lt(3)));
                case "AVAILABLE"    -> builder.and(v.stock.goe(3));
            }
        }

        if (minPrice != null) builder.and(v.basePrice.goe(minPrice));
        if (maxPrice != null) builder.and(v.basePrice.loe(maxPrice));
        if (year     != null) builder.and(v.year.eq(year));

        return builder;
    }
}
