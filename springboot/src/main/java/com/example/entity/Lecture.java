package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * <p>
 *
 * </p>
 *
 * @author example.demo
 * @since 2025-11-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Lecture implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 讲座id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 讲座标题
     */
    private String title;

    /**
     * 主讲人
     */
    private String speaker;

    /**
     * 主讲人来自哪儿：如西南交通大学信息学院
     */
    @TableField("speakerFrom")
    private String speakerfrom;

    /**
     * 讲座时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime time;

    /**
     * 讲座地点
     */
    private String address;

    /**
     * 主持人
     */
    private String host;

    /**
     * 讲座简介
     */
    @TableField("lectureIntroduction")
    private String lectureintroduction;

    /**
     * 主讲人简介
     */
    @TableField("speakerIntroduction")
    private String speakerintroduction;

    /**
     * 图片路径
     */
    @TableField("pictureUrl")
    private String pictureurl;


}
