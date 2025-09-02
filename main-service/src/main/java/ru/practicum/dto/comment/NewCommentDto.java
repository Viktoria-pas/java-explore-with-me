package ru.practicum.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentDto {
    @NotBlank(message = "Comment text cannot be blank")
    @Size(min = 5, max = 1000, message = "Comment text must be between 5 and 1000 characters")
    private String text;
}
