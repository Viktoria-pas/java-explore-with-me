package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventUserRequest;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.dto.request.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.model.enums.RequestStatus;
import ru.practicum.service.EventService;
import ru.practicum.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    void createEvent_WithNullCategory_ShouldReturnBadRequest() throws Exception {
        newEventDto.setCategory(null);

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_WithShortDescription_ShouldReturnBadRequest() throws Exception {
        newEventDto.setDescription("Short"); // Less than 20 characters

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createEvent_WithInvalidCoordinates_ShouldReturnBadRequest() throws Exception {
        newEventDto.setLocation(new LocationDto(91.0f, 37.6173f)); // Invalid latitude

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserEvent_WithValidIds_ShouldReturnEvent() throws Exception {
        when(eventService.getUserEvent(1L, 1L)).thenReturn(eventFullDto);

        mockMvc.perform(get("/users/1/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Event"));

        verify(eventService).getUserEvent(1L, 1L);
    }

    @Test
    void updateEvent_WithValidData_ShouldReturnUpdatedEvent() throws Exception {
        UpdateEventUserRequest updateRequest = new UpdateEventUserRequest();
        updateRequest.setTitle("Updated Event Title");
        updateRequest.setAnnotation("Updated annotation with sufficient length for validation");

        when(eventService.updateUserEvent(eq(1L), eq(1L), any(UpdateEventUserRequest.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(patch("/users/1/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Event"));

        verify(eventService).updateUserEvent(eq(1L), eq(1L), any(UpdateEventUserRequest.class));
    }

    @Test
    void updateEvent_WithInvalidAnnotationLength_ShouldReturnBadRequest() throws Exception {
        UpdateEventUserRequest updateRequest = new UpdateEventUserRequest();
        updateRequest.setAnnotation("Short"); // Too short

        mockMvc.perform(patch("/users/1/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEvent_WithNegativeParticipantLimit_ShouldReturnBadRequest() throws Exception {
        UpdateEventUserRequest updateRequest = new UpdateEventUserRequest();
        updateRequest.setParticipantLimit(-1);

        mockMvc.perform(patch("/users/1/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
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
    void updateRequestStatus_WithEmptyRequestIds_ShouldReturnBadRequest() throws Exception {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of()); // Empty list
        updateRequest.setStatus(RequestStatus.CONFIRMED);

        mockMvc.perform(patch("/users/1/events/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRequestStatus_WithNullStatus_ShouldReturnBadRequest() throws Exception {
        EventRequestStatusUpdateRequest updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(List.of(1L));
        updateRequest.setStatus(null);

        mockMvc.perform(patch("/users/1/events/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());
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
    void createRequest_WithoutEventId_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/users/1/requests"))
                .andExpect(status().isBadRequest());
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

    @Test
    void getUserEvents_WithInvalidPaginationParams_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/users/1/events")
                        .param("from", "-1")
                        .param("size", "0"))
                .andExpect(status().isInternalServerError());
    }
}
