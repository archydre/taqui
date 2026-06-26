package com.taqui.backend.modules.post.mapper;

import com.taqui.backend.modules.post.dto.PostRequestDTO;
import com.taqui.backend.modules.post.dto.PostResponseDTO;
import com.taqui.backend.modules.post.entity.Post;
import com.taqui.backend.modules.product.mapper.ProductMapper;
import com.taqui.backend.modules.user.mapper.UserMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {UserMapper.class, ProductMapper.class})
public interface PostMapper {

    @Mapping(target = "postId", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Post toEntity(PostRequestDTO postRequestDTO);

    PostResponseDTO toResponseDTO(Post post);

    @Mapping(target = "postId", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "product", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(PostRequestDTO postRequestDTO, @MappingTarget Post post);
}
