package org.testspringboot.unibuy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Checking database schema...");

        // 1. Add description column to product table if not exists
        try {
            jdbcTemplate.execute("ALTER TABLE product ADD COLUMN description VARCHAR(500) AFTER channel");
            System.out.println("Added description column to product table.");
        } catch (Exception e) {
            // Column likely exists or other error, ignore
            if (!e.getMessage().contains("Duplicate column")) {
               // System.out.println("Note: " + e.getMessage());
            }
        }

        // 2. Create favorite table if not exists
        String createFavoriteSql = "CREATE TABLE IF NOT EXISTS `favorite` (" +
                "`favorite_id` bigint(20) NOT NULL AUTO_INCREMENT," +
                "`student_id` bigint(20) NOT NULL," +
                "`product_id` bigint(20) NOT NULL," +
                "`create_time` datetime DEFAULT NULL," +
                "PRIMARY KEY (`favorite_id`)," +
                "UNIQUE KEY `uk_student_product` (`student_id`, `product_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        
        try {
            jdbcTemplate.execute(createFavoriteSql);
            System.out.println("Ensured favorite table exists.");
            
            // Fix: Rename id to favorite_id if old version exists
            try {
                jdbcTemplate.execute("ALTER TABLE favorite CHANGE COLUMN id favorite_id BIGINT AUTO_INCREMENT");
                System.out.println("Migrated favorite table: id -> favorite_id");
            } catch (Exception e) {
                // Column id probably doesn't exist (already favorite_id), ignore
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Create chat_message table if not exists
        String createChatSql = "CREATE TABLE IF NOT EXISTS `chat_message` (" +
                "`msg_id` bigint(20) NOT NULL AUTO_INCREMENT," +
                "`order_id` bigint(20) DEFAULT NULL," +
                "`request_id` bigint(20) DEFAULT NULL," +
                "`sender_id` bigint(20) NOT NULL," +
                "`sender_role` int(11) NOT NULL COMMENT '1:Student, 2:Merchant'," +
                "`receiver_id` bigint(20) NOT NULL," +
                "`content` text," +
                "`type` int(11) DEFAULT '0' COMMENT '0:Text, 1:Image'," +
                "`is_read` int(11) DEFAULT '0'," +
                "`create_time` datetime DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`msg_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";

        try {
            jdbcTemplate.execute(createChatSql);
            System.out.println("Ensured chat_message table exists.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 4. Add address column to user_request table if not exists
        try {
            jdbcTemplate.execute("ALTER TABLE user_request ADD COLUMN address VARCHAR(500) AFTER description");
            System.out.println("Added address column to user_request table.");
        } catch (Exception e) {
            if (!e.getMessage().contains("Duplicate column")) {
               // ignore
            }
        }
        
        // 5. Add status to student and merchant tables
        try {
            jdbcTemplate.execute("ALTER TABLE student ADD COLUMN status INT DEFAULT 1 COMMENT '1:Active, 0:Disabled'");
            jdbcTemplate.execute("ALTER TABLE merchant ADD COLUMN status INT DEFAULT 1 COMMENT '1:Active, 0:Disabled'");
            System.out.println("Added status column to user tables.");
        } catch (Exception e) {
            // ignore
        }

        // 6. Create announcement table
        String createAnnouncementSql = "CREATE TABLE IF NOT EXISTS `announcement` (" +
                "`id` bigint(20) NOT NULL AUTO_INCREMENT," +
                "`title` varchar(255) NOT NULL," +
                "`content` text," +
                "`create_time` datetime DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        try {
            jdbcTemplate.execute(createAnnouncementSql);
        } catch (Exception e) { e.printStackTrace(); }

        // 7. Create sys_config table
        String createConfigSql = "CREATE TABLE IF NOT EXISTS `sys_config` (" +
                "`config_key` varchar(50) NOT NULL," +
                "`config_value` varchar(255) DEFAULT NULL," +
                "`description` varchar(255) DEFAULT NULL," +
                "PRIMARY KEY (`config_key`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        try {
            jdbcTemplate.execute(createConfigSql);
        } catch (Exception e) { e.printStackTrace(); }

        // 8. Create login_log table
        String createLoginLogSql = "CREATE TABLE IF NOT EXISTS `login_log` (" +
                "`log_id` bigint(20) NOT NULL AUTO_INCREMENT," +
                "`user_id` bigint(20) DEFAULT NULL," +
                "`username` varchar(255) DEFAULT NULL," +
                "`role` varchar(50) DEFAULT NULL," +
                "`ip` varchar(100) DEFAULT NULL," +
                "`user_agent` varchar(500) DEFAULT NULL," +
                "`status` varchar(20) DEFAULT NULL," +
                "`msg` varchar(255) DEFAULT NULL," +
                "`create_time` datetime DEFAULT CURRENT_TIMESTAMP," +
                "PRIMARY KEY (`log_id`)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
        try {
            jdbcTemplate.execute(createLoginLogSql);
            System.out.println("Ensured login_log table exists.");
        } catch (Exception e) { e.printStackTrace(); }
        
        System.out.println("Database schema check completed.");
    }
}
