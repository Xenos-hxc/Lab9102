package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("forum_post")
public class ForumPost implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer authorId;
    private Integer categoryId;
    private String title;
    private String summary;
    private String content;
    private String coverUrl;
    private String visibility;
    private String status;
    private String rejectReason;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer isTop;
    private Integer isFeatured;
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime submittedAt;
    private LocalDateTime publishedAt;
}
