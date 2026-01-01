package org.testspringboot.unibuy.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.testspringboot.unibuy.entity.PayRecord;

@Mapper
public interface PayRecordMapper extends BaseMapper<PayRecord> {
}
