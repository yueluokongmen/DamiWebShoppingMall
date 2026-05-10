package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("address")
public class Address {

    @TableId(value = "address_id", type = IdType.AUTO)
    private Long addressId;

    private Long userId;

    private String receiverName;

    private String receiverPhone;

    private String province;
    private String city;
    private String district;

    private String detailedAddress;

    private Integer isDefault;
}