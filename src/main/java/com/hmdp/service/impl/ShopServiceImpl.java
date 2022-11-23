package com.hmdp.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.Serializable;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.hmdp.utils.RedisConstants.CACHE_SHOP_TTL;

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

    @Override
    public Shop getById(Serializable id) {

        // 1. 查缓存
        String shopJson = stringRedisTemplate.opsForValue().get(CACHE_SHOP_KEY + id);
        if (StringUtils.isNotEmpty(shopJson)) {
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }

        // 2. 缓存查不到查数据库
        try {
            TimeUnit.SECONDS.sleep(1L); // 模拟查数据库的高耗时
        } catch (InterruptedException e) {
            //TODO scs 发生 interruptedException 后，只打印下 log 线程还能继续执行吗？
            log.warn(e.getMessage(), e);
        }
        Shop shop = super.getById(id);
        if (null == shop) {
            return null;
        }

        // 3. 写缓存
        shopJson = JSONUtil.toJsonStr(shop);
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY + id, shopJson,
                Duration.ofMinutes(CACHE_SHOP_TTL));

        // 4. 返回查询结果
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
