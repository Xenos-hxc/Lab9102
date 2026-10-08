package com.example.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.ResolutionSetting;
import com.example.mapper.ResolutionSettingMapper;
import com.example.service.IResolutionSettingService;
import org.springframework.stereotype.Service;

@Service
public class ResolutionSettingServiceImpl extends ServiceImpl<ResolutionSettingMapper, ResolutionSetting> implements IResolutionSettingService {
}
