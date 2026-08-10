package com.fastfood.order.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
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
        String value = readConfigValue(CONFIG_KEY);
        if (value == null) {
            ensureConfig(CONFIG_KEY, String.valueOf(DEFAULT_INTERVAL_HOURS),
                    "Stock warning check interval in hours");
            return DEFAULT_INTERVAL_HOURS;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Invalid stock warning interval '{}', using default {}", value, DEFAULT_INTERVAL_HOURS);
            return DEFAULT_INTERVAL_HOURS;
        }
    }

    public void setWarningIntervalHours(int hours) {
        ensureConfig(CONFIG_KEY, String.valueOf(hours), "Stock warning check interval in hours");
        log.info("Updated stock warning interval to {} hours", hours);
    }

    public long getWarningIntervalMillis() {
        return getWarningIntervalHours() * 3600000L;
    }

    public boolean isAlertsEnabled() {
        String value = readConfigValue(ALERTS_ENABLED_KEY);
        if (value == null) {
            ensureConfig(ALERTS_ENABLED_KEY, String.valueOf(DEFAULT_ALERTS_ENABLED),
                    "Enable/disable stock alerts notifications");
            return DEFAULT_ALERTS_ENABLED;
        }
        return Boolean.parseBoolean(value.trim());
    }

    public void setAlertsEnabled(boolean enabled) {
        ensureConfig(ALERTS_ENABLED_KEY, String.valueOf(enabled),
                "Enable/disable stock alerts notifications");
        log.info("Updated stock alerts enabled to {}", enabled);
    }

    private String readConfigValue(String key) {
        try {
            return jdbcTemplate.query(
                    "SELECT config_value FROM brand_config WHERE config_key = ?",
                    rs -> rs.next() ? rs.getString(1) : null,
                    key);
        } catch (EmptyResultDataAccessException e) {
            return null;
        } catch (Exception e) {
            log.warn("Error reading brand_config key {}: {}", key, e.getMessage());
            return null;
        }
    }

    private void ensureConfig(String key, String value, String description) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO brand_config (config_key, config_value, description, updated_at) " +
                            "VALUES (?, ?, ?, CURRENT_TIMESTAMP) " +
                            "ON CONFLICT (config_key) DO UPDATE SET " +
                            "config_value = EXCLUDED.config_value, updated_at = CURRENT_TIMESTAMP",
                    key, value, description);
        } catch (Exception e) {
            log.error("Failed to upsert brand_config key {}", key, e);
            throw new RuntimeException("Failed to update " + key, e);
        }
    }
}
