package com.juvenxu.mvnbook.account.email;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

import jakarta.mail.Message;

@SpringJUnitConfig(classes = AccountEmailConfig.class)
public class AccountEmailServiceTest {
    private GreenMail greenMail;
    private static int greenmail_port = 2500;
    private static String greenmail_server = "localhost";

    @Autowired
    private AccountEmailService accountEmailService;

    @BeforeEach
    public void startMailServer()
            throws Exception {
        ServerSetup srvsetup = new ServerSetup(greenmail_port, greenmail_server, "smtp");
        greenMail = new GreenMail(srvsetup);
        // greenMail = new GreenMail( ServerSetup.SMTP );
        greenMail.setUser("test@juvenxu.com", "123456");
        greenMail.start();
    }

    @Test
    public void testSendMail()
            throws Exception {
        String subject = "Test Subject";
        String htmlText = "<h3>Test</h3>";
        accountEmailService.sendMail("test2@juvenxu.com", subject, htmlText);

        greenMail.waitForIncomingEmail(2000, 1);

        Message[] msgs = greenMail.getReceivedMessages();
        assertEquals(1, msgs.length);
        assertEquals(subject, msgs[0].getSubject());
        assertEquals(htmlText, GreenMailUtil.getBody(msgs[0]).trim());
    }

    @AfterEach
    public void stopMailServer()
            throws Exception {
        greenMail.stop();
    }
}
