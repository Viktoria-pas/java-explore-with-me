package ru.practicum.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.EndPointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.service.StatsServiceImpl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StatsController.class)
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatsServiceImpl statsService;

    @Autowired
    private ObjectMapper objectMapper;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void saveHit_shouldReturnCreated_whenValidInput() throws Exception {

        EndPointHitDto hitDto = new EndPointHitDto("app", "/test", "127.0.0.1", LocalDateTime.now());
        String jsonContent = objectMapper.writeValueAsString(hitDto);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andExpect(status().isCreated());
    }

    @Test
    void saveHit_shouldReturnBadRequest_whenInvalidInput() throws Exception {

        EndPointHitDto invalidHitDto = new EndPointHitDto();

        String jsonContent = objectMapper.writeValueAsString(invalidHitDto);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andDo(print()) // для отладки
                .andExpect(status().isBadRequest());
    }

    @Test
    void getStats_shouldReturnStats_whenValidParameters() throws Exception {

        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now();
        List<ViewStatsDto> expectedStats = Arrays.asList(
                new ViewStatsDto("app", "/test", 5L)
        );

        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                isNull(), eq(false))).thenReturn(expectedStats);

        mockMvc.perform(get("/stats")
                        .param("start", start.format(formatter))
                        .param("end", end.format(formatter))
                        .param("unique", "false"))
                .andDo(print()) // для отладки
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].app").value("app"))
                .andExpect(jsonPath("$[0].uri").value("/test"))
                .andExpect(jsonPath("$[0].hits").value(5));
    }

    @Test
    void getStats_shouldReturnBadRequest_whenStartAfterEnd() throws Exception {

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusHours(1);

        when(statsService.getStats(any(LocalDateTime.class), any(LocalDateTime.class),
                isNull(), eq(false))).thenThrow(new IllegalArgumentException("Start date cannot be after end date"));

        mockMvc.perform(get("/stats")
                        .param("start", start.format(formatter))
                        .param("end", end.format(formatter))
                        .param("unique", "false")) // ИСПРАВЛЕНИЕ: добавляем параметр unique
                .andDo(print()) // для отладки
                .andExpect(status().isBadRequest());
    }
}
