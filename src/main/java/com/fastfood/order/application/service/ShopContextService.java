package com.fastfood.order.application.service;

import com.fastfood.order.domain.entity.Shop;
import com.fastfood.order.domain.entity.User;
import com.fastfood.order.infrastructure.repository.ShopRepository;
import com.fastfood.order.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShopContextService {

    public static final long DEFAULT_SHOP_ID = 1L;

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Shop requireCurrentShop() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() != null
                && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null && user.getShop() != null) {
                return user.getShop();
            }
        }
        return shopRepository.findByIdAndIsActiveTrue(DEFAULT_SHOP_ID)
                .or(() -> shopRepository.findById(DEFAULT_SHOP_ID))
                .orElseThrow(() -> new RuntimeException("Default shop not found. Run Flyway migrations."));
    }

    @Transactional(readOnly = true)
    public Long requireCurrentShopId() {
        return requireCurrentShop().getId();
    }
}
