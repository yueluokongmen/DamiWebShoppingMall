package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.entity.User;
import com.example.mall.entity.UserLoginLog;
import com.example.mall.mapper.UserMapper;
import com.example.mall.mapper.UserLoginLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.mall.common.JwtUtils;
import cn.hutool.crypto.digest.BCrypt;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserLoginLogMapper userLoginLogMapper;

    //接口地址为http://localhost:8081/user/list
    @GetMapping("/list")
    public Result<List<User>> list() {
        List<User> users = userMapper.selectList(null);
        return Result.success(users);
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody User loginUser, HttpServletRequest request) {
        //根据用户名去数据库查
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginUser.getUsername());
        User dbUser = userMapper.selectOne(wrapper);

        //校验用户是否存在
        if (dbUser == null) {
            return Result.error("用户不存在");
        }

        //校验账号是否被禁用
        if (dbUser.getStatus() != null && dbUser.getStatus() == 0) {
            return Result.error("账号已被禁用，请联系管理员");
        }

        //校验密码
        if (!passwordEncoder.matches(loginUser.getPassword(), dbUser.getPassword())) {
            return Result.error("密码错误");
        }

        //生成Token
        String token = JwtUtils.generateToken(dbUser.getUsername());

        //更新最后登录时间和IP
        String clientIp = getClientIp(request);
        dbUser.setLastLoginTime(LocalDateTime.now());
        dbUser.setLastLoginIp(clientIp);
        userMapper.updateById(dbUser);

        //记录登录日志
        UserLoginLog loginLog = new UserLoginLog();
        loginLog.setUserId(dbUser.getUserId());
        loginLog.setUsername(dbUser.getUsername());
        loginLog.setRole(dbUser.getRole());
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setLoginIp(clientIp);
        loginLog.setUserAgent(request.getHeader("User-Agent"));
        userLoginLogMapper.insert(loginLog);

        //返回结果
        LoginResult data = new LoginResult();
        data.setToken(token);
        data.setUserId(dbUser.getUserId());
        data.setUsername(dbUser.getUsername());
        data.setAvatar(dbUser.getAvatar());
        data.setRole(dbUser.getRole());
        data.setNickname(dbUser.getNickname());

        return Result.success(data);
    }

    @Data
    class LoginResult {
        private String token;
        private Long userId;
        private String username;
        private String avatar;
        private Integer role;
        private String nickname;
    }

    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        //校验账号是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        Long count = userMapper.selectCount(wrapper);

        if (count > 0) {
            return Result.error("该账号已被注册");
        }

        //密码加密
        String rawPassword = user.getPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);

        //设置默认头像和昵称
        if(user.getNickname() == null) {
            user.setNickname("大米用户_" + System.currentTimeMillis() / 1000);
        }

        //默认角色为普通用户
        if(user.getRole() == null) {
            user.setRole(0);
        }

        //存入数据库
        userMapper.insert(user);

        return Result.success("注册成功");
    }

    //修改个人信息接口
    @PostMapping("/update")
    public Result<User> update(@RequestBody User user) {
        if (user.getUserId() == null) {
            return Result.error("用户ID不能为空");
        }

        //直接根据ID更新非空字段
        userMapper.updateById(user);

        //更新完后，查出最新的用户信息返回给前端刷新缓存
        User newUser = userMapper.selectById(user.getUserId());
        newUser.setPassword(null);

        return Result.success(newUser);
    }

    //修改密码
    @PostMapping("/updatePassword")
    public Result<String> updatePassword(@RequestBody Map<String, String> params) {
        String userIdStr = params.get("userId");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        if (userIdStr == null || oldPassword == null || newPassword == null) {
            return Result.error("参数不完整");
        }

        User user = userMapper.selectById(Long.parseLong(userIdStr));
        if (user == null) return Result.error("用户不存在");

        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            return Result.error("原密码错误");
        }

        String newPwdHash = BCrypt.hashpw(newPassword);
        user.setPassword(newPwdHash);

        userMapper.updateById(user);

        return Result.success("修改成功");
    }

    /** 获取客户端真实IP */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}