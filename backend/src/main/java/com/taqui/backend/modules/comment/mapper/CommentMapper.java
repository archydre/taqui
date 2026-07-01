package com.taqui.backend.modules.comment.mapper;

import com.taqui.backend.modules.comment.dto.CommentResponseDTO;
import com.taqui.backend.modules.comment.entity.Comment;
import com.taqui.backend.modules.user.mapper.UserMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {

    CommentResponseDTO toResponseDTO(Comment comment);
}
