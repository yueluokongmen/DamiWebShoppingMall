package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("user_profile")
public class UserProfile {

    @TableId(value = "profile_id", type = IdType.AUTO)
    private Long profileId;

    private Long userId;
    private String province;
    private String city;
    private String purchaseLevel;       // LOW/MEDIUM/HIGH/VIP
    private BigDecimal totalSpent;
    private Integer totalOrders;
    private BigDecimal avgOrderAmount;
    private Integer preferredCategoryId;
    private String preferredCategoryName;
    private LocalDateTime lastPurchaseTime;
    private LocalDateTime updateTime;
}
