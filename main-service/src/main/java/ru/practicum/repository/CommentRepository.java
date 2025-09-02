package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Comment;
import ru.practicum.model.enums.CommentState;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByEventIdAndState(Long eventId, CommentState state, Pageable pageable);

    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);

    Page<Comment> findByEventId(Long eventId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE (:state IS NULL OR c.state = :state)")
    Page<Comment> findCommentsForModeration(@Param("state") CommentState state, Pageable pageable);

    Optional<Comment> findByIdAndAuthorId(Long commentId, Long authorId);

    long countByEventIdAndState(Long eventId, CommentState state);
}
