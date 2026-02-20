package com.example.hdfc.service;

import com.example.hdfc.Repository.NpciHdfcRegistryRepository;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@Service
public class HdfcService {
    private final NpciHdfcRegistryRepository npciHdfcRegistryRepository;

    public HdfcService(NpciHdfcRegistryRepository npciHdfcRegistryRepository) {
        this.npciHdfcRegistryRepository = npciHdfcRegistryRepository;
    }

    public String loadPublicKeyFromdb(String npciId){
        return npciHdfcRegistryRepository.findByNpciId(npciId).get().getPublic_key();
    }

    public PublicKey convertToPublicKey(String base64Key) {
        try {
            byte[] decoded = Base64.getDecoder().decode(base64Key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePublic(spec);
        }
        catch(Exception e) {
            throw new IllegalStateException("failed to load public key",e);
        }
    }

    public boolean verify(
            String payload,
            String signatureBase64,
            PublicKey publicKey
    ){
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(payload.getBytes(StandardCharsets.UTF_8));

            byte[] sigBytes = Base64.getDecoder().decode(signatureBase64);
            return signature.verify(sigBytes);
        }
        catch(Exception e) {
            throw new IllegalStateException("failed to verify signature",e);
        }
    }
}
