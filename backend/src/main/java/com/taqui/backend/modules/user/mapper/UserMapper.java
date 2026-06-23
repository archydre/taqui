package com.taqui.backend.modules.user.mapper;

import com.taqui.backend.modules.user.dto.RegisterRequestDTO;
import com.taqui.backend.modules.user.dto.UserPublicInfoDTO;
import com.taqui.backend.modules.user.dto.UserResponseDTO;
import com.taqui.backend.modules.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    User toEntity(RegisterRequestDTO registerRequestDTO);

    UserResponseDTO toResponse(User user);

    UserPublicInfoDTO toUserPublicInfo(User user);
}
