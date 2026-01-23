package com.neozeng.trackerserve.util;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.stereotype.Component;
import com.neozeng.trackerserve.pojo.RedisData;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;


/**
 * @author strive_qin
 * @version 1.0
 * @description CacheClient
 * @date 2025/12/7 19:21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheClient {
    private final StringRedisTemplate stringRedisTemplate;

    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    public void setLogicExpire(String key, Object value, Long time, TimeUnit unit) {
       RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }
    public <R,ID>R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallback,Long time, TimeUnit unit) {
        //添加缓存
        String shopJson = stringRedisTemplate.opsForValue().get(keyPrefix + id);
        if (StrUtil.isNotBlank(shopJson)) {
            // 💡 增加判断：如果目标类型本身就是 String，直接返回，不走 JSON 解析
            if (type.equals(String.class)) {
                // 如果字符串是被双引号包裹的 JSON 串(方案B存入的)，toBean 会去掉引号；
                // 如果是普通字符串，toBean 报错时我们捕获并直接返回原串。
                try {
                    return JSONUtil.toBean(shopJson, type);
                } catch (Exception e) {
                    return (R) shopJson;
                }
            }
            return JSONUtil.toBean(shopJson, type);
        }
        if (shopJson != null) {
            return null;
        }
        //不存在，查询数据库
        R r = dbFallback.apply(id);
        if (r == null) {
            //数据库不存在，返回null
            stringRedisTemplate.opsForValue().set(keyPrefix + id, "", RedisConstants.CACHE_NULL_TTL * new Random().nextInt(10), TimeUnit.MINUTES);
            return null;
        }
        log.debug("缓存未命中,差数据库");
        this.set(keyPrefix + id, r, time, unit);
        return r;
    }
    //定义线程池
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public <R, ID> R queryWithLogicExpire(String keyPrefix, ID id, Class<R> type,
                                          Function<ID, R> dbFallback, long time, TimeUnit unit) {
        String key = keyPrefix + id;

        // 1. 从 Redis 查询缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isBlank(json)) {
            return null;
        }

        // 2. 反序列化
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R r = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();

        // 3. 判断是否逻辑过期
        if (expireTime.isAfter(LocalDateTime.now())) {
            // 未过期，直接返回
            log.debug("缓存未过期，直接返回");
            return r;
        }

        // 4. 已过期，尝试获取锁进行缓存重建
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        boolean acquired = tryLock(lockKey);

        // 注意：这里是关键优化！获取锁成功后，再次检查一遍缓存是否已更新
        if (acquired) {
            try {
                // 双重检查：再次读取一次缓存（可能其他线程已经重建完成）
                String jsonAfterLock = stringRedisTemplate.opsForValue().get(key);
                if (!StrUtil.isBlank(jsonAfterLock)) {
                    RedisData newRedisData = JSONUtil.toBean(jsonAfterLock, RedisData.class);
                    if (newRedisData.getExpireTime().isAfter(LocalDateTime.now())) {
                        // 已经被其他线程重建好了，直接返回最新数据
                        log.debug("双重检查发现缓存已被重建，直接返回最新数据");
                        return JSONUtil.toBean((JSONObject) newRedisData.getData(), type);
                    }
                }

                // 确认还是过期，才真正执行重建
                log.debug("获取锁成功，开始重建缓存");
                CACHE_REBUILD_EXECUTOR.submit(() -> {
                    try {
                        // 查询数据库
                        R freshData = dbFallback.apply(id);
                        // 重建缓存（设置新的逻辑过期时间）
                        this.setLogicExpire(key, freshData, time, unit);
                    } catch (Exception e) {
                        throw new RuntimeException("缓存重建失败", e);
                    } finally {
                        unLock(lockKey);
                    }
                });

            } catch (Exception e) {
                // 即使这里抛异常，也要确保释放锁
                unLock(lockKey);
                throw e;
            }
        }

        // 5. 返回旧数据（无论是没拿到锁，还是拿到锁但已提交重建任务）
        return r;
    }
    //尝试获取锁
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }
    //释放锁
    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }
}
