package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.mapper.RequestMapper;
import ru.practicum.model.Event;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.User;
import ru.practicum.model.enums.EventState;
import ru.practicum.model.enums.RequestStatus;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.ParticipationRequestRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RequestServiceTest {

    @Mock
    private ParticipationRequestRepository requestRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RequestMapper requestMapper;

    @InjectMocks
    private RequestService requestService;

    private User testUser;
    private User eventInitiator;
    private Event testEvent;
    private ParticipationRequest testRequest;
    private ParticipationRequestDto testRequestDto;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "John Doe", "john@example.com");
        eventInitiator = new User(2L, "Jane Doe", "jane@example.com");

        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setInitiator(eventInitiator);
        testEvent.setState(EventState.PUBLISHED);
        testEvent.setParticipantLimit(10);
        testEvent.setConfirmedRequests(5);
        testEvent.setRequestModeration(true);

        testRequest = new ParticipationRequest();
        testRequest.setId(1L);
        testRequest.setRequester(testUser);
        testRequest.setEvent(testEvent);
        testRequest.setStatus(RequestStatus.PENDING);
        testRequest.setCreated(LocalDateTime.now());

        testRequestDto = new ParticipationRequestDto();
        testRequestDto.setId(1L);
        testRequestDto.setRequester(1L);
        testRequestDto.setEvent(1L);
        testRequestDto.setStatus(RequestStatus.PENDING);
    }

    @Test
    void createRequest_WithValidData_ShouldReturnParticipationRequestDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));
        when(requestRepository.findByRequesterIdAndEventId(1L, 1L)).thenReturn(Optional.empty());
        when(requestRepository.save(any(ParticipationRequest.class))).thenReturn(testRequest);
        when(requestMapper.toParticipationRequestDto(testRequest)).thenReturn(testRequestDto);

        ParticipationRequestDto result = requestService.createRequest(1L, 1L);

        assertNotNull(result);
        assertEquals(testRequestDto, result);
        verify(requestRepository).save(any(ParticipationRequest.class));
    }

    @Test
    void createRequest_ForOwnEvent_ShouldThrowConflictException() {
        testEvent.setInitiator(testUser); // Same user as requester
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void createRequest_ForUnpublishedEvent_ShouldThrowConflictException() {
        testEvent.setState(EventState.PENDING);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void createRequest_WhenEventFull_ShouldThrowConflictException() {
        testEvent.setConfirmedRequests(10); // Reached limit
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(testEvent));

        assertThrows(ConflictException.class, () -> requestService.createRequest(1L, 1L));
        verify(requestRepository, never()).save(any());
    }

    @Test
    void cancelRequest_WithValidData_ShouldReturnCanceledRequest() {
        testRequest.setStatus(RequestStatus.CONFIRMED);
        testRequestDto.setStatus(RequestStatus.CANCELED);

        when(userRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest));
        when(requestRepository.save(testRequest)).thenReturn(testRequest);
        when(requestMapper.toParticipationRequestDto(testRequest)).thenReturn(testRequestDto);

        ParticipationRequestDto result = requestService.cancelRequest(1L, 1L);

        assertNotNull(result);
        assertEquals(RequestStatus.CANCELED, result.getStatus());
        verify(requestRepository).save(testRequest);
        verify(eventRepository).save(testEvent); // Should update confirmed requests count
    }
}
