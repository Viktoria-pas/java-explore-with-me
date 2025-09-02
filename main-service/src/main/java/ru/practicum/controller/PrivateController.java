package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.service.CommentService;
import ru.practicum.service.EventService;
import ru.practicum.service.RequestService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping("/users/{userId}")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateController {

    private final EventService eventService;
    private final RequestService requestService;
    private final CommentService commentService;


    @GetMapping("/events")
    public List<EventShortDto> getUserEvents(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Getting events for user: {}, from={}, size={}", userId, from, size);
        return eventService.getUserEvents(userId, from, size);
    }

    @PostMapping("/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto createEvent(@PathVariable Long userId,
                                    @Valid @RequestBody NewEventDto newEventDto) {
        log.info("Creating event for user: {}", userId);
        return eventService.createUserEvent(userId, newEventDto);
    }

    @GetMapping("/events/{eventId}")
    public EventFullDto getUserEvent(@PathVariable Long userId,
                                     @PathVariable Long eventId) {
        log.info("Getting event {} for user: {}", eventId, userId);
        return eventService.getUserEvent(userId, eventId);
    }

    @PatchMapping("/events/{eventId}")
    public EventFullDto updateEvent(@PathVariable Long userId,
                                    @PathVariable Long eventId,
                                    @Valid @RequestBody UpdateEventUserRequest updateRequest) {
        log.info("Updating event {} for user: {}", eventId, userId);
        return eventService.updateUserEvent(userId, eventId, updateRequest);
    }

    @GetMapping("/events/{eventId}/requests")
    public List<ParticipationRequestDto> getEventRequests(@PathVariable Long userId,
                                                          @PathVariable Long eventId) {
        log.info("Getting requests for event {} of user: {}", eventId, userId);
        return requestService.getEventRequests(userId, eventId);
    }

    @PatchMapping("/events/{eventId}/requests")
    public EventRequestStatusUpdateResult updateRequestStatus(@PathVariable Long userId,
                                                              @PathVariable Long eventId,
                                                              @Valid @RequestBody EventRequestStatusUpdateRequest updateRequest) {
        log.info("Updating request status for event {} of user: {}", eventId, userId);
        return requestService.updateRequestStatus(userId, eventId, updateRequest);
    }

    @GetMapping("/requests")
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        log.info("Getting requests for user: {}", userId);
        return requestService.getUserRequests(userId);
    }

    @PostMapping("/requests")
    @ResponseStatus(HttpStatus.CREATED)
    public ParticipationRequestDto createRequest(@PathVariable Long userId,
                                                 @RequestParam Long eventId) {
        log.info("Creating request for user: {} to event: {}", userId, eventId);
        return requestService.createRequest(userId, eventId);
    }

    @PatchMapping("/requests/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        log.info("Cancelling request {} for user: {}", requestId, userId);
        return requestService.cancelRequest(userId, requestId);
    }

    @GetMapping("/comments")
    public List<CommentDto> getUserComments(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Getting comments for user: {}", userId);
        return commentService.getUserComments(userId, from, size);
    }

    @PostMapping("/events/{eventId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createComment(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @Valid @RequestBody NewCommentDto newCommentDto) {
        log.info("Creating comment for event {} by user {}", eventId, userId);
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    @GetMapping("/events/{eventId}/comments")
    public List<CommentDto> getEventComments(
            @PathVariable Long userId,
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Getting comments for event {} by owner {}", eventId, userId);
        return commentService.getEventComments(userId, eventId, from, size);
    }

    @PatchMapping("/comments/{commentId}")
    public CommentDto updateComment(
            @PathVariable Long userId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        log.info("Updating comment {} by user {}", commentId, userId);
        return commentService.updateComment(userId, commentId, updateCommentDto);
    }

    @DeleteMapping("/comments/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @PathVariable Long userId,
            @PathVariable Long commentId) {
        log.info("Deleting comment {} by user {}", commentId, userId);
        commentService.deleteComment(userId, commentId);
    }
}
