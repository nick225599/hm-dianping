package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.hmdp.utils.TmpObjectHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.time.Duration;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ExecutorService rebuildCacheExecutors;

    @Override
    public Shop getById(Serializable id) {

        // 1. 查缓存
        TmpObjectHolder objHolder = null;
        String shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        if (StringUtils.isNotEmpty(shopJson)) {
            objHolder = JSONUtil.toBean(shopJson, TmpObjectHolder.class);
        }
        if (null == objHolder) objHolder = new TmpObjectHolder();
        Shop shop;
        if (objHolder.isExpired()) {
            shop = ShopServiceImpl.super.getById(id);

            final TmpObjectHolder finalObjHolder = objHolder;

            rebuildCacheExecutors.execute(() -> {
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
                Calendar c = Calendar.getInstance();
                c.add(Calendar.MINUTE, 3);
                finalObjHolder.setExpiredTime(c.getTime());

                finalObjHolder.setJsonObject(JSONUtil.toJsonStr(shop));
                String temJson = JSONUtil.toJsonStr(finalObjHolder);
                stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, temJson,
                        Duration.ofMinutes(3));
            });

        } else {
            shop = JSONUtil.toBean(objHolder.getJsonObject(), Shop.class);

        }
        return shop;
    }

    @Override
    public boolean updateById(Shop entity) {
        boolean b1 = super.updateById(entity);
        if (!b1) {
            return false;
        }

        new Thread(() -> {
            try {
                stringRedisTemplate.delete(CACHE_SHOP_KEY + entity.getId());
                TimeUnit.MILLISECONDS.sleep(100);
                stringRedisTemplate.delete(CACHE_SHOP_KEY + entity.getId());
            } catch (Exception e) {
                log.warn("延迟双删异常：{}", e.getMessage(), e);
            }
        }).start();

        return true;
    }
}