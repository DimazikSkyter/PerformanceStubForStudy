package ru.nspk.jmeter;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class JmeterEncrypt {

    private final SecretKey secretKey;
    private final IvParameterSpec iv;

    private final String algorithm;

    public JmeterEncrypt(String password, String salt, String algorithm) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Security.addProvider(new BouncyCastleProvider());

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
        this.secretKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

        byte[] ivb = new byte[16];
        new SecureRandom().nextBytes(ivb);
        this.iv = new IvParameterSpec(ivb);

        this.algorithm = algorithm;
    }

    public String encrypt(String input) throws  NoSuchPaddingException,
                                                NoSuchAlgorithmException,
                                                InvalidAlgorithmParameterException,
                                                InvalidKeyException,
                                                IllegalBlockSizeException,
                                                BadPaddingException {
            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);
            byte[] cipherText = cipher.doFinal(input.getBytes());
            return Base64.getEncoder().encodeToString(cipherText);
    }

    public String decrypt(String cipherText) throws InvalidAlgorithmParameterException,
                                                    InvalidKeyException,
                                                    NoSuchPaddingException,
                                                    NoSuchAlgorithmException,
                                                    IllegalBlockSizeException,
                                                    BadPaddingException {
        Cipher cipher = Cipher.getInstance(algorithm);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
        return new String(plainText);
    }
}
