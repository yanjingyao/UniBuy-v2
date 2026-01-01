package org.testspringboot.unibuy.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("reviews")
public class Reviews implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long reviewId;

    private Long orderId;
    private Long studentId;
    private Long merchantId;
    private Integer scoreAttitude;
    private Integer scoreQuality;
    private Integer scoreSpeed;
    private String comment;
    private String reply;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
