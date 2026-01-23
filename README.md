# Dynamic Link Tracker

基于 **Spring Boot 3 (Java 17)** 和 **Vue 3** 构建的**高性能短链接监控系统**，专注于**缓存优先**架构，实现毫秒级跳转、实时点击监控和用户级数据隔离。

> 本 README 偏工程化与架构说明，适合快速了解系统设计与关键链路。

---

## 项目概览

- **项目名称**：Dynamic Link Tracker
- **核心目标**：提供**短链接生成**、**实时点击监控**、**多维度访问统计**及**用户权限隔离**的一站式解决方案
- **设计理念**：针对 **32GB 内存**硬件环境优化，采用**缓存优先**架构，极致压榨 Redis 性能（原子计数器 + ZSet 排行榜 + 穿透缓存策略）

---

## 技术架构与亮点

### 1. 缓存优先的跳转链路（毫秒级响应）

跳转逻辑采用**穿透缓存**方案，防止缓存穿透并降低数据库压力：

- **核心思路**：缓存未命中 → 回源数据库 → 写入缓存；数据库未命中 → 写入短时 null 标记
- **实现位置**：`CacheClient.queryWithPassThrough(...)` (`tracker-serve/src/main/java/com/neozeng/trackerserve/util/CacheClient.java`)

### 2. Redis 多级目录设计

Redis 键空间采用类似文件系统的层级结构，便于运维和 TTL 策略管理：

- **`shortLink:cache:{code}`**
  - 跳转映射：`shortCode -> longUrl`
- **`shortLink:clicks:{code}`**
  - 实时增量计数器（热写路径）
- **`shortLink:ranking:{userId}`**
  - 用户隔离的排行榜（Redis ZSet）
- **`shortLink:ranking:global`**（可选）
  - 全局排行榜（Redis ZSet）

> **设计说明**：
> - `cache:*` 聚焦跳转延迟
> - `clicks:*` 聚焦写入扩展性（原子递增）
> - `ranking:*` 聚焦实时分析

### 3. 高 QPS 点击计数器 + 批量数据库同步

点击计数采用 Redis 原子操作，降低数据库压力：

- **热路径**：`INCR shortLink:clicks:{code}`（原子操作）
- **批量同步**：每 **100 次点击** → `UPDATE ... SET total_clicks = total_clicks + 100`
- **同步后**：Redis 递减 100，仅保留“零头”

该策略在保证高吞吐的同时，保持 MySQL 写入负载稳定。

### 4. 实时 Top-N 排行榜（Redis ZSet）

每次点击更新排行榜：

- **全局榜**：`ZINCRBY shortLink:ranking:global 1 {code}`
- **用户榜**：`ZINCRBY shortLink:ranking:{userId} 1 {code}`

查询逻辑：

- 使用 `ZREVRANGE ... WITHSCORES` 直接从 Redis 获取 Top-5
- 冷启动回退：查询数据库 → 预热 Redis

### 5. 安全防护与用户隔离

- **JWT + 拦截器**：有效 Token 解析用户并存入 `UserHolder`（ThreadLocal）
- **用户隔离**：
  - 短链接列表查询按 `userId` 过滤
  - 访问统计查询按 `userId` 过滤
  - 排行榜键按用户隔离

### 6. 游客模式（先体验后注册）

游客模式支持“先体验后注册”：

- 前端存储 `localStorage.isGuest = true`
- Axios 在无 Token 但启用游客模式时添加请求头 `Guest-Access: true`
- 后端识别游客头并注入虚拟用户（`userId = 0`）
- 游客可查看：
  - 示例数据预览（如 3 条演示短链接）
  - 公开汇总/趋势数据
- 敏感详情保持受控（返回 401 并提示明确消息）

### 7. 登录态持久化（零感知刷新）

- Token 存储在 `localStorage`
- 应用启动时，如果存在 Token 且非游客模式，调用：
  - `GET /api/user/info`
- 恢复的用户信息写回前端状态，确保刷新后不丢失登录态

---

## 用户体验

- **游客模式**：支持“先体验”，提供示例数据和公开仪表板
- **状态持久化**：localStorage + `/api/user/info` 实现无缝刷新，不会丢失会话

---

## 核心界面预览

> 截图请放置在 `docs/screenshots/` 目录下

- **控制台总览**（Dashboard）
  - `docs/screenshots/dashboard.png`
- **链接管理**（Link Management）
  - `docs/screenshots/link-management.png`
- **访问统计**（Analytics）
  - `docs/screenshots/analytics.png`
- **游客引导**（Guest Guard）
  - `docs/screenshots/guest-guard.png`

---

## 快速启动

### 环境要求

- **Java**：17+
- **MySQL**：8.0+
- **Redis**：6.2+（密码：**123321**）
- **Node.js**：18+（推荐）
- **包管理器**：pnpm（项目使用 pnpm）

### 1) 数据库与 Redis

创建数据库：

```sql
CREATE DATABASE IF NOT EXISTS link_tracker DEFAULT CHARSET utf8mb4;
```

Redis 密码配置：

- 将 Redis 密码设置为 **`123321`**（与当前后端默认配置匹配）

### 2) 后端（Spring Boot）

后端位置：`tracker-serve/`

1. 配置 `tracker-serve/src/main/resources/application.yaml`：

- `spring.datasource.url/username/password`（MySQL）
- `spring.data.redis.host/port/password`（Redis）
- `jwt.secret`（必须 >= 32 个字符）

2. 启动后端：

```bash
cd tracker-serve
mvn spring-boot:run
```

默认 API 基础路径：

- `http://localhost:8080/api`

### 3) 前端（Vue 3）

前端位置：`tracker-ui/`

```bash
cd tracker-ui
pnpm install
pnpm dev
```

开发服务器：

- `http://localhost:5173`

---

## API 接口说明

- **认证相关**：
  - `POST /api/auth/login`（用户登录）
  - `POST /api/auth/register`（用户注册）
  - `GET  /api/user/info`（恢复会话）

- **短链接管理**：
  - `POST /api/shortLink/create`（创建短链接）
  - `GET  /api/shortLink/list`（获取短链接列表）

- **访问统计**：
  - `GET /api/shortLink/stats/clickTrend?days=7`（点击量趋势）
  - `GET /api/shortLink/stats/topLinks?limit=5`（热门排行）
  - `GET /api/shortLink/visits/all?page=0&size=20`（访问记录分页）

---

## 项目结构

```text
.
├─ tracker-serve/        # Spring Boot 后端
├─ tracker-ui/           # Vue 3 前端
└─ docs/
   └─ screenshots/       # README 截图占位符
```

---

## 性能调优建议（32GB 内存环境）

- **Redis**
  - 保持 `clicks:*` 和 `ranking:*` 热键常驻内存
  - 根据持久化需求选择 RDB/AOF 策略
- **MySQL**
  - 优先使用批量同步处理计数器（点击量已实现）
- **JVM（Java 17）**
  - 保守配置堆内存（如 4-8GB），为 Redis 和操作系统页缓存留出空间

---

## 许可证

MIT（可根据需要调整）
