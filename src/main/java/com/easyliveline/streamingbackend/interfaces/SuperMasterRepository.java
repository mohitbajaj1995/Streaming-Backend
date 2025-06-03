package com.easyliveline.streamingbackend.interfaces;

import com.easyliveline.streamingbackend.dto.OwnerFilterSuperMasterAndMasterMeta;
import com.easyliveline.streamingbackend.models.SuperMaster;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuperMasterRepository extends JpaRepository<SuperMaster, Long> {

    @Query("SELECT s.points FROM SuperMaster s WHERE s.id = :id")
    Integer findPointsById(@Param("id") Long id);

    @Query("SELECT s.owner.id FROM SuperMaster s WHERE s.id = :id")
    long findOwnerById(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE SuperMaster s SET s.points = :points WHERE s.id = :id")
    void updateSuperMasterPoints(@Param("id") Long id, @Param("points") long points);

    @Query("SELECT new com.easyliveline.streamingbackend.dto.OwnerFilterSuperMasterAndMasterMeta(s.id, s.username, s.role) " +
            "FROM SuperMaster s WHERE s.owner.id = :ownerId")
    List<OwnerFilterSuperMasterAndMasterMeta> findSuperMastersByOwnerId(@Param("ownerId") Long ownerId);
}
