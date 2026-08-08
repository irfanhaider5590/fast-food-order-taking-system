package com.fastfood.order.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String MENU_CATEGORIES = "menuCategories";
    public static final String MENU_ITEMS = "menuItems";
    public static final String ADD_ONS = "addOns";
    public static final String COMBOS = "combos";
    public static final String SETTINGS = "settings";
    public static final String ACTIVE_VOUCHERS = "activeVouchers";

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                MENU_CATEGORIES, MENU_ITEMS, ADD_ONS, COMBOS, SETTINGS, ACTIVE_VOUCHERS
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .recordStats());
        return cacheManager;
    }
}
