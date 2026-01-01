package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("favorite")
public class Favorite implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long favoriteId;

    private Long studentId;
    private Long productId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
