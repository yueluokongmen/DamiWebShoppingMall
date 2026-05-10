package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.Category;
import com.example.mall.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
public class AdminCategoryController {

    @Autowired
    private CategoryMapper categoryMapper;

    @GetMapping("/list")
    public Result<List<Category>> list() {
        List<Category> list = categoryMapper.selectList(null);
        return Result.success(list);
    }
    //添加一级分类
    @PostMapping("/add")
    public Result<String> add(@RequestBody Category category) {
        if (category.getCategoryName() == null) {
            return Result.error("分类名称不能为空");
        }
        categoryMapper.insert(category);
        return Result.success("添加成功");
    }

    //删除一级分类
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        categoryMapper.deleteById(id);
        return Result.success("删除成功");
    }
}