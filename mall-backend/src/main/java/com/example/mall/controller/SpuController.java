package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.Sku;
import com.example.mall.entity.Spu;
import com.example.mall.mapper.SkuMapper;
import com.example.mall.mapper.SpuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/spu")
public class SpuController {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SkuMapper skuMapper; //
    // 访问地址: http://localhost:8081/spu/list
    @GetMapping("/list")
    public Result<List<Spu>> list() {
        //只查询状态为1(上架)的商品
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Spu::getProductStatus, 1);

        List<Spu> list = spuMapper.selectList(wrapper);
        return Result.success(list);
    }
    // 获取详情 (返回SPU+SKU列表)
    // URL: http://localhost:8081/spu/detail/1
    @GetMapping("/detail/{id}")
    public Result<Map<String, Object>> getDetail(@PathVariable Long id) {
        //查SPU信息
        Spu spu = spuMapper.selectById(id);
        if (spu == null) {
            return Result.error("商品不存在");
        }

        //查该商品下的所有SKU
        LambdaQueryWrapper<Sku> skuWrapper = new LambdaQueryWrapper<>();
        skuWrapper.eq(Sku::getProductId, id);
        List<Sku> skuList = skuMapper.selectList(skuWrapper);

        //封装返回
        Map<String, Object> map = new HashMap<>();
        map.put("spu", spu);
        map.put("skuList", skuList);

        return Result.success(map);
    }

    // 通用搜索栏
    @GetMapping("/search")
    public Result<List<Spu>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer familyId,
            @RequestParam(required = false) Integer categoryId
    ) {
        LambdaQueryWrapper<Spu> wrapper = new LambdaQueryWrapper<>();

        //按关键词搜
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Spu::getProductName, keyword);
        }

        //按系列搜 (导航栏点击)
        if (familyId != null) {
            wrapper.eq(Spu::getProductFamilyId, familyId);
        }

        //按大类搜 (首页楼层)
        if (categoryId != null) {
            wrapper.eq(Spu::getCategoryId, categoryId);
        }

        //按创建时间倒序，最新的在前面
        wrapper.orderByDesc(Spu::getCreateTime);

        return Result.success(spuMapper.selectList(wrapper));
    }
}
