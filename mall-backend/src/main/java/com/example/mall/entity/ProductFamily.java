package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("product_family")
public class ProductFamily {
    @TableId(value = "product_family_id", type = IdType.AUTO)
    private Integer productFamilyId;
    private Integer categoryId;
    private String productFamilyName;
}