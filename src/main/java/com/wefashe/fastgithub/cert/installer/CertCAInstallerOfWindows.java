package com.wefashe.fastgithub.cert.installer;

import com.wefashe.fastgithub.OperatingSystem;
import com.wefashe.fastgithub.cert.CertCAInstaller;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

@Slf4j
public class CertCAInstallerOfWindows implements CertCAInstaller {

    @Override
    public boolean isSupported(String caCertPath) {
        return OperatingSystem.isWindows();
    }

    @Override
    public void install(String caCertPath) {
        try {
            // 加载证书库（Java 中称为 KeyStore）
            KeyStore keystore = KeyStore.getInstance("Windows-ROOT");
            keystore.load(null, null);

            FileInputStream inputStream = new FileInputStream(caCertPath);
            Certificate caCert = CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
            inputStream.close();

            // 创建临时列表保存需要删除的证书别名
            List<String> aliasesToRemove = new ArrayList<>();

            // 查找证书库中是否存在与当前要安装的 CA 证书具有相同公钥的证书
            boolean found = false;
            String systemName = OperatingSystem.getSystemName();
            // 枚举证书库中所有别名
            Enumeration<String> aliases = keystore.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                Certificate cert = keystore.getCertificate(alias);
                if (cert instanceof X509Certificate) {
                    if (cert.getPublicKey().equals(caCert.getPublicKey())) {
                        // 如果找到了相同公钥的证书，则标记为已找到，不用重新安装
                        found = true;
                    } else {
                        // 颁布者与使用者相同且都是系统名称
                        Principal subjectDN = ((X509Certificate) cert).getSubjectDN();
                        Principal issuerDN = ((X509Certificate) cert).getIssuerDN();
                        if (alias.equals(systemName) &&
                                subjectDN.getName().equals(issuerDN.getName()) &&
                                subjectDN.getName().contains(systemName)) {
                            // 删除之前旧的的证书
                            aliasesToRemove.add(alias);
                        }
                    }
                }
            }
            // 删除临时列表中保存的证书别名
            for (String certAlias : aliasesToRemove) {
                keystore.deleteEntry(certAlias);
                log.debug("别名 {} CA证书文件删除成功", certAlias);
            }
            // 如果没有找到相同公钥的证书，则将当前 CA 证书添加到证书库中
            if (found) {
                log.debug("CA证书文件 {} 已经安装", caCertPath);
            } else {
                keystore.setCertificateEntry(systemName, caCert);
                // 保存证书库
                keystore.store(null, null);
                log.debug("CA证书文件 {} 安装成功", caCertPath);
            }
        } catch (Exception e) {
            // 如果出现异常，则记录日志
            log.warn("请手动安装CA证书{}到“将所有的证书都放入下列存储”\\“受信任的根证书颁发机构”", caCertPath, e);
        }
    }
}
