package ru.practicum.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.EndPointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.Hit;
import ru.practicum.repository.HitRepository;
import ru.practicum.service.StatsServiceImpl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private HitRepository hitRepository;

    @InjectMocks
    private StatsServiceImpl statsService;

    @Test
    void saveHit_shouldSaveHitToRepository() {

        EndPointHitDto hitDto = new EndPointHitDto("app", "/test", "127.0.0.1", LocalDateTime.now());

        statsService.saveHit(hitDto);

        verify(hitRepository, times(1)).save(any(Hit.class));
    }

    @Test
    void getStats_shouldThrowException_whenStartIsAfterEnd() {

        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.minusHours(1);

        assertThrows(IllegalArgumentException.class,
                () -> statsService.getStats(start, end, null, false));
    }

    @Test
    void getStats_shouldReturnStats_whenValidParameters() {

        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now();
        List<ViewStatsDto> expectedStats = Arrays.asList(
                new ViewStatsDto("app", "/test", 5L)
        );

        when(hitRepository.findStats(start, end)).thenReturn(expectedStats);

        List<ViewStatsDto> result = statsService.getStats(start, end, null, false);

        assertEquals(expectedStats, result);
        verify(hitRepository, times(1)).findStats(start, end);
    }

    @Test
    void getStats_shouldReturnUniqueStats_whenUniqueIsTrue() {

        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now();
        List<ViewStatsDto> expectedStats = Arrays.asList(
                new ViewStatsDto("app", "/test", 3L)
        );

        when(hitRepository.findUniqueStats(start, end)).thenReturn(expectedStats);

        List<ViewStatsDto> result = statsService.getStats(start, end, null, true);

        assertEquals(expectedStats, result);
        verify(hitRepository, times(1)).findUniqueStats(start, end);
    }

    @Test
    void getStats_shouldReturnStatsByUris_whenUrisProvided() {

        LocalDateTime start = LocalDateTime.now().minusHours(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = Arrays.asList("/test1", "/test2");
        List<ViewStatsDto> expectedStats = Arrays.asList(
                new ViewStatsDto("app", "/test1", 5L)
        );

        when(hitRepository.findStatsByUris(start, end, uris)).thenReturn(expectedStats);

        List<ViewStatsDto> result = statsService.getStats(start, end, uris, false);

        assertEquals(expectedStats, result);
        verify(hitRepository, times(1)).findStatsByUris(start, end, uris);
    }
}