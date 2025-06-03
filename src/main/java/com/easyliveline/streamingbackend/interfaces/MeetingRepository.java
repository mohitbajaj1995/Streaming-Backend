package com.easyliveline.streamingbackend.interfaces;

import com.easyliveline.streamingbackend.models.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingRepository extends JpaRepository<Meeting, Long> {
//    @Query("SELECT new com.easyliveline.streamingbackend.DTO.UnassignedMeetingOptionsDTO(m.id, m.name, m.meetingNumber) FROM Meeting m WHERE m.owner.id = :ownerId AND m.slot IS NULL")
//    List<UnassignedMeetingOptionsDTO> getMeetingsWithoutSlotForOwner(@Param("ownerId") Long ownerId);
//
//    @Query("SELECT new com.easyliveline.streamingbackend.DTO.MeetingOwnerSlotForSecurityDTO(m.owner.id, s.id) FROM Meeting m LEFT JOIN m.slot s WHERE m.id = :meetingId")
//    MeetingOwnerSlotForSecurityDTO findOwnerIdAndSlotIdByMeetingId(@Param("meetingId") Long meetingId);

    @Modifying
    @Query("DELETE FROM Meeting m WHERE m.id = :meetingId")
    void deleteByIdCustom(Long meetingId);
}
