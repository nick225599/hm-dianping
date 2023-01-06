package com.hmdp;

import com.hmdp.controller.UserController;
import com.hmdp.utils.RedisIdWorker;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

//TODO scs 这么多乱七八糟的 SpringBoot Test 注解是怎么用的?
@Slf4j
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = HmDianPingApplication.class)
@SpringBootTest
public class HmDianPingApplicationTest {

    @Resource
    private RedisIdWorker redisIdWorker;

    @Test
    public void nextId() throws InterruptedException {
        int taskNumber = 200;
        String idType = "idType_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        log.info("idType: {}", idType);
        // 200 个线程各执行 200 次生成 id
        // 1. id 有没有重复
        // 2. 耗时多久？
        Queue<Long> idQueue = new LinkedBlockingQueue<>();
        Set<Long> idSet = new CopyOnWriteArraySet<>();
        ExecutorService es = Executors.newFixedThreadPool(200);
        CountDownLatch countDownLatch = new CountDownLatch(taskNumber);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start("generate id by redis");
        for (int i = 0; i < taskNumber; i++) {
            es.submit(() -> {
                for (int j = 0; j < 200; j++) {
                    long nextId = redisIdWorker.nextId(idType);
                    idSet.add(nextId);
                    idQueue.add(nextId);
                }
                countDownLatch.countDown();
            });
        }
        boolean b = countDownLatch.await(30, TimeUnit.SECONDS);
        stopWatch.stop();
        log.info("200 各任务是否都已执行完？" + b);
        log.info("耗时 ms：" + stopWatch.getLastTaskTimeMillis());
        log.info("id set 总数：" + idSet.size());
        log.info("id queue 总数：" + idQueue.size());
        System.out.println();
    }

    @Test
    public void test() {
        System.out.println("随机id" + redisIdWorker.nextId("coreTransId"));

    }
}