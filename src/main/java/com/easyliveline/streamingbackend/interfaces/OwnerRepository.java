package com.easyliveline.streamingbackend.interfaces;

import com.easyliveline.streamingbackend.models.Owner;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {
    @Query("SELECT o.points FROM Owner o WHERE o.id = :ownerId")
    Integer findPointsByOwnerId(@Param("ownerId") Long ownerId);

    @Modifying
    @Transactional
    @Query("UPDATE Owner o SET o.points = :points WHERE o.id = :ownerId")
    void updateOwnerPoints(@Param("ownerId") Long ownerId, @Param("points") long points);
}
