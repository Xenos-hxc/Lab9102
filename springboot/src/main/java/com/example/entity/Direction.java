package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
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
public class Direction implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 研究方向id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 研究方向名称
     */
    private String name;

    /**
     * 研究方向图片路径
     */
    @TableField("pictureUrl")
    private String pictureurl;
    /**
     * 若level=2,则有parentid
     */
     @TableField("parentId")
    private Integer parentid;

    /**
     * 研究方向简介
     */
    private String introduction;

    /**
     * 层级：1：父级研究方向；2：子级研究方向
     */
    private Integer level;


}
