package com.easyliveline.streamingbackend.interfaces;

import com.easyliveline.streamingbackend.dto.OwnerFilterSuperMasterAndMasterMeta;
import com.easyliveline.streamingbackend.dto.ParentInfo;
import com.easyliveline.streamingbackend.models.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
//    @Query("SELECT new com.easyliveline.assistant.DTO.HostSummaryDTO(h.id, h.username) FROM Host h WHERE h.parent.id = :parentId")
//    List<HostSummaryDTO> findHostSummaryByParentId(@Param("parentId") Long parentId);

    @Query("SELECT new com.easyliveline.streamingbackend.dto.OwnerFilterSuperMasterAndMasterMeta(h.id, h.username, h.role) " +
            "FROM Subscriber h WHERE h.parent.id = :parentId")
    List<OwnerFilterSuperMasterAndMasterMeta> findHostsByParentId(@Param("parentId") Long parentId);

    @Query("SELECT new com.easyliveline.streamingbackend.dto.ParentInfo(h.parent.id, h.parentType) FROM Subscriber h WHERE h.id = :hostId")
    ParentInfo findParentInfoByHostId(@Param("hostId") Long hostId);
}
