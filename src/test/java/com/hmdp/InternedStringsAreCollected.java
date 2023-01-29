package com.hmdp;

import java.util.concurrent.TimeUnit;

public class InternedStringsAreCollected {
    static String temp = null;

    /**
     * 模拟不出来 string pool 被回收
     */
    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            foo();
            System.gc();
        }
    }

    private static void foo() throws InterruptedException {
        char[] tc = new char[10];
        for (int i = 0; i < tc.length; i++)
            tc[i] = (char) (i * 136757);
        String s = new String(tc).intern();
        if (null == temp) {
            temp = s;
        }

        System.out.println("System.identityHashCode(s): " + System.identityHashCode(s));
        System.out.println("temp == s:                  " + (temp == s));
        System.out.println("temp.equals(s):             " + (temp.equals(s)));
        System.out.println();
        TimeUnit.SECONDS.sleep(1);
    }
}
