package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName(value = "orders", autoResultMap = true) // 开启JSON支持
public class Orders {

    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    private String orderNo; // 订单编号(唯一)
    private Long userId;
    private BigDecimal totalAmount;
    private Integer orderStatus; // 0待付款 1待发货 2已发货 3已完成

    // 地址快照(JSON)
    // 直接复用之前的Address类作为类型，MyBatis Plus 自动转成JSON存进去
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Address receiverInfoSnapshot;

    @TableField("order_create_time")
    private LocalDateTime createTime;

    @TableField("order_pay_time")
    private LocalDateTime payTime;

    @TableField("complete_time")
    private LocalDateTime completeTime;

    @TableField(exist = false)
    private List<OrderItem> orderItems;
}
