package org.testspringboot.unibuy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.testspringboot.unibuy.entity.LoginLog;

public interface ILoginLogService extends IService<LoginLog> {
    void log(Long userId, String username, String role, String ip, String userAgent, boolean success, String msg);
}
