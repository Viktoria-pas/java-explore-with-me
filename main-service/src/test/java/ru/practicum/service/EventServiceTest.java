package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.client.StatsClient;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.location.LocationDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;
import ru.practicum.mapper.EventMapper;
import ru.practicum.model.Category;
import ru.practicum.model.Event;
import ru.practicum.model.Location;
import ru.practicum.model.User;
import ru.practicum.model.enums.EventState;
import ru.practicum.repository.CategoryRepository;
import ru.practicum.repository.EventRepository;
import ru.practicum.repository.LocationRepository;
import ru.practicum.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private StatsClient statsClient;

    @InjectMocks
    private EventService eventService;

    private User testUser;
    private Category testCategory;
    private Location testLocation;
    private Event testEvent;
    private NewEventDto newEventDto;
    private EventFullDto eventFullDto;
    private LocationDto locationDto;

    @BeforeEach
    void setUp() {
        testUser = new User(1L, "John Doe", "john@example.com");
        testCategory = new Category(1L, "Music");
        testLocation = new Location(1L, 55.7558f, 37.6173f);

        testEvent = new Event();
        testEvent.setId(1L);
        testEvent.setTitle("Test Event");
        testEvent.setAnnotation("Test annotation for the event");
        testEvent.setDescription("Test description for the event");
        testEvent.setCategory(testCategory);
        testEvent.setInitiator(testUser);
        testEvent.setLocation(testLocation);
        testEvent.setEventDate(LocalDateTime.now().plusDays(3));
        testEvent.setState(EventState.PENDING);
        testEvent.setPaid(false);
        testEvent.setParticipantLimit(0);
        testEvent.setRequestModeration(true);

        locationDto = new LocationDto(55.7558f, 37.6173f);
        newEventDto = new NewEventDto();
        newEventDto.setTitle("Test Event");
        newEventDto.setAnnotation("Test annotation for the event");
        newEventDto.setDescription("Test description for the event");
        newEventDto.setCategory(1L);
        newEventDto.setLocation(locationDto);
        newEventDto.setEventDate(LocalDateTime.now().plusDays(3));

        eventFullDto = new EventFullDto();
        eventFullDto.setId(1L);
        eventFullDto.setTitle("Test Event");
    }

    @Test
    void createUserEvent_WithValidData_ShouldReturnEventFullDto() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(locationRepository.save(any(Location.class))).thenReturn(testLocation);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        when(eventMapper.toEventFullDto(testEvent)).thenReturn(eventFullDto);

        EventFullDto result = eventService.createUserEvent(1L, newEventDto);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Event", result.getTitle());
        verify(userRepository).existsById(1L);
        verify(categoryRepository).findById(1L);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createUserEvent_WithNonExistentUser_ShouldThrowNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> eventService.createUserEvent(1L, newEventDto));
        verify(userRepository).existsById(1L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void createUserEvent_WithEventDateTooSoon_ShouldThrowValidationException() {
        newEventDto.setEventDate(LocalDateTime.now().plusHours(1));
        when(userRepository.existsById(1L)).thenReturn(true);

        assertThrows(ValidationException.class, () -> eventService.createUserEvent(1L, newEventDto));
    }

    @Test
    void getUserEvent_WithValidIds_ShouldReturnEventFullDto() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(eventRepository.findByIdAndInitiatorId(1L, 1L)).thenReturn(Optional.of(testEvent));
        when(eventMapper.toEventFullDto(testEvent)).thenReturn(eventFullDto);

        EventFullDto result = eventService.getUserEvent(1L, 1L);

        assertNotNull(result);
        assertEquals(eventFullDto, result);
        verify(eventRepository).findByIdAndInitiatorId(1L, 1L);
    }
}
