package com.example.mall.entity;

import lombok.Data;

//接收前端下单参数的数据传输对象
@Data
public class OrderDTO {
    private Long userId;
    private Long addressId;
    private Long skuId;
    private Integer quantity;
}