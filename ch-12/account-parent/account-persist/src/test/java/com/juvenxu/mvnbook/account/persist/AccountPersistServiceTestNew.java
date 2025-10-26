package com.juvenxu.mvnbook.account.persist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * 账户持久化服务测试类
 * 使用JUnit 5、AssertJ进行测试
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AccountPersistServiceTestNew {
    private AccountPersistService service;
    private static final String TEST_ACCOUNT_ID = "juven";
    private static final String TEST_ACCOUNT_NAME = "Juven Xu";
    private static final String TEST_ACCOUNT_EMAIL = "juven@changeme.com";
    private static final String TEST_ACCOUNT_PASSWORD = "this_should_be_encrypted";
    private static final boolean TEST_ACCOUNT_ACTIVATED = true;

    @BeforeEach
    void setUp() throws Exception {
        // 清理测试数据文件
        File persistDataFile = new File("target/test-classes/persist-data.xml");
        if (persistDataFile.exists()) {
            persistDataFile.delete();
        }

        // 初始化Spring上下文
        ApplicationContext ctx = new ClassPathXmlApplicationContext("account-persist.xml");
        service = (AccountPersistService) ctx.getBean("accountPersistService");

        // 创建测试账户
        createTestAccount(TEST_ACCOUNT_ID);
    }

    @AfterEach
    void tearDown() throws Exception {
        // 清理测试数据
        if (service.readAccount(TEST_ACCOUNT_ID) != null) {
            service.deleteAccount(TEST_ACCOUNT_ID);
        }
    }

    @Test
    @Order(1)
    @DisplayName("测试读取账户")
    void testReadAccount() throws Exception {
        // 执行测试
        Account account = service.readAccount(TEST_ACCOUNT_ID);

        // 验证结果
        assertAll("账户信息验证",
            () -> assertThat(account).isNotNull(),
            () -> assertThat(account.getId()).isEqualTo(TEST_ACCOUNT_ID),
            () -> assertThat(account.getName()).isEqualTo(TEST_ACCOUNT_NAME),
            () -> assertThat(account.getEmail()).isEqualTo(TEST_ACCOUNT_EMAIL),
            () -> assertThat(account.getPassword()).isEqualTo(TEST_ACCOUNT_PASSWORD),
            () -> assertThat(account.isActivated()).isEqualTo(TEST_ACCOUNT_ACTIVATED)
        );
    }

    @Test
    @Order(2)
    @DisplayName("测试删除账户")
    void testDeleteAccount() throws Exception {
        // 验证账户存在
        assertThat(service.readAccount(TEST_ACCOUNT_ID)).isNotNull();

        // 执行删除操作
        service.deleteAccount(TEST_ACCOUNT_ID);

        // 验证账户已被删除
        assertThat(service.readAccount(TEST_ACCOUNT_ID)).isNull();
    }

    @Test
    @Order(3)
    @DisplayName("测试创建账户")
    void testCreateAccount() throws Exception {
        String newAccountId = "mike";

        // 验证账户不存在
        assertThat(service.readAccount(newAccountId)).isNull();

        // 创建新账户
        Account newAccount = createTestAccount(newAccountId);

        // 验证账户已创建
        Account retrievedAccount = service.readAccount(newAccountId);
        assertThat(retrievedAccount).isNotNull();
        assertThat(retrievedAccount.getId()).isEqualTo(newAccountId);
        assertThat(retrievedAccount.getName()).isEqualTo("Mike");
        assertThat(retrievedAccount.getEmail()).isEqualTo("mike@changeme.com");
        assertThat(retrievedAccount.isActivated()).isTrue();
    }

    @Test
    @Order(4)
    @DisplayName("测试更新账户")
    void testUpdateAccount() throws Exception {
        // 获取要更新的账户
        Account account = service.readAccount(TEST_ACCOUNT_ID);

        // 更新账户信息
        account.setName("Juven Xu 1");
        account.setEmail("juven1@changeme.com");
        account.setPassword("this_still_should_be_encrypted");
        account.setActivated(false);

        // 执行更新操作
        service.updateAccount(account);

        // 验证更新结果
        Account updatedAccount = service.readAccount(TEST_ACCOUNT_ID);
        assertAll("更新后的账户信息验证",
            () -> assertThat(updatedAccount.getName()).isEqualTo("Juven Xu 1"),
            () -> assertThat(updatedAccount.getEmail()).isEqualTo("juven1@changeme.com"),
            () -> assertThat(updatedAccount.getPassword()).isEqualTo("this_still_should_be_encrypted"),
            () -> assertThat(updatedAccount.isActivated()).isFalse()
        );
    }

    @Test
    @Order(5)
    @DisplayName("测试读取不存在的账户")
    void testReadNonExistentAccount() throws Exception {
        // 验证读取不存在的账户返回null
        assertThat(service.readAccount("nonexistent")).isNull();
    }

    @Test
    @Order(6)
    @DisplayName("测试更新不存在的账户")
    void testUpdateNonExistentAccount() throws Exception {
        // 创建一个新账户但不保存
        Account account = new Account();
        account.setId("nonexistent");
        account.setName("Nonexistent");
        account.setEmail("nonexistent@changeme.com");
        account.setPassword("password");
        account.setActivated(true);

        // 验证更新不存在的账户返回null
        assertThat(service.updateAccount(account)).isNull();
    }

    /**
     * 创建测试账户的辅助方法
     */
    private Account createTestAccount(String accountId) throws Exception {
        Account account = new Account();
        account.setId(accountId);
        account.setName(accountId.equals(TEST_ACCOUNT_ID) ? TEST_ACCOUNT_NAME : "Mike");
        account.setEmail(accountId.equals(TEST_ACCOUNT_ID) ? TEST_ACCOUNT_EMAIL : accountId + "@changeme.com");
        account.setPassword(TEST_ACCOUNT_PASSWORD);
        account.setActivated(TEST_ACCOUNT_ACTIVATED);

        service.createAccount(account);
        return account;
    }
}