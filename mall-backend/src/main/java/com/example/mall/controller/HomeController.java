package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.Category;
import com.example.mall.entity.ProductFamily;
import com.example.mall.entity.Spu;
import com.example.mall.mapper.CategoryMapper;
import com.example.mall.mapper.ProductFamilyMapper;
import com.example.mall.mapper.SpuMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private ProductFamilyMapper productFamilyMapper;
    @Autowired
    private SpuMapper spuMapper;

    @Data
    public static class NavCategoryVO {
        private Integer id;
        private String name;
        private String icon;
        private List<NavFamilyVO> children;
    }

    @Data
    public static class NavFamilyVO {
        private Integer id;
        private String name;
        private String img;
        private Long linkSpuId;
    }

    @GetMapping("/nav")
    public Result<List<NavCategoryVO>> getNav() {
        List<NavCategoryVO> result = new ArrayList<>();
        List<Category> categories = categoryMapper.selectList(null);

        for (Category cat : categories) {
            NavCategoryVO catVO = new NavCategoryVO();
            catVO.setId(cat.getCategoryId());
            catVO.setName(cat.getCategoryName());


            catVO.setIcon(cat.getCategoryIcon());

            LambdaQueryWrapper<ProductFamily> familyWrapper = new LambdaQueryWrapper<>();
            familyWrapper.eq(ProductFamily::getCategoryId, cat.getCategoryId());
            List<ProductFamily> families = productFamilyMapper.selectList(familyWrapper);

            List<NavFamilyVO> children = new ArrayList<>();
            for (ProductFamily family : families) {
                NavFamilyVO familyVO = new NavFamilyVO();
                familyVO.setId(family.getProductFamilyId());
                familyVO.setName(family.getProductFamilyName());

                LambdaQueryWrapper<Spu> spuWrapper = new LambdaQueryWrapper<>();
                spuWrapper.eq(Spu::getProductFamilyId, family.getProductFamilyId());
                spuWrapper.last("LIMIT 1");
                Spu spu = spuMapper.selectOne(spuWrapper);

                if (spu != null) {
                    if (spu.getImages() != null && !spu.getImages().isEmpty()) {
                        familyVO.setImg(spu.getImages().get(0));
                    }
                    familyVO.setLinkSpuId(spu.getProductId());
                } else {
                    familyVO.setImg("https://images.unsplash.com/photo-1598327105666-5b89351aff23?w=50&h=50&fit=crop");
                }
                children.add(familyVO);
            }

            catVO.setChildren(children);
            result.add(catVO);
        }

        return Result.success(result);
    }
}