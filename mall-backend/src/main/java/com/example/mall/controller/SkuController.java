package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.Sku;
import com.example.mall.mapper.SkuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sku")
public class SkuController {

    @Autowired
    private SkuMapper skuMapper;

    //获取某个商品的所有SKU
    //测试URL: http://localhost:8081/sku/list?productId=1
    @GetMapping("/list")
    public Result<List<Sku>> list(@RequestParam Long productId) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sku::getProductId, productId);

        List<Sku> list = skuMapper.selectList(wrapper);
        return Result.success(list);
    }
}