package com.example.controller;

import com.example.entity.Paper;
import com.example.service.IPaperService;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author example.demo
 * @since 2025-11-04
 */
@RestController
@RequestMapping("/paper")
public class PaperController {

    @Autowired
    private IPaperService paperService;

    /**
     * 获取所有科研论文列表
     */
    @GetMapping("/list")
    public Map<String, Object> getPaperList(@RequestParam(value = "title", required = false) String title,
                                            @RequestParam(value = "authors", required = false) String authors,
                                            @RequestParam(value = "place", required = false) String place,
                                            @RequestParam(value = "level", required = false) String level) {
        Map<String, Object> result = new HashMap<>();
        try {
            var queryWrapper = paperService.lambdaQuery();

            // 添加搜索条件
            if (title != null && !title.trim().isEmpty()) {
                queryWrapper.like(Paper::getTitle, title.trim());
            }
            if (authors != null && !authors.trim().isEmpty()) {
                queryWrapper.like(Paper::getAuthors, authors.trim());
            }
            if (place != null && !place.trim().isEmpty()) {
                queryWrapper.like(Paper::getPlace, place.trim());
            }
            if (level != null && !level.trim().isEmpty()) {
                queryWrapper.like(Paper::getLevel, level.trim());
            }

            List<Paper> papers = queryWrapper.list();
            result.put("code", 200);
            result.put("message", "获取论文列表成功");
            result.put("data", papers);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取论文列表失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 根据年份范围筛选论文
     */
    @GetMapping("/filter")
    public Map<String, Object> getPapersByYearRange(@RequestParam("startYear") Integer startYear,
                                                   @RequestParam("endYear") Integer endYear) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Paper> papers = paperService.lambdaQuery()
                .ge(Paper::getTime, startYear)
                .le(Paper::getTime, endYear)
                .list();
            result.put("code", 200);
            result.put("message", "筛选论文成功");
            result.put("data", papers);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "筛选论文失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 根据ID获取论文详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getPaperById(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Paper paper = paperService.getById(id);
            if (paper != null) {
                result.put("code", 200);
                result.put("message", "获取论文详情成功");
                result.put("data", paper);
            } else {
                result.put("code", 404);
                result.put("message", "论文不存在");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取论文详情失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 添加科研论文
     */
    @PostMapping
    public Map<String, Object> addPaper(@RequestBody Paper paper) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = paperService.save(paper);
            if (success) {
                result.put("code", 200);
                result.put("message", "添加论文成功");
                result.put("data", paper);
            } else {
                result.put("code", 500);
                result.put("message", "添加论文失败");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "添加论文失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 编辑科研论文
     */
    @PutMapping
    public Map<String, Object> updatePaper(@RequestBody Paper paper) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = paperService.updateById(paper);
            if (success) {
                result.put("code", 200);
                result.put("message", "编辑论文成功");
                result.put("data", paper);
            } else {
                result.put("code", 500);
                result.put("message", "编辑论文失败");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "编辑论文失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 删除科研论文
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deletePaper(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = paperService.removeById(id);
            if (success) {
                result.put("code", 200);
                result.put("message", "删除论文成功");
                result.put("data", null);
            } else {
                result.put("code", 500);
                result.put("message", "删除论文失败");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除论文失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }
}
