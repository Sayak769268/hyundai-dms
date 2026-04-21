package com.hyundai.dms.query;

import com.hyundai.dms.entity.QSalesOrder;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * QueryDSL predicate builder for SalesOrder dynamic filtering.
 */
public class SalesOrderPredicate {

    private SalesOrderPredicate() {}

    /**
     * @param dealerId  null = admin (all dealers), non-null = dealer-scoped
     * @param search    text search on customer name, vehicle model/variant
     * @param status    PENDING | CONFIRMED | INVOICED | CANCELLED
     * @param minAmount minimum final amount
     * @param maxAmount maximum final amount
     * @param fromDate  start date (inclusive)
     * @param toDate    end date (inclusive)
     */
    public static Predicate build(Long dealerId, String search, String status,
                                  BigDecimal minAmount, BigDecimal maxAmount,
                                  LocalDateTime fromDate, LocalDateTime toDate) {
        QSalesOrder s = QSalesOrder.salesOrder;
        BooleanBuilder builder = new BooleanBuilder();

        if (dealerId != null) {
            builder.and(s.dealerId.eq(dealerId));
        }

        if (status != null && !status.isBlank()) {
            builder.and(s.status.equalsIgnoreCase(status));
        }

        if (minAmount != null) builder.and(s.finalAmount.goe(minAmount));
        if (maxAmount != null) builder.and(s.finalAmount.loe(maxAmount));
        if (fromDate  != null) builder.and(s.createdAt.goe(fromDate));
        if (toDate    != null) builder.and(s.createdAt.loe(toDate));

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            builder.and(
                s.customer.firstName.lower().like(pattern)
                .or(s.customer.lastName.lower().like(pattern))
                .or(s.vehicle.modelName.lower().like(pattern))
                .or(s.vehicle.variant.lower().like(pattern))
            );
        }

        return builder;
    }
}
