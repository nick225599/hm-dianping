package com.hmdp.utils;

import lombok.Data;

import java.util.Date;

@Data
public class TmpObjectHolder {
    Date expiredTime;

    String jsonObject;

   public boolean isExpired(){
        return null == expiredTime || new Date().after(expiredTime);
    }
}
