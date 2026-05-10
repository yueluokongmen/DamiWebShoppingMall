package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user_login_log")
public class UserLoginLog {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    private Long userId;
    private String username;
    private Integer role;       // 0=用户, 1=Admin, 2=Sales
    private LocalDateTime loginTime;
    private String loginIp;
    private String userAgent;
}
