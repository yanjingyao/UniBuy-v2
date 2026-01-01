package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;

@Data
@TableName("sys_config")
public class SysConfig implements Serializable {
    @TableId
    private String configKey;
    private String configValue;
    private String description;
}
