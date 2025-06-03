package com.easyliveline.streamingbackend.controllers;

import com.easyliveline.streamingbackend.dto.TransactionDTO;
import com.easyliveline.streamingbackend.models.ApiResponse;
import com.easyliveline.streamingbackend.models.FilterRequest;
import com.easyliveline.streamingbackend.services.TransactionService;
import com.easyliveline.streamingbackend.util.ApiResponseBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','OWNER','MASTER')")
    @PostMapping("/filters")
    public ResponseEntity<ApiResponse<Page<TransactionDTO>>> getFilteredUsers(@RequestBody FilterRequest filterRequest) {
        Page<TransactionDTO> masters = transactionService.getAllTransactions(filterRequest);
        return ResponseEntity.ok(ApiResponseBuilder.success("USER_TRANSACTIONS", masters));
    }
}
