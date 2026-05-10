package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    private String username;
    private String password;
    private String nickname;
    private String mobile;
    private String avatar;
    private Integer role;           // 0=Customer, 1=Admin, 2=Sales
    private Integer status;         // 1=正常, 0=禁用
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}