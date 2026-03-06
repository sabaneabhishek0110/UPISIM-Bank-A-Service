package com.example.hdfc.service;

import com.example.hdfc.Repository.AccountsRepository;
import com.example.hdfc.Repository.TransactionRepository;
import com.example.hdfc.dto.*;
import com.example.hdfc.model.hdfc_accounts;
import com.example.hdfc.model.hdfc_transactions;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class PaymentService {
    private final AccountsRepository accountsRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;


    //This are the for old version
    public DebitResponse debit(String vpa,BigDecimal amount,String pin,String rrn,String upi_txn_id,String psp_txn_id) {
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

//        double balanceInDouble = balance.doubleValue();

        if(balance.compareTo(amount) < 0){
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

    //This are the for old version
    public CreditResponse credit(String vpa, BigDecimal amount, String rrn, String upi_txn_id,String psp_txn_id) {
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
    public BankResponse handleDebit(DebitRequest request) {

        // 1 : Idempotency check
        Optional<hdfc_transactions> existing =
                transactionRepository.findByUpiTxnId(request.getUpiTxnId());

        if (existing.isPresent()) {
            return new BankResponse(
                    existing.get().getId().toString(),
                    existing.get().getStatus().name(),
                    "Already processed"
            );
        }

        // 2 : Fetch account
        Optional<hdfc_accounts> accountOpt = accountsRepository.findByVpa(
                request.getPayerVpa()
        );

        if (accountOpt.isEmpty()) {
            return new BankResponse(
                    null,
                    "FAILED",
                    "Account not found"
            );
        }

        hdfc_accounts account = accountOpt.get();

        // 3 : PIN VALIDATION
        if (!passwordEncoder.matches(request.getPin(), account.getUpiPinHash())) {

            hdfc_transactions failedTxn = new hdfc_transactions();
            failedTxn.setAccountId(account);
            failedTxn.setUpiTxnId(request.getUpiTxnId());
            failedTxn.setPayerVpa(request.getPayerVpa());
            failedTxn.setPayeeVpa(request.getPayeeVpa());
            failedTxn.setPspTxnId(request.getPspTxnId());
            failedTxn.setRrn(request.getRrn());
            failedTxn.setAmount(request.getAmount());
            failedTxn.setTransactionType(hdfc_transactions.TransactionType.DEBIT);
            failedTxn.setStatus(hdfc_transactions.TransactionStatus.FAILED);

            transactionRepository.save(failedTxn);

            return new BankResponse(
                    failedTxn.getId().toString(),
                    "FAILED",
                    "Invalid PIN"
            );
        }

        // 4 : Balance check
        BigDecimal balance = account.getBalance();
        BigDecimal amount = request.getAmount();
        if (balance.compareTo(amount) < 0) {

            hdfc_transactions failedTxn = new hdfc_transactions();
            failedTxn.setAccountId(account);
            failedTxn.setUpiTxnId(request.getUpiTxnId());
            failedTxn.setPayerVpa(request.getPayerVpa());
            failedTxn.setPayeeVpa(request.getPayeeVpa());
            failedTxn.setPspTxnId(request.getPspTxnId());
            failedTxn.setRrn(request.getRrn());
            failedTxn.setAmount(request.getAmount());
            failedTxn.setTransactionType(hdfc_transactions.TransactionType.DEBIT);
            failedTxn.setStatus(hdfc_transactions.TransactionStatus.FAILED);

            transactionRepository.save(failedTxn);

            return new BankResponse(
                    failedTxn.getId().toString(),
                    "FAILED",
                    "Insufficient balance"
            );
        }

        // 5 : Deduct balance
        account.setBalance(
                balance.subtract(amount)
        );
        accountsRepository.save(account);

        // 6 : Save success transaction
        hdfc_transactions successTxn = new hdfc_transactions();
        successTxn.setAccountId(account);
        successTxn.setUpiTxnId(request.getUpiTxnId());
        successTxn.setPayerVpa(request.getPayerVpa());
        successTxn.setPayeeVpa(request.getPayeeVpa());
        successTxn.setPspTxnId(request.getPspTxnId());
        successTxn.setRrn(request.getRrn());
        successTxn.setAmount(request.getAmount());
        successTxn.setTransactionType(hdfc_transactions.TransactionType.DEBIT);
        successTxn.setStatus(hdfc_transactions.TransactionStatus.SUCCESS);

        transactionRepository.save(successTxn);

        return new BankResponse(
                successTxn.getId().toString(),
                "SUCCESS",
                "Debit successful"
        );
    }

    @Transactional
    public BankResponse handleCredit(CreditRequest request) {

        // 1 : Idempotency check
        Optional<hdfc_transactions> existing =
                transactionRepository.findByUpiTxnId(request.getUpiTxnId());

        if (existing.isPresent()) {
            return new BankResponse(
                    existing.get().getId().toString(),
                    existing.get().getStatus().name(),
                    "Already processed"
            );
        }

        // 2 : Fetch account
        Optional<hdfc_accounts> accountOpt = accountsRepository.findByVpa(
                request.getPayeeVpa()
        );

        if (accountOpt.isEmpty()) {
            return new BankResponse(
                    null,
                    "FAILED",
                    "Account not found"
            );
        }

        hdfc_accounts account = accountOpt.get();

        BigDecimal balance = account.getBalance();
        BigDecimal amount = request.getAmount();

        // 3 : Add balance
        account.setBalance(
                balance.add(amount)
        );
        accountsRepository.save(account);

        // 4 : Save success transaction
        hdfc_transactions successTxn = new hdfc_transactions();
        successTxn.setAccountId(account);
        successTxn.setUpiTxnId(request.getUpiTxnId());
        successTxn.setPayerVpa(request.getPayerVpa());
        successTxn.setPayeeVpa(request.getPayeeVpa());
        successTxn.setPspTxnId(request.getPspTxnId());
        successTxn.setRrn(request.getRrn());
        successTxn.setAmount(request.getAmount());
        successTxn.setTransactionType(hdfc_transactions.TransactionType.CREDIT);
        successTxn.setStatus(hdfc_transactions.TransactionStatus.SUCCESS);

        transactionRepository.save(successTxn);

        return new BankResponse(
                successTxn.getId().toString(),
                "SUCCESS",
                "Credit successful"
        );
    }

    @Transactional
    public BankResponse handleReversal(ReversalRequest request) {

        // 1 : Check original debit exists
        Optional<hdfc_transactions> originalTxnOpt =
                transactionRepository.findByUpiTxnId(request.getUpiTxnId());

        if (originalTxnOpt.isEmpty()) {
            return new BankResponse(
                    null,
                    "FAILED",
                    "Original transaction not found"
            );
        }

        hdfc_transactions originalTxn = originalTxnOpt.get();

        // 2 : Ensure original txn was SUCCESS debit
        if (originalTxn.getTransactionType() != hdfc_transactions.TransactionType.DEBIT
                || originalTxn.getStatus() != hdfc_transactions.TransactionStatus.SUCCESS) {

            return new BankResponse(
                    originalTxn.getId().toString(),
                    "FAILED",
                    "Reversal not allowed"
            );
        }

        // 3 : Check if reversal already done (idempotency)
        Optional<hdfc_transactions> existingReversal =
                transactionRepository.findByUpiTxnId(
                        request.getUpiTxnId()
                );

        if (existingReversal.isPresent() &&
                existingReversal.get().getStatus() == hdfc_transactions.TransactionStatus.REVERSED
        ) {
            return new BankResponse(
                    existingReversal.get().getId().toString(),
                    existingReversal.get().getStatus().name(),
                    "Already reversed"
            );
        }

        // 4 : Fetch account
        Optional<hdfc_accounts> reversalAccountOpt = accountsRepository.findByVpa(
                request.getPayerVpa()
        );

        if (reversalAccountOpt.isEmpty()) {
            return new BankResponse(
                    originalTxn.getId().toString(),
                    "FAILED",
                    "Account not found for reversal"
            );
        }

        hdfc_accounts account = reversalAccountOpt.get();

        // 5 : Add money back
        account.setBalance(
                account.getBalance().add(request.getAmount())
        );
        accountsRepository.save(account);

        // 6 : Save reversal transaction
        hdfc_transactions reversalTxn = new hdfc_transactions();
        reversalTxn.setAccountId(account);
        reversalTxn.setUpiTxnId(request.getUpiTxnId());
        reversalTxn.setPayerVpa(request.getPayerVpa());
        reversalTxn.setPayeeVpa(request.getPayeeVpa());
        reversalTxn.setPspTxnId(request.getPspTxnId());
        reversalTxn.setRrn(request.getRrn());
        reversalTxn.setAmount(request.getAmount());
        reversalTxn.setTransactionType(hdfc_transactions.TransactionType.REVERSED);
        reversalTxn.setStatus(hdfc_transactions.TransactionStatus.REVERSED);

        transactionRepository.save(reversalTxn);

        return new BankResponse(
                reversalTxn.getId().toString(),
                "SUCCESS",
                "Reversal successful"
        );
    }

    @Transactional
    public UUID createTransactions(
            hdfc_accounts account,
            String txnType,
            BigDecimal amount,
            String PspTxnId,
            String status,
            String failureReason
    ){
        hdfc_transactions transaction = new hdfc_transactions();
        transaction.setAccountId(account);
        transaction.setTransactionType(Enum.valueOf(hdfc_transactions.TransactionType.class, txnType));
        transaction.setAmount(amount);
        transaction.setPspTxnId(PspTxnId);
        transaction.setStatus(Enum.valueOf(hdfc_transactions.TransactionStatus.class, status));
        transaction.setFailure_reason(failureReason);
        transactionRepository.save(transaction);
        return transaction.getId();
    }

    public boolean authenticateUser(String vpa,String pin){
        hdfc_accounts account = accountsRepository.findByVpa(vpa).get();
        if(account != null && passwordEncoder.matches(pin, account.getUpiPinHash())){
            return true;
        }
        return false;
    }

    public BigDecimal getAccountBalance(String vpa){
        Optional<hdfc_accounts> account = accountsRepository.findByVpa(vpa);
//        return account.get().getBalance().doubleValue();
        return account.get().getBalance();
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
