package ru.practicum.dto.comment;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import ru.practicum.model.enums.CommentState;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentModerationDto {
    @NotNull(message = "State cannot be null")
    private CommentState state;

    @Size(max = 500, message = "Moderator comment cannot exceed 500 characters")
    private String moderatorComment;
}
