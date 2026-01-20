package com.banking.platform.controller;

import com.banking.platform.domain.Transaction;
import com.banking.platform.dto.TransactionRequest;
import com.banking.platform.dto.TransactionResponse;
import com.banking.platform.dto.TransferRequest;
import com.banking.platform.service.TransactionService;
import com.banking.platform.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final TransferService transferService;

    public TransactionController(TransactionService transactionService, TransferService transferService) {
        this.transactionService = transactionService; this.transferService = transferService;
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
        transferService.transfer(request);
        return ResponseEntity.ok("Transfer Successful");
    }

    @GetMapping
    public List<TransactionResponse> getTransaction(
            @RequestParam String accountNumber,
            @RequestParam int page,
            @RequestParam int size){
        System.out.println(">>> DB HIT 1<<<");
        return transactionService.getTransaction(accountNumber,page,size);
    }
}
