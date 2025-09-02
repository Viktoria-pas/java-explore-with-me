package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.CommentModerationDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommentMapper commentMapper;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Event testEvent;
    private Comment testComment;
    private CommentDto testCommentDto;
    private NewCommentDto newCommentDto;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "John Doe", "john@example.com");

        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setState(EventState.PUBLISHED);
        testEvent.setInitiator(testUser);

        testComment = new Comment();
        testComment.setId(1L);
        testComment.setText("Great event!");
        testComment.setAuthor(testUser);
        testComment.setEvent(testEvent);
        testComment.setState(CommentState.CONFIRMED);
        testComment.setCreatedOn(LocalDateTime.now());

        testCommentDto = new CommentDto();
        testCommentDto.setId(1L);
        testCommentDto.setText("Great event!");
        testCommentDto.setState(CommentState.CONFIRMED);

        newCommentDto = new NewCommentDto("Great event!");
    }

    @Test
    void getPublicComments_ShouldReturnConfirmedComments() {
        Page<Comment> commentPage = new PageImpl<>(List.of(testComment));

        when(eventRepository.findByIdAndState(1L, EventState.PUBLISHED))
                .thenReturn(Optional.of(testEvent));
        when(commentRepository.findByEventIdAndState(eq(1L), eq(CommentState.CONFIRMED), any(Pageable.class)))
                .thenReturn(commentPage);
        when(commentMapper.toCommentDto(testComment)).thenReturn(testCommentDto);

        List<CommentDto> result = commentService.getPublicComments(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCommentDto, result.get(0));

        verify(eventRepository).findByIdAndState(1L, EventState.PUBLISHED);
        verify(commentRepository).findByEventIdAndState(eq(1L), eq(CommentState.CONFIRMED), any(Pageable.class));
    }

    @Test
    void getPublicComments_WithNonExistentEvent_ShouldThrowNotFoundException() {
        when(eventRepository.findByIdAndState(1L, EventState.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> commentService.getPublicComments(1L, 0, 10));
    }

    @Test
    void createComment_WithValidData_ShouldReturnCommentDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findByIdAndState(1L, EventState.PUBLISHED))
                .thenReturn(Optional.of(testEvent));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(commentMapper.toCommentDto(testComment)).thenReturn(testCommentDto);

        CommentDto result = commentService.createComment(1L, 1L, newCommentDto);

        assertNotNull(result);
        assertEquals(testCommentDto, result);
        verify(commentRepository).save(any(Comment.class));
        verify(userRepository).findById(1L);
        verify(eventRepository).findByIdAndState(1L, EventState.PUBLISHED);
    }

    @Test
    void createComment_WithNonExistentUser_ShouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> commentService.createComment(1L, 1L, newCommentDto));

        verify(userRepository).findById(1L);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createComment_WithNonExistentEvent_ShouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findByIdAndState(1L, EventState.PUBLISHED))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> commentService.createComment(1L, 1L, newCommentDto));

        verify(userRepository).findById(1L);
        verify(eventRepository).findByIdAndState(1L, EventState.PUBLISHED);
        verify(commentRepository, never()).save(any());
    }

    @Test
    void getUserComments_ShouldReturnUserComments() {
        Page<Comment> commentPage = new PageImpl<>(List.of(testComment));

        when(userRepository.existsById(1L)).thenReturn(true);
        when(commentRepository.findByAuthorId(eq(1L), any(Pageable.class)))
                .thenReturn(commentPage);
        when(commentMapper.toCommentDto(testComment)).thenReturn(testCommentDto);

        List<CommentDto> result = commentService.getUserComments(1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCommentDto, result.get(0));

        verify(userRepository).existsById(1L);
        verify(commentRepository).findByAuthorId(eq(1L), any(Pageable.class));
    }

    @Test
    void getUserComments_WithNonExistentUser_ShouldThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> commentService.getUserComments(1L, 0, 10));

        verify(userRepository).existsById(1L);
        verify(commentRepository, never()).findByAuthorId(any(), any());
    }

    @Test
    void updateComment_WithValidData_ShouldReturnUpdatedComment() {
        testComment.setState(CommentState.PENDING);
        UpdateCommentDto updateDto = new UpdateCommentDto("Updated comment text");

        when(commentRepository.findByIdAndAuthorId(1L, 1L))
                .thenReturn(Optional.of(testComment));
        when(commentRepository.save(testComment)).thenReturn(testComment);
        when(commentMapper.toCommentDto(testComment)).thenReturn(testCommentDto);

        CommentDto result = commentService.updateComment(1L, 1L, updateDto);

        assertNotNull(result);
        assertEquals("Updated comment text", testComment.getText());
        assertEquals(CommentState.PENDING, testComment.getState());
        verify(commentRepository).save(testComment);
    }

    @Test
    void updateComment_WithConfirmedComment_ShouldThrowConflictException() {
        testComment.setState(CommentState.CONFIRMED);
        UpdateCommentDto updateDto = new UpdateCommentDto("Updated text");

        when(commentRepository.findByIdAndAuthorId(1L, 1L))
                .thenReturn(Optional.of(testComment));

        assertThrows(ConflictException.class,
                () -> commentService.updateComment(1L, 1L, updateDto));
    }

    @Test
    void deleteComment_WithValidData_ShouldDeleteComment() {
        testComment.setState(CommentState.PENDING);

        when(commentRepository.findByIdAndAuthorId(1L, 1L))
                .thenReturn(Optional.of(testComment));

        assertDoesNotThrow(() -> commentService.deleteComment(1L, 1L));

        verify(commentRepository).deleteById(1L);
    }

    @Test
    void deleteComment_WithConfirmedComment_ShouldThrowConflictException() {
        testComment.setState(CommentState.CONFIRMED);

        when(commentRepository.findByIdAndAuthorId(1L, 1L))
                .thenReturn(Optional.of(testComment));

        assertThrows(ConflictException.class,
                () -> commentService.deleteComment(1L, 1L));
    }

    @Test
    void getCommentsForModeration_ShouldReturnCommentsWithSpecifiedState() {
        Page<Comment> commentPage = new PageImpl<>(List.of(testComment));

        when(commentRepository.findCommentsForModeration(eq(CommentState.PENDING), any(Pageable.class)))
                .thenReturn(commentPage);
        when(commentMapper.toCommentDto(testComment)).thenReturn(testCommentDto);

        List<CommentDto> result = commentService.getCommentsForModeration(CommentState.PENDING, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCommentDto, result.get(0));

        verify(commentRepository).findCommentsForModeration(eq(CommentState.PENDING), any(Pageable.class));
    }

    @Test
    void moderateComment_WithValidData_ShouldReturnModeratedComment() {
        testComment.setState(CommentState.PENDING);
        CommentModerationDto moderationDto = new CommentModerationDto(CommentState.CONFIRMED, "Approved");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));
        when(commentRepository.save(testComment)).thenReturn(testComment);
        when(commentMapper.toCommentDto(testComment)).thenReturn(testCommentDto);

        CommentDto result = commentService.moderateComment(1L, moderationDto);

        assertNotNull(result);
        assertEquals(CommentState.CONFIRMED, testComment.getState());
        assertEquals("Approved", testComment.getModeratorComment());
        assertNotNull(testComment.getUpdatedOn());
        verify(commentRepository).save(testComment);
    }

    @Test
    void moderateComment_WithNonPendingComment_ShouldThrowConflictException() {
        testComment.setState(CommentState.CONFIRMED);
        CommentModerationDto moderationDto = new CommentModerationDto(CommentState.REJECTED, "Rejected");

        when(commentRepository.findById(1L)).thenReturn(Optional.of(testComment));

        assertThrows(ConflictException.class,
                () -> commentService.moderateComment(1L, moderationDto));
    }

    @Test
    void getEventComments_WithValidEventInitiator_ShouldReturnComments() {
        Page<Comment> commentPage = new PageImpl<>(List.of(testComment));

        when(eventRepository.findByIdAndInitiatorId(1L, 1L))
                .thenReturn(Optional.of(testEvent));
        when(commentRepository.findByEventId(eq(1L), any(Pageable.class)))
                .thenReturn(commentPage);
        when(commentMapper.toCommentDto(testComment)).thenReturn(testCommentDto);

        List<CommentDto> result = commentService.getEventComments(1L, 1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(eventRepository).findByIdAndInitiatorId(1L, 1L);
    }

    @Test
    void getEventComments_WithNonInitiatorUser_ShouldThrowNotFoundException() {
        when(eventRepository.findByIdAndInitiatorId(1L, 2L))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> commentService.getEventComments(2L, 1L, 0, 10));

        verify(eventRepository).findByIdAndInitiatorId(1L, 2L);
        verify(commentRepository, never()).findByEventId(any(), any());
    }

    @Test
    void deleteCommentByAdmin_WithExistingComment_ShouldDeleteComment() {
        when(commentRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> commentService.deleteCommentByAdmin(1L));

        verify(commentRepository).existsById(1L);
        verify(commentRepository).deleteById(1L);
    }

    @Test
    void deleteCommentByAdmin_WithNonExistentComment_ShouldThrowNotFoundException() {
        when(commentRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class,
                () -> commentService.deleteCommentByAdmin(1L));

        verify(commentRepository).existsById(1L);
        verify(commentRepository, never()).deleteById(any());
    }
}
