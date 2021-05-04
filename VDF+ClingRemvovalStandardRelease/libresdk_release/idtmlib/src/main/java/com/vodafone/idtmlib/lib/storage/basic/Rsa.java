package com.vodafone.idtmlib.lib.storage.basic;

import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

public class Rsa {
    private static final String DELIMITER = "]";
    public static final String KEY_GENERATOR_ALGORITHM = "RSA";

    private Rsa() {}

    public static String getBase64Exponent(RSAPublicKey rsaPublicKey) {
        return BaseEncoding.base64Url().encode(rsaPublicKey.getPublicExponent().toByteArray());
    }

    public static String getBase64Modulus(RSAPublicKey rsaPublicKey) {
        return BaseEncoding.base64Url().encode(rsaPublicKey.getModulus().toByteArray());
    }

    public static String getKeyId(RSAPublicKey publicKey) {
        return Hashing.sha1().hashBytes(publicKey.getEncoded()).toString();
    }

    public static String toString(RSAPublicKey rsaPublicKey) {
        return getBase64Modulus(rsaPublicKey) + DELIMITER + getBase64Exponent(rsaPublicKey);
    }

    public static String toString(String modulus, String exponent) {
        return modulus + DELIMITER + exponent;
    }

    public static RSAPublicKey create(String fromRsaKeyString) throws RsaException, IllegalArgumentException {
        String[] pubKeyParts = fromRsaKeyString.split(DELIMITER);
        if (pubKeyParts.length > 2 || !BaseEncoding.base64Url().canDecode(pubKeyParts[0]) || !BaseEncoding.base64Url().canDecode(pubKeyParts[1])) {
            throw new IllegalArgumentException("Malformed RSA key string");
        }
        return create(pubKeyParts[0], pubKeyParts[1]);
    }

    public static RSAPublicKey create(String modulus, String exponent) throws RsaException {
        try {
            KeyFactory factory = KeyFactory.getInstance(KEY_GENERATOR_ALGORITHM);
            BigInteger modBigInt = new BigInteger(1, BaseEncoding.base64Url().decode(modulus));
            BigInteger expBigInt = new BigInteger(1, BaseEncoding.base64Url().decode(exponent));
            RSAPublicKeySpec spec = new RSAPublicKeySpec(modBigInt, expBigInt);
            return (RSAPublicKey) factory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RsaException(e);
        }
    }
}
