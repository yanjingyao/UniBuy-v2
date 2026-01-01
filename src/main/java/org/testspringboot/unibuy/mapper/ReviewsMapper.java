package org.testspringboot.unibuy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.testspringboot.unibuy.entity.Reviews;

@Mapper
public interface ReviewsMapper extends BaseMapper<Reviews> {
}
