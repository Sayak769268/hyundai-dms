package com.hyundai.dms.query;

import com.hyundai.dms.entity.Customer;
import com.hyundai.dms.entity.QCustomer;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;

/**
 * QueryDSL predicate builder for Customer dynamic filtering.
 * Type-safe, composable — replaces hardcoded JPQL @Query strings.
 */
public class CustomerPredicate {

    private CustomerPredicate() {}

    /**
     * @param dealerId           null = admin (all dealers), non-null = dealer-scoped
     * @param search             optional text search across name/email/phone
     * @param status             optional lead status filter
     * @param assignedEmployeeId optional employee assignment filter
     * @param activeOnly         restrict to is_active = true
     */
    public static Predicate build(Long dealerId, String search,
                                  Customer.CustomerStatus status,
                                  Long assignedEmployeeId,
                                  boolean activeOnly) {
        QCustomer c = QCustomer.customer;
        BooleanBuilder builder = new BooleanBuilder();

        if (activeOnly) {
            builder.and(c.isActive.isTrue());
        }

        if (dealerId != null) {
            builder.and(c.dealer.id.eq(dealerId));
        }

        if (status != null) {
            builder.and(c.status.eq(status));
        }

        if (assignedEmployeeId != null) {
            builder.and(c.assignedEmployeeId.eq(assignedEmployeeId));
        }

        if (search != null && !search.isBlank()) {
            String pattern = "%" + search.toLowerCase() + "%";
            builder.and(
                c.firstName.lower().like(pattern)
                .or(c.lastName.lower().like(pattern))
                .or(c.email.lower().like(pattern))
                .or(c.phone.like("%" + search + "%"))
            );
        }

        return builder;
    }

    /** Employee-scoped: only their assigned customers within their dealer. */
    public static Predicate buildForEmployee(Long dealerId, Long employeeId,
                                             String search,
                                             Customer.CustomerStatus status) {
        return build(dealerId, search, status, employeeId, true);
    }
}
