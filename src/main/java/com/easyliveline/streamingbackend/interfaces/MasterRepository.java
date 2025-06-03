package com.easyliveline.streamingbackend.interfaces;

import com.easyliveline.streamingbackend.dto.MasterDeleteSecurityCheck;
import com.easyliveline.streamingbackend.dto.OwnerFilterSuperMasterAndMasterMeta;
import com.easyliveline.streamingbackend.dto.ParentInfo;
import com.easyliveline.streamingbackend.models.Master;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MasterRepository extends JpaRepository<Master, Long> {
    @Query("SELECT s.points FROM Master s WHERE s.id = :id")
    long findPointsById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Master s SET s.points = :points WHERE s.id = :id")
    void updateMasterPoints(@Param("id") Long id, @Param("points") long points);

    @Query("SELECT new com.easyliveline.streamingbackend.dto.OwnerFilterSuperMasterAndMasterMeta(m.id, m.username, m.role) " +
            "FROM Master m WHERE m.parent.id = :parentId")
    List<OwnerFilterSuperMasterAndMasterMeta> findMastersByOwnerId(@Param("parentId") Long parentId);

    @Query("SELECT new com.easyliveline.streamingbackend.dto.ParentInfo(m.parent.id, m.parentType) " +
            "FROM Master m WHERE m.id = :masterId")
    ParentInfo findParentInfoByMasterId(@Param("masterId") Long masterId);

    @Query("SELECT new com.easyliveline.streamingbackend.dto.MasterDeleteSecurityCheck(m.parent.id, SIZE(m.subscribers)) FROM Master m WHERE m.id = :masterId")
    MasterDeleteSecurityCheck findMasterSummaryById(@Param("masterId") Long masterId);
}
