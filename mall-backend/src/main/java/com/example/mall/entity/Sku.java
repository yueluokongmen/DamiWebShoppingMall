package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@TableName(value = "sku", autoResultMap = true)
public class Sku {

    @TableId(type = IdType.AUTO)
    private Long skuId;

    private Long productId;
    private String skuName;

    private String skuImage;

    private BigDecimal skuPrice;

    private Integer stock;

    //对应数据库的 specs (JSON格式)
    @TableField(typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> specs;
}