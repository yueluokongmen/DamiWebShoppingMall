package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.Sku;
import com.example.mall.mapper.SkuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/sku")
public class AdminSkuController {

    @Autowired
    private SkuMapper skuMapper;

    //根据商品ID查所有SKU
    // GET /admin/sku/list?productId=101
    @GetMapping("/list")
    public Result<List<Sku>> list(@RequestParam Long productId) {
        LambdaQueryWrapper<Sku> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Sku::getProductId, productId);
        wrapper.orderByAsc(Sku::getSkuId);
        return Result.success(skuMapper.selectList(wrapper));
    }

    //新增或更新 SKU
    @PostMapping("/save")
    public Result<String> save(@RequestBody Sku sku) {
        if (sku.getSkuId() == null) {
            //新增
            skuMapper.insert(sku);
        } else {
            //更新
            skuMapper.updateById(sku);
        }
        return Result.success("保存成功");
    }

    //删除SKU
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        skuMapper.deleteById(id);
        return Result.success("删除成功");
    }
}