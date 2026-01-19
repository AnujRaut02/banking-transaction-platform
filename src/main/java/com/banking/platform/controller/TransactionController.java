package com.banking.platform.controller;

import com.banking.platform.domain.Transaction;
import com.banking.platform.dto.TransactionRequest;
import com.banking.platform.dto.TransferRequest;
import com.banking.platform.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping
    public Page<Transaction> getTransaction(@RequestParam String accountNumber,
                                           @RequestParam int page,
                                           @RequestParam int size){
        return transactionService.getTransaction(accountNumber,page,size);
    }
}
