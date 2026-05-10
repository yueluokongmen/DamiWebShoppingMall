package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.User;
import com.example.mall.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 获取客户列表（role=0） */
    @GetMapping("/list")
    public Result<List<User>> list() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, 0);
        wrapper.orderByDesc(User::getCreateTime);
        List<User> list = userMapper.selectList(wrapper);
        list.forEach(u -> u.setPassword(null));
        return Result.success(list);
    }

    /** 重置密码 */
    @PostMapping("/reset-pwd/{userId}")
    public Result<String> resetPwd(@PathVariable Long userId) {
        User user = new User();
        user.setUserId(userId);
        String encodedPwd = passwordEncoder.encode("123456");
        user.setPassword(encodedPwd);
        userMapper.updateById(user);
        return Result.success("密码已重置为 123456");
    }

    /** 禁用用户 */
    @PostMapping("/disable/{userId}")
    public Result<String> disable(@PathVariable Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return Result.error("用户不存在");

        User update = new User();
        update.setUserId(userId);
        update.setStatus(0);
        userMapper.updateById(update);
        return Result.success("已禁用");
    }

    /** 启用用户 */
    @PostMapping("/enable/{userId}")
    public Result<String> enable(@PathVariable Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) return Result.error("用户不存在");

        User update = new User();
        update.setUserId(userId);
        update.setStatus(1);
        userMapper.updateById(update);
        return Result.success("已启用");
    }
}
