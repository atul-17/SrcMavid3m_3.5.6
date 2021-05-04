package com.vodafone.idtmlib.lib.storage.basic;

import com.google.common.base.Charsets;
import com.google.common.io.BaseEncoding;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class Aes {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS7Padding";
    private static final String DELIMITER = "]";
    private static final String PASSWORD_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int PASSWORD_ITERATIONS = 1000; // warning: generating keys takes a lot depending on this value
    private static final int PASSWORD_KEY_LENGTH = 256;

    public static String toString(SecretKey secretKey) {
        return BaseEncoding.base64Url().encode(secretKey.getEncoded());
    }

    public static SecretKey create(String aesKeyString) {
        byte[] dataDecoded = BaseEncoding.base64Url().decode(aesKeyString);
        return new SecretKeySpec(dataDecoded, ALGORITHM);
    }

    public static Cipher getEncryptCipher(SecretKey aesKey) throws AesException {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            return cipher;
        } catch (Exception e) {
            throw new AesException(e);
        }
    }

    public static Cipher getDecryptCipher(String encData, SecretKey aesKey) throws AesException {
        try {
            String[] encDataParts = encData.split(DELIMITER);
            byte[] iv = BaseEncoding.base64Url().decode(encDataParts[0]);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, aesKey, ivParameterSpec);
            return cipher;
        } catch (Exception e) {
            throw new AesException(e);
        }
    }

    /*** OPERATIONS WITH PROVIDED AES KEY ***/

    public static String encrypt(String data, SecretKey aesKey) throws AesException {
        try {
            Cipher cipher = getEncryptCipher(aesKey);
            byte[] encMsg = cipher.doFinal(data.getBytes(Charsets.UTF_8));
            byte[] iv = cipher.getIV();
            StringBuilder encStr = new StringBuilder();
            encStr.append(BaseEncoding.base64Url().encode(iv));
            encStr.append(DELIMITER);
            encStr.append(BaseEncoding.base64Url().encode(encMsg));
            return encStr.toString();
        } catch (Exception e) {
            throw new AesException(e);
        }
    }

    public static String decrypt(String encData, SecretKey aesKey) throws AesException {
        try {
            String[] encDataParts = encData.split(DELIMITER);
            byte[] encMsg = BaseEncoding.base64Url().decode(encDataParts[1]);
            Cipher cipher = getDecryptCipher(encData, aesKey);
            byte[] decMsgBytes = cipher.doFinal(encMsg);
            return new String(decMsgBytes, Charsets.UTF_8);
        } catch (Exception e) {
            throw new AesException(e);
        }
    }

    /*** OPERATIONS WITH PROVIDED CIPHER ***/

    public static String encrypt(String data, Cipher cipher) throws AesException {
        try {
            byte[] encMsg = cipher.doFinal(data.getBytes(Charsets.UTF_8));
            byte[] iv = cipher.getIV();
            StringBuilder encStr = new StringBuilder();
            encStr.append(BaseEncoding.base64Url().encode(iv));
            encStr.append(DELIMITER);
            encStr.append(BaseEncoding.base64Url().encode(encMsg));
            return encStr.toString();
        } catch (Exception e) {
            throw new AesException(e);
        }
    }

    public static String decrypt(String encData, Cipher cipher) throws AesException {
        try {
            String[] encDataParts = encData.split(DELIMITER);
            byte[] encMsg = BaseEncoding.base64Url().decode(encDataParts[1]);
            byte[] decMsgBytes = cipher.doFinal(encMsg);
            return new String(decMsgBytes, Charsets.UTF_8);
        } catch (Exception e) {
            throw new AesException(e);
        }
    }

    /*** OPERATIONS WITH PROVIDED PASSWORD ***/

    public static String encrypt(String data, String password) throws AesException {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[PASSWORD_KEY_LENGTH / 8];
            random.nextBytes(salt);
            SecretKey key = createWithPbkdf2(password, salt);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte[] encMsg = cipher.doFinal(data.getBytes(Charsets.UTF_8));
            byte[] iv = cipher.getIV();
            StringBuilder encStr = new StringBuilder();
            encStr.append(BaseEncoding.base64Url().encode(salt));
            encStr.append(DELIMITER);
            encStr.append(BaseEncoding.base64Url().encode(iv));
            encStr.append(DELIMITER);
            encStr.append(BaseEncoding.base64Url().encode(encMsg));
            return encStr.toString();
        } catch (Exception e) {
            throw new AesException(e);
        }
    }

    public static String decrypt(String encData, String password) throws AesException, WrongPasswordException {
        try {
            String[] encDataParts = encData.split(DELIMITER);
            byte[] salt = BaseEncoding.base64Url().decode(encDataParts[0]);
            byte[] iv = BaseEncoding.base64Url().decode(encDataParts[1]);
            byte[] encMsg = BaseEncoding.base64Url().decode(encDataParts[2]);
            SecretKey key = createWithPbkdf2(password, salt);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            IvParameterSpec ivParams = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, key, ivParams);
            byte[] plaintext = cipher.doFinal(encMsg);
            return new String(plaintext, Charsets.UTF_8);
        } catch (BadPaddingException e) {
            // bad padding usually happens when using a wrong password to decrypt the data
            // hoping this is a good way to determine a wrong pw
            throw new WrongPasswordException();
        } catch (Exception e) {
            throw new AesException(e);
        }
    }

    private static SecretKey createWithPbkdf2(String password, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, PASSWORD_ITERATIONS, PASSWORD_KEY_LENGTH);
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PASSWORD_ALGORITHM);
        byte[] keyBytes = keyFactory.generateSecret(keySpec).getEncoded();
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }
}
