package ru.practicum.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.EndPointHitDto;
import ru.practicum.dto.ViewStatsDto;
import ru.practicum.model.Hit;
import ru.practicum.repository.HitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StatsServiceImpl implements StatsService {

    private final HitRepository hitRepository;

    @Autowired
    public StatsServiceImpl(HitRepository hitRepository) {
        this.hitRepository = hitRepository;
    }

    @Override
    @Transactional
    public void saveHit(EndPointHitDto endPointHitDto) {
        Hit hit = new Hit(
                endPointHitDto.getApp(),
                endPointHitDto.getUri(),
                endPointHitDto.getIp(),
                endPointHitDto.getTimestamp()
        );
        hitRepository.save(hit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end,
                                       List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        if (unique == null) {
            unique = false;
        }

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return hitRepository.findUniqueStats(start, end);
            } else {
                return hitRepository.findStats(start, end);
            }
        } else {
            if (unique) {
                return hitRepository.findUniqueStatsByUris(start, end, uris);
            } else {
                return hitRepository.findStatsByUris(start, end, uris);
            }
        }
    }
}
