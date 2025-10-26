package com.juvenxu.mvnbook.account.email;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.mail.Message;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

/**
 * 账户邮件服务测试类
 * 使用JUnit 5、AssertJ进行测试
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountEmailServiceTest {
    private static GreenMail greenMail;
    private static int greenmailPort = 2500;
    private static String greenmailServer = "localhost";
    private static final String TEST_EMAIL = "test2@juvenxu.com";
    private static final String TEST_SUBJECT = "Test Subject";
    private static final String TEST_HTML_TEXT = "<h3>Test</h3>";
    private static final String TEST_USER = "test@juvenxu.com";
    private static final String TEST_PASSWORD = "123456";
    private static final String ADMIN_EMAIL = "admin@juvenxu.com";

    @BeforeAll
    static void startMailServer() throws Exception {
        ServerSetup srvSetup = new ServerSetup(greenmailPort, greenmailServer, "smtp");
        greenMail = new GreenMail(srvSetup);
        greenMail.setUser(TEST_USER, TEST_PASSWORD);
        greenMail.start();
    }

    @Test
    @Order(1)
    @DisplayName("测试发送邮件")
    void testSendMail() throws Exception {
        // 初始化Spring上下文
        try (ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("account-email.xml")) {
            AccountEmailService accountEmailService = ctx.getBean(AccountEmailService.class);

            // 发送邮件
            accountEmailService.sendMail(TEST_EMAIL, TEST_SUBJECT, TEST_HTML_TEXT);

            // 等待邮件接收
            greenMail.waitForIncomingEmail(2000, 1);

            // 验证邮件内容
            Message[] messages = greenMail.getReceivedMessages();
            assertThat(messages).hasSize(1);
            assertThat(messages[0].getFrom()[0].toString()).isEqualTo(ADMIN_EMAIL);
            assertThat(messages[0].getSubject()).isEqualTo(TEST_SUBJECT);
            assertThat(GreenMailUtil.getBody(messages[0]).trim()).isEqualTo(TEST_HTML_TEXT);
        }
    }

    @Test
    @Order(2)
    @DisplayName("测试发送多封邮件")
    void testSendMultipleMails() throws Exception {
        // 清理之前的邮件
        greenMail.purgeEmailFromAllMailboxes();
        
        // 初始化Spring上下文
        try (ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("account-email.xml")) {
            AccountEmailService accountEmailService = ctx.getBean(AccountEmailService.class);

            // 发送多封邮件
            accountEmailService.sendMail(TEST_EMAIL, "First Subject", "<h3>First</h3>");
            accountEmailService.sendMail(TEST_EMAIL, "Second Subject", "<h3>Second</h3>");

            // 等待邮件接收
            greenMail.waitForIncomingEmail(2000, 2);

            // 验证邮件数量
            Message[] messages = greenMail.getReceivedMessages();
            assertThat(messages).hasSize(2);
            
            // 验证第一封邮件
            assertThat(messages[0].getSubject()).isEqualTo("First Subject");
            assertThat(GreenMailUtil.getBody(messages[0]).trim()).isEqualTo("<h3>First</h3>");
            
            // 验证第二封邮件
            assertThat(messages[1].getSubject()).isEqualTo("Second Subject");
            assertThat(GreenMailUtil.getBody(messages[1]).trim()).isEqualTo("<h3>Second</h3>");
        }
    }

    @AfterAll
    static void stopMailServer() throws Exception {
        if (greenMail != null) {
            greenMail.stop();
        }
    }
}
