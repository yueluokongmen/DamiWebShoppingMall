package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.Sku;
import com.example.mall.entity.Spu;
import com.example.mall.mapper.SkuMapper;
import com.example.mall.mapper.SpuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/product")
public class AdminProductController {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private SkuMapper skuMapper;

    //获取商品列表
    @GetMapping("/list")
    public Result<List<Spu>> list() {
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Spu::getProductId);
        List<Spu> list = spuMapper.selectList(wrapper);
        return Result.success(list);
    }

    //修改上架状态
    @PostMapping("/status/{productId}/{status}")
    public Result<String> updateStatus(@PathVariable Long productId, @PathVariable Integer status) {
        Spu spu = new Spu();
        spu.setProductId(productId);
        spu.setProductStatus(status);
        spuMapper.updateById(spu);
        return Result.success("状态更新成功");
    }

    //发布/保存商品
    @PostMapping("/save")
    @Transactional
    public Result<String> save(@RequestBody Spu spu) {
        if (spu.getProductId() == null) {
            spu.setCreateTime(LocalDateTime.now());
            spu.setUpdateTime(LocalDateTime.now());
            spu.setProductStatus(1);
            spuMapper.insert(spu);

            //自动生成一个默认SKU
            Sku defaultSku = new Sku();
            defaultSku.setProductId(spu.getProductId());
            defaultSku.setSkuName(spu.getProductName() + " (标准版)");
            defaultSku.setSkuImage(spu.getImages() != null && !spu.getImages().isEmpty() ? spu.getImages().get(0) : "");
            defaultSku.setSkuPrice(spu.getSpuPrice());
            defaultSku.setStock(999);
            skuMapper.insert(defaultSku);
        } else {
            spu.setUpdateTime(LocalDateTime.now());
            spuMapper.updateById(spu);
            // 暂时不处理SKU的更新，比较复杂，先只更新SPU主信息
        }
        return Result.success("保存成功");
    }

    //获取详情
    @GetMapping("/info/{id}")
    public Result<Spu> info(@PathVariable Long id) {
        Spu spu = spuMapper.selectById(id);
        return Result.success(spu);
    }

    //删除商品
    @DeleteMapping("/delete/{id}")
    @Transactional
    public Result<String> delete(@PathVariable Long id) {
        //删SPU
        spuMapper.deleteById(id);
        //删SKU
        LambdaQueryWrapper<Sku> skuWrapper = new LambdaQueryWrapper<>();
        skuWrapper.eq(Sku::getProductId, id);
        skuMapper.delete(skuWrapper);

        return Result.success("删除成功");
    }
}