package org.testspringboot.unibuy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.testspringboot.unibuy.entity.Admin;

public interface IAdminService extends IService<Admin> {
    Admin login(String username, String password);
}
