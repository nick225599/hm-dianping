package com.hmdp.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    @Override
    public List<ShopType> queryTypeList() {
        List<ShopType> types;
        List<String> typesJson;

        typesJson = stringRedisTemplate.opsForList().range(CACHE_SHOP_TYPES_ALL_KEY, 0, Integer.MAX_VALUE);
        stringRedisTemplate.expire(CACHE_SHOP_TYPES_ALL_KEY, Duration.ofMinutes(CACHE_SHOP_TYPES_ALL_KEY_TTL));
        if (CollectionUtil.isNotEmpty(typesJson)) {
            types = new ArrayList<>(typesJson.size());
            for (String typeJson : typesJson) {
                ShopType shopType = JSONUtil.toBean(typeJson, ShopType.class);
                types.add(shopType);
            }
        } else {
            types = this.query().orderByAsc("sort").list();
            typesJson = new ArrayList<>(types.size());
            for (ShopType type : types) {
                typesJson.add(JSONUtil.toJsonStr(type));
            }
            stringRedisTemplate.opsForList().leftPushAll(CACHE_SHOP_TYPES_ALL_KEY, typesJson);
        }
        return types;
    }
}
