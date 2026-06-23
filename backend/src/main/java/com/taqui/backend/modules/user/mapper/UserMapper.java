package com.taqui.backend.modules.user.mapper;

import com.taqui.backend.modules.user.dto.UserLoginDTO;
import com.taqui.backend.modules.user.dto.UserResponseDTO;
import com.taqui.backend.modules.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    User toEntity(UserLoginDTO userLoginDTO);

    UserResponseDTO toResponse(User user);
}
