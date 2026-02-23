package com.salesinsight.meeting.repository;

import com.salesinsight.meeting.domain.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MeetingRepository extends JpaRepository<Meeting, UUID> {

    List<Meeting> findByOwnerId(UUID ownerId);

}
