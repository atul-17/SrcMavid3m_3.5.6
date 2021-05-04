package com.vodafone.idtmlib.lib.network.models;


import com.vodafone.idtmlib.lib.storage.basic.Rsa;

import java.security.interfaces.RSAPublicKey;

public class PublicKey {
    private final static String USE = "enc";
    private final static String ALG = "RS512";

    private String kty;
    private String e;
    private String use;
    private String kid;
    private String alg;
    private String n;

    public PublicKey(RSAPublicKey publicKey) {
        this(publicKey.getAlgorithm(), Rsa.getBase64Exponent(publicKey), USE, Rsa.getKeyId(publicKey), ALG, Rsa.getBase64Modulus(publicKey));
    }

    public PublicKey(String kty, String e, String use, String kid, String alg, String n) {
        this.kty = kty;
        this.e = e;
        this.use = use;
        this.kid = kid;
        this.alg = alg;
        this.n = n;
    }

    public String getKty() {
        return kty;
    }

    public void setKty(String kty) {
        this.kty = kty;
    }

    public String getE() {
        return e;
    }

    public void setE(String e) {
        this.e = e;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    public String getAlg() {
        return alg;
    }

    public void setAlg(String alg) {
        this.alg = alg;
    }

    public String getN() {
        return n;
    }

    public void setN(String n) {
        this.n = n;
    }
}
