package com.example.mall.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.entity.OperationLog;
import com.example.mall.entity.UserBrowseLog;
import com.example.mall.entity.UserLoginLog;
import com.example.mall.mapper.OperationLogMapper;
import com.example.mall.mapper.UserBrowseLogMapper;
import com.example.mall.mapper.UserLoginLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
public class AdminLogController {

    @Autowired
    private UserBrowseLogMapper userBrowseLogMapper;

    @Autowired
    private UserLoginLogMapper userLoginLogMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    // ========== 后台接口 ==========

    /** 获取浏览日志 */
    @GetMapping("/admin/log/browse/list")
    public Result<List<UserBrowseLog>> browseLogList(@RequestParam(required = false) Integer limit) {
        LambdaQueryWrapper<UserBrowseLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(UserBrowseLog::getBrowseTime);
        wrapper.last("LIMIT " + (limit != null ? limit : 200));
        return Result.success(userBrowseLogMapper.selectList(wrapper));
    }

    /** 获取登录日志 */
    @GetMapping("/admin/log/login/list")
    public Result<List<UserLoginLog>> loginLogList(@RequestParam(required = false) Integer limit) {
        LambdaQueryWrapper<UserLoginLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(UserLoginLog::getLoginTime);
        wrapper.last("LIMIT " + (limit != null ? limit : 200));
        return Result.success(userLoginLogMapper.selectList(wrapper));
    }

    /** 获取操作日志 */
    @GetMapping("/admin/log/operation/list")
    public Result<List<OperationLog>> operationLogList(@RequestParam(required = false) Integer limit) {
        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(OperationLog::getOpTime);
        wrapper.last("LIMIT " + (limit != null ? limit : 200));
        return Result.success(operationLogMapper.selectList(wrapper));
    }

    // ========== 前台接口 ==========

    /** 记录浏览日志 */
    @PostMapping("/user/log/add")
    public Result<String> addLog(@RequestBody UserBrowseLog log) {
        log.setBrowseTime(LocalDateTime.now());
        userBrowseLogMapper.insert(log);
        return Result.success("记录成功");
    }
}
