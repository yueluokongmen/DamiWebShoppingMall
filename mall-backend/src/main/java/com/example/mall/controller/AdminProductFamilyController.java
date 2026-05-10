package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.ProductFamily;
import com.example.mall.entity.Sku;
import com.example.mall.entity.Spu;
import com.example.mall.mapper.ProductFamilyMapper;
import com.example.mall.mapper.SkuMapper;
import com.example.mall.mapper.SpuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/product-family")
public class AdminProductFamilyController {

    @Autowired
    private ProductFamilyMapper productFamilyMapper;

    //根据分类ID获取系列
    // URL: GET /admin/product-family/list?categoryId=10
    @GetMapping("/list")
    public Result<List<ProductFamily>> list(@RequestParam(required = false) Integer categoryId) {
        LambdaQueryWrapper<ProductFamily> wrapper = new LambdaQueryWrapper<>();

        if (categoryId != null) {
            wrapper.eq(ProductFamily::getCategoryId, categoryId);
        }

        List<ProductFamily> list = productFamilyMapper.selectList(wrapper);
        return Result.success(list);
    }
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private SpuMapper spuMapper;
    //发布商品
    @PostMapping("/save")
    @Transactional //
    public Result<String> save(@RequestBody Spu spu) {
        //补全SPU基础信息
        spu.setCreateTime(LocalDateTime.now());
        spu.setUpdateTime(LocalDateTime.now());
        spu.setProductStatus(1); // 默认上架

        //保存SPU到数据库
        spuMapper.insert(spu);

        Sku defaultSku = new Sku();
        defaultSku.setProductId(spu.getProductId());
        defaultSku.setSkuName(spu.getProductName() + " (标准版)");
        defaultSku.setSkuImage(spu.getImages().isEmpty() ? "" : spu.getImages().get(0)); // 用主图
        defaultSku.setSkuPrice(spu.getSpuPrice());
        defaultSku.setStock(100);


        skuMapper.insert(defaultSku);

        return Result.success("发布成功");
    }

    //新增二级系列
    @PostMapping("/add")
    public Result<String> add(@RequestBody ProductFamily family) {
        if (family.getCategoryId() == null) {
            return Result.error("必须选择所属的一级分类");
        }
        if (family.getProductFamilyName() == null) {
            return Result.error("系列名称不能为空");
        }
        productFamilyMapper.insert(family);
        return Result.success("添加成功");
    }

    //删除二级系列
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        productFamilyMapper.deleteById(id);
        return Result.success("删除成功");
    }
}