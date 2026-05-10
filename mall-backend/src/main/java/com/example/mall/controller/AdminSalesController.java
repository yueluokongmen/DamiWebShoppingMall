package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.OperationLog;
import com.example.mall.entity.User;
import com.example.mall.mapper.OperationLogMapper;
import com.example.mall.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员 - 销售人员管理
 * 需求：3.1 管理者 - 销售人员ID管理（添加/删除）、销售人员密码重置、销售业绩查询
 */
@RestController
@RequestMapping("/admin/sales")
public class AdminSalesController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /** 获取所有销售人员列表 */
    @GetMapping("/list")
    public Result<List<User>> list() {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getRole, 2);
        wrapper.orderByDesc(User::getCreateTime);
        List<User> list = userMapper.selectList(wrapper);
        // 隐藏密码
        list.forEach(u -> u.setPassword(null));
        return Result.success(list);
    }

    /** 添加销售人员 */
    @PostMapping("/add")
    public Result<String> add(@RequestBody User sales, HttpServletRequest request) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, sales.getUsername());
        if (userMapper.selectCount(wrapper) > 0) {
            return Result.error("用户名已存在");
        }

        sales.setRole(2); // Sales角色
        sales.setStatus(1); // 正常状态
        if (sales.getNickname() == null) {
            sales.setNickname("销售人员_" + System.currentTimeMillis() / 1000);
        }
        sales.setPassword(passwordEncoder.encode(sales.getPassword()));
        userMapper.insert(sales);

        // 记录操作日志
        logOperation(sales.getUserId(), "管理员添加销售人员: " + sales.getUsername(),
                "ADD", "USER", sales.getUserId(), request);

        return Result.success("添加成功");
    }

    /** 删除（禁用）销售人员 */
    @PostMapping("/disable/{userId}")
    public Result<String> disable(@PathVariable Long userId, HttpServletRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getRole() != 2) {
            return Result.error("销售人员不存在");
        }

        User update = new User();
        update.setUserId(userId);
        update.setStatus(0); // 禁用
        userMapper.updateById(update);

        logOperation(userId, "管理员禁用销售人员: " + user.getUsername(),
                "DELETE", "USER", userId, request);

        return Result.success("已禁用");
    }

    /** 启用销售人员 */
    @PostMapping("/enable/{userId}")
    public Result<String> enable(@PathVariable Long userId, HttpServletRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getRole() != 2) {
            return Result.error("销售人员不存在");
        }

        User update = new User();
        update.setUserId(userId);
        update.setStatus(1);
        userMapper.updateById(update);

        logOperation(userId, "管理员启用销售人员: " + user.getUsername(),
                "UPDATE", "USER", userId, request);

        return Result.success("已启用");
    }

    /** 重置销售人员密码 */
    @PostMapping("/reset-pwd/{userId}")
    public Result<String> resetPwd(@PathVariable Long userId, HttpServletRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null || user.getRole() != 2) {
            return Result.error("销售人员不存在");
        }

        User update = new User();
        update.setUserId(userId);
        update.setPassword(passwordEncoder.encode("123456"));
        userMapper.updateById(update);

        logOperation(userId, "管理员重置销售人员密码: " + user.getUsername(),
                "UPDATE", "USER", userId, request);

        return Result.success("密码已重置为 123456");
    }

    /** 记录操作日志 */
    private void logOperation(Long operatorId, String operation, String opType, String targetType, Long targetId, HttpServletRequest request) {
        OperationLog log = new OperationLog();
        log.setUserId(operatorId);
        log.setRole(1); // Admin
        log.setOperation(operation);
        log.setOperationType(opType);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setOpTime(LocalDateTime.now());

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) ip = request.getRemoteAddr();
        log.setOpIp(ip);

        operationLogMapper.insert(log);
    }
}
