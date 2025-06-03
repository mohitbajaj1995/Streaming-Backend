package com.easyliveline.streamingbackend.interfaces;

import com.easyliveline.streamingbackend.dto.ZoomWithMeetingSize;
import com.easyliveline.streamingbackend.models.Zoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZoomRepository extends JpaRepository<Zoom, Long> {

//    @Query("SELECT new com.easyliveline.streamingbackend.DTO.ZoomStatsDTO(z.parent.id, SIZE(z.meetings)) " +
//            "FROM Zoom z WHERE z.id = :zoomId")
//    ZoomStatsDTO fetchZoomStats(@Param("zoomId") Long zoomId);

    @Modifying
    @Query("DELETE FROM Zoom z WHERE z.id = :zoomId")
    void deleteByZoomId(@Param("zoomId") Long zoomId);

    @Query("SELECT new com.easyliveline.streamingbackend.dto.ZoomWithMeetingSize(z.id, z.createdAt, z.updatedAt, z.email, z.password, z.sdkKey, z.sdkSecret, z.apiKey, z.apiSecret, z.accountId, SIZE(z.meetings)) FROM Zoom z")
    List<ZoomWithMeetingSize> findZoomsWithMeetingSize();


//    @Query("SELECT new com.easyliveline.assistant.DTO.ZoomWithMeetingSize(z, SIZE(z.meetings)) FROM Zoom z WHERE z.parent.id = :parentId")
//    List<ZoomWithMeetingSize> findZoomsWithMeetingSizeByParentId(@Param("parentId") Long parentId);
}
