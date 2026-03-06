package com.example.hdfc.Repository;

import com.example.hdfc.model.hdfc_accounts;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

public interface AccountsRepository extends JpaRepository<hdfc_accounts, String> {
    Optional<hdfc_accounts> findByVpa(@Param("vpa") String vpa);

    @Modifying
    @Transactional
    @Query("UPDATE hdfc_accounts a SET a.balance = a.balance - :amount WHERE a.vpa = :vpa AND a.balance >= :amount")
    int debitBalance(@Param("vpa") String vpa,@Param("amount") BigDecimal amount);

    @Modifying
    @Transactional
    @Query("UPDATE hdfc_accounts a SET a.balance = a.balance + :amount WHERE a.vpa = :vpa")
    int creditBalance(@Param("vpa") String vpa,@Param("amount") BigDecimal amount);
}

