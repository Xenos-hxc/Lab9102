package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("forum_like")
public class ForumLike implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer postId;
    private Integer memberId;
    private LocalDateTime createdAt;
}
