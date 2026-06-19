CREATE DATABASE IF NOT EXISTS test1 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE test1;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品表
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '商品名称',
    price DECIMAL(10, 2) NOT NULL COMMENT '价格',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    category VARCHAR(50) COMMENT '分类',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_category (category),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 订单表
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    order_no VARCHAR(64) NOT NULL UNIQUE COMMENT '订单编号',
    total_amount DECIMAL(10, 2) NOT NULL COMMENT '总金额',
    status INT NOT NULL DEFAULT 0 COMMENT '状态: 0待支付 1已支付 2已发货 3已完成 4已取消',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_order_no (order_no),
    INDEX idx_status (status),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单项表
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    quantity INT NOT NULL COMMENT '数量',
    unit_price DECIMAL(10, 2) NOT NULL COMMENT '单价',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id),
    CONSTRAINT fk_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
    CONSTRAINT fk_items_product FOREIGN KEY (product_id) REFERENCES products(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- 日志表
CREATE TABLE IF NOT EXISTS logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT COMMENT '用户ID',
    action VARCHAR(100) NOT NULL COMMENT '操作',
    description TEXT COMMENT '描述',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日志表';

-- 插入测试数据

-- 用户数据
INSERT INTO users (username, email, password) VALUES
('admin', 'admin@example.com', 'admin123'),
('zhangsan', 'zhangsan@example.com', '123456'),
('lisi', 'lisi@example.com', '123456'),
('wangwu', 'wangwu@example.com', '123456'),
('testuser', 'test@example.com', 'test123');

-- 商品数据
INSERT INTO products (name, price, stock, category) VALUES
('iPhone 15 Pro', 7999.00, 100, '手机'),
('MacBook Pro', 14999.00, 50, '电脑'),
('AirPods Pro', 1899.00, 200, '配件'),
('iPad Air', 4799.00, 80, '平板'),
('Apple Watch', 2999.00, 120, '手表'),
('华为 Mate 60', 5999.00, 150, '手机'),
('小米 14', 3999.00, 180, '手机'),
('联想 ThinkPad', 8999.00, 60, '电脑');

-- 订单数据
INSERT INTO orders (user_id, order_no, total_amount, status) VALUES
(1, 'ORD202401010001', 9898.00, 1),
(2, 'ORD202401010002', 14999.00, 2),
(3, 'ORD202401010003', 4799.00, 0),
(1, 'ORD202401010004', 1899.00, 3),
(4, 'ORD202401010005', 7999.00, 1);

-- 订单项数据
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES
(1, 1, 1, 7999.00),
(1, 3, 1, 1899.00),
(2, 2, 1, 14999.00),
(3, 4, 1, 4799.00),
(4, 3, 1, 1899.00),
(5, 1, 1, 7999.00);

-- 日志数据
INSERT INTO logs (user_id, action, description, ip_address) VALUES
(1, 'LOGIN', '用户登录成功', '127.0.0.1'),
(2, 'ORDER_CREATE', '创建订单: ORD202401010002', '192.168.1.100'),
(1, 'ORDER_PAY', '支付订单: ORD202401010001', '127.0.0.1'),
(3, 'LOGIN', '用户登录成功', '192.168.1.101');