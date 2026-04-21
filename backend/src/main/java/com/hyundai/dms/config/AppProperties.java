package com.hyundai.dms.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Strongly-typed binding for all app.* properties in application.yml.
 * Inject this anywhere instead of scattering @Value annotations.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String name;
    private String version;
    private Security security = new Security();
    private Pagination pagination = new Pagination();
    private Stock stock = new Stock();

    @Data
    public static class Security {
        private int maxFailedAttempts = 5;
        private int lockDurationMinutes = 30;
    }

    @Data
    public static class Pagination {
        private int defaultPageSize = 15;
        private int maxPageSize = 100;
    }

    @Data
    public static class Stock {
        private int lowStockThreshold = 3;
    }
}
