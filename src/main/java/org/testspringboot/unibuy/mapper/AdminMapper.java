package org.testspringboot.unibuy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.testspringboot.unibuy.entity.Admin;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {
}
