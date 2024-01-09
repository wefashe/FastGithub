package com.wefashe.fastgithub.cert;

/**
 * CA证书安装器
 */
public interface CertCAInstaller {

    /**
     * 是否支持
     * @param caCertPath 证书路径
     * @return
     */
    boolean isSupported(String caCertPath);

    /**
     * 安装CA证书
     * @param caCertPath 证书路径
     */
    void install(String caCertPath);
}
