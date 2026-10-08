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
public class Memberdirection implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 人员研究方向关联表id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 人员id
     */
    @TableField("memberId")
    private Integer memberid;

    /**
     * 研究方向id
     */
    @TableField("directionId")
    private Integer directionid;


}
