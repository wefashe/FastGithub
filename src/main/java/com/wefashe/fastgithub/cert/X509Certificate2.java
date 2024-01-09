package com.wefashe.fastgithub.cert;

import lombok.Data;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

@Data
public class X509Certificate2 {

    /**
     * CA证书
     */
    private X509Certificate x509Certificate;

    /**
     * 证书加密私钥
     */
    private PrivateKey privateKey;

    public X509Certificate2(X509Certificate x509Certificate, PrivateKey privateKey){
        this.x509Certificate = x509Certificate;
        this.privateKey = privateKey;
    }
}
