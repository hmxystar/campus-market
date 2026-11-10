# Campus Market

校园二手交易平台后端服务，基于 Spring Boot 4 构建，提供用户认证、商品发布与检索、分类管理、订单交易、收藏以及后台审核等能力。

---

## 技术栈

| 类别 | 选型 |
|---|---|
| 基础框架 | Spring Boot 4.1.2 |
| 运行环境 | JDK 21 |
| 数据访问 | MyBatis-Plus 3.5.18 |
| 数据库 | MySQL 8.x |
| 缓存 | Redis 8.x |
| 安全 | Spring Security Crypto（BCrypt） + JJWT 0.12.6 |
| 接口文档 | SpringDoc OpenAPI 3.2.0 |
| 构建工具 | Maven 3.9+ |

---

## 功能模块

| 模块 | 能力 |
|---|---|
| 用户 | 注册（BCrypt 加密）、登录（JWT 签发）、获取当前用户信息 |
| 商品 | 发布、分页列表、详情（Cache Aside 缓存）、编辑、上架 / 下架 |
| 分类 | 两级分类树查询 |
| 订单 | 下单（分布式锁 + 原子扣减库存）、支付、取消、完成、我的订单、超时自动取消 |
| 收藏 | 收藏、取消收藏、我的收藏列表 |
| 后台管理 | 用户列表、待审核商品、商品审核通过 / 驳回 |
| 基础设施 | 统一响应体、全局异常处理、参数校验、操作日志、接口限流、定时任务、文件上传 |

---

## 项目结构

```
campus-market
├── pom.xml
├── sql/init.sql                 # 建库建表与初始数据
├── Dockerfile                   # 多阶段镜像构建
├── docker-compose.yml           # MySQL + Redis + 应用 + Nginx
├── nginx.conf                   # 静态资源托管与 /api 反向代理
├── .env.example                 # 环境变量样例
├── upload/                      # 图片上传落盘目录
└── src/main
    ├── java/com/campus/market
    │   ├── CampusMarketApplication.java
    │   ├── common/              # Result、PageResult、BusinessException、GlobalExceptionHandler、UserContext、状态枚举
    │   ├── config/              # MybatisPlus、Redis、WebMvc、Async、OpenApi 配置
    │   ├── annotation/          # @RequireLogin、@RequireAdmin、@RateLimit、@OperationLog
    │   ├── interceptor/         # AuthInterceptor
    │   ├── aspect/              # 操作日志切面、限流切面
    │   ├── entity/              # BaseEntity 及 7 张表实体
    │   ├── mapper/              # 7 个 Mapper，ProductMapper 含自定义 SQL
    │   ├── service/             # 业务接口与实现
    │   ├── controller/          # User、Product、Category、Order、Favorite、Admin
    │   ├── dto/ vo/             # 请求参数与响应视图对象
    │   ├── task/                # 订单超时取消、浏览数落库
    │   └── util/                # JwtUtil、RedisUtil、RedisLock
    └── resources
        ├── application.yml
        ├── application-dev.yml
        └── mapper/ProductMapper.xml
```

---

## 快速开始

### 环境要求

- JDK 21 及以上
- MySQL 8.0 及以上
- Redis 7.0 及以上
- Maven 3.9 及以上

### 1. 初始化数据库

执行 `sql/init.sql`，自动创建 `campus_market` 库、7 张数据表及初始分类与演示账号。

```bash
mysql -u root -p < sql/init.sql
```

### 2. 启动 Redis

任选一种方式，二者均占用 6379 端口，请勿同时启动。

**Docker：**

```bash
docker run -d --name cm-redis -p 6379:6379 redis:latest
```

**Windows 原生版：**

```powershell
cd <Redis 安装目录>
.\redis-server.exe redis.conf
```

> 该发行版基于 Cygwin 编译，参数中的路径需使用相对路径或 `/cygdrive/c/...` 格式，直接传入 Windows 绝对路径会导致启动失败。也可通过 `RedisService.exe install -c redis.conf --dir C:/redis-data` 注册为系统服务。

验证：`redis-cli ping` 返回 `PONG`。

### 3. 配置

数据库账号与 JWT 密钥均通过环境变量注入，未设置时使用开发环境默认值：

| 环境变量 | 说明 | 默认值 |
|---|---|---|
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | `123456` |
| `JWT_SECRET` | JWT 签名密钥（HS256，不少于 32 字节） | 内置开发用默认值 |

```bash
cp .env.example .env   # 按实际环境修改
```

### 4. 启动

在 IDE 中运行 `CampusMarketApplication`，或使用命令行：

```bash
mvn spring-boot:run
```

启动成功后控制台输出 `Tomcat started on port 8080`。

### 5. 打包部署

```bash
mvn clean package -DskipTests
java -jar target/campus-market-1.0.0.jar
```

容器化部署：

```bash
docker compose up -d --build
```

---

## 接口文档

启动后访问 Swagger UI：`http://localhost:8080/swagger-ui.html`

OpenAPI 描述文件：`http://localhost:8080/v3/api-docs`

| 模块 | 接口 |
|---|---|
| 用户 | `POST /api/user/register`、`POST /api/user/login`、`GET /api/user/me` |
| 商品 | `GET /api/product`、`GET /api/product/{id}`、`POST /api/product` |
| 分类 | `GET /api/category/tree` |
| 订单 | `POST /api/order`、`POST /api/order/{orderNo}/pay`、`POST /api/order/{orderNo}/cancel`、`POST /api/order/{orderNo}/finish`、`GET /api/order/mine` |
| 收藏 | `POST /api/favorite/{productId}`、`GET /api/favorite/mine` |
| 文件 | `POST /api/file/upload` |
| 后台 | `GET /api/admin/users`、`GET /api/admin/products/pending`、`POST /api/admin/product/{id}/audit` |

除注册、登录及公开查询外，接口需在请求头携带 `Authorization: Bearer <token>`。

---

## 设计说明

- **鉴权**：JWT 签发与解析，拦截器统一校验，通过 ThreadLocal 传递当前用户上下文；`@RequireLogin`、`@RequireAdmin` 控制访问级别。
- **缓存**：商品详情采用 Cache Aside 模式，浏览数通过定时任务合并写回数据库，降低热点写压力。
- **并发控制**：下单流程使用 Redis 分布式锁，配合 SQL 原子扣减库存避免超卖；订单状态流转使用 CAS 更新。
- **接口治理**：基于 AOP 的操作日志与限流（Redis Lua 原子计数）。

---

## 已知限制

- 支付环节为模拟实现，接入真实支付网关时需替换 `OrderServiceImpl#pay`。
- 分类数据变更后暂未清理 `category:tree` 缓存。
- 商品详情未做空值缓存，极端场景下存在缓存穿透风险。
- 浏览历史目前仅有表结构与 Mapper，写入逻辑尚未实现。
- 后台管理仅提供基础查询与审核接口。
- 本仓库仅包含后端服务，不含前端工程。

---

## License

本项目仅用于学习与教学演示。
