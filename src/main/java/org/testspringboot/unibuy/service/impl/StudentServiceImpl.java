package org.testspringboot.unibuy.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.testspringboot.unibuy.entity.RechargeRecord;
import org.testspringboot.unibuy.entity.Student;
import org.testspringboot.unibuy.mapper.RechargeRecordMapper;
import org.testspringboot.unibuy.mapper.StudentMapper;
import org.testspringboot.unibuy.service.IStudentService;

import java.math.BigDecimal;
import java.util.List;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements IStudentService {

    @Autowired
    private RechargeRecordMapper rechargeRecordMapper;

    @Override
    public Student login(String username, String password) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getUsername, username);
        wrapper.eq(Student::getPassword, password); // In real app, use BCrypt
        return this.getOne(wrapper);
    }

    @Override
    public void register(Student student) {
        // Check if username exists
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getUsername, student.getUsername());
        if (this.count(wrapper) > 0) {
            throw new RuntimeException("Username already exists");
        }
        this.save(student);
    }

    @Override
    public void resetPassword(String phone, String newPassword) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getPhone, phone);
        Student student = this.getOne(wrapper);
        if (student == null) {
            throw new RuntimeException("Phone number not found");
        }
        student.setPassword(newPassword);
        this.updateById(student);
    }

    @Override
    public BigDecimal getBalance(Long studentId) {
        Student student = this.getById(studentId);
        return student != null && student.getBalance() != null ? student.getBalance() : BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public void recharge(Long studentId, BigDecimal amount, String method) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }
        
        // 1. Update Balance
        Student student = this.getById(studentId);
        BigDecimal current = student.getBalance() != null ? student.getBalance() : BigDecimal.ZERO;
        student.setBalance(current.add(amount));
        this.updateById(student);
        
        // 2. Record
        RechargeRecord record = new RechargeRecord();
        record.setStudentId(studentId);
        record.setAmount(amount);
        record.setMethod(method);
        record.setStatus(1);
        rechargeRecordMapper.insert(record);
    }

    @Override
    @Transactional
    public void deductBalance(Long studentId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Amount must be positive");
        }
        Student student = this.getById(studentId);
        BigDecimal current = student.getBalance() != null ? student.getBalance() : BigDecimal.ZERO;
        
        if (current.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }
        
        student.setBalance(current.subtract(amount));
        this.updateById(student);
    }

    @Override
    public List<RechargeRecord> getRechargeHistory(Long studentId) {
        LambdaQueryWrapper<RechargeRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RechargeRecord::getStudentId, studentId);
        wrapper.orderByDesc(RechargeRecord::getCreateTime);
        return rechargeRecordMapper.selectList(wrapper);
    }
}
