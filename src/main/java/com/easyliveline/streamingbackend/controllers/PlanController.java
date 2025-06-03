package com.easyliveline.streamingbackend.controllers;

import com.easyliveline.streamingbackend.models.ApiResponse;
import com.easyliveline.streamingbackend.models.Plan;
import com.easyliveline.streamingbackend.services.PlanService;
import com.easyliveline.streamingbackend.util.ApiResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    private final PlanService planService;

    @Autowired
    public PlanController(PlanService planService) {
        this.planService = planService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Plan>>> getAllPlans() {
        List<Plan> plans = planService.getAllPlans(); // will throw if not found
        return ResponseEntity.ok(ApiResponseBuilder.success("PLANS", plans));
    }
}
