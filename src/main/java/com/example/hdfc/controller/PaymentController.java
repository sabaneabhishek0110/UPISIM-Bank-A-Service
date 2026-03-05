package com.example.hdfc.controller;

import com.example.hdfc.dto.*;
import com.example.hdfc.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/bank")
@RequiredArgsConstructor
public class PaymentController {
    private PaymentService paymentService;


//    @PostMapping("/debit")
//    public ResponseEntity<DebitResponse> debit(@RequestBody DebitRequest requestBody, HttpServletRequest request) {
//        String PayerVpa = requestBody.getPayerVpa();
//        BigDecimal amount = requestBody.getAmount();
//        String pin = requestBody.getPin();
//        String rrn = requestBody.getRrn();
//        String upi_txn_id = requestBody.getUpi_txn_id();
//        String psp_txn_id = requestBody.getPsp_txn_id();
//        DebitResponse response = paymentService.debit(PayerVpa, amount,pin,rrn,upi_txn_id,psp_txn_id);
//
//        System.out.println("HDFC Debit called");
//        System.out.println("Response : "+ response.getStatus());
//
//        return ResponseEntity.status(200).body(response);
//    }
//
//    @PostMapping("/credit")
//    public ResponseEntity<CreditResponse> credit(@RequestBody CreditRequest requestBody, HttpServletRequest request) {
//        String PayeeVpa = requestBody.getPayeeVpa();
//        BigDecimal amount = requestBody.getAmount();
//        String rrn = requestBody.getRrn();
//        String upi_txn_id = requestBody.getUpi_txn_id();
//        String psp_txn_id = requestBody.getPsp_txn_id();
//        System.out.println("HDFC Credit called for PayeeVpa: " + PayeeVpa + ", amount: " + amount);
//        CreditResponse response = paymentService.credit(PayeeVpa, amount,rrn,upi_txn_id,psp_txn_id);
//
//        System.out.println("HDFC Credit called");
//        System.out.println("Response : "+ response.getStatus());
//
//        return ResponseEntity.status(200).body(response);
//    }


    @PostMapping("/debit")
    public ResponseEntity<BankResponse> debit(
            @RequestBody DebitRequest request) {

        BankResponse response = paymentService.handleDebit(request);
        return ResponseEntity.ok(response); // always 200
    }

    @PostMapping("/credit")
    public ResponseEntity<BankResponse> credit(
            @RequestBody CreditRequest request) {

        BankResponse response = paymentService.handleCredit(request);
        return ResponseEntity.ok(response); // always 200
    }

    @PostMapping("/reversal")
    public ResponseEntity<BankResponse> reverse(
            @RequestBody ReversalRequest request) {

        BankResponse response = paymentService.handleReversal(request);
        return ResponseEntity.ok(response);
    }


}
