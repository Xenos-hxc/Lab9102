package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import java.time.Year;
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
public class Member implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 名字
     */
    private String name;

    /**
     * 身份：负责人、导师、博士研究生、硕士研究生、毕业生
     */
    private String identity;

    /**
     * 个人介绍
     */
    private String introduction;

    /**
     * 入学/职年份
     */
    @TableField("entryTime")
    private Year entrytime;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 个人图片路径
     */
    @TableField("pictureUrl")
    private String pictureurl;

    /**
     * 账号
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 毕业去向
     */
    private String workplace;

    /**
     * 个人主页链接（如 Google Scholar）
     */
    @TableField("profileUrl")
    private String profileUrl;

    /**
     * 个人主页显示名称
     */
    @TableField("profileLabel")
    private String profileLabel;

    /**
     * 是否拥有管理员端访问权限
     */
    @TableField("isAdmin")
    private Boolean isAdmin;

    /**
     * 管理员端菜单权限，使用英文逗号分隔；admin 账号使用 *
     */
    @TableField("adminPermissions")
    private String adminPermissions;


}
