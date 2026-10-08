package com.example.controller;

import com.example.common.Result;
import com.example.entity.Equipment;
import com.example.entity.EquipmentCategory;
import com.example.service.IEquipmentCategoryService;
import com.example.service.IEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/equipment")
public class EquipmentController {
    @Autowired
    private IEquipmentService equipmentService;

    @Autowired
    private IEquipmentCategoryService equipmentCategoryService;

    @GetMapping("/list")
    public Result list() {
        List<Equipment> equipment = equipmentService.lambdaQuery()
                .orderByDesc(Equipment::getPurchaseDate)
                .orderByDesc(Equipment::getId)
                .list();
        fillCategoryNames(equipment);
        return Result.success(equipment);
    }

    @GetMapping("/year/{year}")
    public Result listByYear(@PathVariable Integer year) {
        List<Equipment> equipment = equipmentService.lambdaQuery()
                .apply("YEAR(purchaseDate) = {0}", year)
                .orderByDesc(Equipment::getPurchaseDate)
                .list();
        fillCategoryNames(equipment);
        return Result.success(equipment);
    }

    @GetMapping("/categories")
    public Result categories() {
        return Result.success(equipmentCategoryService.lambdaQuery()
                .orderByAsc(EquipmentCategory::getSortOrder)
                .orderByAsc(EquipmentCategory::getId)
                .list());
    }

    @PostMapping("/save")
    public Result save(@RequestBody Equipment equipment) {
        if (equipment.getCategoryId() == null || equipmentCategoryService.getById(equipment.getCategoryId()) == null) {
            return Result.error("请选择有效的设备分类");
        }
        boolean success = equipmentService.saveOrUpdate(equipment);
        return success ? Result.success(equipment) : Result.error("设备保存失败");
    }

    @PostMapping("/category/save")
    public Result saveCategory(@RequestBody EquipmentCategory category) {
        String name = category.getName() == null ? "" : category.getName().trim();
        if (name.isEmpty()) {
            return Result.error("请输入分类名称");
        }
        List<EquipmentCategory> duplicates = equipmentCategoryService.lambdaQuery()
                .eq(EquipmentCategory::getName, name)
                .list();
        boolean nameExists = duplicates.stream()
                .anyMatch(item -> category.getId() == null || !category.getId().equals(item.getId()));
        if (nameExists) {
            return Result.error("分类名称已存在");
        }
        category.setName(name);
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        boolean success = equipmentCategoryService.saveOrUpdate(category);
        return success ? Result.success(category) : Result.error("分类保存失败");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return equipmentService.removeById(id) ? Result.success(true) : Result.error("设备删除失败");
    }

    @DeleteMapping("/category/{id}")
    public Result deleteCategory(@PathVariable Integer id) {
        if (!equipmentService.lambdaQuery().eq(Equipment::getCategoryId, id).list().isEmpty()) {
            return Result.error("该分类下仍有设备，请先移动或删除相关设备");
        }
        return equipmentCategoryService.removeById(id)
                ? Result.success(true)
                : Result.error("分类删除失败");
    }

    private void fillCategoryNames(List<Equipment> equipment) {
        if (equipment == null || equipment.isEmpty()) {
            return;
        }
        List<EquipmentCategory> categories = equipmentCategoryService.list();
        Map<Integer, String> categoryNames = new HashMap<>();
        for (EquipmentCategory category : categories == null ? Collections.<EquipmentCategory>emptyList() : categories) {
            categoryNames.put(category.getId(), category.getName());
        }
        for (Equipment item : equipment) {
            item.setCategoryName(categoryNames.get(item.getCategoryId()));
        }
    }
}
