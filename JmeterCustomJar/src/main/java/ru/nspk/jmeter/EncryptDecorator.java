package ru.nspk.jmeter;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Properties;


public class EncryptDecorator {

    public static JmeterEncrypt decorate() throws NoSuchAlgorithmException, InvalidKeySpecException {
        Properties props = new Properties();
        try(InputStream resourceStream = EncryptDecorator.class.getClassLoader().getResourceAsStream("encrypt.properties")) {
            props.load(resourceStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new JmeterEncrypt(props.get("encrypt.password").toString(), props.getProperty("encrypt.salt"), props.getProperty("encrypt.algorithm"));
    }
}
