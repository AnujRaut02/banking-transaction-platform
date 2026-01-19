package com.banking.platform.controller;

import com.banking.platform.dto.TransactionRequest;
import com.banking.platform.dto.TransferRequest;
import com.banking.platform.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/credit")
    public ResponseEntity<String> credit(@Valid @RequestBody TransactionRequest request){
        transactionService.credit(request);
        return ResponseEntity.ok("Amount Credited Successfully");
    }

    @PostMapping("/debit")
    public ResponseEntity<String> debit(@Valid @RequestBody TransactionRequest request){
        transactionService.debit(request);
        return ResponseEntity.ok("Amount debited Successfully");
    }

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(@Valid @RequestBody TransferRequest request){
        transactionService.transfer(request);
        return ResponseEntity.ok("Transfer Successful");
    }
}
