package com.hyundai.dms.projection;

import java.math.BigDecimal;

public interface DealerProjection {
    Long getId();
    String getName();
    String getAddress();
    Boolean getIsActive();
    Long getTotalSales();
    BigDecimal getTotalRevenue();
}
