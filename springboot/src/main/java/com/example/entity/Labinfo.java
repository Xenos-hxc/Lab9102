package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 实验室信息表
 * </p>
 *
 * @author example.demo
 * @since 2025-11-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Labinfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 实验室名称
     */
    private String labName;

    /**
     * 省份
     */
    private String addressProvince;

    /**
     * 城市
     */
    private String addressCity;

    /**
     * 区县
     */
    private String addressDistrict;

    /**
     * 街道地址
     */
    private String addressStreet;

    /**
     * 详细地址
     */
    private String addressDetail;

    /**
     * 所属大学
     */
    private String university;

    /**
     * 所属学院
     */
    private String college;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 联系人1姓名
     */
    private String contactPerson1Name;

    /**
     * 联系人1邮箱
     */
    private String contactPerson1Email;

    /**
     * 联系人2姓名
     */
    private String contactPerson2Name;

    /**
     * 联系人2邮箱
     */
    private String contactPerson2Email;

    /**
     * 联系人3姓名
     */
    private String contactPerson3Name;

    /**
     * 联系人3邮箱
     */
    private String contactPerson3Email;

    /**
     * 联系人4姓名
     */
    private String contactPerson4Name;

    /**
     * 联系人4邮箱
     */
    private String contactPerson4Email;

    /**
     * 联系人5姓名（预留）
     */
    private String contactPerson5Name;

    /**
     * 联系人5邮箱（预留）
     */
    private String contactPerson5Email;

    /**
     * 工作日工作时间
     */
    private String workdayHours;

    /**
     * 周末工作时间
     */
    private String weekendHours;

    /**
     * 节假日说明
     */
    private String holidayNote;

    /**
     * 地图链接
     */
    private String mapUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    /**
     * 更新时间
     */
    private String introduction;


}
