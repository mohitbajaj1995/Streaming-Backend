package com.easyliveline.streamingbackend.services;

import com.easyliveline.streamingbackend.interfaces.PlanRepository;
import com.easyliveline.streamingbackend.models.Plan;
import com.easyliveline.streamingbackend.util.ExceptionWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class PlanService {

    private final PlanRepository planRepository;

    @Autowired
    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public List<Plan> getAllPlans() {
        log.info("Fetching all plans");
        return ExceptionWrapper.handle(() -> {
            List<Plan> plans = planRepository.findAll();
            log.debug("Found {} plans", plans.size());
            return plans;
        });
    }
}
