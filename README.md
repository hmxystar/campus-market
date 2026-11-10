# campus-market 后端工程骨架

按《校园集市项目实战教程（第二版）· Spring Boot 4.1.2》第 3–4 章的目录结构与依赖清单，把「工程立起来 + 基础设施一次搭好」这部分**提前落成可编辑的代码**，其余业务模块留出完整签名和骨架，方便按章节逐个填。

- 技术基准：Spring Boot 4.1.2 / JDK 21 / MyBatis-Plus 3.5.18 / MySQL 26.10 / Redis 8.10.3 / springdoc 3.2.0 / jjwt 0.12.6
- 包名：`com.campus.market`，坐标 `com.campus:campus-market:1.0.0`
- 状态：结构与依赖完整，**未做编译验证**（按你的要求只保证骨架完整）

---

## 一、目录结构

```
campus-market
├── pom.xml                    # 依赖与版本基准（讲义 3.3）
├── sql/init.sql               # 建库建表 + 初始分类/账号（讲义 2.4）
├── Dockerfile                 # 多阶段构建（讲义 12.2）
├── docker-compose.yml         # MySQL + Redis + app + Nginx（讲义 12.4）
├── nginx.conf                 # 静态托管 + /api 反向代理（讲义 12.3）
├── upload/                    # 本地图片落盘目录（file.upload-path）
└── src/main
    ├── java/com/campus/market
    │   ├── CampusMarketApplication.java   # @EnableAsync @EnableScheduling @MapperScan
    │   ├── common/       Result、PageResult、BusinessException、GlobalExceptionHandler、UserContext、ProductStatus、OrderStatus
    │   ├── config/       MybatisPlusConfig、MyMetaObjectHandler、RedisConfig、WebMvcConfig、AsyncConfig、OpenApiConfig
    │   ├── annotation/   @RequireLogin @RequireAdmin @RateLimit @OperationLog
    │   ├── interceptor/  AuthInterceptor
    │   ├── aspect/       OperationLogAspect、RateLimitAspect
    │   ├── entity/       BaseEntity + User / Category / Product / Order / Favorite / BrowseHistory / OperationLogEntity
    │   ├── mapper/       7 个 Mapper（ProductMapper 带 3 个自定义方法）
    │   ├── service/      6 个接口 + impl
    │   ├── controller/   User / Product / Category / Order / Favorite / Admin
    │   ├── dto/          RegisterDTO、LoginDTO、ProductCreateDTO、OrderCreateDTO
    │   ├── vo/           LoginVO、UserVO、ProductCardVO、ProductDetailVO、CategoryVO、OrderVO
    │   ├── task/         OrderTimeoutTask、ViewCountFlushTask
    │   └── util/         JwtUtil、RedisUtil、RedisLock
    └── resources
        ├── application.yml
        ├── application-dev.yml
        └── mapper/ProductMapper.xml       # 列表联查 + 原子扣/回补库存
```

共 62 个 Java 文件 + 4 个配置文件/脚本。

---

## 二、跑起来（配合讲义第 0 章环境）

1. **建库建表**：DBeaver 或命令行执行 `sql/init.sql`（自动建 `campus_market` 库、7 张表、初始分类与演示账号）。
2. **起 Redis**：`docker run -d --name cm-redis -p 6379:6379 redis:latest`（本机 Docker Hub 连不上，用本地已有的 `redis:latest`，实测版本就是 8.10.3）
3. **数据库口令**：配置里用的是占位写法 `${DB_PASSWORD:123456}` —— 不设环境变量时默认走 `123456`，所以你本机直接能跑；要换密码就设置环境变量 `DB_PASSWORD`（或复制 `.env.example` 为 `.env` 后修改），不用改代码。JWT 密钥同理，用 `JWT_SECRET` 覆盖。
4. **运行**：IDEA 里选中 `CampusMarketApplication` 点 Run，看到 `Tomcat started on port 8080` 即成功；浏览器打开 `http://localhost:8080/swagger-ui.html`。

```bash
# 命令行等价操作
mvn clean package -DskipTests
java -jar target/campus-market-1.0.0.jar
```

---

## 三、哪些已经写好了，哪些还要你填

| 模块 | 骨架状态 | 说明 |
|---|---|---|
| 统一返回 / 异常 | ✅ 完整 | `Result`、`BusinessException`、4 类异常兜底 |
| 分页 + 字段填充 | ✅ 完整 | `MybatisPlusConfig`（maxLimit 100）、`MyMetaObjectHandler` |
| Redis 序列化 | ✅ 完整 | `GenericJacksonJsonRedisSerializer.builder()`（无参构造在 4.1.2 已移除） |
| 鉴权链路 | ✅ 完整 | JWT 生成/解析、拦截器、ThreadLocal、`@RequireLogin` / `@RequireAdmin` |
| 操作日志 / 限流 | ✅ 基本完整 | AOP + 注解 + Lua 原子计数；`@Async` 自调用问题见下方提示 1 |
| 用户模块 | ✅ 完整 | 注册（BCrypt）、登录（防枚举）、`/api/user/me` |
| 商品模块 | ✅ 主流程可用 | 发布、分页、详情 Cache Aside、编辑、下架 |
| 分类模块 | ✅ 构树完整 | 一次查询 + 内存构树；**缓存清理留 TODO**（讲义 7.3） |
| 订单模块 | ✅ 主流程可用 | 分布式锁 + 原子扣库存 + 状态机 + CAS 流转 |
| 收藏模块 | ✅ 可用 | 新增/取消/我的收藏 |
| 定时任务 | ✅ 可用 | 超时取消、浏览数落库 |
| 管理端 | ✅ 基础接口 | 用户列表、待审核列表、审核通过/驳回 |
| 浏览记录落库 | ⚠️ 仅实体与 Mapper | 讲义第 6 章「浏览历史」的写入逻辑待补 |
| 前端 Vue 3 | ❌ 未包含 | 本次只交付后端骨架（讲义第 11 章自行跟做） |

### 本机实测修正（2026-11-10，Windows + Boot 4.1.2 真实环境）

讲义里这两处写法在真实依赖上跑不通，已在工程里改掉：

| 位置 | 讲义写法 | 实际报错 | 已改为 |
|---|---|---|---|
| `pom.xml` 第 49 行左右 | `spring-boot-starter-aop` | `'dependencies.dependency.version' ... is missing`（Boot 4.1.2 的 BOM 里根本没有这个 artifactId） | `spring-boot-starter-aspectj` |
| `config/RedisConfig` | `new GenericJacksonJsonRedisSerializer()` | 编译失败：该类在 Spring Data Redis 4.1.2 已不提供无参构造 | `GenericJacksonJsonRedisSerializer.builder().enableUnsafeDefaultTyping().build()` |

实测结果：`mvn compile` 69 个源文件通过；`spring-boot:run` 3.8 秒启动成功，`/v3/api-docs` 暴露 17 个接口；本地 `campus_market` 7 张表齐全（root/123456）。Redis 用 `docker run -d --name cm-redis -p 6379:6379 redis:latest` 起（本地镜像 tag 虽是 latest，`INFO server` 显示 `redis_version:8.10.3`），商品详情接口连续两次 200，Redis 中确认生成 `product:detail:{id}` 与 `product:view:{id}`，改写缓存里的 title 后接口返回值同步变化，证明缓存命中与反序列化（含 `@class` 类型信息）都正常。

> `enableUnsafeDefaultTyping()` 会把 `@class` 类型信息写进 JSON，这样缓存读回来才是 `ProductDetailVO` 而不是 `LinkedHashMap`。教学项目够用；若 Redis 可能被外部写入，应换成 allow-list 的 `enableDefaultTyping(validator)`。

### 代码里的 TODO 集中清单

- `CategoryServiceImpl#clearCache`：分类变更后删 `category:tree` 缓存
- `ProductServiceImpl#detail`：补「空值缓存」防缓存穿透（讲义 6.5 进阶）
- `OrderServiceImpl`：支付为模拟实现，接真实支付网关时替换 `pay()`
- `FavoriteServiceImpl#pageMine`：批量拼装已做，若要带卖家昵称需再改联查
- `OperationLogAspect#saveLog`：自调用 `@Async` 不生效，正式版抽独立 Bean

---

## 四、三个容易踩的坑（讲义已强调，骨架里已规避）

1. **`@Async` 自调用无效**：`around()` 里直接调本类 `saveLog()`，不经过 Spring 代理，实际仍是同步执行。骨架保留了讲义写法并标了 TODO，正式实现请拆成独立 `OperationLogRecorder`。
2. **多实例定时任务重复跑**：`OrderTimeoutTask` 每个实例都会扫表，两台部署会重复取消。上线前加分布式锁或单实例开关（讲义 10.3）。
3. **分页缺 jsqlparser**：`mybatis-plus-jsqlparser` 已写进 pom，删掉它第一次分页就 `NoClassDefFoundError`。

---

## 五、与讲义章节的对应关系

| 讲义 | 骨架中的落点 |
|---|---|
| 第 2 章 数据库设计 | `sql/init.sql` |
| 第 3 章 工程搭建 | `pom.xml`、`application.yml`、启动类、`config/` |
| 第 4 章 基础设施 | `common/`、`config/MybatisPlusConfig`、`config/RedisConfig`、`entity/BaseEntity` |
| 第 5 章 用户模块 | `annotation/`、`interceptor/AuthInterceptor`、`util/JwtUtil`、User 全链路 |
| 第 6 章 商品模块 | Product 全链路 + `ProductMapper.xml` + `FileService` |
| 第 7 章 分类模块 | `CategoryServiceImpl`（缓存部分待补） |
| 第 8 章 订单模块 | `OrderServiceImpl`、`util/RedisLock`、`ProductMapper#deductStock` |
| 第 9 章 AOP | `aspect/` 两个切面 + `@OperationLog` / `@RateLimit` |
| 第 10 章 定时任务 | `task/` 两个任务 + `config/AsyncConfig` |
| 第 12 章 部署 | `Dockerfile`、`docker-compose.yml`、`nginx.conf` |

---

## 六、建议的下一步

1. 先把 `mvn -q -DskipTests compile` 跑通（首次会下载大量依赖，注意网络）。
2. 按第 5 章的验收清单过一遍：注册 → 登录 → 带 token 访问 `/api/user/me` → 不带 token 返回 401。
3. 再用 Swagger 走一遍商品发布 → 列表 → 详情 → 下单 → 取消，确认 Redis 锁与扣库存生效。
4. 最后补前端（第 11 章）与压测（第 13 章）。
