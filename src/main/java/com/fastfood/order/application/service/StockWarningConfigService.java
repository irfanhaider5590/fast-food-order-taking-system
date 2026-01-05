package com.fastfood.order.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockWarningConfigService {

    private final JdbcTemplate jdbcTemplate;
    private static final String CONFIG_KEY = "STOCK_WARNING_INTERVAL_HOURS";
    private static final String ALERTS_ENABLED_KEY = "STOCK_ALERTS_ENABLED";
    private static final int DEFAULT_INTERVAL_HOURS = 2;
    private static final boolean DEFAULT_ALERTS_ENABLED = true;

    public int getWarningIntervalHours() {
        try {
            String sql = "SELECT config_value FROM brand_config WHERE config_key = ?";
            String value = jdbcTemplate.queryForObject(sql, String.class, CONFIG_KEY);
            if (value != null) {
                return Integer.parseInt(value.trim());
            }
        } catch (Exception e) {
            log.warn("Error reading stock warning interval config, using default: {}", DEFAULT_INTERVAL_HOURS, e);
        }
        return DEFAULT_INTERVAL_HOURS;
    }

    public void setWarningIntervalHours(int hours) {
        try {
            String sql = "INSERT INTO brand_config (config_key, config_value, description, updated_at) " +
                        "VALUES (?, ?, ?, CURRENT_TIMESTAMP) " +
                        "ON CONFLICT (config_key) DO UPDATE SET config_value = EXCLUDED.config_value, updated_at = CURRENT_TIMESTAMP";
            jdbcTemplate.update(sql, CONFIG_KEY, String.valueOf(hours), "Stock warning check interval in hours");
            log.info("Updated stock warning interval to {} hours", hours);
        } catch (Exception e) {
            log.error("Error updating stock warning interval config", e);
            throw new RuntimeException("Failed to update stock warning interval", e);
        }
    }

    public long getWarningIntervalMillis() {
        return getWarningIntervalHours() * 3600000L; // Convert hours to milliseconds
    }

    public boolean isAlertsEnabled() {
        try {
            String sql = "SELECT config_value FROM brand_config WHERE config_key = ?";
            String value = jdbcTemplate.queryForObject(sql, String.class, ALERTS_ENABLED_KEY);
            if (value != null) {
                return Boolean.parseBoolean(value.trim());
            }
        } catch (Exception e) {
            log.warn("Error reading stock alerts enabled config, using default: {}", DEFAULT_ALERTS_ENABLED, e);
        }
        return DEFAULT_ALERTS_ENABLED;
    }

    public void setAlertsEnabled(boolean enabled) {
        try {
            String sql = "INSERT INTO brand_config (config_key, config_value, description, updated_at) " +
                        "VALUES (?, ?, ?, CURRENT_TIMESTAMP) " +
                        "ON CONFLICT (config_key) DO UPDATE SET config_value = EXCLUDED.config_value, updated_at = CURRENT_TIMESTAMP";
            jdbcTemplate.update(sql, ALERTS_ENABLED_KEY, String.valueOf(enabled), "Enable/disable stock alerts notifications");
            log.info("Updated stock alerts enabled to {}", enabled);
        } catch (Exception e) {
            log.error("Error updating stock alerts enabled config", e);
            throw new RuntimeException("Failed to update stock alerts enabled", e);
        }
    }
}

