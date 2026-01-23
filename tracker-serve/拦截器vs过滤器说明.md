# 为什么 Filter 可以而 Interceptor 不行？

## 执行顺序对比

### Filter（过滤器）
```
请求 → Filter → DispatcherServlet → Interceptor → Controller
```
- **执行时机**：在请求进入 Spring MVC 之前
- **执行位置**：Servlet 容器层面
- **可靠性**：高，不受 Spring 配置影响

### Interceptor（拦截器）
```
请求 → Filter → DispatcherServlet → Interceptor → Controller
```
- **执行时机**：在 DispatcherServlet 之后，Controller 之前
- **执行位置**：Spring MVC 层面
- **可靠性**：中等，受 Spring 配置影响

## 可能的原因分析

### 1. **CORS 预检请求（OPTIONS）**
浏览器在发送 POST 请求前会先发送 OPTIONS 预检请求：
- Filter 会处理所有请求（包括 OPTIONS）
- Interceptor 可能被配置为跳过 OPTIONS 请求
- 或者 OPTIONS 请求直接返回，不经过 Interceptor

### 2. **Spring Boot 4.x 的变化**
Spring Boot 4.0.1 是新版本，可能：
- 拦截器的注册方式有变化
- 路径匹配规则有变化
- 执行顺序有变化

### 3. **路径匹配问题**
拦截器的路径匹配可能有问题：
- `/**/{shortCode}` 这种模式可能匹配到了 `/api/shortLink/create`
- 或者路径匹配规则在 Spring Boot 4.x 中有变化

### 4. **HandlerMapping 问题**
Interceptor 依赖于 HandlerMapping：
- 如果请求没有匹配到 Handler，Interceptor 不会执行
- Filter 不依赖 HandlerMapping，所有请求都会执行

## 解决方案

### 方案 1：继续使用 Filter（推荐）
✅ Filter 已经工作正常，继续使用即可

### 方案 2：修复 Interceptor
如果一定要使用 Interceptor，可以：
1. 检查路径匹配规则
2. 确保 OPTIONS 请求也经过 Interceptor
3. 添加更详细的日志来诊断问题

