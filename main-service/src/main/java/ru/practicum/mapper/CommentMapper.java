package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.model.Comment;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface CommentMapper {

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "author", source = "author")
    CommentDto toCommentDto(Comment comment);
}
