package com.example.hdfc.controller;

import com.example.hdfc.service.HdfcService;
import com.example.hdfc.dto.BalanceRequest;
import com.example.hdfc.dto.BalanceResponse;
import com.example.hdfc.service.HdfcService;
import com.example.hdfc.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.security.PrivateKey;
import java.time.Instant;

@RestController
@RequestMapping("/api/account")
public class AccountController {
    private final HdfcService hdfcService;
    private final PaymentService paymentService;
    private ObjectMapper objectMapper;
    private RestTemplate restTemplate;

    public AccountController(HdfcService hdfcService, PaymentService paymentService, ObjectMapper objectMapper) {
        this.hdfcService = hdfcService;
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/balance")
    public ResponseEntity<BalanceResponse> checkBalance(@RequestBody BalanceRequest balanceRequest) {
        try{
            String vpa = balanceRequest.getVpa();
            String pin = balanceRequest.getPin();
            boolean valid = paymentService.authenticateUser(vpa, pin);
            BalanceResponse response = new BalanceResponse();
            if(!valid){
                response.setBalance(0.0);
                response.setStatus("failed");
                response.setResponseCode("U01");
                response.setFailureReason("Wrong pin entered");
                return ResponseEntity.ok(response);
            }
            Double balance = paymentService.getAccountBalance(vpa);
            response.setBalance(balance);
            response.setStatus("success");
            response.setResponseCode("U00");
            response.setFailureReason(null);

            return ResponseEntity.ok(response);
        }
        catch(Exception e){
            return ResponseEntity.internalServerError().body(new BalanceResponse());
        }
    }
}
