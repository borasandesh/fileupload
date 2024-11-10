package com.alzionlabs.service;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class EncryptionService {

    public byte[] encrypt(byte[] fileContent, String passcode) throws Exception {
        SecretKey secretKey = generateKey(passcode);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(fileContent);
    }

    public byte[] decrypt(byte[] encryptedContent, String passcode) throws Exception {
        SecretKey secretKey = generateKey(passcode);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(encryptedContent);
    }

    private SecretKey generateKey(String passcode) throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        byte[] key = passcode.getBytes("UTF-8");
        key = sha.digest(key);
        return new SecretKeySpec(key, 0, 16, "AES"); // AES requires a 16-byte key
    }
}
