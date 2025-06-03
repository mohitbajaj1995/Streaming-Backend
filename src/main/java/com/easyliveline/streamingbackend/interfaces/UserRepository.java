package com.easyliveline.streamingbackend.interfaces;

import com.easyliveline.streamingbackend.enums.RoleType;
import com.easyliveline.streamingbackend.models.User;
import com.easyliveline.streamingbackend.projections.UserLoginView;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    Optional<UserLoginView> findByUsername(String username);

    @Query("SELECT u.role FROM User u WHERE u.id = :id")
    RoleType findRoleById(@Param("id") Long id);

    @Query("SELECT u.username FROM User u WHERE u.id = :id")
    String findUsernameById(@Param("id") Long id);

    @Modifying
    @Query(value = "UPDATE royal_users SET name = :name WHERE id = :id", nativeQuery = true)
    void updateNameById(@Param("id") Long id, @Param("name") String name);

    @Modifying
    @Query(value = "UPDATE royal_users SET name = :name, password = :password WHERE id = :id", nativeQuery = true)
    void updateNameAndPasswordById(@Param("id") Long id, @Param("name") String name, @Param("password") String password);

    @Modifying
    @Query(value = "UPDATE royal_users SET enabled = NOT enabled WHERE id = :id", nativeQuery = true)
    void toggleUserEnabled(@Param("id") Long id);

//    @Query("SELECT new com.easyliveline.streamingbackend.DTO.MeetingWithZoomDto(m.id, m.meetingNumber, m.meetingPassword, m.email, m.zoom) FROM Participant p JOIN p.slot s JOIN s.meeting m WHERE p.id = :participantId")
//    MeetingWithZoomDto findMeetingWithZoomByParticipantId(@Param("participantId") Long participantId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET last_seen = :lastSeen WHERE id = :id", nativeQuery = true)
    void updateLastSeenByIdNative(@Param("id") Long id, @Param("lastSeen") long lastSeen);

//    @Modifying
//    @Transactional
//    @Query("UPDATE User u SET u.lastSeen = :lastSeen WHERE u.id = :id")
//    void updateLastSeenById(@Param("id") Long id, @Param("lastSeen") long lastSeen);
}
