package org.testspringboot.unibuy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.testspringboot.unibuy.entity.Favorite;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
    // MyBatis Plus provides basic CRUD operations
}
