package com.juvenxu.mvnbook.account.email;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

import jakarta.mail.Message;

public class AccountEmailServiceTest
{
    private GreenMail greenMail;

    @BeforeEach
    public void startMailServer()
        throws Exception
    {
        ServerSetup setup = new ServerSetup(3025, "127.0.0.1", ServerSetup.PROTOCOL_SMTP);
        setup.setServerStartupTimeout(5000);
        greenMail = new GreenMail(setup);
        greenMail.setUser("test@juvenxu.com", "123456");
        greenMail.start();
    }

    @Test
    public void testSendMail()
        throws Exception
    {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext( "account-email.xml" );
        try {
            AccountEmailService accountEmailService = (AccountEmailService) ctx.getBean( "accountEmailService" );

            String subject = "Test Subject";
            String htmlText = "<h3>Test</h3>";
            accountEmailService.sendMail( "test2@juvenxu.com", subject, htmlText );

            greenMail.waitForIncomingEmail( 2000, 1 );

            Message[] msgs = greenMail.getReceivedMessages();
            assertEquals( 1, msgs.length );
            assertEquals( subject, msgs[0].getSubject() );
            assertEquals( htmlText, GreenMailUtil.getBody( msgs[0] ).trim() );
        } finally {
            ctx.close();
        }
    }

    @AfterEach
    public void stopMailServer()
        throws Exception
    {
        greenMail.stop();
    }
}
