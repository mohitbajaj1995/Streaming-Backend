package com.easyliveline.streamingbackend.interfaces;

import com.easyliveline.streamingbackend.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Modifying
    @Query(value = "INSERT INTO telecom_transactions (user_id, points, description, is_credit, created_at, now, after) VALUES (:userId, :points, :description, :isCredit, :created_at, :now, :after)", nativeQuery = true)
    int insertTransaction(@Param("userId") Long userId, @Param("points") long points, @Param("description") String description, @Param("isCredit") boolean isCredit, @Param("created_at") Long created_at, @Param("now") Long now, @Param("after") Long after);
}
