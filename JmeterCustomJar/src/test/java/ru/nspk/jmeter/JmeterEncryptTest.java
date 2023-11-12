package ru.nspk.jmeter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.jupiter.api.Assertions.*;

class JmeterEncryptTest {

    private JmeterEncrypt jmeterEncrypt = EncryptDecorator.decorate();

    JmeterEncryptTest() throws NoSuchAlgorithmException, InvalidKeySpecException {
    }

    @Test
    void checkEncryptDecrypt() throws InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException, BadPaddingException, InvalidKeyException, NoSuchProviderException {
        String testStringToEncrypt = "Mother washed the frame";

        String encryptedString = jmeterEncrypt.encrypt(testStringToEncrypt);
        Assertions.assertNotEquals(testStringToEncrypt, encryptedString);
        String decryptedString = jmeterEncrypt.decrypt(encryptedString);
        Assertions.assertEquals(testStringToEncrypt, decryptedString);
    }
}