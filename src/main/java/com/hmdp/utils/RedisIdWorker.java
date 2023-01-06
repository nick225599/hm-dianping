package com.hmdp.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdWorker {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public long nextId(String categoryOfId){
        // 1. 计算时间戳
        // 一个开始时间戳
        LocalDateTime startTime = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        long startTimestamp = startTime.toEpochSecond(ZoneOffset.UTC);
        // 一个当前时间戳
        LocalDateTime currentTime = LocalDateTime.now();
        long currentTimestamp = currentTime.toEpochSecond(ZoneOffset.UTC);
        // 时间戳差异
        long timestamp = currentTimestamp - startTimestamp;

        // 2. 计算 redis 全局递增 id
        String currentDateStr = currentTime.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String redisIdKey = "irc:keys:" + categoryOfId + ":" + currentDateStr;
        Long redisIdValue = stringRedisTemplate.opsForValue().increment(redisIdKey);
        if(null == redisIdValue
//                || redisIdValue > 0xFFFFFFFF
//                每秒 id 创建次数不能超过 2^32 次方，
//                4,294,967,296，每秒 42w 单，预期不会出现这么高的并发量，淘宝双十一峰值每秒 58 万
        ){
            throw new RuntimeException("redis id is null or invalid");
        }

        // 3. 拼接成全局 id
        return timestamp << 32 // 左移 32 位，
                // long 一共 64 位，左移 32 位则会丢弃原有的 32 位高位
                // 并将低位的 32 位左移
                |
                (
                         0x0000FFFF &
                        redisIdValue); // 或运算拼接上 < 0xFFFFFFFF 的自增 long
    }
}
