package com.example.mall.controller;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.Result;
import com.example.mall.common.JwtUtils;
import com.example.mall.entity.User;
import com.example.mall.entity.UserLoginLog;
import com.example.mall.mapper.UserLoginLogMapper;
import com.example.mall.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserLoginLogMapper userLoginLogMapper;

    //管理员登录接口
    @PostMapping("/login")
    public Result<Map<String, Object>> adminLogin(@RequestBody User params, HttpServletRequest request) {
        //查用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, params.getUsername());
        User user = userMapper.selectOne(wrapper);

        //校验是否存在
        if (user == null) {
            return Result.error("用户不存在");
        }

        if (!BCrypt.checkpw(params.getPassword(), user.getPassword())) {
            return Result.error("密码错误");
        }

        //校验角色：Admin(role=1) 或 Sales(role=2) 都可进入后台
        if (user.getRole() == null || (user.getRole() != 1 && user.getRole() != 2)) {
            return Result.error("无权访问后台管理系统");
        }

        //校验账号是否被禁用
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("账号已被禁用，请联系管理员");
        }

        String token = JwtUtils.generateToken(user.getUsername());

        //更新最后登录时间和IP
        String clientIp = getClientIp(request);
        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(clientIp);
        userMapper.updateById(user);

        //记录登录日志
        UserLoginLog loginLog = new UserLoginLog();
        loginLog.setUserId(user.getUserId());
        loginLog.setUsername(user.getUsername());
        loginLog.setRole(user.getRole());
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setLoginIp(clientIp);
        loginLog.setUserAgent(request.getHeader("User-Agent"));
        userLoginLogMapper.insert(loginLog);

        user.setPassword(null);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", user);

        return Result.success(data);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
