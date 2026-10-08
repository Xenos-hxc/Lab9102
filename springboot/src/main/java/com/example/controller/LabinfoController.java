package com.example.controller;

import com.example.entity.Labinfo;
import com.example.service.ILabinfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 实验室信息表 前端控制器
 * </p>
 *
 * @author example.demo
 * @since 2025-11-05
 */
@RestController
@RequestMapping("/labinfo")
@CrossOrigin(origins = "*")
public class LabinfoController {

    @Autowired
    private ILabinfoService labinfoService;

    /**
     * 获取实验室信息详情
     */
    @GetMapping("/detail")
    public Map<String, Object> getLabinfoDetail() {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取第一条实验室信息（假设只有一条记录）
            List<Labinfo> labinfoList = labinfoService.list();
            if (labinfoList != null && !labinfoList.isEmpty()) {
                Labinfo labinfo = labinfoList.get(0);
                result.put("code", 200);
                result.put("message", "获取实验室信息成功");
                result.put("data", labinfo);
            } else {
                result.put("code", 404);
                result.put("message", "实验室信息不存在");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取实验室信息失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 获取联系人员列表
     */
    @GetMapping("/contacts")
    public Map<String, Object> getContactPersons() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Labinfo> labinfoList = labinfoService.list();
            if (labinfoList != null && !labinfoList.isEmpty()) {
                Labinfo labinfo = labinfoList.get(0);

                // 构建联系人员列表
                List<Map<String, String>> contactPersons = new java.util.ArrayList<>();

                // 添加联系人1
                if (labinfo.getContactPerson1Name() != null && labinfo.getContactPerson1Email() != null) {
                    contactPersons.add(createContactPerson(labinfo.getContactPerson1Name(), labinfo.getContactPerson1Email()));
                }

                // 添加联系人2
                if (labinfo.getContactPerson2Name() != null && labinfo.getContactPerson2Email() != null) {
                    contactPersons.add(createContactPerson(labinfo.getContactPerson2Name(), labinfo.getContactPerson2Email()));
                }

                // 添加联系人3
                if (labinfo.getContactPerson3Name() != null && labinfo.getContactPerson3Email() != null) {
                    contactPersons.add(createContactPerson(labinfo.getContactPerson3Name(), labinfo.getContactPerson3Email()));
                }

                // 添加联系人4
                if (labinfo.getContactPerson4Name() != null && labinfo.getContactPerson4Email() != null) {
                    contactPersons.add(createContactPerson(labinfo.getContactPerson4Name(), labinfo.getContactPerson4Email()));
                }

                // 添加联系人5
                if (labinfo.getContactPerson5Name() != null && labinfo.getContactPerson5Email() != null) {
                    contactPersons.add(createContactPerson(labinfo.getContactPerson5Name(), labinfo.getContactPerson5Email()));
                }

                result.put("code", 200);
                result.put("message", "获取联系人员列表成功");
                result.put("data", contactPersons);
            } else {
                result.put("code", 404);
                result.put("message", "实验室信息不存在");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取联系人员列表失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 获取实验室基本信息（用于页脚）
     */
    @GetMapping("/basic")
    public Map<String, Object> getBasicInfo() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Labinfo> labinfoList = labinfoService.list();
            if (labinfoList != null && !labinfoList.isEmpty()) {
                Labinfo labinfo = labinfoList.get(0);

                // 构建基本信息
                Map<String, String> basicInfo = new HashMap<>();
                basicInfo.put("labName", labinfo.getLabName());
                basicInfo.put("university", labinfo.getUniversity());
                basicInfo.put("college", labinfo.getCollege());
                basicInfo.put("phone", labinfo.getPhone());
                basicInfo.put("address", buildFullAddress(labinfo));
                basicInfo.put("email", labinfo.getContactPerson1Email()); // 使用第一个联系人的邮箱

                result.put("code", 200);
                result.put("message", "获取实验室基本信息成功");
                result.put("data", basicInfo);
            } else {
                result.put("code", 404);
                result.put("message", "实验室信息不存在");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取实验室基本信息失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 创建联系人员对象
     */
    private Map<String, String> createContactPerson(String name, String email) {
        Map<String, String> person = new HashMap<>();
        person.put("name", name);
        person.put("email", email);
        return person;
    }

    /**
     * 构建完整地址
     */
    private String buildFullAddress(Labinfo labinfo) {
        StringBuilder address = new StringBuilder();
        if (labinfo.getAddressProvince() != null) {
            address.append(labinfo.getAddressProvince());
        }
        if (labinfo.getAddressCity() != null) {
            address.append(labinfo.getAddressCity());
        }
        if (labinfo.getAddressDistrict() != null) {
            address.append(labinfo.getAddressDistrict());
        }
        if (labinfo.getAddressStreet() != null) {
            address.append(labinfo.getAddressStreet());
        }
        if (labinfo.getAddressDetail() != null) {
            address.append(labinfo.getAddressDetail());
        }
        return address.toString();
    }

    /**
     * 更新实验室信息
     */
    @PostMapping("/update")
    public Map<String, Object> updateLabinfo(@RequestBody Labinfo labinfo) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取第一条实验室信息（假设只有一条记录）
            List<Labinfo> labinfoList = labinfoService.list();
            if (labinfoList != null && !labinfoList.isEmpty()) {
                Labinfo existingLabinfo = labinfoList.get(0);

                // 更新字段
                existingLabinfo.setLabName(labinfo.getLabName());
                existingLabinfo.setUniversity(labinfo.getUniversity());
                existingLabinfo.setCollege(labinfo.getCollege());
                existingLabinfo.setPhone(labinfo.getPhone());
                existingLabinfo.setAddressProvince(labinfo.getAddressProvince());
                existingLabinfo.setAddressCity(labinfo.getAddressCity());
                existingLabinfo.setAddressDistrict(labinfo.getAddressDistrict());
                existingLabinfo.setAddressStreet(labinfo.getAddressStreet());
                existingLabinfo.setAddressDetail(labinfo.getAddressDetail());
                existingLabinfo.setContactPerson1Name(labinfo.getContactPerson1Name());
                existingLabinfo.setContactPerson1Email(labinfo.getContactPerson1Email());
                existingLabinfo.setContactPerson2Name(labinfo.getContactPerson2Name());
                existingLabinfo.setContactPerson2Email(labinfo.getContactPerson2Email());
                existingLabinfo.setContactPerson3Name(labinfo.getContactPerson3Name());
                existingLabinfo.setContactPerson3Email(labinfo.getContactPerson3Email());
                existingLabinfo.setContactPerson4Name(labinfo.getContactPerson4Name());
                existingLabinfo.setContactPerson4Email(labinfo.getContactPerson4Email());
                existingLabinfo.setContactPerson5Name(labinfo.getContactPerson5Name());
                existingLabinfo.setContactPerson5Email(labinfo.getContactPerson5Email());
                existingLabinfo.setWorkdayHours(labinfo.getWorkdayHours());
                existingLabinfo.setWeekendHours(labinfo.getWeekendHours());
                existingLabinfo.setHolidayNote(labinfo.getHolidayNote());
                existingLabinfo.setMapUrl(labinfo.getMapUrl());
                existingLabinfo.setIntroduction(labinfo.getIntroduction());

                // 设置更新时间
                existingLabinfo.setUpdatedAt(java.time.LocalDateTime.now());

                boolean success = labinfoService.updateById(existingLabinfo);
                if (success) {
                    result.put("code", 200);
                    result.put("message", "实验室信息更新成功");
                    result.put("data", existingLabinfo);
                } else {
                    result.put("code", 500);
                    result.put("message", "实验室信息更新失败");
                    result.put("data", null);
                }
            } else {
                // 如果没有记录，则创建新的
                labinfo.setCreatedAt(java.time.LocalDateTime.now());
                labinfo.setUpdatedAt(java.time.LocalDateTime.now());
                boolean success = labinfoService.save(labinfo);
                if (success) {
                    result.put("code", 200);
                    result.put("message", "实验室信息创建成功");
                    result.put("data", labinfo);
                } else {
                    result.put("code", 500);
                    result.put("message", "实验室信息创建失败");
                    result.put("data", null);
                }
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新实验室信息失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }
}
