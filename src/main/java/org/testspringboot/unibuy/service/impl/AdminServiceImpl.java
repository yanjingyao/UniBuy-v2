package org.testspringboot.unibuy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.testspringboot.unibuy.entity.Admin;
import org.testspringboot.unibuy.mapper.AdminMapper;
import org.testspringboot.unibuy.service.IAdminService;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

    @Override
    public Admin login(String username, String password) {
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, username);
        wrapper.eq(Admin::getPassword, password);
        return this.getOne(wrapper);
    }
}
