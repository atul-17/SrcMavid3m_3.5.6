package com.vodafone.idtmlib.lib.network;

import android.text.TextUtils;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.DirectDecrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;

import javax.crypto.SecretKey;

public class JwtHelper {
    private static final long JWT_EXPIRE_TIME_INTERVAL = 86400000L; // 1 day

    private static final JWEAlgorithm RSA_JWE_ALGORITHM = JWEAlgorithm.RSA1_5;
    private static final EncryptionMethod RSA_JWE_METHOD = EncryptionMethod.A128CBC_HS256;

    private static final JWSAlgorithm AES_JWS_ALGORITHM = JWSAlgorithm.HS256;
    private static final JWEAlgorithm AES_JWE_ALGORITHM = JWEAlgorithm.DIR;
    private static final EncryptionMethod AES_JWE_ENCRYPTION = EncryptionMethod.A128CBC_HS256;//A256GCM)

    private JwtHelper() {}

    public static String getRsaJweAlgorithm() {
        return RSA_JWE_ALGORITHM.toString();
    }

    public static String getRsaJweMethod() {
        return RSA_JWE_METHOD.toString();
    }

    public static EncryptedJWT rsaEncrypt(RSAPublicKey serverPublicKey, String msg) throws JOSEException {
        JWTClaimsSet.Builder jwtClaimsBuilder = new JWTClaimsSet.Builder()
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + JWT_EXPIRE_TIME_INTERVAL))
                .claim("msg", msg);
        JWEHeader header = new JWEHeader(RSA_JWE_ALGORITHM, RSA_JWE_METHOD);
        EncryptedJWT jwt = new EncryptedJWT(header, jwtClaimsBuilder.build());
        jwt.encrypt(new RSAEncrypter(serverPublicKey));
        return jwt;
    }

    public static EncryptedJWT rsaDecrypt(PrivateKey privateKey, String jwtString) throws ParseException, JOSEException {
        EncryptedJWT jwt = EncryptedJWT.parse(jwtString);
        RSADecrypter decrypter = new RSADecrypter(privateKey);
        jwt.decrypt(decrypter);
        return jwt;
    }

    public static String aesEncrypt(SecretKey aesKey, String msg) throws JOSEException {
        JWTClaimsSet.Builder jwtClaimsBuilder = new JWTClaimsSet.Builder()
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + JWT_EXPIRE_TIME_INTERVAL))
                .claim("msg", msg);
        JWSHeader header = new JWSHeader(AES_JWS_ALGORITHM);
        SignedJWT signedJWT = new SignedJWT(header, jwtClaimsBuilder.build());
        JWSSigner signer = new MACSigner(aesKey.getEncoded());
        signedJWT.sign(signer);
        JWEObject jwe = new JWEObject(new JWEHeader.Builder(AES_JWE_ALGORITHM, AES_JWE_ENCRYPTION)
                .contentType("JWT")
                .build(),
                new Payload(signedJWT));
        jwe.encrypt(new DirectEncrypter(aesKey.getEncoded()));
        return jwe.serialize();
    }

    public static String aesDecrypt(SecretKey secretKey, String jwtString) throws JOSEException, ParseException {
        JWEObject jweObject = JWEObject.parse(jwtString);
        jweObject.decrypt(new DirectDecrypter(secretKey.getEncoded()));
        SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
        if(signedJWT == null) {
            return null;
        }
        if(!signedJWT.verify(new MACVerifier(secretKey.getEncoded()))) {
            return null;
        }
        return getJwtMsg(signedJWT);
    }

    public static String getJwtMsg(JWT jwt) throws JOSEException {
        try {
            JWTClaimsSet jwtClaimsSet = jwt.getJWTClaimsSet();
            if (jwtClaimsSet != null) {
                Date currentDate = new Date(System.currentTimeMillis());
                // TODO: not checking IAT or EXP because the server time is not reliable
                Date issuedAtDate = null; // jwtClaimsSet.getIssueTime();
                Date expiresAtDate = null; // jwtClaimsSet.getExpirationTime();
                String msg = jwtClaimsSet.getStringClaim("msg");
                // checking if the current date is between the "issued at" and the "expires at" date
                // if one value is missing, the corresponding checks is considered valid
                if ((issuedAtDate == null || currentDate.after(issuedAtDate)) &&
                        (expiresAtDate == null || currentDate.before(expiresAtDate)) &&
                        !TextUtils.isEmpty(msg)) {
                    return msg;
                }
            }
        } catch (ParseException e) {

        }
        throw new JOSEException("JWT not valid");
    }
}
