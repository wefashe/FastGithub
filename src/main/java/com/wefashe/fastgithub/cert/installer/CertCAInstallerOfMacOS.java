package com.wefashe.fastgithub.cert.installer;

import com.wefashe.fastgithub.OperatingSystem;
import com.wefashe.fastgithub.cert.CertCAInstaller;

public class CertCAInstallerOfMacOS implements CertCAInstaller {

    @Override
    public boolean isSupported(String caCertPath) {
        return OperatingSystem.isMacOS();
    }

    @Override
    public void install(String caCertPath) {

    }
}
