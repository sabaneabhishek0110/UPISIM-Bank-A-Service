package com.example.hdfc.controller;


import com.example.hdfc.Repository.AccountsRepository;
import com.example.hdfc.model.hdfc_accounts;
import com.example.hdfc.Repository.AccountsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class BankController {

    private final AccountsRepository repository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/account/create")
    public ResponseEntity<?> createAccount(@RequestBody hdfc_accounts account) {

        String hashedPin = passwordEncoder.encode(account.getUpiPinHash());
        account.setUpiPinHash(hashedPin);
        hdfc_accounts savedAccount = repository.save(account);

        Map<String,Object> response = new HashMap<>();
        response.put("message","Account created successfully");
        response.put("account",savedAccount);

        return ResponseEntity.ok(response);
    }
}