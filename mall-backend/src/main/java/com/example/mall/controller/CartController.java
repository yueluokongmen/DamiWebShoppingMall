package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.Cart;
import com.example.mall.entity.Sku;
import com.example.mall.entity.Spu;
import com.example.mall.mapper.CartMapper;
import com.example.mall.mapper.SkuMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.mall.mapper.SpuMapper;
import com.example.mall.entity.CartVo;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper; // 🔥 注入 SkuMapper

    @GetMapping("/list")
    public Result<List<CartVo>> list(@RequestParam Long userId) {
        //查购物车数据
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        wrapper.orderByDesc(Cart::getCreateTime);
        List<Cart> cartList = cartMapper.selectList(wrapper);

        List<CartVo> voList = new ArrayList<>();

        for (Cart cart : cartList) {
            CartVo vo = new CartVo();
            BeanUtils.copyProperties(cart, vo);

            // 先用skuId查SKU信息
            Sku sku = skuMapper.selectById(cart.getSkuId());

            if (sku != null) {
                //设置SKU特有的信息
                vo.setPrice(sku.getSkuPrice());
                vo.setProductImage(sku.getSkuImage());


                Spu spu = spuMapper.selectById(sku.getProductId());

                if (spu != null) {

                    vo.setProductName(spu.getProductName() + " " + sku.getSkuName());
                }
            } else {

                vo.setProductName("商品已失效");
            }

            voList.add(vo);
        }

        return Result.success(voList);
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Cart cart) {
        cart.setSelected(1); // 默认选中
        cart.setCreateTime(LocalDateTime.now());
        cartMapper.insert(cart);
        return Result.success("添加成功");
    }

    @PostMapping("/update")
    public Result<String> update(@RequestBody Cart cart) {
        if (cart.getCartId() == null || cart.getCartQuantity() == null) {
            return Result.error("参数不全");
        }
        cartMapper.updateById(cart);
        return Result.success("更新成功");
    }

    @DeleteMapping("/delete/{cartId}")
    public Result<String> delete(@PathVariable Long cartId) {
        cartMapper.deleteById(cartId);
        return Result.success("删除成功");
    }
}