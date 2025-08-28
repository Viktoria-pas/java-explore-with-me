package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.ParticipationRequest;
import ru.practicum.model.enums.RequestStatus;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    List<ParticipationRequest> findByEventId(Long eventId);

    List<ParticipationRequest> findByEventInitiatorId(Long initiatorId);

    Optional<ParticipationRequest> findByRequesterIdAndEventId(Long requesterId, Long eventId);

    @Query("SELECT pr FROM ParticipationRequest pr " +
            "WHERE pr.id = :requestId AND pr.event.initiator.id = :initiatorId")
    Optional<ParticipationRequest> findByIdAndEventInitiatorId(@Param("requestId") Long requestId,
                                                               @Param("initiatorId") Long initiatorId);

    int countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<ParticipationRequest> findByIdIn(List<Long> ids);

    List<ParticipationRequest> findByEventIdAndStatus(Long eventId, RequestStatus status);
}
