package com.hmdp.service.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SeckillVoucherServiceImplTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testString() {
        String str1 = new String("hello world");
        String str2 = new String("hello " + "world");
        System.out.println(str1 == str2); // false
        System.out.println(str1.equals(str2)); // true
        System.out.println();

        String str3 = "hello world";
        String str4 = "hello world";
        System.out.println(str3 == str4); // true
        System.out.println(str3.equals(str4)); // true
        System.out.println();

        String str5 = new String("hello world");
        String str6 = new String("hello world");
        System.out.println(str5 == str6); // false
        System.out.println(str5.equals(str6)); // true
        System.out.println();

        String str7 = new String("hello world").intern();
        String str8 = new String("hello world").intern();
        System.out.println(str7 == str8); // true
        System.out.println(str7.equals(str8)); // true
    }
}