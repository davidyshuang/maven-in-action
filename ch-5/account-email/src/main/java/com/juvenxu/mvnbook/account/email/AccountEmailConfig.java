package com.juvenxu.mvnbook.account.email;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
@PropertySource("classpath:service.properties")
public class AccountEmailConfig {

    @Value("${email.protocol}")
    private String protocol;

    @Value("${email.host}")
    private String host;

    @Value("${email.port}")
    private int port;

    @Value("${email.username}")
    private String username;

    @Value("${email.password}")
    private String password;

    @Value("${email.auth}")
    private String auth;

    @Value("${email.systemEmail}")
    private String systemEmail;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setProtocol(protocol);
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail." + protocol + ".auth", auth);

        return mailSender;
    }

    @Bean
    public AccountEmailService accountEmailService() {
        AccountEmailServiceImpl service = new AccountEmailServiceImpl();
        service.setJavaMailSender(javaMailSender());
        service.setSystemEmail(systemEmail);
        return service;
    }
}
