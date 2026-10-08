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
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 科研项目id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 科研项目标题
     */
    private String title;

    /**
     * 开始时间
     */
    @TableField("startTime")
    private LocalDate starttime;

    /**
     * 结题时间：如果为空则代表还在进行中
     */
    @TableField("endTime")
    private LocalDate endtime;

    /**
     * 项目类型：横向项目、纵向项目
     */
    private String type;

    /**
     * 合作方
     */
    private String company;


}
