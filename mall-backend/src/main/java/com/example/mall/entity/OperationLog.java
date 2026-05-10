package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(value = "log_id", type = IdType.AUTO)
    private Long logId;

    private Long userId;
    private String username;
    private Integer role;           // 1=Admin, 2=Sales
    private String operation;       // 操作内容描述
    private String operationType;   // ADD/UPDATE/DELETE/LOGIN
    private String targetType;      // PRODUCT/CATEGORY/ORDER/USER
    private Long targetId;          // 操作对象ID
    private LocalDateTime opTime;
    private String opIp;
}
