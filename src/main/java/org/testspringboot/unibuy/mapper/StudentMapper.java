package org.testspringboot.unibuy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.testspringboot.unibuy.entity.Student;

@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}
