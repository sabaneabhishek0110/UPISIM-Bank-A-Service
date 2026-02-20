package com.example.hdfc.controller;

import com.example.hdfc.dto.CreditRequest;
import com.example.hdfc.dto.CreditResponse;
import com.example.hdfc.dto.DebitRequest;
import com.example.hdfc.dto.DebitResponse;
import com.example.hdfc.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bank")
public class PaymentController {
    private PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
    @PostMapping("/debit")
    public ResponseEntity<DebitResponse> debit(@RequestBody DebitRequest requestBody, HttpServletRequest request) {
        String PayerVpa = requestBody.getPayerVpa();
        Double amount = requestBody.getAmount();
        String pin = requestBody.getPin();
        String rrn = requestBody.getRrn();
        String upi_txn_id = requestBody.getUpi_txn_id();
        String psp_txn_id = requestBody.getPsp_txn_id();
        DebitResponse response = paymentService.debit(PayerVpa, amount,pin,rrn,upi_txn_id,psp_txn_id);

        System.out.println("HDFC Debit called");
        System.out.println("Response : "+ response.getStatus());

        return ResponseEntity.status(200).body(response);
    }

    @PostMapping("/credit")
    public ResponseEntity<CreditResponse> credit(@RequestBody CreditRequest requestBody, HttpServletRequest request) {
        String PayeeVpa = requestBody.getPayeeVpa();
        Double amount = requestBody.getAmount();
        String rrn = requestBody.getRrn();
        String upi_txn_id = requestBody.getUpi_txn_id();
        String psp_txn_id = requestBody.getPsp_txn_id();
        System.out.println("HDFC Credit called for PayeeVpa: " + PayeeVpa + ", amount: " + amount);
        CreditResponse response = paymentService.credit(PayeeVpa, amount,rrn,upi_txn_id,psp_txn_id);

        System.out.println("HDFC Credit called");
        System.out.println("Response : "+ response.getStatus());

        return ResponseEntity.status(200).body(response);
    }
}
