package com.easyliveline.streamingbackend.controllers;

import com.easyliveline.streamingbackend.dto.RefundHistoryResponse;
import com.easyliveline.streamingbackend.dto.RefundDTO;
import com.easyliveline.streamingbackend.models.ApiResponse;
import com.easyliveline.streamingbackend.models.FilterRequest;
import com.easyliveline.streamingbackend.models.Refund;
import com.easyliveline.streamingbackend.models.RefundRequest;
import com.easyliveline.streamingbackend.services.RefundService;
import com.easyliveline.streamingbackend.util.ApiResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;

    @Autowired
    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<Page<RefundDTO>>> getRefunds(@RequestBody FilterRequest filterRequest) {
        Page<RefundDTO> refunds = refundService.getAllRefunds(filterRequest);
        return ResponseEntity.ok(ApiResponseBuilder.success("REFUNDS_FETCHED_SUCCESSFULLY", refunds));
    }

//    @GetMapping("/{status}")
//    public ResponseEntity<ApiResponse<List<Refund>>> getPendingRefunds(@PathVariable("status") String status) {
//        List<Refund> pendingRefunds = refundService.getRefunds(status);
//        return ResponseEntity.ok(ApiResponseBuilder.success(status.toUpperCase() +"_REFUNDS_FETCHED_SUCCESSFULLY", pendingRefunds));
//    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_OWNER')")
    @GetMapping("/parentHistory/{parent}")
    public ResponseEntity<ApiResponse<RefundHistoryResponse>> getRefundhistory(@PathVariable("parent") Long parentId) {
        RefundHistoryResponse parentHistory = refundService.getRefundHistory(parentId);
        return ResponseEntity.ok(ApiResponseBuilder.success("REFUND_HISTORY_FETCHED_SUCCESSFULLY", parentHistory));
    }

    @PreAuthorize("!hasAuthority('ROLE_SUBSCRIBER')")
    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<Refund>> createRefund(@PathVariable("userId") long userId, @RequestBody RefundRequest refundRequest) {
        refundService.createRefund(userId,refundRequest);
        return ResponseEntity.ok(ApiResponseBuilder.success("REFUND_CREATED_SUCCESSFULLY", null));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_OWNER')")
    @GetMapping("/accept/{refundId}")
    public ResponseEntity<ApiResponse<String>> acceptRefund(@PathVariable("refundId") Long refundId) {
        refundService.acceptRefund(refundId);
        return ResponseEntity.ok(ApiResponseBuilder.success("REFUND_ACCEPTED_SUCCESSFULLY", null));
    }

    @PreAuthorize("hasAnyAuthority('ROLE_MANAGER', 'ROLE_OWNER')")
    @GetMapping("/reject/{refundId}")
    public ResponseEntity<ApiResponse<String>> rejectRefund(@PathVariable("refundId") Long refundId) {
        refundService.rejectRefund(refundId);
        return ResponseEntity.ok(ApiResponseBuilder.success("REFUND_REJECTED_SUCCESSFULLY", null));
    }
}