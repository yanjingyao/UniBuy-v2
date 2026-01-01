package org.testspringboot.unibuy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.testspringboot.unibuy.entity.Merchant;

public interface IMerchantService extends IService<Merchant> {
    Merchant login(String username, String password);
    void register(Merchant merchant);
    void resetPassword(String phone, String newPassword);
}
