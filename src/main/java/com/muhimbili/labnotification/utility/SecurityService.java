package com.muhimbili.labnotification.utility;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.util.Base64;


@Component
@Slf4j
public class SecurityService {

    private final UtilityService utilityService;
    private final LoggerService loggerService;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public SecurityService(UtilityService utilityService, LoggerService loggerService) {
        this.utilityService = utilityService;
        this.loggerService = loggerService;
    }

    //@PostConstruct
    public void initiateCertificates() {
        privateKey = getPrivateKey();
        publicKey = getPublicKey();
    }

    public String getPayloadSignature(String content) {
        String signed = null;
        try {
            byte[] data = content.getBytes();
            Signature signature = Signature.getInstance(getConfig("CERTIFICATE_ALGORITHM"));
            signature.initSign(privateKey);
            signature.update(data);
            byte[] signatureBytes = signature.sign();
            signed = org.apache.commons.codec.binary.Base64.encodeBase64String(signatureBytes);
            log(signed);
        } catch (Exception e) {
            log("CreateSignature Error has occurred : ");
            log(e.getMessage());
        }
        return signed;
    }

    public boolean verifyPayloadSignature(String signatur, String content) {
        boolean verified = false;
        try {
            Signature sig = Signature.getInstance(getConfig("CERTIFICATE_ALGORITHM"));
            signatur = signatur.replace("\n", "");
            log(signatur);
            byte[] data = content.getBytes();
            sig.initVerify(publicKey);
            sig.update(data);
            verified = sig.verify(Base64.getDecoder().decode(signatur.getBytes()));
        } catch (Exception e) {
           log("VerifySignature error has occurred ");
        }
        return verified;
    }

    private PrivateKey getPrivateKey() {
        try {
            char[] passCharArray = getConfig("CERTIFICATE_PASS").toCharArray();
            KeyStore keyStore = KeyStore.getInstance(getConfig("CERTIFICATE_KEYSTORE"));
            FileInputStream is = new FileInputStream(getConfig("CERTIFICATE_PATH"));
            keyStore.load(is, passCharArray);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(getConfig("CERTIFICATE_ALIAS"), passCharArray);
            log("Private key has been loaded");
            log(String.valueOf(privateKey));
            return privateKey;
        } catch (Exception e) {
            log("Error in creating a private key");
            log(e.getMessage());
            return null;
        }
    }
    private PublicKey getPublicKey() {
        try {
            KeyStore keyStore = KeyStore.getInstance(getConfig("CERTIFICATE_KEYSTORE"));
            FileInputStream is = new FileInputStream(getConfig("CERTIFICATE_PATH"));
            keyStore.load(is, getConfig("CERTIFICATE_PASS").toCharArray());
            Certificate cert = keyStore.getCertificate(getConfig("CERTIFICATE_ALIAS"));
            PublicKey publicKey = cert.getPublicKey();
            log("Public key has been loaded");
            log(String.valueOf(publicKey));
            return publicKey;
        } catch (Exception e) {
            log("Error in fetching public key");
            log(e.getMessage());
            return null;
        }
    }

    private void log(String log) {
        loggerService.log(log);
    }

    private String getConfig(String key) {
        return "";
    }

//    private String getConfig(String key) {
//        return utilityService.getConfiguration().getConstant(key);
//    }
}
