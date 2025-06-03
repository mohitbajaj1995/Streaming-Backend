package com.easyliveline.streamingbackend.interfaces;

import com.easyliveline.streamingbackend.models.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRepository extends JpaRepository<Refund, Long> {


    @Query("""
    SELECT 
        COUNT(r) AS totalRefunds,
        SUM(CASE WHEN r.subscriptionStartedAt >= :lastWeek THEN 1 ELSE 0 END),
        SUM(CASE WHEN r.subscriptionStartedAt >= :lastMonth THEN 1 ELSE 0 END),
        SUM(CASE WHEN r.subscriptionStartedAt >= :lastYear THEN 1 ELSE 0 END),
        SUM(CASE WHEN r.status = 'REFUNDED' THEN 1 ELSE 0 END),
        SUM(CASE WHEN r.status = 'REJECTED' THEN 1 ELSE 0 END),
        SUM(CASE WHEN r.status = 'PENDING' THEN 1 ELSE 0 END)
    FROM Refund r
    WHERE r.parentId = :parentId
""")
    Object[] getRefundStatistics(
            @Param("parentId") Long parentId,
            @Param("lastWeek") Long lastWeek,
            @Param("lastMonth") Long lastMonth,
            @Param("lastYear") Long lastYear
    );
}
