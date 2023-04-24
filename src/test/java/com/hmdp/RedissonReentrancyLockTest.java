package com.hmdp;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

public class RedissonReentrancyLockTest {
static RedissonClient client;
    public static void main(String[] args) throws InterruptedException {
//        System.out.println(Thread.currentThread().getId());
        RLock redisLock = client.getLock("lockId");
        redisLock.tryLock(1, TimeUnit.SECONDS);
    }
}
