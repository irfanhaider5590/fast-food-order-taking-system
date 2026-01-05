package com.fastfood.order.infrastructure.config;

import com.fastfood.order.infrastructure.interceptor.LicenseValidationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final LicenseValidationInterceptor licenseValidationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(licenseValidationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/license/**",
                        "/api/auth/**",
                        "/api/public/**",
                        "/api/health/**",
                        "/api/receipt/**"
                );
    }
}

