package org.testspringboot.unibuy.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.testspringboot.unibuy.entity.LoginLog;
import org.testspringboot.unibuy.mapper.LoginLogMapper;
import org.testspringboot.unibuy.service.ILoginLogService;

import java.time.LocalDateTime;

@Service
public class LoginLogServiceImpl extends ServiceImpl<LoginLogMapper, LoginLog> implements ILoginLogService {

    @Override
    public void log(Long userId, String username, String role, String ip, String userAgent, boolean success, String msg) {
        try {
            LoginLog log = new LoginLog();
            log.setUserId(userId);
            log.setUsername(username);
            log.setRole(role);
            log.setIp(ip);
            log.setUserAgent(userAgent);
            log.setStatus(success ? "SUCCESS" : "FAIL");
            log.setMsg(msg);
            log.setCreateTime(LocalDateTime.now());
            save(log);
        } catch (Exception e) {
            e.printStackTrace(); // Log failure shouldn't stop flow
        }
    }
}
