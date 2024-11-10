package com.alzionlabs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.*;

class EncryptionServiceTest {

    private EncryptionService encryptionService;

    @BeforeEach
    void setUp() {
        encryptionService = Mockito.mock(EncryptionService.class);
    }

    @Test
    void testEncrypt() throws Exception {
        byte[] data = "sample data".getBytes();
        String passcode = "passcode";
        byte[] encryptedData = "encrypted data".getBytes();

        when(encryptionService.encrypt(data, passcode)).thenReturn(encryptedData);

        byte[] result = encryptionService.encrypt(data, passcode);
        assertArrayEquals(encryptedData, result);
    }

    @Test
    void testDecrypt() throws Exception {
        byte[] encryptedData = "encrypted data".getBytes();
        String passcode = "passcode";
        byte[] decryptedData = "sample data".getBytes();

        when(encryptionService.decrypt(encryptedData, passcode)).thenReturn(decryptedData);

        byte[] result = encryptionService.decrypt(encryptedData, passcode);
        assertArrayEquals(decryptedData, result);
    }
}
