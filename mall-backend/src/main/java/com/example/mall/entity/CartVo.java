package com.example.mall.entity; // 或者 com.example.mall.vo

import lombok.Data;
import java.math.BigDecimal;
// VO = View Object，专门返给前端看的对象
@Data
public class CartVo extends Cart {
    // 额外补充商品信息
    private String productName;
    private BigDecimal price;
    private String productImage;
}