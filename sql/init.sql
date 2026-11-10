-- ============================================================
-- 校园集市 campus_market 建库建表脚本（对应讲义第 2 章）
-- 执行方式：mysql -u root -p < init.sql  或  DBeaver 里整段执行
-- ============================================================

-- 建库：utf8mb4 + MySQL 8 起的默认排序规则
CREATE DATABASE IF NOT EXISTS campus_market
  DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
USE campus_market;

-- 用户表
CREATE TABLE `user` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `username`    VARCHAR(32)  NOT NULL COMMENT '登录名',
  `password`    VARCHAR(100) NOT NULL COMMENT 'BCrypt 哈希',
  `nickname`    VARCHAR(32)  DEFAULT NULL,
  `avatar`      VARCHAR(255) DEFAULT NULL,
  `phone`       VARCHAR(20)  DEFAULT NULL,
  `role`        TINYINT      NOT NULL DEFAULT 0 COMMENT '0-用户 1-管理员',
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1-正常 0-封禁',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 分类表
CREATE TABLE `category` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `parent_id`   BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '0-一级分类',
  `name`        VARCHAR(32) NOT NULL,
  `sort`        INT         NOT NULL DEFAULT 0,
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 商品表
CREATE TABLE `product` (
  `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`        BIGINT UNSIGNED NOT NULL COMMENT '卖家',
  `category_id`    BIGINT UNSIGNED NOT NULL,
  `title`          VARCHAR(64)   NOT NULL,
  `description`    VARCHAR(2000) DEFAULT NULL,
  `price`          DECIMAL(10,2) NOT NULL,
  `original_price` DECIMAL(10,2) DEFAULT NULL,
  `cover`          VARCHAR(255)  DEFAULT NULL,
  `images`         VARCHAR(2000) DEFAULT NULL COMMENT '逗号分隔图片地址',
  `stock`          INT           NOT NULL DEFAULT 1,
  `status`         TINYINT       NOT NULL DEFAULT 0
                   COMMENT '0-待审核 1-在售 2-已下架 3-已售出',
  `view_count`     INT           NOT NULL DEFAULT 0,
  `favorite_count` INT           NOT NULL DEFAULT 0,
  `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_cat_status` (`category_id`, `status`, `deleted`),
  KEY `idx_user` (`user_id`),
  KEY `idx_create` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 订单表
CREATE TABLE `orders` (
  `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `order_no`      VARCHAR(32)   NOT NULL,
  `buyer_id`      BIGINT UNSIGNED NOT NULL,
  `seller_id`     BIGINT UNSIGNED NOT NULL,
  `product_id`    BIGINT UNSIGNED NOT NULL,
  `product_title` VARCHAR(64)   NOT NULL COMMENT '冗余快照',
  `product_cover` VARCHAR(255)  DEFAULT NULL,
  `amount`        DECIMAL(10,2) NOT NULL,
  `status`        TINYINT       NOT NULL DEFAULT 0
                  COMMENT '0-待付款 1-待发货 2-已完成 3-已取消',
  `expire_time`   DATETIME      DEFAULT NULL COMMENT '超时取消时间',
  `pay_time`      DATETIME      DEFAULT NULL,
  `finish_time`   DATETIME      DEFAULT NULL,
  `cancel_time`   DATETIME      DEFAULT NULL,
  `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT       NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  UNIQUE KEY `uk_product_buyer` (`product_id`, `buyer_id`),
  KEY `idx_buyer` (`buyer_id`, `status`),
  KEY `idx_status_expire` (`status`, `expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 收藏表
CREATE TABLE `favorite` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED NOT NULL,
  `product_id`  BIGINT UNSIGNED NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';

-- 浏览记录
CREATE TABLE `browse_history` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED NOT NULL,
  `product_id`  BIGINT UNSIGNED NOT NULL,
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='浏览记录';

-- 操作日志
CREATE TABLE `operation_log` (
  `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT UNSIGNED DEFAULT NULL,
  `module`      VARCHAR(32)  DEFAULT NULL,
  `action`      VARCHAR(64)  DEFAULT NULL,
  `method`      VARCHAR(255) DEFAULT NULL,
  `params`      VARCHAR(2000) DEFAULT NULL,
  `cost_ms`     BIGINT       DEFAULT NULL,
  `success`     TINYINT      NOT NULL DEFAULT 1,
  `ip`          VARCHAR(64)  DEFAULT NULL,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志';

-- ============================================================
-- 初始化数据（分类树 + 演示账号）
-- 演示账号密码均为 123456，哈希由 BCryptPasswordEncoder 生成
-- ============================================================
INSERT INTO `category` (`id`, `parent_id`, `name`, `sort`) VALUES
  (1, 0, '数码电子', 10),
  (2, 0, '图书教材', 20),
  (3, 0, '生活用品', 30),
  (4, 1, '手机', 1),
  (5, 1, '笔记本电脑', 2),
  (6, 2, '专业教材', 1),
  (7, 2, '课外读物', 2);

INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `role`, `status`) VALUES
  (1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '管理员', 1, 1),
  (2, 'student', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '同学A', 0, 1);
