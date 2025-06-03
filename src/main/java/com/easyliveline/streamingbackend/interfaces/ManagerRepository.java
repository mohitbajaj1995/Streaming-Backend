package com.easyliveline.streamingbackend.interfaces;

import com.easyliveline.streamingbackend.dto.ManagerDTO;
import com.easyliveline.streamingbackend.dto.ParentInfo;
import com.easyliveline.streamingbackend.models.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {
    @Query("SELECT m.parent.id FROM Manager m WHERE m.id = :managerId")
    Long findParentIdByManagerId(@Param("managerId") Long managerId);

    @Query("SELECT new com.easyliveline.streamingbackend.dto.ManagerDTO(m.id, m.name, m.username, m.enabled, m.lastSeen) " +
            "FROM Manager m WHERE m.parent.id = :parentId")
    List<ManagerDTO> findManagersByParentId(@Param("parentId") Long parentId);

    @Modifying
    @Query("DELETE FROM Manager m WHERE m.id = :id")
    void deleteManagerById(@Param("id") Long id);

    @Query("SELECT new com.easyliveline.streamingbackend.dto.ParentInfo(m.parent.id, m.parentType) " +
            "FROM Manager m WHERE m.id = :parentId")
    ParentInfo findParentInfoByManagerId(@Param("parentId") Long parentId);
}
