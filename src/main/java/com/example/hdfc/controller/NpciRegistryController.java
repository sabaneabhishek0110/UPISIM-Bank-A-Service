package com.example.hdfc.controller;

import com.example.hdfc.Repository.NpciHdfcRegistryRepository;
import com.example.hdfc.dto.NpciPublicKeyRegistryRequest;
import com.example.hdfc.model.NpciHdfcRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/internal/hdfc/npci")
public class NpciRegistryController {
    private final NpciHdfcRegistryRepository repository;

    public NpciRegistryController(NpciHdfcRegistryRepository repository) {
        this.repository = repository;
    }

    @PostMapping("/register-public-key")
    public ResponseEntity<String> registerPublicKey(
            @RequestBody NpciPublicKeyRegistryRequest request
    ) {
        Optional<NpciHdfcRegistry> existing =
                repository.findByNpciId(request.getNpciId());

        System.out.println("Npci Code :"+request.getNpciId());
        if (existing.isPresent()) {
            NpciHdfcRegistry npci = existing.get();
            npci.setPublic_key(request.getPublicKey());
            repository.save(npci);
            return ResponseEntity.ok("Public key updated");
        }

        NpciHdfcRegistry npci = new NpciHdfcRegistry();
        npci.setNpciId(request.getNpciId());
        npci.setPublic_key(request.getPublicKey());
        npci.setStatus("ACTIVE");
        repository.save(npci);

        return ResponseEntity.ok("Npci public key registered at hdfc bank");
    }
}
