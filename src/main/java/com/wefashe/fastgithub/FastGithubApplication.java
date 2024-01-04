package com.wefashe.fastgithub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.net.InetAddress;

@Slf4j
@SpringBootApplication
public class FastGithubApplication {

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext context = SpringApplication.run(FastGithubApplication.class, args);
        ConfigurableEnvironment environment = context.getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = environment.getProperty("server.port");
        String path = environment.getProperty("server.servlet.context-path");
        path = path == null ? "" : path;
        log.debug("\n\t" +
                "----------------------------------------------------------\n\t" +
                "Application FastGithub is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + port + path + "/\n\t" +
                "External: \thttp://" + ip + ":" + port + path + "/\n\t" +
                "------------------------------------------------------------");
    }

}
