package com.hmdp.interceptor;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.utils.UserHolder;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_KEY;
import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

public class LoginInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;

    public LoginInterceptor(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 获取 session
        String authorization = request.getHeader("authorization");
        if (StringUtils.isEmpty(authorization)) {
            response.setStatus(401);
            return false;
        }

        String redisKey = LOGIN_USER_KEY + authorization;
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        Map<String, Object> userMap = hashOperations.entries(redisKey);
        if (userMap.isEmpty()) {
            response.setStatus(401);
            return false;
        } else {
            UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), true);
            UserHolder.saveUser(userDTO);
            redisTemplate.expire(redisKey, Duration.ofSeconds(LOGIN_USER_TTL));
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
