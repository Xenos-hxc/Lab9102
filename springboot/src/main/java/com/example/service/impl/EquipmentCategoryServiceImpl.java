package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.EquipmentCategory;
import com.example.mapper.EquipmentCategoryMapper;
import com.example.service.IEquipmentCategoryService;
import org.springframework.stereotype.Service;

@Service
public class EquipmentCategoryServiceImpl extends ServiceImpl<EquipmentCategoryMapper, EquipmentCategory>
        implements IEquipmentCategoryService {
}
