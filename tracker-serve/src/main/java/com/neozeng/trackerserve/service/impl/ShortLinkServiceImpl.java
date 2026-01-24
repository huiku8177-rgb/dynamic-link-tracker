package com.neozeng.trackerserve.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.neozeng.trackerserve.mapper.ShortLinkMapper;
import com.neozeng.trackerserve.mapper.VisitLogMapper;
import com.neozeng.trackerserve.pojo.ShortLink;
import com.neozeng.trackerserve.pojo.User;
import com.neozeng.trackerserve.pojo.VisitLog;
import com.neozeng.trackerserve.pojo.dto.TopLinkItem;
import com.neozeng.trackerserve.service.ShortLinkService;
import com.neozeng.trackerserve.exception.UnAuthorizedException;
import com.neozeng.trackerserve.util.Base62Utils;
import com.neozeng.trackerserve.util.CacheClient;
import com.neozeng.trackerserve.util.IpLocationUtils;
import com.neozeng.trackerserve.util.UserHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author strive_qin
 * @version 1.0
 * @description ShortLinkServiceImpl
 * @date 2026/1/8 14:35
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ShortLinkServiceImpl implements ShortLinkService {
    private final ShortLinkMapper shortLinkMapper;
    private final VisitLogMapper visitLogMapper;
    private final CacheClient cacheClient;
    private final StringRedisTemplate redisTemplate;
    /**
     * @param longUrl
     * @param workspace
     * @return
     */
    @Override
    @Transactional
    public String createShortLink(String longUrl, String workspace, String expireDate) {
        log.info("=== 开始生成短链接 ===");
        User user = UserHolder.getUser();

        if (user == null) {
            throw new RuntimeException("请先登录后操作");
        }

        // 1. 填充并保存初始对象获取 ID
        ShortLink link = new ShortLink();
        link.setLongUrl(longUrl);
        link.setUserId(user.getId());
        link.setTotalClicks(0);
        if (expireDate != null && !expireDate.isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            link.setExpireTime(LocalDateTime.parse(expireDate, formatter));
        }
        link.setCreateTime(LocalDateTime.now());
        shortLinkMapper.save(link); // 第一次保存，获取自增 ID

        // 2. 转换为短码并更新数据库
        String shortCode = Base62Utils.encode(link.getId());
        link.setShortCode(shortCode);
        shortLinkMapper.save(link); // 第二次保存，更新短码字段

        // 3. 💡 关键：在数据库事务即将提交前预热缓存
        // 这样可以确保：只要缓存里有，数据库里一定有
        cacheClient.set(
                "shortLink:cache:" + shortCode,
                JSONUtil.toJsonStr(longUrl),
                24L,
                TimeUnit.HOURS
        );

        log.info("短链接创建成功并已预热缓存: {} -> {}", shortCode, longUrl);
        return shortCode;
    }


    /**
     * 获取短链接列表（实时合并 Redis 点击量）
     * @return
     */
    @Override
    public List<ShortLink> listShortLinks() {
        log.info("开始获取当前用户的短链接列表并合并实时计数值");

        // 0. 获取当前登录用户
        User user = UserHolder.getUser();
        if (user == null) {
            log.warn("listShortLinks 调用时未获取到用户信息，可能未登录或 Token 无效");
            // 抛出未授权异常，由全局异常处理器统一返回 401
            throw new UnAuthorizedException();
        }

        Long userId = user.getId();
        // 游客模式：返回一组示例数据，而不是空列表
        if (userId != null && userId == 0L) {
            log.info("检测到游客模式，返回示例短链接列表");
            return buildGuestMockShortLinks();
        }

        log.info("正在为用户 userId={} 查询短链接列表", userId);

        // 1. 从数据库获取当前用户的原始记录
        List<ShortLink> list = shortLinkMapper.findByUserId(userId);

        // 2. 遍历列表，将 Redis 中的“增量点击量”累加到对象中
        for (ShortLink link : list) {
            // 💡 关键：Key 必须与 incrementClicks 方法中的 "shortLink:clicks:" 保持一致
            String redisKey = "shortLink:clicks:" + link.getShortCode();
            String redisValue = redisTemplate.opsForValue().get(redisKey);

            if (StrUtil.isNotBlank(redisValue)) {
                try {
                    // 将 Redis 里的增量数值加到数据库查询出的总量上
                    assert redisValue != null;
                    long extraClicks = Long.parseLong(redisValue);
                    link.setTotalClicks(link.getTotalClicks() + (int) extraClicks);
                } catch (NumberFormatException e) {
                    log.error("解析短链接 {} 的 Redis 点击量失败: {}", link.getShortCode(), redisValue);
                }
            }
        }

        return list;
    }

    /**
     * 游客模式下返回的示例短链接列表（仅展示用途，不落库）
     */
    private List<ShortLink> buildGuestMockShortLinks() {
        LocalDateTime now = LocalDateTime.now();

        ShortLink a = new ShortLink();
        a.setId(1L);
        a.setUserId(0L);
        a.setLongUrl("https://example.com/landing-page");
        a.setShortCode("guestA1");
        a.setTotalClicks(128);
        a.setExpireTime(now.plusDays(7));
        a.setCreateTime(now.minusDays(2));

        ShortLink b = new ShortLink();
        b.setId(2L);
        b.setUserId(0L);
        b.setLongUrl("https://docs.dynamic-link-tracker.dev/guide");
        b.setShortCode("guestB2");
        b.setTotalClicks(56);
        b.setExpireTime(null); // 永久有效
        b.setCreateTime(now.minusDays(5));

        ShortLink c = new ShortLink();
        c.setId(3L);
        c.setUserId(0L);
        c.setLongUrl("https://blog.example.com/campaign-2026");
        c.setShortCode("guestC3");
        c.setTotalClicks(9);
        c.setExpireTime(now.plusDays(1));
        c.setCreateTime(now.minusHours(6));

        return List.of(a, b, c);
    }

    @Override
    public void deleteShortLink(Long id) {
        shortLinkMapper.deleteById(id);

    }

    /**
     * 获取原始链接
     * @param shortCode
     * @return
     */
    @Override
    public String getRedirectUrl(String shortCode) {
        return cacheClient.queryWithPassThrough(
                "shortLink:cache:",
                shortCode,
                String.class,
                code -> {
                    // 数据库回源逻辑
                    ShortLink link = shortLinkMapper.findByShortCode(shortCode);
                    return link != null ? link.getLongUrl(): null;
                },
                24L,
                TimeUnit.HOURS
        );

    }



    @Override
    @Async
    public void incrementClicks(String shortCode) {
        // 0. 根据短码查询短链接，获取归属用户
        ShortLink link = shortLinkMapper.findByShortCode(shortCode);
        Long userId = link != null ? link.getUserId() : null;

        // 1. 定义 Key（建议统一使用常量）
        String clicksKey = "shortLink:clicks:" + shortCode;
        String globalRankingKey = "shortLink:ranking:global"; // 全局排行榜 ZSet Key（可选）
        String userRankingKey = userId != null ? "shortLink:ranking:" + userId : null; // 用户私有排行榜 Key

        // 2. Redis 原子递增（用于每 100 次同步数据库的“计数器”）
        Long currentClicks = redisTemplate.opsForValue().increment(clicksKey, 1);

        // 3. 💡 增加 ZSet 的分数（用于实时排行榜）
        // 3.1 全局总榜（可选）
        redisTemplate.opsForZSet().incrementScore(globalRankingKey, shortCode, 1);
        // 3.2 当前短链接所属用户的私有榜
        if (userRankingKey != null) {
            redisTemplate.opsForZSet().incrementScore(userRankingKey, shortCode, 1);
        }

        log.info("短链接 {} 点击量+1，当前增量计数值: {}", shortCode, currentClicks);

        // 4. 每 100 次点击同步一次数据库
        if (currentClicks != null && currentClicks % 100 == 0) {
            log.info("短链接 {} 达到同步阈值，正在将 100 次点击同步至数据库", shortCode);

            // 💡 建议直接更新，减少一次 findByShortCode 的数据库查询开销
            int updatedRows = shortLinkMapper.updateTotalClicks(shortCode, 100);
            if (updatedRows > 0) {
                // 同步成功后，可选：减去已入库的 100，让 Redis 只留“零头”
                redisTemplate.opsForValue().decrement(clicksKey, 100);
            }
        }
    }

    /**
     * 记录访问日志
     * @param shortCode
     * @param ip
     * @param userAgent
     */
    @Override
    @Transactional
    @Async
    public void recordVisitLog(String shortCode, String ip, String userAgent) {
        try {
            // 1. 创建访问日志对象
            VisitLog visitLog = new VisitLog();
            visitLog.setShortCode(shortCode);

            // 1.1 根据短码反查短链接，填充所属用户 ID，便于后续按用户维度统计
            ShortLink link = shortLinkMapper.findByShortCode(shortCode);
            if (link != null && link.getUserId() != null) {
                visitLog.setUserId(link.getUserId());
            }

            // 2. 获取真实客户端 IP 地址（支持代理、负载均衡等场景）

            visitLog.setIp(ip);

            // 3. 获取设备信息 (User-Agent)
            visitLog.setUserAgent(userAgent != null ? userAgent : "Unknown");

            // 4. 获取地理位置（根据 IP 查询）
            // 方式一：同步查询（可能会稍微影响响应速度）
            String location = IpLocationUtils.getLocation(ip);
            visitLog.setLocation(location);

            // 方式二：异步查询（推荐，不阻塞主流程）
            // 如果选择异步，可以先保存 null，之后更新
            // IpLocationUtils.getLocationAsync(clientIp, location -> {
            //     visitLog.setLocation(location);
            //     visitLogMapper.save(visitLog);
            // });

            // 5. 保存到数据库
            visitLogMapper.save(visitLog);
            
            log.info("访问日志已记录: shortCode={}, ip={}, location={}", shortCode, ip, location);
            
        } catch (Exception e) {
            // 记录日志失败不应影响主流程（重定向）
            log.error("记录访问日志失败: shortCode={}, error={}", shortCode, e.getMessage(), e);
        }
    }


    /**
     * 更新原始链接
     *
     * @param id
     * @param longUrl
     * @return
     */
    @Override
    public boolean updateShortLink(Long id, String longUrl, LocalDateTime expireDate) {
        ShortLink shortLink = shortLinkMapper.findById(id).orElse(null);
        if (shortLink != null) {
            shortLink.setLongUrl(longUrl);
            shortLink.setExpireTime(expireDate);
            shortLinkMapper.save(shortLink);
            return true;
        }
        return false;
    }

    /**
     * 获取实时排名
     * @param limit
     * @return
     */
    @Override
    public List<TopLinkItem> getTopLinksRealTime(int limit) {
        // 0. 获取当前登录用户
        User user = UserHolder.getUser();
        if (user == null) {
            throw new UnAuthorizedException();
        }

        Long userId = user.getId();

        // 1. 定义当前用户的 ZSet Key
        String rankingKey = "shortLink:ranking:" + userId;

        // 2. 尝试从 Redis ZSet 获取该用户前 N 名 (Score从高到低)
        Set<ZSetOperations.TypedTuple<String>> typedTuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(rankingKey, 0, limit - 1);

        // 3. 如果 Redis 为空（比如刚上线），调用 Mapper 方法从 DB 查该用户的数据
        if (CollUtil.isEmpty(typedTuples)) {
            log.info("用户 {} 的 Redis 排行榜为空，回退到数据库查询并预热数据", userId);
            Pageable pageable = PageRequest.of(0, limit);
            List<ShortLink> topLinks = shortLinkMapper.findTopByUserIdOrderByTotalClicksDesc(userId, pageable); //

            // 顺便把 DB 数据异步存入 Redis，下次就快了 (预热逻辑)
            topLinks.forEach(link ->
                  redisTemplate.opsForZSet().add(rankingKey, link.getShortCode(), link.getTotalClicks().doubleValue())
            );

            return topLinks.stream().map(l -> new TopLinkItem(l.getShortCode(), l.getLongUrl(), l.getTotalClicks())).collect(Collectors.toList());
        }

        // 4. 如果 Redis 有数据，直接封装返回
        return typedTuples.stream().map(tuple -> {
            String shortCode = tuple.getValue();
            int score = tuple.getScore().intValue();
            // 这里获取 LongUrl 建议走 cache 目录
            String longUrl = getRedirectUrl(shortCode);
            return new TopLinkItem(shortCode, longUrl, score);
        }).collect(Collectors.toList());
    }

    /**
     * 从全局排行榜获取热门短链接（游客模式使用）
     * @param limit 返回数量
     * @return 热门短链接列表
     */
    @Override
    public List<TopLinkItem> getTopLinksFromGlobalRanking(int limit) {
        // 1. 定义全局排行榜的 ZSet Key
        String globalRankingKey = "shortLink:ranking:global";

        // 2. 尝试从 Redis ZSet 获取全局前 N 名 (Score从高到低)
        Set<ZSetOperations.TypedTuple<String>> typedTuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(globalRankingKey, 0, limit - 1);

        // 3. 如果 Redis 为空，回退到 DB 并预热
        if (CollUtil.isEmpty(typedTuples)) {
            log.info("全局 Redis 排行榜为空，回退到数据库查询并预热数据");
            // 使用全局查询方法（不按用户过滤）
            List<ShortLink> allLinks = shortLinkMapper.findAll();
            List<ShortLink> topLinks = allLinks.stream()
                    .sorted((a, b) -> Integer.compare(
                            (b.getTotalClicks() != null ? b.getTotalClicks() : 0),
                            (a.getTotalClicks() != null ? a.getTotalClicks() : 0)
                    ))
                    .limit(limit)
                    .collect(Collectors.toList());

            // 预热全局排行榜
            topLinks.forEach(link ->
                  redisTemplate.opsForZSet().add(globalRankingKey, link.getShortCode(), link.getTotalClicks().doubleValue())
            );

            return topLinks.stream()
                    .map(l -> new TopLinkItem(l.getShortCode(), l.getLongUrl(), l.getTotalClicks()))
                    .collect(Collectors.toList());
        }

        // 4. Redis 有数据时，直接从缓存构造结果
        return typedTuples.stream().map(tuple -> {
            String shortCode = tuple.getValue();
            int score = tuple.getScore().intValue();
            String longUrl = getRedirectUrl(shortCode);
            return new TopLinkItem(shortCode, longUrl, score);
        }).collect(Collectors.toList());
    }
}
