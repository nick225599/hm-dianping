package com.hmdp;

import com.hmdp.controller.UserController;
import com.hmdp.utils.RedisIdWorker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import static org.junit.Assert.*;

//TODO scs 这么多乱七八糟的 SpringBoot Test 注解是怎么用的?
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = HmDianPingApplication.class)
@SpringBootTest
public class HmDianPingApplicationTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Resource
    private RedisIdWorker redisIdWorker;

    @Resource
    private UserController userController;

    @Test
    public void nextId() {
        //TODO scs 为啥注入失败？spring boot 咋写测试？
        System.out.println(redisIdWorker.nextId("temp"));
    }
}