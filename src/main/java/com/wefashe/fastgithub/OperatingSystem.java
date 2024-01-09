package com.wefashe.fastgithub;

import java.io.File;

public class OperatingSystem {

    /**
     * 判断是否 Linux 系统
     * @return
     */
    public static boolean isLinux(){
        String osName = System.getProperty("os.name");
        return osName != null && osName.startsWith("Linux");
    }

    /**
     * 判断是否 Windows 系统
     * @return
     */
    public static boolean isWindows(){
        String osName = System.getProperty("os.name");
        return osName != null && osName.startsWith("Windows");
    }

    /**
     * 判断是否 Mac 系统
     * @return
     */
    public static boolean isMacOS(){
        String osName = System.getProperty("os.name");
        return osName != null && osName.startsWith("Mac");
    }

    /**
     * 获取系统路径
     * @return
     */
    public static String getSystemPath(){
        String userDir = System.getProperty("user.dir");
        return userDir + File.separator;
    }

    /**
     * 获取系统名称
     * @return
     */
    public static String getSystemName(){
        String userDir = System.getProperty("user.dir");
        return userDir.substring(userDir.lastIndexOf(File.separator) + 1);
    }

}
