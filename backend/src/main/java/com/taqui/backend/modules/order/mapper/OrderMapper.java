package com.taqui.backend.modules.order.mapper;

import com.taqui.backend.modules.order.dto.AddressDTO;
import com.taqui.backend.modules.order.dto.OrderResponseDTO;
import com.taqui.backend.modules.order.entity.Address;
import com.taqui.backend.modules.order.entity.Order;
import com.taqui.backend.modules.product.mapper.ProductMapper;
import com.taqui.backend.modules.user.mapper.UserMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ProductMapper.class})
public interface OrderMapper {

    OrderResponseDTO toResponseDTO(Order order);

    Address toAddress(AddressDTO addressDTO);
}
