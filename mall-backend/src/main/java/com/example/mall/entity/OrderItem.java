package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("order_item")
public class OrderItem {

    @TableId(value = "item_id", type = IdType.AUTO)
    private Long itemId;

    private Long orderId; //关联主订单
    private Long skuId;
    private Integer categoryId;     // 商品类别ID
    private String skuName;
    private String skuImage;
    private BigDecimal currentPrice; // 下单时的单价 snapshot
    private Integer itemQuantity; // 买了几个
    private BigDecimal itemTotalPrice; // 总价(单价*数量)
}