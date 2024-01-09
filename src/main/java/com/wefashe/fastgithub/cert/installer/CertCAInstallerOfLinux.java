package com.wefashe.fastgithub.cert.installer;

import com.wefashe.fastgithub.OperatingSystem;
import com.wefashe.fastgithub.cert.CertCAInstaller;

public class CertCAInstallerOfLinux implements CertCAInstaller {

    @Override
    public boolean isSupported(String caCertPath) {
        return OperatingSystem.isLinux();
    }

    @Override
    public void install(String caCertPath) {

    }
}
