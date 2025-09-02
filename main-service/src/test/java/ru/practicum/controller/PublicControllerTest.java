package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.model.enums.CommentState;
import ru.practicum.model.enums.EventState;
import ru.practicum.service.CategoryService;
import ru.practicum.service.CommentService;
import ru.practicum.service.CompilationService;
import ru.practicum.service.EventService;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PublicController.class)
class PublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private CompilationService compilationService;

    @MockBean
    private CommentService commentService;

    private EventShortDto eventShortDto;
    private EventFullDto eventFullDto;
    private CategoryDto categoryDto;
    private CompilationDto compilationDto;

    @BeforeEach
    void setUp() {
        eventShortDto = new EventShortDto();
        eventShortDto.setId(1L);
        eventShortDto.setTitle("Public Test Event");
        eventShortDto.setAnnotation("This is a public test event annotation with sufficient length");
        eventShortDto.setPaid(false);
        eventShortDto.setViews(100L);

        eventFullDto = new EventFullDto();
        eventFullDto.setId(1L);
        eventFullDto.setTitle("Public Test Event");
        eventFullDto.setAnnotation("This is a public test event annotation with sufficient length");
        eventFullDto.setDescription("This is a detailed description of the public test event");
        eventFullDto.setViews(150L);
        eventFullDto.setState(EventState.PUBLISHED);

        categoryDto = new CategoryDto(1L, "Music");

        compilationDto = new CompilationDto();
        compilationDto.setId(1L);
        compilationDto.setTitle("Best Events");
        compilationDto.setPinned(true);
        compilationDto.setEvents(List.of(eventShortDto));
    }

    @Test
    void getEvents_WithoutFilters_ShouldReturnAllEvents() throws Exception {
        when(eventService.getPublicEvents(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(false), eq("EVENT_DATE"), eq(0), eq(10), any(HttpServletRequest.class)))
                .thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Public Test Event"))
                .andExpect(jsonPath("$[0].views").value(100));

        verify(eventService).getPublicEvents(
                isNull(), isNull(), isNull(), isNull(), isNull(),
                eq(false), eq("EVENT_DATE"), eq(0), eq(10), any(HttpServletRequest.class));
    }

    @Test
    void getEvent_WithValidId_ShouldReturnEvent() throws Exception {
        when(eventService.getPublicEvent(eq(1L), any(HttpServletRequest.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(get("/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Public Test Event"))
                .andExpect(jsonPath("$.views").value(150))
                .andExpect(jsonPath("$.state").value("PUBLISHED"));

        verify(eventService).getPublicEvent(eq(1L), any(HttpServletRequest.class));
    }

    @Test
    void getEvent_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        when(eventService.getPublicEvent(eq(999L), any(HttpServletRequest.class)))
                .thenThrow(new NotFoundException("Event not found"));

        mockMvc.perform(get("/events/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEventComments_ShouldReturnConfirmedComments() throws Exception {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(1L);
        commentDto.setText("Great public event!");
        commentDto.setState(CommentState.CONFIRMED);

        when(commentService.getPublicComments(eq(1L), eq(0), eq(10)))
                .thenReturn(List.of(commentDto));

        mockMvc.perform(get("/events/1/comments")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].state").value("CONFIRMED"));

        verify(commentService).getPublicComments(1L, 0, 10);
    }

    @Test
    void getEventComments_WithNonExistentEvent_ShouldReturnNotFound() throws Exception {
        when(commentService.getPublicComments(eq(999L), eq(0), eq(10)))
                .thenThrow(new NotFoundException("Event not found"));

        mockMvc.perform(get("/events/999/comments")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getEventComments_WithInvalidPaginationParams_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/events/1/comments")
                        .param("from", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    // === ОСТАЛЬНЫЕ СУЩЕСТВУЮЩИЕ ТЕСТЫ (ИСПРАВЛЕННЫЕ) ===

    @Test
    void getEvents_WithTextFilter_ShouldReturnFilteredEvents() throws Exception {
        when(eventService.getPublicEvents(
                eq("music"), isNull(), isNull(), isNull(), isNull(),
                eq(false), eq("EVENT_DATE"), eq(0), eq(10), any(HttpServletRequest.class)))
                .thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/events")
                        .param("text", "music")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(eventService).getPublicEvents(
                eq("music"), isNull(), isNull(), isNull(), isNull(),
                eq(false), eq("EVENT_DATE"), eq(0), eq(10), any(HttpServletRequest.class));
    }

    @Test
    void getCategories_ShouldReturnCategoryList() throws Exception {
        when(categoryService.getCategories(0, 10)).thenReturn(List.of(categoryDto));

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Music"));

        verify(categoryService).getCategories(0, 10);
    }

    @Test
    void getCategory_WithValidId_ShouldReturnCategory() throws Exception {
        when(categoryService.getCategory(1L)).thenReturn(categoryDto);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Music"));

        verify(categoryService).getCategory(1L);
    }

    @Test
    void getCategory_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        when(categoryService.getCategory(999L))
                .thenThrow(new NotFoundException("Category not found"));

        mockMvc.perform(get("/categories/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCompilations_ShouldReturnCompilationList() throws Exception {
        when(compilationService.getCompilations(null, 0, 10)).thenReturn(List.of(compilationDto));

        mockMvc.perform(get("/compilations")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Best Events"))
                .andExpect(jsonPath("$[0].pinned").value(true));

        verify(compilationService).getCompilations(null, 0, 10);
    }

    @Test
    void getEvents_WithInvalidPaginationParams_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/events")
                        .param("from", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEvents_WithEmptyResponse_ShouldReturnEmptyArray() throws Exception {
        when(eventService.getPublicEvents(
                any(), any(), any(), any(), any(),
                any(), any(), anyInt(), anyInt(), any(HttpServletRequest.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}