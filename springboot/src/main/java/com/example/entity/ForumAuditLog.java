package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("forum_audit_log")
public class ForumAuditLog implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer postId;
    private String action;
    private String reason;
    private String operatorName;
    private LocalDateTime createdAt;
}
