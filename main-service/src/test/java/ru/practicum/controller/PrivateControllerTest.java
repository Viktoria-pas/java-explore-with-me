package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.comment.NewCommentDto;
import ru.practicum.dto.comment.UpdateCommentDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.model.enums.CommentState;
import ru.practicum.model.enums.RequestStatus;
import ru.practicum.service.CommentService;
import ru.practicum.service.EventService;
import ru.practicum.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PrivateController.class)
class PrivateControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private RequestService requestService;

    @MockBean
    private CommentService commentService;

    private EventShortDto eventShortDto;
    private EventFullDto eventFullDto;
    private NewEventDto newEventDto;
    private LocationDto locationDto;

    @BeforeEach
    void setUp() {
        locationDto = new LocationDto(55.7558f, 37.6173f);

        eventShortDto = new EventShortDto();
        eventShortDto.setId(1L);
        eventShortDto.setTitle("Test Event");
        eventShortDto.setAnnotation("This is a test event annotation with sufficient length");

        eventFullDto = new EventFullDto();
        eventFullDto.setId(1L);
        eventFullDto.setTitle("Test Event");
        eventFullDto.setAnnotation("This is a test event annotation with sufficient length");
        eventFullDto.setDescription("This is a detailed description of the test event with sufficient length");

        newEventDto = new NewEventDto();
        newEventDto.setTitle("New Test Event");
        newEventDto.setAnnotation("This is a new test event annotation with sufficient length");
        newEventDto.setDescription("This is a detailed description of the new test event with sufficient length");
        newEventDto.setCategory(1L);
        newEventDto.setEventDate(LocalDateTime.now().plusDays(3));
        newEventDto.setLocation(locationDto);
    }

    @Test
    void getUserEvents_ShouldReturnUserEvents() throws Exception {
        when(eventService.getUserEvents(eq(1L), anyInt(), anyInt()))
                .thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/users/1/events")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Event"));

        verify(eventService).getUserEvents(1L, 0, 10);
    }

    @Test
    void createEvent_WithValidData_ShouldReturnCreatedEvent() throws Exception {
        when(eventService.createUserEvent(eq(1L), any(NewEventDto.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Event"));

        verify(eventService).createUserEvent(eq(1L), any(NewEventDto.class));
    }

    @Test
    void createEvent_WithInvalidAnnotation_ShouldReturnBadRequest() throws Exception {
        newEventDto.setAnnotation("Too short"); // Less than 20 characters

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserComments_ShouldReturnUserComments() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("User comment");
        commentDto.setState(CommentState.CONFIRMED);

        when(commentService.getUserComments(eq(1L), eq(0), eq(10)))
                .thenReturn(List.of(commentDto));

        mockMvc.perform(get("/users/1/comments")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].text").value("User comment"));

        verify(commentService).getUserComments(1L, 0, 10);
    }

    @Test
    void createComment_WithValidData_ShouldReturnCreatedComment() throws Exception {
        NewCommentDto newCommentDto = new NewCommentDto("Great event! Really enjoyed it.");
        CommentDto createdComment = new CommentDto();
        createdComment.setId(1L);
        createdComment.setText("Great event! Really enjoyed it.");
        createdComment.setState(CommentState.PENDING);

        when(commentService.createComment(eq(1L), eq(1L), any(NewCommentDto.class)))
                .thenReturn(createdComment);

        mockMvc.perform(post("/users/1/events/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCommentDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.state").value("PENDING"));

        verify(commentService).createComment(eq(1L), eq(1L), any(NewCommentDto.class));
    }

    @Test
    void createComment_WithShortText_ShouldReturnBadRequest() throws Exception {
        NewCommentDto shortComment = new NewCommentDto("Ok"); // Too short

        mockMvc.perform(post("/users/1/events/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortComment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createComment_WithBlankText_ShouldReturnBadRequest() throws Exception {
        NewCommentDto blankComment = new NewCommentDto(""); // Blank

        mockMvc.perform(post("/users/1/events/1/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(blankComment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEventComments_ShouldReturnEventComments() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Comment on my event");
        commentDto.setState(CommentState.PENDING);

        when(commentService.getEventComments(eq(1L), eq(1L), eq(0), eq(10)))
                .thenReturn(List.of(commentDto));

        mockMvc.perform(get("/users/1/events/1/comments")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(commentService).getEventComments(1L, 1L, 0, 10);
    }

    @Test
    void updateComment_WithValidData_ShouldReturnUpdatedComment() throws Exception {
        UpdateCommentDto updateDto = new UpdateCommentDto("Updated comment text with sufficient length");
        CommentDto updatedComment = new CommentDto();
        updatedComment.setId(1L);
        updatedComment.setText("Updated comment text with sufficient length");
        updatedComment.setState(CommentState.PENDING);

        when(commentService.updateComment(eq(1L), eq(1L), any(UpdateCommentDto.class)))
                .thenReturn(updatedComment);

        mockMvc.perform(patch("/users/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.state").value("PENDING"));

        verify(commentService).updateComment(eq(1L), eq(1L), any(UpdateCommentDto.class));
    }

    @Test
    void updateComment_WithShortText_ShouldReturnBadRequest() throws Exception {
        UpdateCommentDto shortUpdate = new UpdateCommentDto("Hi"); // Too short

        mockMvc.perform(patch("/users/1/comments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shortUpdate)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteComment_ShouldReturnNoContent() throws Exception {
        doNothing().when(commentService).deleteComment(1L, 1L);

        mockMvc.perform(delete("/users/1/comments/1"))
                .andExpect(status().isNoContent());

        verify(commentService).deleteComment(1L, 1L);
    }

    @Test
    void getUserComments_WithInvalidPaginationParams_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/users/1/comments")
                        .param("from", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEventRequests_ShouldReturnRequestList() throws Exception {
        ParticipationRequestDto requestDto = new ParticipationRequestDto();
        requestDto.setId(1L);
        requestDto.setRequester(2L);
        requestDto.setEvent(1L);
        requestDto.setStatus(RequestStatus.PENDING);

        when(requestService.getEventRequests(1L, 1L)).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/users/1/events/1/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].requester").value(2))
                .andExpect(jsonPath("$[0].event").value(1));

        verify(requestService).getEventRequests(1L, 1L);
    }

    @Test
    void updateRequestStatus_WithValidData_ShouldReturnUpdateResult() throws Exception {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L, 2L));
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        ParticipationRequestDto confirmedRequest = new ParticipationRequestDto();
        confirmedRequest.setId(1L);
        confirmedRequest.setStatus(RequestStatus.CONFIRMED);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(List.of(confirmedRequest));
        result.setRejectedRequests(List.of());

        when(requestService.updateRequestStatus(eq(1L), eq(1L), any(EventRequestStatusUpdateRequest.class)))
                .thenReturn(result);

        mockMvc.perform(patch("/users/1/events/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests").isArray())
                .andExpect(jsonPath("$.rejectedRequests").isArray())
                .andExpect(jsonPath("$.confirmedRequests[0].id").value(1));

        verify(requestService).updateRequestStatus(eq(1L), eq(1L), any(EventRequestStatusUpdateRequest.class));
    }

    @Test
    void getUserRequests_ShouldReturnUserRequests() throws Exception {
        ParticipationRequestDto requestDto = new ParticipationRequestDto();
        requestDto.setId(1L);
        requestDto.setRequester(1L);
        requestDto.setEvent(2L);
        requestDto.setStatus(RequestStatus.PENDING);

        when(requestService.getUserRequests(1L)).thenReturn(List.of(requestDto));

        mockMvc.perform(get("/users/1/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].requester").value(1))
                .andExpect(jsonPath("$[0].event").value(2));

        verify(requestService).getUserRequests(1L);
    }

    @Test
    void createRequest_ShouldReturnCreatedRequest() throws Exception {
        ParticipationRequestDto requestDto = new ParticipationRequestDto();
        requestDto.setId(1L);
        requestDto.setRequester(1L);
        requestDto.setEvent(2L);
        requestDto.setStatus(RequestStatus.PENDING);

        when(requestService.createRequest(1L, 2L)).thenReturn(requestDto);

        mockMvc.perform(post("/users/1/requests")
                        .param("eventId", "2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.requester").value(1))
                .andExpect(jsonPath("$.event").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"));

        verify(requestService).createRequest(1L, 2L);
    }

    @Test
    void cancelRequest_ShouldReturnCanceledRequest() throws Exception {
        ParticipationRequestDto requestDto = new ParticipationRequestDto();
        requestDto.setId(1L);
        requestDto.setRequester(1L);
        requestDto.setEvent(2L);
        requestDto.setStatus(RequestStatus.CANCELED);

        when(requestService.cancelRequest(1L, 1L)).thenReturn(requestDto);

        mockMvc.perform(patch("/users/1/requests/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CANCELED"));

        verify(requestService).cancelRequest(1L, 1L);
    }
}
