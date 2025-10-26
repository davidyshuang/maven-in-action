package com.juvenxu.mvnbook.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.juvenxu.mvnbook.account.captcha.AccountCaptchaService;
import com.juvenxu.mvnbook.account.email.AccountEmailException;
import com.juvenxu.mvnbook.account.persist.AccountPersistException;
import com.juvenxu.mvnbook.account.persist.AccountPersistService;

import jakarta.mail.Message;

/**
 * 账户服务测试类
 * 使用JUnit 5、Mockito和AssertJ进行测试
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountServiceTest {
    private static GreenMail greenMail;
    private static int greenmailPort = 2500;
    private static String greenmailServer = "localhost";

    private static AccountService accountService;
    private static ApplicationContext ctx;
    private static AccountCaptchaService accountCaptchaService;
    private static AccountPersistService accountPersistService;

    @BeforeAll
    static void setUp() throws Exception {
        String[] springConfigFiles = {
                "account-email.xml",
                "account-persist.xml",
                "account-captcha.xml",
                "account-service.xml" };

        ctx = new ClassPathXmlApplicationContext(springConfigFiles);
        accountService = (AccountService) ctx.getBean("accountService");
        accountCaptchaService = (AccountCaptchaService) ctx.getBean("accountCaptchaService");
        accountPersistService = (AccountPersistService) ctx.getBean("accountPersistService");

        // 设置预定义的验证码文本
        List<String> preDefinedTexts = new ArrayList<>();
        preDefinedTexts.add("12345");
        preDefinedTexts.add("abcde");
        accountCaptchaService.setPreDefinedTexts(preDefinedTexts);

        // 启动测试邮件服务器
        ServerSetup srvSetup = new ServerSetup(greenmailPort, greenmailServer, "smtp");
        greenMail = new GreenMail(srvSetup);
        greenMail.setUser("test@juvenxu.com", "123456");
        greenMail.start();

        // 清理测试数据文件
        File persistDataFile = new File("target/test-classes/persist-data.xml");
        if (persistDataFile.exists()) {
            persistDataFile.delete();
        }
    }

    @AfterAll
    static void tearDown() throws Exception {
        // 停止测试邮件服务器
        if (greenMail != null) {
            greenMail.stop();
        }

        // 关闭Spring上下文
        if (ctx instanceof ClassPathXmlApplicationContext) {
            ((ClassPathXmlApplicationContext) ctx).close();
        }
    }

    @BeforeEach
    void init() {
        // 每个测试方法执行前的初始化工作
    }

    @Test
    @Order(1)
    @DisplayName("测试生成验证码")
    void testGenerateCaptcha() {
        // 测试生成验证码
        try {
            String captchaKey = accountService.generateCaptchaKey();
            assertThat(captchaKey).isNotEmpty();

            byte[] captchaImage = accountService.generateCaptchaImage(captchaKey);
            assertThat(captchaImage).isNotEmpty();
        } catch (AccountServiceException e) {
            Assertions.fail("生成验证码时发生异常: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("测试账户注册流程")
    void testSignUpFlow() throws Exception {
        // 1. 获取验证码
        String captchaKey;
        try {
            captchaKey = accountService.generateCaptchaKey();
            accountService.generateCaptchaImage(captchaKey);
        } catch (AccountServiceException e) {
            Assertions.fail("生成验证码时发生异常: " + e.getMessage());
            return;
        }
        String captchaValue = "12345";

        // 2. 提交注册请求
        SignUpRequest signUpRequest = createSignUpRequest(captchaKey, captchaValue);
        try {
            accountService.signUp(signUpRequest);
        } catch (AccountServiceException e) {
            Assertions.fail("注册账户时发生异常: " + e.getMessage());
            return;
        }

        // 3. 检查激活邮件
        greenMail.waitForIncomingEmail(2000, 1);
        Message[] messages = greenMail.getReceivedMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages[0].getSubject()).isEqualTo("Please Activate Your Account");
        String activationLink = GreenMailUtil.getBody(messages[0]).trim();

        // 3a. 尝试登录但未激活
        assertThatThrownBy(() -> accountService.login("juven", "admin123"))
            .isInstanceOf(AccountServiceException.class)
            .hasMessageContaining("disabled");

        // 4. 激活账户
        String activationCode = activationLink.substring(activationLink.lastIndexOf("=") + 1);
        try {
            accountService.activate(activationCode);
        } catch (AccountServiceException e) {
            Assertions.fail("激活账户时发生异常: " + e.getMessage());
            return;
        }

        // 5. 使用正确的ID和密码登录
        try {
            accountService.login("juven", "admin123");
        } catch (AccountServiceException e) {
            Assertions.fail("登录账户时发生异常: " + e.getMessage());
            return;
        }

        // 5a. 使用错误的密码登录
        assertThatThrownBy(() -> accountService.login("juven", "admin456"))
            .isInstanceOf(AccountServiceException.class)
            .hasMessageContaining("Incorrect password");
    }

    @Test
    @Order(3)
    @DisplayName("测试验证码错误情况")
    void testInvalidCaptcha() {
        // 1. 获取验证码
        String captchaKey;
        try {
            captchaKey = accountService.generateCaptchaKey();
            accountService.generateCaptchaImage(captchaKey);
        } catch (AccountServiceException e) {
            Assertions.fail("生成验证码时发生异常: " + e.getMessage());
            return;
        }
        String captchaValue = "wrongcaptcha"; // 使用错误的验证码

        // 2. 提交注册请求
        SignUpRequest signUpRequest = createSignUpRequest(captchaKey, captchaValue);

        // 验证会抛出异常
        assertThatThrownBy(() -> accountService.signUp(signUpRequest))
            .isInstanceOf(AccountServiceException.class)
            .hasMessageContaining("Incorrect Captcha");
    }

    @Test
    @Order(4)
    @DisplayName("测试密码不匹配情况")
    void testPasswordMismatch() {
        // 1. 获取验证码
        String captchaKey;
        try {
            captchaKey = accountService.generateCaptchaKey();
            accountService.generateCaptchaImage(captchaKey);
        } catch (AccountServiceException e) {
            Assertions.fail("生成验证码时发生异常: " + e.getMessage());
            return;
        }
        String captchaValue = "12345";

        // 2. 创建密码不匹配的注册请求
        SignUpRequest signUpRequest = createSignUpRequest(captchaKey, captchaValue);
        signUpRequest.setPassword("admin123");
        signUpRequest.setConfirmPassword("different123"); // 密码不匹配

        // 验证会抛出异常
        assertThatThrownBy(() -> accountService.signUp(signUpRequest))
            .isInstanceOf(AccountServiceException.class)
            .hasMessageContaining("do not match");
    }

    @Test
    @Order(5)
    @DisplayName("测试持久层异常情况")
    void testPersistException() throws AccountPersistException {
        // 使用Mockito模拟持久层异常
        doThrow(new AccountPersistException("Mocked persist exception"))
            .when(accountPersistService).createAccount(any());

        // 1. 获取验证码
        String captchaKey;
        try {
            captchaKey = accountService.generateCaptchaKey();
            accountService.generateCaptchaImage(captchaKey);
        } catch (AccountServiceException e) {
            Assertions.fail("生成验证码时发生异常: " + e.getMessage());
            return;
        }
        String captchaValue = "12345";

        // 2. 提交注册请求
        SignUpRequest signUpRequest = createSignUpRequest(captchaKey, captchaValue);

        // 验证会抛出异常
        assertThatThrownBy(() -> accountService.signUp(signUpRequest))
            .isInstanceOf(AccountServiceException.class)
            .hasMessageContaining("Unable to create account");
    }

    @Test
    @Order(6)
    @DisplayName("测试邮件服务异常情况")
    void testEmailException() throws AccountPersistException {
        // 使用Mockito模拟邮件服务异常
        doThrow(new AccountEmailException("Mocked email exception"))
            .when(accountPersistService).createAccount(any());

        // 1. 获取验证码
        String captchaKey;
        try {
            captchaKey = accountService.generateCaptchaKey();
            accountService.generateCaptchaImage(captchaKey);
        } catch (AccountServiceException e) {
            Assertions.fail("生成验证码时发生异常: " + e.getMessage());
            return;
        }
        String captchaValue = "12345";

        // 2. 提交注册请求
        SignUpRequest signUpRequest = createSignUpRequest(captchaKey, captchaValue);

        // 验证会抛出异常
        assertThatThrownBy(() -> accountService.signUp(signUpRequest))
            .isInstanceOf(AccountServiceException.class)
            .hasMessageContaining("Unable to create account");
    }

    @Test
    @Order(7)
    @DisplayName("测试无效激活码")
    void testInvalidActivationCode() {
        // 尝试使用无效的激活码
        assertThatThrownBy(() -> accountService.activate("invalid-code"))
            .isInstanceOf(AccountServiceException.class)
            .hasMessageContaining("Invalid account activation ID");
    }

    /**
     * 创建注册请求的辅助方法
     */
    private SignUpRequest createSignUpRequest(String captchaKey, String captchaValue) {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setCaptchaKey(captchaKey);
        signUpRequest.setCaptchaValue(captchaValue);
        signUpRequest.setId("juven");
        signUpRequest.setEmail("test@juvenxu.com");
        signUpRequest.setName("Juven Xu");
        signUpRequest.setPassword("admin123");
        signUpRequest.setConfirmPassword("admin123");
        signUpRequest.setActivateServiceUrl("http://localhost:8080/account/activate");
        return signUpRequest;
    }
}
