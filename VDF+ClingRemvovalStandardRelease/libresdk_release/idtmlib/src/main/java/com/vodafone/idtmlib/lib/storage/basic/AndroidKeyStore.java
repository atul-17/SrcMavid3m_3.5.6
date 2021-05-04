package com.vodafone.idtmlib.lib.storage.basic;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Date;
import java.util.Enumeration;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

public class AndroidKeyStore {
    private static final String PROVIDER = "AndroidKeyStore";
    private static final String RSA_ALGORITHM = "RSA";
    private static final String AES_ALGORITHM = "AES";

    private AndroidKeyStore() {
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static KeyPair generateRsa(String alias, boolean overwrite) throws RsaException {
        try {
            KeyPair keyPair = AndroidKeyStore.getRsaKeyPair(alias);
            if (overwrite || keyPair == null) {
                AlgorithmParameterSpec spec = new KeyGenParameterSpec.Builder(
                        alias,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1)
                        .setKeySize(2048)
                        .build();
                KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM, PROVIDER);
                kpGenerator.initialize(spec);
                keyPair = kpGenerator.generateKeyPair();
            }
            return keyPair;
        } catch (Exception e) {
            throw new RsaException(e);
        }
    }

    public static KeyPair generateRsa(Context context, String alias, boolean overwrite) throws RsaException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return generateRsa(alias, overwrite);
        } else {
            try {
                KeyPair keyPair = AndroidKeyStore.getRsaKeyPair(alias);
                if (overwrite || keyPair == null) {
                    AlgorithmParameterSpec spec = new KeyPairGeneratorSpec.Builder(context)
                            .setAlias(alias)
                            .setSubject(new X500Principal(String.format("CN=%s, OU=%s", alias,
                                    context.getPackageName())))
                            .setSerialNumber(BigInteger.ONE)
                            .setStartDate(new Date(0))
                            .setEndDate(new Date(Integer.MAX_VALUE))
                            .build();
                    KeyPairGenerator kpGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM, PROVIDER);
                    kpGenerator.initialize(spec);
                    keyPair = kpGenerator.generateKeyPair();
                }
                return keyPair;
            } catch (Exception e) {
                throw new RsaException(e);
            }
        }
    }

    public static KeyPair getRsaKeyPair(String alias) throws RsaException {
        try {
            KeyStore keyStore = KeyStore.getInstance(PROVIDER);
            if (keyStore != null) {
                keyStore.load(null);
                KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry)
                        keyStore.getEntry(alias, null);
                if (keyEntry == null) {
                    return null;
                } else {
                    if(keyEntry.getCertificate() == null || keyEntry.getCertificate().getPublicKey() == null || keyEntry.getPrivateKey() == null)
                    {
                        return null;
                    }
                    return new KeyPair(keyEntry.getCertificate().getPublicKey(), keyEntry.getPrivateKey());
                }
            } else {
                return null;
            }

        } catch (Exception e) {
            throw new RsaException(e);
        }
    }

    public static RSAPublicKey getRsaPublicKey(String alias) throws RsaException {
        try {
            KeyStore keyStore = KeyStore.getInstance(PROVIDER);
            if (keyStore != null) {
                keyStore.load(null);
                KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
                return (RSAPublicKey) keyEntry.getCertificate().getPublicKey();
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RsaException(e);
        }
    }

    public static PrivateKey getRsaPrivateKey(String alias) throws RsaException {
        try {
            KeyStore keyStore = KeyStore.getInstance(PROVIDER);
            if (keyStore != null) {
                keyStore.load(null);
                KeyStore.PrivateKeyEntry keyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
                return keyEntry.getPrivateKey();
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new RsaException(e);
        }
    }

    public static SecretKey generateAes(String alias, boolean overwrite) throws AesException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                SecretKey secretKey = getAesKey(alias);
                if (overwrite || secretKey == null) {
                    AlgorithmParameterSpec spec = new KeyGenParameterSpec.Builder(
                            alias, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                            .build();
                    KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM, PROVIDER);
                    keyGenerator.init(spec);
                    secretKey = keyGenerator.generateKey();
                }
                return secretKey;
            } catch (Exception e) {
                throw new AesException(e);
            }
        } else {
            return null;
        }
    }

    public static SecretKey getAesKey(String alias) throws AesException {
        try {
            KeyStore keyStore = KeyStore.getInstance(PROVIDER);
            if (keyStore != null) {
                keyStore.load(null);
                return (SecretKey) keyStore.getKey(alias, null);
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new AesException(e);
        }
    }

    public static void delete(String... aliases) throws KeyStoreException {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(PROVIDER);
            if (keyStore != null) {
                keyStore.load(null);
                for (String alias : aliases) {
                    if (keyStore.containsAlias(alias)) {
                        keyStore.deleteEntry(alias);
                    }
                }
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new KeyStoreException(e);
        }
    }

    public static void deleteAllKeystoreKeys() throws KeyStoreException {
        KeyStore keyStore;
        try {
            keyStore = KeyStore.getInstance(PROVIDER);
            if (keyStore != null) {
                keyStore.load(null);
                Enumeration<String> aliases = keyStore.aliases();
                while (aliases.hasMoreElements()) {
                    keyStore.deleteEntry(aliases.nextElement());
                }
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            throw new KeyStoreException(e);
        }
    }
}
