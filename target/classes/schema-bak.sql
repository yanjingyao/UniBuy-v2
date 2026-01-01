SET FOREIGN_KEY_CHECKS = 0;
SET NAMES utf8mb4;
-- admin DDL
CREATE TABLE `admin` (`admin_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`username` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
`password` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
`real_name` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`phone` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`role_level` TINYINT(4) NULL DEFAULT 1 Comment "0:Super Admin, 1:Normal Admin",
`create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
UNIQUE INDEX `username`(`username` ASC) USING BTREE,
PRIMARY KEY (`admin_id`)) ENGINE = MyISAM CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci AUTO_INCREMENT = 2 ROW_FORMAT = Dynamic COMMENT = "Admin Table";
-- admin_login_log DDL
CREATE TABLE `admin_login_log` (`log_id` BIGINT(20) NOT NULL AUTO_INCREMENT Comment "Primary Key",
`admin_id` BIGINT(20) NOT NULL Comment "管理员ID",
`admin_username` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL Comment "管理员用户名",
`login_ip` VARCHAR(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL Comment "登录IP地址",
`user_agent` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL Comment "用户代理信息",
`login_status` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL Comment "登录状态: SUCCESS/FAILED",
`failure_reason` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL Comment "失败原因（如果登录失败）",
`login_time` DATETIME NOT NULL Comment "登录时间",
`location` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL Comment "登录地点（可选）",
PRIMARY KEY (`log_id`)) ENGINE = MyISAM CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci AUTO_INCREMENT = 1 ROW_FORMAT = Dynamic COMMENT = "管理员登录日志表";
-- announcement DDL
CREATE TABLE `announcement` (`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`title` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
`content` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
`create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (`id`)) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci AUTO_INCREMENT = 1 ROW_FORMAT = Dynamic;
-- chat_message DDL
CREATE TABLE `chat_message` (`msg_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`order_id` BIGINT(20) NULL DEFAULT 0,
`request_id` BIGINT(20) NULL DEFAULT 0,
`sender_id` BIGINT(20) NOT NULL Comment "User ID",
`sender_role` TINYINT(4) NOT NULL Comment "1:Student, 2:Merchant, 3:Admin",
`receiver_id` BIGINT(20) NOT NULL,
`receiver_role` TINYINT(4) NULL DEFAULT 0 Comment "1:Student, 2:Merchant, 3:Admin",
`content` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`type` TINYINT(4) NULL DEFAULT 0 Comment "0:Text, 1:Image, 2:System",
`is_read` TINYINT(4) NULL DEFAULT 0,
`create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (`msg_id`)) ENGINE = MyISAM CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci AUTO_INCREMENT = 44 ROW_FORMAT = Dynamic COMMENT = "Chat Messages";
-- complaint DDL
CREATE TABLE `complaint` (`complaint_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`user_id` BIGINT(20) NOT NULL,
`user_role` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
`target_id` BIGINT(20) NOT NULL,
`type` TINYINT(4) NOT NULL,
`content` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
`proof_imgs` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
`status` TINYINT(4) NULL DEFAULT 0,
`result_note` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
`create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
`update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
PRIMARY KEY (`complaint_id`)) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci AUTO_INCREMENT = 1 ROW_FORMAT = Dynamic;
-- favorite DDL
CREATE TABLE `favorite` (`favorite_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`student_id` BIGINT(20) NOT NULL,
`product_id` BIGINT(20) NOT NULL,
`create_time` DATETIME NULL,
PRIMARY KEY (`favorite_id`)) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci AUTO_INCREMENT = 67 ROW_FORMAT = Dynamic;
-- login_log DDL
CREATE TABLE `login_log` (`log_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`admin_id` BIGINT(20) NOT NULL,
`ip_address` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
`user_agent` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
`login_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
`deleted` TINYINT(4) NULL DEFAULT 0,
PRIMARY KEY (`log_id`)) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci AUTO_INCREMENT = 1 ROW_FORMAT = Dynamic;
-- merchant DDL
CREATE TABLE `merchant` (`merchant_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`username` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
`password` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
`phone` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
`shop_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
`shop_avatar` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`shop_intro` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`pickup_location` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL Comment "Fixed pickup location on campus",
`qualification_url` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL Comment "Qualification Image URL",
`audit_status` TINYINT(4) NULL DEFAULT 0 Comment "0:Pending, 1:Pass, 2:Reject",
`create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
`update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
`deleted` TINYINT(4) NULL DEFAULT 0,
`status` INT(11) NULL DEFAULT 1 Comment "1:Active, 0:Disabled",
`ban_reason` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`ban_end_time` DATETIME NULL,
UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
UNIQUE INDEX `username`(`username` ASC) USING BTREE,
PRIMARY KEY (`merchant_id`)) ENGINE = MyISAM CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci AUTO_INCREMENT = 3 ROW_FORMAT = Dynamic COMMENT = "Merchant Table";
-- order_item DDL
CREATE TABLE `order_item` (`item_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`order_id` BIGINT(20) NOT NULL,
`product_id` BIGINT(20) NOT NULL,
`product_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`product_img` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`quantity` INT(11) NULL DEFAULT 1,
`price` DECIMAL(10,2) NULL,
PRIMARY KEY (`item_id`)) ENGINE = MyISAM CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci AUTO_INCREMENT = 18 ROW_FORMAT = Dynamic COMMENT = "Order Items";
-- orders DDL
CREATE TABLE `orders` (`order_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`order_no` VARCHAR(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
`student_id` BIGINT(20) NOT NULL,
`merchant_id` BIGINT(20) NOT NULL,
`total_price` DECIMAL(10,2) NOT NULL,
`pay_status` TINYINT(4) NULL DEFAULT 0 Comment "0:Unpaid, 1:Paid",
`order_status` TINYINT(4) NULL DEFAULT 0 Comment "0:WaitPay, 1:WaitAccept, 2:WaitPickup, 3:Done, 4:Cancelled",
`address_snapshot` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL Comment "Snapshot of address",
`source_request_id` BIGINT(20) NULL DEFAULT 0,
`voucher_img` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL Comment "Shopping Receipt Image",
`cancel_reason` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`pay_time` DATETIME NULL,
`accept_time` DATETIME NULL,
`finish_time` DATETIME NULL,
`create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
`update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
`service_fee` DECIMAL(10,2) NULL DEFAULT 0.00 Comment "Proxy Service Fee included in total",
UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
PRIMARY KEY (`order_id`)) ENGINE = MyISAM CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci AUTO_INCREMENT = 18 ROW_FORMAT = Dynamic COMMENT = "Order Table";
-- pay_record DDL
CREATE TABLE `pay_record` (`pay_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`order_id` BIGINT(20) NOT NULL,
`amount` DECIMAL(10,2) NULL,
`method` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL Comment "Balance, Proxy",
`status` TINYINT(4) NULL DEFAULT 1 Comment "1:Success",
`create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (`pay_id`)) ENGINE = MyISAM CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci AUTO_INCREMENT = 26 ROW_FORMAT = Dynamic COMMENT = "Payment Records";
-- product DDL
CREATE TABLE `product` (`product_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`merchant_id` BIGINT(20) NOT NULL Comment "FK Merchant",
`name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
`category` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL Comment "Snacks, Life, Learning, Fresh, Digital, etc.",
`specs` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL Comment "Specifications",
`original_price` DECIMAL(10,2) NULL Comment "Original Price",
`proxy_price` DECIMAL(10,2) NOT NULL Comment "Proxy Price (Selling Price)",
`stock` INT(11) NULL DEFAULT 0,
`channel` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL Comment "Source Channel (e.g. Walmart, JD)",
`description` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`images` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL Comment "Product Images (JSON or Comma separated)",
`audit_status` TINYINT(4) NULL DEFAULT 0 Comment "0:Pending, 1:Pass, 2:Reject",
`status` TINYINT(4) NULL DEFAULT 0 Comment "0:Down (Shelf), 1:Up (Shelf)",
`source_request_id` BIGINT(20) NULL DEFAULT 0 Comment "If >0, created from request",
`create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
`update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
`deleted` TINYINT(4) NULL DEFAULT 0,
PRIMARY KEY (`product_id`)) ENGINE = MyISAM CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci AUTO_INCREMENT = 87 ROW_FORMAT = Dynamic COMMENT = "Product Table";
-- profile_update_audit DDL
CREATE TABLE `profile_update_audit` (`audit_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`user_id` BIGINT(20) NOT NULL,
`role` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
`field_name` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
`old_value` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
`new_value` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
`status` TINYINT(4) NULL DEFAULT 0,
`create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (`audit_id`)) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci AUTO_INCREMENT = 1 ROW_FORMAT = Dynamic;
-- recharge_record DDL
CREATE TABLE `recharge_record` (`recharge_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`student_id` BIGINT(20) NOT NULL,
`amount` DECIMAL(10,2) NOT NULL,
`method` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL Comment "WeChat, Alipay, Bank",
`status` TINYINT(4) NULL DEFAULT 1 Comment "1:Success",
`create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
PRIMARY KEY (`recharge_id`)) ENGINE = MyISAM CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci AUTO_INCREMENT = 6 ROW_FORMAT = Dynamic COMMENT = "Student Recharge Records";
-- request_response DDL
CREATE TABLE `request_response` (`response_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`request_id` BIGINT(20) NOT NULL,
`merchant_id` BIGINT(20) NOT NULL,
`quoted_price` DECIMAL(10,2) NOT NULL,
`response_note` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`is_selected` TINYINT(4) NULL DEFAULT 0 Comment "0:No, 1:Yes",
`create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
`service_fee` DECIMAL(10,2) NULL DEFAULT 0.00 Comment "Service Fee Quote",
PRIMARY KEY (`response_id`)) ENGINE = MyISAM CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci AUTO_INCREMENT = 10 ROW_FORMAT = Dynamic COMMENT = "Merchant Responses to Requests";
-- student DDL
CREATE TABLE `student` (`student_id` BIGINT(20) NOT NULL AUTO_INCREMENT Comment "Primary Key",
`username` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL Comment "Username",
`password` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL Comment "Encrypted Password",
`phone` VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL Comment "Phone Number",
`school_id` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL Comment "Student ID Card Number",
`avatar` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL Comment "Avatar URL",
`nickname` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL Comment "Nickname",
`address_list` JSON NULL Comment "JSON list of addresses",
`create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
`update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
`deleted` TINYINT(4) NULL DEFAULT 0 Comment "Logic Delete: 1-Deleted, 0-Normal",
`status` INT(11) NULL DEFAULT 1 Comment "1:Active, 0:Disabled",
`balance` DECIMAL(10,2) NULL DEFAULT 0.00 Comment "Account Balance",
`ban_reason` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`ban_end_time` DATETIME NULL,
UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
UNIQUE INDEX `username`(`username` ASC) USING BTREE,
PRIMARY KEY (`student_id`)) ENGINE = MyISAM CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci AUTO_INCREMENT = 48 ROW_FORMAT = Dynamic COMMENT = "Student Table";
-- sys_config DDL
CREATE TABLE `sys_config` (`config_key` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
`config_value` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
`description` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
PRIMARY KEY (`config_key`)) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;
-- user_request DDL
CREATE TABLE `user_request` (`request_id` BIGINT(20) NOT NULL AUTO_INCREMENT,
`student_id` BIGINT(20) NOT NULL,
`product_name` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
`category` VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`description` TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`address` VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`expected_price` DECIMAL(10,2) NULL,
`ref_image` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL,
`deadline` DATETIME NULL Comment "Deadline",
`urgency_level` TINYINT(4) NULL DEFAULT 2 Comment "1:Low, 2:Medium (3 days), 3:High (24h)",
`min_join_users` INT(11) NULL DEFAULT 1 Comment "For group buying",
`status` TINYINT(4) NULL DEFAULT 0 Comment "0:Pending, 1:Responding, 2:Accepted, 3:Expired, 4:Completed",
`create_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
`update_time` DATETIME NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
`budget_fee` DECIMAL(10,2) NULL Comment "Proxy Fee Budget",
`purchase_location` VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL Comment "Specific Location",
PRIMARY KEY (`request_id`)) ENGINE = MyISAM CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci AUTO_INCREMENT = 9 ROW_FORMAT = Dynamic COMMENT = "User Demand Request";
-- admin DML
INSERT INTO `admin` (`admin_id`,`username`,`password`,`real_name`,`phone`,`role_level`,`create_time`) VALUES (1,'admin','admin123','Super Admin','14718245678',0,'2025-12-20 00:02:34'),(2,'adtest','adtest','adtest','888888',1,NULL),(3,'Admin1','123456','红红','888-678345',1,'2025-12-23 03:02:46');
-- admin_login_log DML
INSERT INTO `admin_login_log` (`log_id`,`admin_id`,`admin_username`,`login_ip`,`user_agent`,`login_status`,`failure_reason`,`login_time`,`location`) VALUES (1,2,'adtest','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36 Edg/143.0.0.0','SUCCESS',NULL,'2025-12-23 13:01:20',NULL),(2,1,'admin','0:0:0:0:0:0:0:1','Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36 Edg/143.0.0.0','SUCCESS',NULL,'2025-12-23 15:16:49',NULL);
-- chat_message DML
INSERT INTO `chat_message` (`msg_id`,`order_id`,`request_id`,`sender_id`,`sender_role`,`receiver_id`,`receiver_role`,`content`,`type`,`is_read`,`create_time`) VALUES (12,4,0,0,0,1,1,'订单进度：商家已接单，正在准备您的商品',2,1,NULL),(11,4,0,0,0,2,2,'新订单通知：您有一笔新订单 2002065142271021056 待接单',2,0,NULL),(5,0,1,2,2,1,1,'你好',0,1,NULL),(6,0,0,1,1,2,2,'你好，我需要一个键盘',0,1,NULL),(7,0,0,2,2,1,1,'好的',0,1,NULL),(8,3,0,0,0,2,2,'新订单通知：您有一笔新订单 2002054332459106304 待接单',2,1,NULL),(9,3,0,0,0,1,1,'订单进度：商家已接单，正在准备您的商品',2,1,NULL),(10,3,0,0,0,1,1,'订单完成：您的订单已送达/取货，欢迎评价',2,1,NULL),(13,4,0,0,0,1,1,'订单完成：您的订单已送达/取货，欢迎评价',2,1,NULL),(14,5,0,0,0,2,2,'新订单通知：您有一笔新订单 2002068123410345984 待接单',2,0,NULL),(15,0,0,1,1,1,2,'老板可以优惠一点吗\n',0,1,NULL),(16,0,0,1,2,1,1,'可以的，亲',0,1,NULL),(17,0,0,1,1,1,2,'谢谢老板',0,1,'2025-12-20 18:38:34'),(18,0,0,1,1,2,2,'1',0,1,'2025-12-20 18:54:43'),(19,0,0,2,2,1,1,'1',0,1,'2025-12-20 18:55:51'),(20,0,0,1,1,2,2,'2',0,1,'2025-12-20 18:56:39'),(21,0,0,2,2,1,1,'3',0,1,'2025-12-20 18:57:28'),(22,5,0,0,0,1,1,'订单进度：商家已接单，正在准备您的商品',2,0,NULL),(23,5,0,0,0,1,1,'订单完成：您的订单已送达/取货，欢迎评价',2,0,NULL),(24,7,0,0,0,1,2,'新订单通知：您有一笔新订单 2002785005629100032 待接单',2,0,NULL),(25,7,0,0,0,1,1,'订单进度：商家已接单，正在准备您的商品',2,0,NULL),(26,7,0,0,0,1,1,'订单完成：您的订单已送达/取货，欢迎评价',2,0,NULL),(27,10,0,0,0,1,2,'新订单通知：您有一笔新订单 2002798093539491840 待接单',2,0,NULL),(28,10,0,0,0,47,1,'订单进度：商家已接单，正在准备您的商品',2,1,NULL),(29,10,0,0,0,47,1,'订单完成：您的订单已送达/取货，欢迎评价',2,1,NULL),(30,10,0,1,2,47,1,'http://localhost:8080/files/5f271444-5387-4818-bd88-03ea18c04774.webp',1,1,'2025-12-21 17:56:40'),(31,11,0,0,0,2,2,'新订单通知：您有一笔新订单 2002802677901205504 待接单',2,0,NULL),(32,11,0,0,0,47,1,'订单进度：商家已接单，正在准备您的商品',2,1,NULL),(33,11,0,0,0,47,1,'订单完成：您的订单已送达/取货，欢迎评价',2,1,NULL),(34,14,0,0,0,2,2,'新订单通知：您有一笔新订单 2002812078695448576 待接单',2,0,NULL),(35,14,0,0,0,1,1,'订单进度：商家已接单，正在准备您的商品',2,0,NULL),(36,14,0,0,0,1,1,'订单完成：您的订单已送达/取货，欢迎评价',2,0,NULL),(37,15,0,0,0,2,2,'新订单通知：您有一笔新订单 2002813503320162304 待接单',2,0,NULL),(38,15,0,0,0,1,1,'订单进度：商家已接单，正在准备您的商品',2,0,NULL),(39,15,0,0,0,1,1,'订单完成：您的订单已送达/取货，欢迎评价',2,0,NULL),(40,16,0,0,0,2,2,'新订单通知：您有一笔新订单 2002814056188104704 待接单',2,0,NULL),(41,16,0,0,0,1,1,'订单进度：商家已接单，正在准备您的商品',2,0,NULL),(42,16,0,0,0,1,1,'订单完成：您的订单已送达/取货，欢迎评价',2,0,NULL),(43,16,0,1,1,2,2,'😊',0,1,'2025-12-21 18:54:24'),(44,0,0,47,1,0,0,'你好',0,0,'2025-12-22 09:29:29'),(45,0,0,47,1,1,3,'1',0,0,'2025-12-22 09:37:04'),(46,0,0,2,2,1,3,'1',0,1,'2025-12-22 09:38:22'),(47,0,0,1,3,2,2,'2',0,1,'2025-12-22 09:38:33'),(48,0,0,1,3,47,1,'3',0,1,'2025-12-22 09:38:37'),(49,0,0,47,1,1,3,'4',0,0,'2025-12-22 10:01:56'),(50,0,0,47,1,2,3,'1',0,1,'2025-12-22 10:26:57'),(51,11,0,0,0,2,2,'订单纠纷通知：买家申请了纠纷处理，原因：1',2,0,NULL),(52,22,0,0,0,2,2,'新订单通知：您有一笔新订单 2003178517096882176 待接单',2,0,'2025-12-23 02:59:19'),(53,22,0,0,0,1,1,'订单进度：商家已接单，正在准备您的商品',2,0,'2025-12-23 02:59:27'),(54,22,0,0,0,1,1,'订单完成：您的订单已送达/取货，欢迎评价',2,0,'2025-12-23 03:00:05'),(55,23,0,0,0,3,2,'新订单通知：您有一笔新订单 2003332637808148480 待接单',2,1,'2025-12-23 13:12:05'),(56,23,0,0,0,47,1,'订单进度：商家已接单，正在准备您的商品',2,1,'2025-12-23 13:16:43'),(57,24,0,0,0,3,2,'新订单通知：您有一笔新订单 2003360678542352384 待接单',2,1,'2025-12-23 15:03:17'),(58,24,0,0,0,47,1,'订单进度：商家已接单，正在准备您的商品',2,1,'2025-12-23 15:03:38'),(59,24,0,0,0,47,1,'订单完成：您的订单已送达/取货，欢迎评价',2,1,'2025-12-23 15:12:58'),(60,24,0,47,1,3,2,'你好，商品还有货吗',0,1,'2025-12-23 07:14:00'),(61,0,0,3,2,47,1,'有的',0,1,'2025-12-23 07:14:45');
-- favorite DML
INSERT INTO `favorite` (`favorite_id`,`student_id`,`product_id`,`create_time`) VALUES (62,1,3,NULL),(65,1,2,NULL),(66,1,80,NULL),(67,47,1,NULL),(80,1,1,'2025-12-23 03:03:57'),(105,47,144,'2025-12-23 13:11:02');
-- merchant DML
INSERT INTO `merchant` (`merchant_id`,`username`,`password`,`phone`,`shop_name`,`shop_avatar`,`shop_intro`,`pickup_location`,`qualification_url`,`audit_status`,`create_time`,`update_time`,`deleted`,`status`,`ban_reason`,`ban_end_time`) VALUES (1,'ydxmb','ydxmb','13206661234','云端小卖部',NULL,NULL,'校门口',NULL,1,NULL,NULL,0,1,NULL,NULL),(2,'cbd','123456','13226661234','茶百道','http://localhost:8080/files/8d1480f4-01bb-4fd4-91f4-2954efc8c21a.jpg',NULL,'校门口',NULL,1,NULL,NULL,0,1,NULL,NULL),(3,'mxbc','mxbc','14712822658','蜜雪冰城','http://localhost:8080/files/18e1e109-d8c2-41eb-b5ea-bfdeb6ab70bc.jpg',NULL,'第二饭堂旁边',NULL,1,'2025-12-23 13:00:36','2025-12-23 13:00:36',0,1,NULL,NULL);
-- order_item DML
INSERT INTO `order_item` (`item_id`,`order_id`,`product_id`,`product_name`,`product_img`,`quantity`,`price`) VALUES (1,1,1,NULL,NULL,1,6.00),(2,2,1,NULL,NULL,1,6.00),(3,3,2,NULL,NULL,1,10.00),(4,4,2,NULL,NULL,1,10.00),(5,5,2,NULL,NULL,1,10.00),(6,6,2,NULL,NULL,1,10.00),(7,7,1,NULL,NULL,1,6.00),(8,8,80,NULL,NULL,1,60.00),(9,9,2,NULL,NULL,1,10.00),(10,10,80,NULL,NULL,1,60.00),(11,11,2,NULL,NULL,1,10.00),(12,12,2,NULL,NULL,1,10.00),(13,13,1,NULL,NULL,1,6.00),(14,14,83,'平板电脑 [定制]',NULL,1,1000.00),(15,15,84,'被子 [定制]',NULL,1,50.00),(16,16,85,'电竞椅 [定制]',NULL,1,90.00),(17,17,86,'洗面奶 [定制]',NULL,1,35.00),(18,18,84,NULL,NULL,1,50.00),(19,19,1,'番茄','http://localhost:8080/files/3bf513ba-69ec-4ebc-a736-82d46a263465.webp',1,6.00),(20,20,2,NULL,NULL,1,10.00),(21,21,2,NULL,NULL,1,10.00),(22,22,2,'茶百道奶茶','http://localhost:8080/files/fadab672-a3ba-4a29-aac0-85ab9841aedb.jpg',1,10.00),(23,23,144,'冰鲜柠檬水','http://localhost:8080/files/80171145-755f-4adf-9877-a2e86d13ad5d.png',3,5.00),(24,24,145,'鲜橙大王桶','http://localhost:8080/files/0d82ffa6-973a-4615-90b7-589520e56a46.webp',1,8.00);
-- orders DML
INSERT INTO `orders` (`order_id`,`order_no`,`student_id`,`merchant_id`,`total_price`,`pay_status`,`order_status`,`address_snapshot`,`source_request_id`,`voucher_img`,`cancel_reason`,`pay_time`,`accept_time`,`finish_time`,`create_time`,`update_time`,`service_fee`) VALUES (1,'2002047620943560704',1,1,6.00,0,4,NULL,0,NULL,'',NULL,NULL,NULL,NULL,NULL,0.00),(2,'2002053693905682432',1,1,6.00,0,4,NULL,0,NULL,'',NULL,NULL,NULL,NULL,NULL,0.00),(3,'2002054332459106304',1,2,10.00,1,4,NULL,0,'http://localhost:8080/files/03697e28-d4a3-4971-86f4-45f5aaba4080.jpg','1 [Admin Refund: ]','2025-12-20 00:32:09','2025-12-20 00:32:40','2025-12-20 00:34:10',NULL,'2025-12-23 02:39:07',0.00),(4,'2002065142271021056',1,2,10.00,1,4,NULL,0,'http://localhost:8080/files/435b61f5-4493-4408-8654-10cd7e62f5f5.jpg','10 [Admin Refund: ]','2025-12-20 01:15:06','2025-12-20 01:15:25','2025-12-20 01:15:39',NULL,'2025-12-23 02:58:01',0.00),(5,'2002068123410345984',1,2,10.00,1,3,NULL,0,NULL,NULL,'2025-12-20 01:26:57','2025-12-21 02:59:14','2025-12-21 03:04:57',NULL,NULL,0.00),(6,'2002452706366660608',1,2,10.00,0,4,NULL,0,NULL,'',NULL,NULL,NULL,NULL,NULL,0.00),(7,'2002785005629100032',1,1,6.00,1,3,NULL,0,NULL,NULL,'2025-12-22 00:57:47','2025-12-22 00:58:18','2025-12-22 01:43:04',NULL,NULL,0.00),(8,'2002797186579972096',1,1,60.00,0,4,NULL,0,NULL,'',NULL,NULL,NULL,NULL,NULL,0.00),(9,'2002797305035505664',1,2,10.00,0,4,NULL,0,NULL,'',NULL,NULL,NULL,NULL,NULL,0.00),(10,'2002798093539491840',47,1,60.00,1,3,NULL,0,NULL,NULL,'2025-12-22 01:55:17','2025-12-22 01:55:44','2025-12-22 01:56:02',NULL,NULL,0.00),(11,'2002802677901205504',47,2,10.00,1,5,'啊坤 15677771234 32栋宿舍楼 202',0,NULL,'Dispute: 1','2025-12-22 02:26:40','2025-12-22 02:27:30','2025-12-22 02:27:49',NULL,NULL,0.00),(12,'2002803111374135296',47,2,10.00,0,4,NULL,0,NULL,'',NULL,NULL,NULL,NULL,NULL,0.00),(13,'2002810084979130368',1,1,6.00,0,4,NULL,0,NULL,'',NULL,NULL,NULL,NULL,NULL,0.00),(14,'2002812078695448576',1,2,1000.00,1,3,'阿耶 13209991234 东区宿舍楼31506',0,NULL,NULL,'2025-12-22 02:43:36','2025-12-22 02:43:49','2025-12-22 02:44:09',NULL,NULL,0.00),(15,'2002813503320162304',1,2,50.00,1,3,'阿耶 13209991234 东区宿舍楼31506',0,NULL,NULL,'2025-12-22 02:48:55','2025-12-22 02:49:06','2025-12-22 02:51:46',NULL,NULL,0.00),(16,'2002814056188104704',1,2,90.00,1,3,'阿耶 13209991234 东区宿舍楼31506',0,NULL,NULL,'2025-12-22 02:52:43','2025-12-22 02:52:58','2025-12-22 02:53:39',NULL,NULL,0.00),(17,'2002818032010809344',1,2,35.00,0,0,'阿耶 13209991234 东区宿舍楼31506',0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00),(18,'2003017574928920576',47,2,50.00,0,0,'啊坤 15677771234 32栋宿舍楼 202',0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00),(19,'2003059173419417600',47,1,6.00,0,0,NULL,0,NULL,NULL,NULL,NULL,NULL,'2025-12-22 19:04:57',NULL,0.00),(20,'2003163260450320384',1,2,10.00,0,0,NULL,0,NULL,NULL,NULL,NULL,NULL,NULL,NULL,0.00),(21,'2003173870336602112',1,2,10.00,0,0,'阿耶 13209991234 东区宿舍楼31506',0,NULL,NULL,NULL,NULL,NULL,'2025-12-23 02:40:43','2025-12-23 02:40:43',0.00),(22,'2003178517096882176',1,2,10.00,1,3,'阿耶 13209991234 东区宿舍楼31506',0,NULL,NULL,'2025-12-23 02:59:19','2025-12-23 02:59:27','2025-12-23 03:00:05','2025-12-23 02:59:10','2025-12-23 02:59:10',0.00),(23,'2003332637808148480',47,3,15.00,1,2,'啊坤 15677771234 32栋宿舍楼 204',0,'http://localhost:8080/files/b0d73a5e-72d4-48de-b11b-3affb4a1005b.png',NULL,'2025-12-23 13:12:05','2025-12-23 13:16:43',NULL,'2025-12-23 13:11:36','2025-12-23 13:11:36',0.00),(24,'2003360678542352384',47,3,8.00,1,3,'啊坤 15677771234 32栋宿舍楼 204',0,'http://localhost:8080/files/a12fcbf2-ca3d-4c6d-b23c-a00132b65713.webp',NULL,'2025-12-23 15:03:17','2025-12-23 15:03:38','2025-12-23 15:12:58','2025-12-23 15:03:01','2025-12-23 15:03:01',0.00);
-- pay_record DML
INSERT INTO `pay_record` (`pay_id`,`order_id`,`amount`,`method`,`status`,`create_time`) VALUES (1,3,10.00,'balance',1,NULL),(2,4,10.00,'balance',1,NULL),(3,5,10.00,'balance',1,NULL),(4,5,10.00,'Balance',1,NULL),(5,7,6.00,'balance',1,NULL),(6,7,6.00,'Balance',1,NULL),(7,10,60.00,'balance',1,NULL),(8,10,60.00,'balance',1,NULL),(9,10,60.00,'balance',1,NULL),(10,10,60.00,'balance',1,NULL),(11,10,60.00,'balance',1,NULL),(12,10,60.00,'balance',1,NULL),(13,10,60.00,'proxy',1,NULL),(14,10,60.00,'balance',1,NULL),(15,10,60.00,'Balance',1,NULL),(16,11,10.00,'balance',1,NULL),(17,11,10.00,'Balance',1,NULL),(18,14,1000.00,'balance',1,NULL),(19,14,1000.00,'balance',1,NULL),(20,14,1000.00,'Balance',1,NULL),(21,15,50.00,'balance',1,NULL),(22,15,50.00,'Balance',1,NULL),(23,16,90.00,'balance',1,NULL),(24,16,90.00,'balance',1,NULL),(25,16,90.00,'Balance',1,NULL),(26,22,10.00,'balance',1,'2025-12-23 02:59:19'),(27,22,10.00,'Balance',1,'2025-12-23 02:59:19'),(28,23,15.00,'balance',1,'2025-12-23 13:12:05'),(29,23,15.00,'Balance',1,'2025-12-23 13:12:05'),(30,24,8.00,'balance',1,'2025-12-23 15:03:17'),(31,24,8.00,'Balance',1,'2025-12-23 15:03:17');
-- product DML
INSERT INTO `product` (`product_id`,`merchant_id`,`name`,`category`,`specs`,`original_price`,`proxy_price`,`stock`,`channel`,`description`,`images`,`audit_status`,`status`,`source_request_id`,`create_time`,`update_time`,`deleted`) VALUES (1,1,'番茄','生鲜水果','1kg',5.00,6.00,1,'超市','1111','http://localhost:8080/files/3e873ddd-a226-46c4-92b6-84f816484b74.webp',1,1,0,NULL,'2025-12-23 03:07:08',0),(2,2,'茶百道奶茶','零食饮料','1kg',6.00,10.00,1,'茶百道','茶百道奶茶','http://localhost:8080/files/fadab672-a3ba-4a29-aac0-85ab9841aedb.jpg',1,1,0,NULL,NULL,0),(145,3,'鲜橙大王桶','零食饮料','1000ml',6.00,8.00,2,'蜜雪冰城','商家代购','http://localhost:8080/files/0d82ffa6-973a-4615-90b7-589520e56a46.webp',1,1,0,'2025-12-23 15:01:09','2025-12-23 15:01:09',0),(144,3,'冰鲜柠檬水','生活用品','500ml',4.00,5.00,3,'蜜雪冰城','','http://localhost:8080/files/80171145-755f-4adf-9877-a2e86d13ad5d.png',1,1,0,'2025-12-23 13:07:20','2025-12-23 13:07:20',0),(86,2,'洗面奶 [定制]','日用百货',NULL,30.00,35.00,1,'Request Conversion',NULL,NULL,1,0,7,NULL,NULL,0),(85,2,'电竞椅 [定制]','日用百货',NULL,90.00,90.00,1,'Request Conversion',NULL,NULL,1,0,6,NULL,NULL,0),(84,2,'被子 [定制]','日用百货',NULL,50.00,50.00,1,'Request Conversion',NULL,NULL,1,1,5,NULL,NULL,0),(83,2,'平板电脑 [定制]','数码产品',NULL,1000.00,1000.00,1,'Request Conversion',NULL,NULL,1,1,4,NULL,NULL,0),(82,2,'ps5 [定制]','数码产品',NULL,0.00,20.00,1,'Request Conversion',NULL,NULL,1,0,3,NULL,NULL,0),(81,2,'单肩包 [Requested]','其他',NULL,50.00,50.00,1,'Request Conversion',NULL,NULL,1,0,2,NULL,NULL,0),(80,1,'键盘','数码配件','1',50.00,60.00,1,'拼多多','','http://localhost:8080/files/229f3174-dc80-4057-b77c-3f8d8f3b64b6.webp',1,1,0,NULL,NULL,0);
-- recharge_record DML
INSERT INTO `recharge_record` (`recharge_id`,`student_id`,`amount`,`method`,`status`,`create_time`) VALUES (1,1,100.00,'WeChat',1,NULL),(2,1,50.00,'WeChat',1,NULL),(3,47,100.00,'WeChat',1,NULL),(4,1,1000.00,'WeChat',1,NULL),(5,1,100.00,'WeChat',1,NULL),(6,1,10.00,'Refund',1,'2025-12-23 02:58:13');
-- request_response DML
INSERT INTO `request_response` (`response_id`,`request_id`,`merchant_id`,`quoted_price`,`response_note`,`is_selected`,`create_time`,`service_fee`) VALUES (1,2,2,50.00,'',1,NULL,0.00),(2,3,2,20.00,'',1,NULL,0.00),(3,4,2,1000.00,'',1,NULL,0.00),(4,5,2,50.00,'',1,NULL,0.00),(5,6,2,90.00,'',1,NULL,0.00),(6,7,2,30.00,'',0,NULL,0.00),(7,7,2,30.00,'',0,NULL,0.00),(8,7,2,30.00,'',0,NULL,0.00),(9,7,2,30.00,'',1,NULL,5.00),(10,8,2,350.00,'',0,NULL,20.00),(11,9,2,50.00,'有现货',0,'2025-12-23 04:50:56',10.00),(12,9,1,50.00,'下午可以送',0,'2025-12-23 04:52:36',5.00);
-- student DML
INSERT INTO `student` (`student_id`,`username`,`password`,`phone`,`school_id`,`avatar`,`nickname`,`address_list`,`create_time`,`update_time`,`deleted`,`status`,`balance`,`ban_reason`,`ban_end_time`) VALUES (1,'15277776123','123456','15277776123','77776123','http://localhost:8080/files/eb4c0d5b-539c-45dc-99b6-9cff329f766d.jpg','aye','[{"name": "阿耶", "phone": "13209991234", "detail": "东区宿舍楼31506", "isDefault": true}]',NULL,'2025-12-23 02:58:13',0,1,94.00,'1111','2025-12-24 04:09:21'),(47,'kunkun','123456','18309871234','221314',NULL,'kunkun','[{"name": "啊坤", "phone": "15677771234", "detail": "32栋宿舍楼 204", "isDefault": true}]',NULL,'2025-12-23 13:12:05',0,1,7.00,'测试两天','2025-12-24 16:58:17'),(69,'test_user_1766432284704','password','13832284704','sid_1766432284704',NULL,'Test Student',NULL,'2025-12-23 03:38:05','2025-12-23 03:38:05',0,1,0.00,NULL,NULL);
-- sys_config DML
INSERT INTO `sys_config` (`config_key`,`config_value`,`description`) VALUES ('product_categories','["零食","饮料","生活用品","数码电子","书籍文具","二手书"]','Product Category List'),('system_phone','555-9527888','Customer Service Phone');
-- user_request DML
INSERT INTO `user_request` (`request_id`,`student_id`,`product_name`,`category`,`description`,`address`,`expected_price`,`ref_image`,`deadline`,`urgency_level`,`min_join_users`,`status`,`create_time`,`update_time`,`budget_fee`,`purchase_location`) VALUES (1,1,'键盘','数码产品','','东区宿舍楼31506 阿耶 13209991234',88.00,NULL,'2025-12-21 00:29:06',3,1,3,NULL,NULL,NULL,NULL),(2,1,'单肩包','其他','黑色','东区宿舍楼31506 阿耶 13209991234',50.00,NULL,'2025-12-25 01:05:35',2,1,2,NULL,NULL,0.00,''),(3,1,'ps5','数码产品','白色','东区宿舍楼31506 阿耶 13209991234',0.00,NULL,'2025-12-27 02:33:11',1,1,2,NULL,NULL,0.00,''),(4,1,'平板电脑','数码产品','128g','东区宿舍楼31506 阿耶 13209991234',1000.00,NULL,'2025-12-27 02:42:14',1,1,2,NULL,NULL,0.00,''),(5,1,'被子','日用百货','','东区宿舍楼31506 阿耶 13209991234',50.00,NULL,'2025-12-25 02:48:08',2,1,2,NULL,NULL,0.00,''),(6,1,'电竞椅','日用百货','','东区宿舍楼31506 阿耶 13209991234',90.00,NULL,'2025-12-25 02:50:17',2,1,2,NULL,NULL,0.00,''),(7,1,'洗面奶','日用百货','','东区宿舍楼31506 阿耶 13209991234',30.00,NULL,'2025-12-25 02:57:19',2,1,2,NULL,NULL,0.00,''),(8,47,'显示器','数码产品','','32栋宿舍楼 202 啊坤 15677771234',350.00,NULL,'2025-12-25 16:16:07',2,1,1,NULL,NULL,0.00,''),(9,1,'遥控赛车','数码产品','白色','东区宿舍楼31506 阿耶 13209991234',50.00,NULL,'2025-12-28 04:41:22',1,2,1,'2025-12-23 04:41:22','2025-12-23 04:41:22',0.00,'');
SET FOREIGN_KEY_CHECKS = 1;
