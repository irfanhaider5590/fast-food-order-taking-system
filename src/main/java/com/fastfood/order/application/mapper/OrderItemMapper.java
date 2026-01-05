package com.fastfood.order.application.mapper;

import com.fastfood.order.application.dto.OrderItemResponse;
import com.fastfood.order.domain.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
    @Mapping(target = "menuItemId", source = "menuItem.id")
    @Mapping(target = "comboId", source = "combo.id")
    OrderItemResponse toResponse(OrderItem orderItem);
}
