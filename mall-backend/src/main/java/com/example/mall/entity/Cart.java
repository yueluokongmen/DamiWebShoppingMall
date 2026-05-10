package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cart")
public class Cart {

    @TableId(value = "cart_id", type = IdType.AUTO)
    private Long cartId;

    private Long userId;

    private Long skuId;

    private Integer cartQuantity;

    private Integer selected;

    @TableField("cart_create_time")
    private LocalDateTime createTime;
}