-- Database Initialization
CREATE DATABASE IF NOT EXISTS `unibuy` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `unibuy`;

-- 1. Student Table
CREATE TABLE IF NOT EXISTS `student` (
    `student_id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT 'Username',
    `password` VARCHAR(100) NOT NULL COMMENT 'Encrypted Password',
    `phone` VARCHAR(20) NOT NULL UNIQUE COMMENT 'Phone Number',
    `school_id` VARCHAR(50) NOT NULL COMMENT 'Student ID Card Number',
    `avatar` VARCHAR(255) COMMENT 'Avatar URL',
    `nickname` VARCHAR(50) COMMENT 'Nickname',
    `balance` DECIMAL(10, 2) DEFAULT 0.00 COMMENT 'Account Balance',
    `address_list` JSON COMMENT 'JSON list of addresses',
    `status` TINYINT DEFAULT 1 COMMENT '1:Active, 0:Disabled',
    `ban_reason` VARCHAR(255) COMMENT '封禁原因',
    `ban_end_time` DATETIME COMMENT '封禁结束时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0 COMMENT 'Logic Delete: 1-Deleted, 0-Normal'
) COMMENT 'Student Table';

-- 2. Merchant Table
CREATE TABLE IF NOT EXISTS `merchant` (
    `merchant_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20) NOT NULL UNIQUE,
    `shop_name` VARCHAR(100) NOT NULL,
    `shop_avatar` VARCHAR(255),
    `shop_intro` TEXT,
    `pickup_location` VARCHAR(255) NOT NULL COMMENT 'Fixed pickup location on campus',
    `qualification_url` VARCHAR(255) COMMENT 'Qualification Image URL',
    `audit_status` TINYINT DEFAULT 0 COMMENT '0:Pending, 1:Pass, 2:Reject',
    `status` TINYINT DEFAULT 1 COMMENT '1:Active, 0:Disabled',
    `ban_reason` VARCHAR(255) COMMENT '封禁原因',
    `ban_end_time` DATETIME COMMENT '封禁结束时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) COMMENT 'Merchant Table';

-- 3. Admin Table
CREATE TABLE IF NOT EXISTS `admin` (
    `admin_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(100) NOT NULL,
    `real_name` VARCHAR(50),
    `phone` VARCHAR(20),
    `role_level` TINYINT DEFAULT 1 COMMENT '0:Super Admin, 1:Normal Admin',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT 'Admin Table';

-- Initial Admin
INSERT IGNORE INTO `admin` (`username`, `password`, `real_name`, `role_level`) VALUES ('admin', 'admin123', 'Super Admin', 0);

-- 4. Product Table
CREATE TABLE IF NOT EXISTS `product` (
    `product_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `merchant_id` BIGINT NOT NULL COMMENT 'FK Merchant',
    `name` VARCHAR(100) NOT NULL,
    `category` VARCHAR(50) NOT NULL COMMENT 'Snacks, Life, Learning, Fresh, Digital, etc.',
    `specs` VARCHAR(100) COMMENT 'Specifications',
    `original_price` DECIMAL(10, 2) COMMENT 'Original Price',
    `proxy_price` DECIMAL(10, 2) NOT NULL COMMENT 'Proxy Price (Selling Price)',
    `stock` INT DEFAULT 0,
    `channel` VARCHAR(100) COMMENT 'Source Channel (e.g. Walmart, JD)',
    `images` TEXT COMMENT 'Product Images (JSON or Comma separated)',
    `audit_status` TINYINT DEFAULT 0 COMMENT '0:Pending, 1:Pass, 2:Reject',
    `status` TINYINT DEFAULT 0 COMMENT '0:Down (Shelf), 1:Up (Shelf)',
    `source_request_id` BIGINT DEFAULT 0 COMMENT 'If >0, created from request',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0
) COMMENT 'Product Table';

-- 5. User Request Table (Demand)
CREATE TABLE IF NOT EXISTS `user_request` (
    `request_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id` BIGINT NOT NULL,
    `product_name` VARCHAR(100) NOT NULL,
    `category` VARCHAR(50),
    `description` TEXT,
    `expected_price` DECIMAL(10, 2),
    `ref_image` VARCHAR(255),
    `deadline` DATETIME COMMENT 'Deadline',
    `urgency_level` TINYINT DEFAULT 2 COMMENT '1:Low, 2:Medium (3 days), 3:High (24h)',
    `min_join_users` INT DEFAULT 1 COMMENT 'For group buying',
    `status` TINYINT DEFAULT 0 COMMENT '0:Pending, 1:Responding, 2:Accepted, 3:Expired, 4:Completed',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT 'User Demand Request';

-- 6. Request Response Table
CREATE TABLE IF NOT EXISTS `request_response` (
    `response_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `request_id` BIGINT NOT NULL,
    `merchant_id` BIGINT NOT NULL,
    `quoted_price` DECIMAL(10, 2) NOT NULL,
    `response_note` TEXT,
    `is_selected` TINYINT DEFAULT 0 COMMENT '0:No, 1:Yes',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT 'Merchant Responses to Requests';

-- 7. Order Table
CREATE TABLE IF NOT EXISTS `orders` (
    `order_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_no` VARCHAR(64) NOT NULL UNIQUE,
    `student_id` BIGINT NOT NULL,
    `merchant_id` BIGINT NOT NULL,
    `total_price` DECIMAL(10, 2) NOT NULL,
    `service_fee` DECIMAL(10, 2) DEFAULT 0.00 COMMENT 'Proxy Service Fee included in total',
    `pay_status` TINYINT DEFAULT 0 COMMENT '0:Unpaid, 1:Paid',
    `order_status` TINYINT DEFAULT 0 COMMENT '0:WaitPay, 1:WaitAccept, 2:WaitPickup, 3:Done, 4:Cancelled',
    `address_snapshot` TEXT COMMENT 'Snapshot of address',
    `source_request_id` BIGINT DEFAULT 0,
    `voucher_img` VARCHAR(255) COMMENT 'Shopping Receipt Image',
    `cancel_reason` VARCHAR(255),
    `pay_time` DATETIME,
    `accept_time` DATETIME,
    `finish_time` DATETIME,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) COMMENT 'Order Table';

-- 8. Order Item Table
CREATE TABLE IF NOT EXISTS `order_item` (
    `item_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `product_name` VARCHAR(100),
    `product_img` VARCHAR(255),
    `quantity` INT DEFAULT 1,
    `price` DECIMAL(10, 2)
) COMMENT 'Order Items';

-- 9. Chat Message
CREATE TABLE IF NOT EXISTS `chat_message` (
    `msg_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT DEFAULT 0,
    `request_id` BIGINT DEFAULT 0,
    `sender_id` BIGINT NOT NULL COMMENT 'User ID',
    `sender_role` TINYINT NOT NULL COMMENT '1:Student, 2:Merchant, 3:Admin',
    `receiver_id` BIGINT NOT NULL,
    `receiver_role` TINYINT DEFAULT 0 COMMENT '1:Student, 2:Merchant, 3:Admin',
    `content` TEXT,
    `type` TINYINT DEFAULT 0 COMMENT '0:Text, 1:Image, 2:System',
    `is_read` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT 'Chat Messages';

-- 10. Reviews
CREATE TABLE IF NOT EXISTS `reviews` (
    `review_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `student_id` BIGINT NOT NULL,
    `merchant_id` BIGINT NOT NULL,
    `score_attitude` INT DEFAULT 5,
    `score_quality` INT DEFAULT 5,
    `score_speed` INT DEFAULT 5,
    `comment` TEXT,
    `reply` TEXT,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT 'Order Reviews';

-- 11. Pay Record
CREATE TABLE IF NOT EXISTS `pay_record` (
    `pay_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `order_id` BIGINT NOT NULL,
    `amount` DECIMAL(10, 2),
    `method` VARCHAR(20) COMMENT 'Balance, Proxy',
    `status` TINYINT DEFAULT 1 COMMENT '1:Success',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT 'Payment Records';

-- 12. Recharge Record
CREATE TABLE IF NOT EXISTS `recharge_record` (
    `recharge_id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `student_id` BIGINT NOT NULL,
    `amount` DECIMAL(10, 2) NOT NULL,
    `method` VARCHAR(20) COMMENT 'WeChat, Alipay, Bank',
    `status` TINYINT DEFAULT 1 COMMENT '1:Success',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP
) COMMENT 'Student Recharge Records';

-- 13. Favorite Table
CREATE TABLE IF NOT EXISTS `favorite` (
    `favorite_id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key',
    `student_id` BIGINT NOT NULL COMMENT 'FK to student table',
    `product_id` BIGINT NOT NULL COMMENT 'FK to product table',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Favorite creation time',
    UNIQUE KEY `uk_student_product` (`student_id`, `product_id`),
    INDEX `idx_student_id` (`student_id`),
    INDEX `idx_product_id` (`product_id`)
) COMMENT 'Student Favorite Products';

-- 14. Complaint Table
CREATE TABLE IF NOT EXISTS `complaint` (
    `complaint_id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key',
    `complainant_id` BIGINT NOT NULL COMMENT '投诉人ID',
    `complainant_role` VARCHAR(20) NOT NULL COMMENT '投诉人角色: student/merchant',
    `respondent_id` BIGINT NOT NULL COMMENT '被投诉人ID',
    `respondent_role` VARCHAR(20) NOT NULL COMMENT '被投诉人角色: student/merchant/admin',
    `complaint_type` VARCHAR(50) NOT NULL COMMENT '投诉类型',
    `complaint_content` TEXT NOT NULL COMMENT '投诉内容',
    `evidence_urls` TEXT COMMENT '证据图片URLs，JSON格式',
    `status` TINYINT DEFAULT 0 COMMENT '状态: 0-待处理, 1-已解决, 2-已拒绝',
    `result_note` TEXT COMMENT '处理结果说明',
    `processor_id` BIGINT COMMENT '处理人ID（管理员）',
    `process_time` DATETIME COMMENT '处理时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记'
) COMMENT '投诉表';

-- 15. Profile Update Audit Table
CREATE TABLE IF NOT EXISTS `profile_update_audit` (
    `audit_id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role` VARCHAR(20) NOT NULL COMMENT '用户角色: student/merchant',
    `field_name` VARCHAR(50) NOT NULL COMMENT '修改的字段名',
    `old_value` TEXT COMMENT '原值',
    `new_value` TEXT COMMENT '新值',
    `reason` TEXT COMMENT '修改原因',
    `evidence_urls` TEXT COMMENT '证明材料URLs，JSON格式',
    `status` TINYINT DEFAULT 0 COMMENT '审核状态: 0-待审核, 1-通过, 2-拒绝',
    `audit_note` TEXT COMMENT '审核备注',
    `auditor_id` BIGINT COMMENT '审核人ID（管理员）',
    `audit_time` DATETIME COMMENT '审核时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除标记'
) COMMENT '资料更新审核表';

-- 16. Admin Login Log Table
CREATE TABLE IF NOT EXISTS `admin_login_log` (
    `log_id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary Key',
    `admin_id` BIGINT NOT NULL COMMENT '管理员ID',
    `admin_username` VARCHAR(50) NOT NULL COMMENT '管理员用户名',
    `login_ip` VARCHAR(45) COMMENT '登录IP地址',
    `user_agent` TEXT COMMENT '用户代理信息',
    `login_status` VARCHAR(20) NOT NULL COMMENT '登录状态: SUCCESS/FAILED',
    `failure_reason` VARCHAR(255) COMMENT '失败原因（如果登录失败）',
    `login_time` DATETIME NOT NULL COMMENT '登录时间',
    `location` VARCHAR(100) COMMENT '登录地点（可选）'
) COMMENT '管理员登录日志表';
