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
public class News implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 新闻id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 新闻标题
     */
    private String title;

    /**
     * 新闻摘要
     */
    private String summary;

    /**
     * 新闻内容，内容是富文本
     */
    private String content;

    /**
     * 新闻图片路径
     */
    @TableField("pictureUrl")
    private String pictureurl;

    /**
     * 新闻类型
     */
    private String type;

    /**
     * 新闻时间
     */
    private LocalDate time;


}
