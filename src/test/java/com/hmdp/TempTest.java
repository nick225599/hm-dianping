package com.hmdp;

import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TempTest {

    @Test
    public void test() throws InterruptedException {
        for(int i = 0; i < 10; i++){
            TimeUnit.SECONDS.sleep(3);
            System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:ss:mm")) +"_"+ i);
        }
        ExecutorService es = Executors.newFixedThreadPool(100);
        for (long l1 = 0; l1 < Long.MAX_VALUE; l1++) {
            for (long l2 = 0; l2 < Long.MAX_VALUE; l2++) {
                for (long l3 = 0; l3 < Long.MAX_VALUE; l3++) {
                    long finalL1 = l1;
                    long finalL2 = l2;
                    long finalL3 = l3;
                    es.submit(() -> {
                        String temp = finalL1 + "_" + finalL2 + "-" + finalL3;
                        temp.intern();
                    });

                }
            }
        }
        System.out.println("正常结束");
        es.shutdown();
    }

}
