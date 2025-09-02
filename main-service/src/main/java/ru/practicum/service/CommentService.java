package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.comment.*;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.CommentMapper;
import ru.practicum.model.Comment;
import ru.practicum.model.Event;
import ru.practicum.model.User;
import ru.practicum.model.enums.CommentState;
import ru.practicum.model.enums.EventState;
import ru.practicum.repository.CommentRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CommentMapper commentMapper;

    public List<CommentDto> getPublicComments(Long eventId, int from, int size) {
        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found or not published"));

        Pageable pageable = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.ASC, "createdOn"));

        return commentRepository.findByEventIdAndState(eventId, CommentState.CONFIRMED, pageable)
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        Event event = eventRepository.findByIdAndState(eventId, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event not found or not published"));

        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        comment.setEvent(event);
        comment.setAuthor(user);
        comment.setCreatedOn(LocalDateTime.now());
        comment.setState(CommentState.PENDING);

        comment = commentRepository.save(comment);

        log.info("Comment created: {} by user: {}", comment.getId(), userId);
        return commentMapper.toCommentDto(comment);
    }

    public List<CommentDto> getUserComments(Long userId, int from, int size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User not found");
        }

        Pageable pageable = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.DESC, "createdOn"));

        return commentRepository.findByAuthorId(userId, pageable)
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (comment.getState() == CommentState.CONFIRMED) {
            throw new ConflictException("Cannot update confirmed comment");
        }

        if (updateCommentDto.getText() != null) {
            comment.setText(updateCommentDto.getText());
            comment.setUpdatedOn(LocalDateTime.now());
            comment.setState(CommentState.PENDING); // Возвращаем на модерацию
        }

        comment = commentRepository.save(comment);
        return commentMapper.toCommentDto(comment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (comment.getState() == CommentState.CONFIRMED) {
            throw new ConflictException("Cannot delete confirmed comment");
        }

        commentRepository.deleteById(commentId);
        log.info("Comment deleted: {} by user: {}", commentId, userId);
    }

    public List<CommentDto> getEventComments(Long userId, Long eventId, int from, int size) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event not found or user is not initiator"));

        Pageable pageable = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.ASC, "createdOn"));

        return commentRepository.findByEventId(eventId, pageable)
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    public List<CommentDto> getCommentsForModeration(CommentState state, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size,
                Sort.by(Sort.Direction.ASC, "createdOn"));

        return commentRepository.findCommentsForModeration(state, pageable)
                .stream()
                .map(commentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CommentDto moderateComment(Long commentId, CommentModerationDto moderationDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment not found"));

        if (comment.getState() != CommentState.PENDING) {
            throw new ConflictException("Only pending comments can be moderated");
        }

        comment.setState(moderationDto.getState());
        comment.setModeratorComment(moderationDto.getModeratorComment());
        comment.setUpdatedOn(LocalDateTime.now());

        comment = commentRepository.save(comment);

        log.info("Comment {} moderated with state: {}", commentId, moderationDto.getState());
        return commentMapper.toCommentDto(comment);
    }

    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment not found");
        }

        commentRepository.deleteById(commentId);
        log.info("Comment {} deleted by admin", commentId);
    }
}
