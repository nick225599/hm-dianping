package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    //TODO scs 怎么知道是否已使用自己声明的 redisTemplate ？

    @Override
    public Result sendCode(String phone) {
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式不正确");
        }
        String code = RandomUtil.randomNumbers(6);
        String redisKey = LOGIN_CODE_KEY + phone;
        redisTemplate.opsForValue().set(redisKey, code, Duration.ofMinutes(LOGIN_CODE_TTL));

        // 发送短信验证码...

        log.info("发送短信验证码成功，验证码：{}", code);
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm) {
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式不正确");
        }
        String code  = loginForm.getCode();
        if(StringUtils.isEmpty(code)){
            return Result.fail("短信验证码不能为空");
        }
        String redisKey = LOGIN_CODE_KEY + phone;
        Object redisCode = redisTemplate.opsForValue().get(redisKey);

        if (!Objects.equals(loginForm.getCode(), redisCode)) {
            return Result.fail("短信验证码不正确");
        }
        User user = query().eq("phone", phone).one();
        if (null == user) {
            user = createUser(phone);
        }

        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO);
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        String uuid = IdUtil.simpleUUID();
        String key = LOGIN_USER_KEY + uuid;
        hashOperations.putAll(key, userMap);
        redisTemplate.expire(key, Duration.ofSeconds(LOGIN_USER_TTL));
        return Result.ok(uuid);
    }

    private User createUser(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomNumbers(12));
        save(user);
        return user;
    }
}
