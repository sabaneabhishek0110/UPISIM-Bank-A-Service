package com.example.hdfc.Repository;

import com.example.hdfc.model.hdfc_transactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TransactionRepository extends JpaRepository<hdfc_transactions, UUID> {

}
