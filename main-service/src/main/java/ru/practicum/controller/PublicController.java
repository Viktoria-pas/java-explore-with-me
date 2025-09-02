package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.comment.CommentDto;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.service.CategoryService;
import ru.practicum.service.CommentService;
import ru.practicum.service.CompilationService;
import ru.practicum.service.EventService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicController {

    private final EventService eventService;
    private final CategoryService categoryService;
    private final CompilationService compilationService;
    private final CommentService commentService;

    @GetMapping("/events")
    public List<EventShortDto> getEvents(
            @RequestParam(required = false) String text,
            @RequestParam(required = false) List<Long> categories,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @RequestParam(defaultValue = "false") Boolean onlyAvailable,
            @RequestParam(defaultValue = "EVENT_DATE") String sort,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size,
            HttpServletRequest request) {

        log.info("Getting events with filters: text={}, categories={}, paid={}, rangeStart={}, rangeEnd={}, onlyAvailable={}, sort={}, from={}, size={}",
                text, categories, paid, rangeStart, rangeEnd, onlyAvailable, sort, from, size);

        return eventService.getPublicEvents(text, categories, paid, rangeStart, rangeEnd,
                onlyAvailable, sort, from, size, request);
    }

    @GetMapping("/events/{id}")
    public EventFullDto getEvent(@PathVariable Long id, HttpServletRequest request) {
        log.info("Getting event with id: {}", id);
        return eventService.getPublicEvent(id, request);
    }

    @GetMapping("/categories")
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Getting categories with from={}, size={}", from, size);
        return categoryService.getCategories(from, size);
    }

    @GetMapping("/categories/{catId}")
    public CategoryDto getCategory(@PathVariable Long catId) {
        log.info("Getting category with id: {}", catId);
        return categoryService.getCategory(catId);
    }

    @GetMapping("/compilations")
    public List<CompilationDto> getCompilations(
            @RequestParam(required = false) Boolean pinned,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Getting compilations with pinned={}, from={}, size={}", pinned, from, size);
        return compilationService.getCompilations(pinned, from, size);
    }

    @GetMapping("/compilations/{compId}")
    public CompilationDto getCompilation(@PathVariable Long compId) {
        log.info("Getting compilation with id: {}", compId);
        return compilationService.getCompilation(compId);
    }

    @GetMapping("/events/{id}/comments")
    public List<CommentDto> getEventComments(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
            @RequestParam(defaultValue = "10") @Positive Integer size) {
        log.info("Getting public comments for event: {}", id);
        return commentService.getPublicComments(id, from, size);
    }
}
