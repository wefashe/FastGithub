package com.wefashe.fastgithub.cert;

import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;


/**
 * 证书生成器
 */
@Slf4j
public class CertGenerator {

    private static final String KEY_ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    static {
        // 系统添加BC加密算法 以后系统中调用的算法都是BC的算法
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 生成CA证书
     *
     * @param subjectName
     * @param notBefore
     * @param notAfter
     * @return
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws CertIOException
     * @throws OperatorCreationException
     */
    public static X509Certificate2 createCACertificate(String subjectName, Date notBefore, Date notAfter) {

        int rsaKeySizeInBits = 2048;
        int pathLengthConstraint = 1;

        try {
            // 使用指定的密钥大小创建RSA对象
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGen.initialize(rsaKeySizeInBits);
            KeyPair keyPair = keyPairGen.generateKeyPair();

            // 创建证书请求
            X500Name subject = new X500Name("CN=" + subjectName);
            SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());
            BigInteger serial = new BigInteger(18 * 4, new SecureRandom()).mod(BigInteger.TEN.pow(18));
            // 使用者和颁发者一致,使用者和颁发者一致
            X509v3CertificateBuilder certBuilder = new X509v3CertificateBuilder(subject, serial, notBefore, notAfter, subject, publicKeyInfo);

            // 添加证书的扩展
            // 基本约束 添加Basic Constraints扩展，指示证书可以用于签名CA证书或TLS服务器证书，并指定路径长度约束
            certBuilder.addExtension(X509Extension.basicConstraints, true, new BasicConstraints(pathLengthConstraint));

            // 密钥用法 添加Key Usage扩展，指示证书可以用于数字签名、CRL签名和签名CA证书
            certBuilder.addExtension(X509Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyCertSign | KeyUsage.cRLSign));

            // 增强型密钥用法 添加Enhanced Key Usage扩展，指示证书可以用于TLS服务器或客户端认证
            certBuilder.addExtension(X509Extension.extendedKeyUsage, true, new ExtendedKeyUsage(new KeyPurposeId[]{KeyPurposeId.id_kp_serverAuth, KeyPurposeId.id_kp_clientAuth}));

            // 使用者可选名称 添加Subject Alternative Name扩展，指示证书可以用于指定的DNS主机名
            certBuilder.addExtension(X509Extension.subjectAlternativeName, false, new GeneralNames(new GeneralName[]{new GeneralName(GeneralName.dNSName, subjectName)}));

            // 使用者密钥标识符 添加Subject Key Identifier扩展，用于标识证书的公钥
            certBuilder.addExtension(X509Extension.subjectKeyIdentifier, false, new JcaX509ExtensionUtils().createSubjectKeyIdentifier(publicKeyInfo));

            // 使用私钥对证书进行签名
            ContentSigner contentSigner = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(keyPair.getPrivate());
            X509Certificate x509Certificate = new JcaX509CertificateConverter().getCertificate(certBuilder.build(contentSigner));

            log.debug("CA证书信息：颁发者 {}，使用者 {}，有效期从 {} 到 {}", subjectName, subjectName, notBefore, notAfter);
            return new X509Certificate2(x509Certificate, keyPair.getPrivate());
        } catch (Exception e) {
            log.error("CA证书生成出错！", e);
        }
        return null;

    }

}
