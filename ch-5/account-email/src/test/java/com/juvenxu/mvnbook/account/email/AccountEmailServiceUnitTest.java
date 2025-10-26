package com.juvenxu.mvnbook.account.email;

// ...existing imports...
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.internet.MimeMessage;

public class AccountEmailServiceUnitTest {

    private AccountEmailServiceImpl service;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage mimeMessage;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AccountEmailServiceImpl();
        service.setJavaMailSender(javaMailSender);
        service.setSystemEmail("system@juvenxu.com");
    }

    @Test
    public void testSendMailSuccess() throws Exception {
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        service.sendMail("to@domain.com", "subj", "<p>hi</p>");

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    public void testSendMailPropagatesMailSendException() throws Exception {
        // simulate send() failing with an unchecked MailSendException -> should propagate
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new org.springframework.mail.MailSendException("boom")).when(javaMailSender).send(mimeMessage);

        assertThrows(org.springframework.mail.MailSendException.class,
                () -> service.sendMail("to@domain.com", "subj", "<p>hi</p>"));
    }
}
