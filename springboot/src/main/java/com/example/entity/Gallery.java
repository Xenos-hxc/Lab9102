package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author example.demo
 * @since 2025-11-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Gallery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 团队建设id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 团建标题
     */
    private String title;

    /**
     * 活动摘要
     */
    private String summary;

    /**
     * 活动内容，内容是富文本
     */
    private String content;

    /**
     * 活动图片路径
     */
    @TableField("pictureUrl")
    private String pictureurl;

    /**
     * 活动类型
     */
    private String type;

    /**
     * 活动时间
     */
    private LocalDate time;


}
