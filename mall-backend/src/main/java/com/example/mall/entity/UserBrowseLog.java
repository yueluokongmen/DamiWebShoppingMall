package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_browse_log")
public class UserBrowseLog {
    @TableId(type = IdType.AUTO)
    private Long logId;
    private Long userId;
    private String username;
    private Long productId;
    private String productName;
    private Integer categoryId;     // 商品类别ID
    private Integer duration;       // 停留时长(秒)
    private String browseIp;        // 浏览时IP
    private LocalDateTime browseTime;
}