package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.time.Year;
import com.baomidou.mybatisplus.annotation.TableId;
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
public class Paper implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 论文id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 论文标题
     */
    private String title;

    /**
     * 作者
     */
    private String authors;

    /**
     * 发表年份
     */
    private Year time;

    /**
     * 发表的期刊或者会议
     */
    private String place;
    /**
     * 论文链接
     */
    private String url;
    /**
     * 论文pdf下载地址
     */
    private String pdfurl;
    /**
     * 论文级别
     */
    private String level;

    /**
     * BibTeX 引用内容
     */
    private String bib;


}
