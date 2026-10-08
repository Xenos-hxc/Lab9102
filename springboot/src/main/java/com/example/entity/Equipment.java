package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
public class Equipment implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;

    @TableField("categoryId")
    private Integer categoryId;

    @TableField(exist = false)
    private String categoryName;

    @TableField("purchaseDate")
    private LocalDate purchaseDate;

    private String introduction;

    @TableField("pictureUrl")
    private String pictureUrl;
}
