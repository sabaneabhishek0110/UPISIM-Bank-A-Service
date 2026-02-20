package com.example.hdfc.Repository;

import com.example.hdfc.model.NpciHdfcRegistry;
import com.example.hdfc.model.NpciHdfcRegistry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NpciHdfcRegistryRepository extends JpaRepository<NpciHdfcRegistry,String> {
    Optional<NpciHdfcRegistry> findByNpciId(String npciId);
}
