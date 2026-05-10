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
import java.util.Map;

@Data
@TableName(value = "spu", autoResultMap = true) // 👈 必须开启 autoResultMap
public class Spu {

    @TableId(value = "product_id", type = IdType.AUTO)
    private Long productId;

    private Integer productFamilyId;
    private Integer categoryId;
    private String productName;
    private String subtitle;

    //将数据库的JSON转为java对象
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> images;

    private String detail;
    private BigDecimal spuPrice;
    private Integer productStatus;
    private Long salesId;           // 负责该商品的销售人员ID

    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<Map<String, Object>> specOptions;

    @TableField("spu_create_time")
    private LocalDateTime createTime;
    @TableField("spu_update_time")
    private LocalDateTime updateTime;
}