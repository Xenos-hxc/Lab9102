package com.example.controller;

import com.example.common.Result;
import com.example.entity.ResolutionSetting;
import com.example.service.IResolutionSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/resolution-setting")
public class ResolutionSettingController {
    @Autowired
    private IResolutionSettingService settingService;

    @GetMapping("/list")
    public Result list() {
        return Result.success(settingService.lambdaQuery()
                .orderByAsc(ResolutionSetting::getWidth)
                .orderByAsc(ResolutionSetting::getHeight)
                .list());
    }

    @PostMapping("/save")
    public Result save(@RequestBody ResolutionSetting setting) {
        if (setting.getWidth() == null || setting.getWidth() < 320
                || setting.getHeight() == null || setting.getHeight() < 240) {
            return Result.error("请输入有效的分辨率");
        }
        if (setting.getScale() == null
                || setting.getScale().compareTo(new BigDecimal("0.5")) < 0
                || setting.getScale().compareTo(new BigDecimal("2.0")) > 0) {
            return Result.error("缩放系数必须在 0.5 到 2.0 之间");
        }

        ResolutionSetting duplicated = settingService.lambdaQuery()
                .eq(ResolutionSetting::getWidth, setting.getWidth())
                .eq(ResolutionSetting::getHeight, setting.getHeight())
                .ne(setting.getId() != null, ResolutionSetting::getId, setting.getId())
                .one();
        if (duplicated != null) {
            return Result.error("该分辨率已经存在");
        }

        boolean success = settingService.saveOrUpdate(setting);
        return success ? Result.success(setting) : Result.error("分辨率规则保存失败");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return settingService.removeById(id) ? Result.success(true) : Result.error("分辨率规则删除失败");
    }
}
