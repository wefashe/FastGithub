package com.wefashe.fastgithub.cert;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.wefashe.fastgithub.OperatingSystem;
import com.wefashe.fastgithub.cert.installer.CertCAInstallerOfLinux;
import com.wefashe.fastgithub.cert.installer.CertCAInstallerOfMacOS;
import com.wefashe.fastgithub.cert.installer.CertCAInstallerOfWindows;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.openssl.PEMWriter;
import org.bouncycastle.operator.OperatorCreationException;

import java.io.*;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * 证书服务
 */
@Slf4j
public class CertServer {

    private final static String CERT_PATH = "cert";

    private String getCertPath(){
        String systemPath = OperatingSystem.getSystemPath();
        return systemPath + CERT_PATH + File.separator;
    }

    /**
     * 获取证书文件路径
     * @return
     */
    private String getCerCAFilePath(){
        String cerCAFileName = OperatingSystem.isLinux() ? "fastgithub.crt" : "fastgithub.cer";
        return getCertPath() + cerCAFileName;
    }

    /**
     * 获取私钥文件路径
     * @return
     */
    private String getKeyCAFilePath(){
        return getCertPath() + "fastgithub.key";
    }

    /**
     * 生成CA证书
     * @return
     */
    private boolean createCertCAIfNotExists() {

        File cerCAFile = new File(getCerCAFilePath());
        File keyCAFile = new File(getKeyCAFilePath());
        if (cerCAFile.exists() && keyCAFile.exists()) {
            return false;
        }
        cerCAFile.delete();
        keyCAFile.delete();

        Date date = DateUtil.date();
        Date notBefore = DateUtil.offset(date, DateField.DAY_OF_YEAR, -1);
        Date notAfter = DateUtil.offset(date, DateField.YEAR, 10);

        String systemName = OperatingSystem.getSystemName();
        X509Certificate2 certCA = CertGenerator.createCACertificate(systemName, notBefore, notAfter);
        if (Objects.isNull(certCA)) {
            return false;
        }

        File certPath = new File(getCertPath());
        if (!certPath.exists()) {
            certPath.mkdirs();
        }

        try {
            OutputStream outputStream = new FileOutputStream(cerCAFile);
            X509Certificate certificate = certCA.getX509Certificate();
            if (Objects.isNull(certificate)) {
                return false;
            }
            //证书可以二进制形式存入库表，存储字段类型为BLOB
            outputStream.write(certificate.getEncoded());
            outputStream.close();
        } catch (IOException | CertificateEncodingException e) {
            log.error("生成证书文件{}出错！", cerCAFile.getAbsolutePath(), e);
            return false;
        }

        try {
            OutputStream outputStream = new FileOutputStream(keyCAFile);
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            PEMWriter pemWriter = new PEMWriter(writer);
            PrivateKey privateKey = certCA.getPrivateKey();
            if (Objects.isNull(privateKey)) {
                return false;
            }
            pemWriter.writeObject(privateKey);
            pemWriter.close();
            writer.close();
            outputStream.close();
        } catch (IOException e) {
            log.error("生成私钥文件{}出错！", keyCAFile.getAbsolutePath(), e);
        }
        return true;
    }

    /**
     * 安装和信任CA证书
     */
    public void installAndTrustCertCA(){
        List<CertCAInstaller> certInstallers = new ArrayList<>();
        certInstallers.add(new CertCAInstallerOfLinux());
        certInstallers.add(new CertCAInstallerOfWindows());
        certInstallers.add(new CertCAInstallerOfMacOS());

        String cerCAFilePath = getCerCAFilePath();
        Optional<CertCAInstaller> installerOptional = certInstallers.stream()
                .filter(installer -> installer.isSupported(cerCAFilePath))
                .findFirst();
        if (installerOptional.isPresent()) {
            installerOptional.get().install(cerCAFilePath);
        } else {
            log.warn("请根据你的系统平台手动安装和信任CA证书{}", cerCAFilePath);
        }
        checkConfigSSLChannel("schannel");
    }

    /**
     * 设置ssl验证
     */
    public boolean checkConfigSSLChannel(String value){
        try {
            Process process = new ProcessBuilder()
                    .command("git", "config", "--global", "http.sslbackend", value.toLowerCase())
                    .inheritIO()
                    .start();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException | InterruptedException e) {
            log.warn("ssl验证失败", e);
            return false;
        }
    }

    /**
     * 获取颁发给指定域名的证书
     */
    public void getOrCreateServerCert() {

    }

    /**
     * 获取域名
     */
    public void getExtraDomains(){

    }

}
