package com.example.hdfc.service;

import com.example.hdfc.Repository.AccountsRepository;
import com.example.hdfc.Repository.TransactionRepository;
import com.example.hdfc.dto.CreditResponse;
import com.example.hdfc.dto.DebitResponse;
import com.example.hdfc.model.hdfc_accounts;
import com.example.hdfc.model.hdfc_transactions;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;


@Service
public class PaymentService {
    private final AccountsRepository accountsRepository;
    private final TransactionRepository transactionRepository;

    public PaymentService(AccountsRepository accountsRepository,TransactionRepository transactionRepository) {
        this.accountsRepository = accountsRepository;
        this.transactionRepository = transactionRepository;
    }

    public DebitResponse debit(String vpa,Double amount,String pin,String rrn,String upi_txn_id,String psp_txn_id) {
        Optional<hdfc_accounts> account = accountsRepository.findByVpa(vpa);
        DebitResponse response;
        if (account.isEmpty()) {
            response = new DebitResponse(
                    "FAILED",
                    DebitResponse.ResponseCodeType.U03,
                    upi_txn_id,
                    null,
                    null,
                    "Account with provided Vpa does not exists"
            );
            return response;
        }
        if(account.get().getStatus().equals("INACTIVE")){
            String bank_txn_id = createTransactions(
                    account.get(),
                    "DEBIT",
                    amount,
                    psp_txn_id,
                    "FAILED",
                    "Account is Inactive"
            ).toString();
            response = new DebitResponse(
                    "FAILED",
                    DebitResponse.ResponseCodeType.U02,
                    upi_txn_id,
                    null,
                    bank_txn_id,
                    "Account is Inactive"
            );
            return response;
        }
        String storedPin = account.get().getUpiPinHash();
        if (!storedPin.equals(pin)) {
            String bank_txn_id = createTransactions(
                    account.get(),
                    "DEBIT",
                    amount,
                    psp_txn_id,
                    "FAILED",
                    "Wrong pin entered"
            ).toString();
            response = new DebitResponse(
                    "FAILED",
                    DebitResponse.ResponseCodeType.U01,
                    upi_txn_id,
                    null,
                    bank_txn_id,
                    "Wrong pin enter"
            );
            return response;
        }

        BigDecimal balance = account.get().getBalance();

        double balanceInDouble = balance.doubleValue();

        if(balanceInDouble < amount){
            String bank_txn_id = createTransactions(
                    account.get(),
                    "DEBIT",
                    amount,
                    psp_txn_id,
                    "FAILED",
                    "BALANCE is Not Sufficient"
            ).toString();
            response = new DebitResponse(
                    "FAILED",
                    DebitResponse.ResponseCodeType.U14,
                    upi_txn_id,
                    null,
                    bank_txn_id,
                    "BALANCE is Not Sufficient"
            );
            return response;
        }

        accountsRepository.debitBalance(vpa,amount);
        String bank_txn_id = createTransactions(
                account.get(),
                "DEBIT",
                amount,
                psp_txn_id,
                "SUCCESS",
                null
        ).toString();
        response = new DebitResponse(
                "SUCCESS",
                DebitResponse.ResponseCodeType.U00,
                upi_txn_id,
                rrn,
                bank_txn_id,
                null
        );
        System.out.println("ResponseCode : "+String.valueOf(response.getResponseCode()));
        return response;
    }

    public CreditResponse credit(String vpa, Double amount, String rrn, String upi_txn_id,String psp_txn_id) {
        System.out.println("vpa : "+vpa);
        Optional<hdfc_accounts> account = accountsRepository.findByVpa(vpa);
        CreditResponse response;
        if (account.isEmpty()) {
            response = new CreditResponse(
                    "FAILED",
                    CreditResponse.ResponseCodeType.U03,
                    upi_txn_id,
                    null,
                    null,
                    "Account with provided Vpa does not exists"
            );
            return response;
        }
        if(account.get().getStatus().equals("INACTIVE")){
            String bank_txn_id = createTransactions(
                    account.get(),
                    "CREDIT",
                    amount,
                    psp_txn_id,
                    "FAILED",
                    "Account is Inactive"
            ).toString();
            response = new CreditResponse(
                    "FAILED",
                    CreditResponse.ResponseCodeType.U02,
                    upi_txn_id,
                    null,
                    bank_txn_id,
                    "Account is Inactive"
            );
            return response;
        }

        BigDecimal balance = account.get().getBalance();

        double balanceInDouble = balance.doubleValue();

        accountsRepository.creditBalance(vpa,amount);
        String bank_txn_id = createTransactions(
                account.get(),
                "CREDIT",
                amount,
                psp_txn_id,
                "SUCCESS",
                null
        ).toString();
        response = new CreditResponse(
                "SUCCESS",
                CreditResponse.ResponseCodeType.U00,
                upi_txn_id,
                rrn,
                bank_txn_id,
                null
        );
        return response;
    }

    @Transactional
    public UUID createTransactions(
            hdfc_accounts account,
            String txnType,
            Double amount,
            String PspTxnId,
            String status,
            String failureReason
    ){
        hdfc_transactions transaction = new hdfc_transactions();
        transaction.setAccount_id(account);
        transaction.setTransactionType(Enum.valueOf(hdfc_transactions.TransactionType.class, txnType));
        transaction.setAmount(amount);
        transaction.setPsp_txn_id(PspTxnId);
        transaction.setStatus(Enum.valueOf(hdfc_transactions.TransactionStatus.class, status));
        transaction.setFailure_reason(failureReason);
        transactionRepository.save(transaction);
        return transaction.getId();
    }

    public boolean authenticateUser(String vpa,String pin){
        hdfc_accounts account = accountsRepository.findByVpa(vpa).get();
        if(account != null && account.getUpiPinHash().equals(pin)){
            return true;
        }
        return false;
    }

    public Double getAccountBalance(String vpa){
        Optional<hdfc_accounts> account = accountsRepository.findByVpa(vpa);
        return account.get().getBalance().doubleValue();
    }
}

/*
Code	Meaning
U00	Success
U01	PIN incorrect
U02	Account Inactive
U03	Account not found
U14	Insufficient balance
U28	Debit timeout
U30	Transaction declined
91	Bank system down
96	System error
 */
