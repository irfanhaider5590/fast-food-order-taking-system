package com.fastfood.order.application.mapper;

import com.fastfood.order.application.dto.OrderResponse;
import com.fastfood.order.domain.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = { OrderItemMapper.class })
public interface OrderMapper {
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.name")
    @Mapping(target = "items", ignore = true) // Items will be mapped separately in service
    @Mapping(target = "stockWarnings", ignore = true) // Stock warnings set separately in service
    OrderResponse toResponse(Order order);
}
