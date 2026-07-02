package com.taqui.backend.modules.cart.mapper;

import com.taqui.backend.modules.cart.dto.CartItemResponseDTO;
import com.taqui.backend.modules.cart.entity.CartItem;
import com.taqui.backend.modules.product.mapper.ProductMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProductMapper.class})
public interface CartMapper {

    CartItemResponseDTO toResponseDTO(CartItem item);
}
