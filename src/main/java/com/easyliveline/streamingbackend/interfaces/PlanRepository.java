package com.easyliveline.streamingbackend.interfaces;

import com.easyliveline.streamingbackend.models.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {

}
