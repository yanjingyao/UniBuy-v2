package org.testspringboot.unibuy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.testspringboot.unibuy.entity.Merchant;
import org.testspringboot.unibuy.mapper.MerchantMapper;
import org.testspringboot.unibuy.service.IMerchantService;

@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements IMerchantService {

    @Override
    public Merchant login(String username, String password) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getUsername, username);
        wrapper.eq(Merchant::getPassword, password);
        Merchant merchant = this.getOne(wrapper);
        if (merchant != null && merchant.getAuditStatus() != 1) {
            throw new RuntimeException("Merchant not audited or rejected");
        }
        return merchant;
    }

    @Override
    public void register(Merchant merchant) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getUsername, merchant.getUsername());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("Username already exists");
        }
        merchant.setAuditStatus(0); // Pending
        this.save(merchant);
    }

    @Override
    public void resetPassword(String phone, String newPassword) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getPhone, phone);
        Merchant merchant = this.getOne(wrapper);
        if (merchant == null) {
            throw new RuntimeException("Phone number not found");
        }
        merchant.setPassword(newPassword);
        this.updateById(merchant);
    }
}
