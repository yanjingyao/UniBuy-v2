package org.testspringboot.unibuy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.testspringboot.unibuy.entity.RechargeRecord;
import org.testspringboot.unibuy.entity.Student;
import java.math.BigDecimal;
import java.util.List;

public interface IStudentService extends IService<Student> {
    Student login(String username, String password);
    void register(Student student);
    void resetPassword(String phone, String newPassword);
    
    // Wallet
    BigDecimal getBalance(Long studentId);
    void recharge(Long studentId, BigDecimal amount, String method);
    void deductBalance(Long studentId, BigDecimal amount);
    List<RechargeRecord> getRechargeHistory(Long studentId);
}
