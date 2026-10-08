package com.example.controller;

import com.example.entity.Gallery;
import com.example.service.IGalleryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author example.demo
 * @since 2025-11-04
 */
@RestController
@RequestMapping("/gallery")
@CrossOrigin(origins = "*") // 允许跨域请求
public class GalleryController {

    @Autowired
    private IGalleryService galleryService;

    /**
     * 获取团建活动列表
     * @return 活动列表
     */
    @GetMapping("/list")
    public Map<String, Object> getGalleryList() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Gallery> galleryList = galleryService.list();
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", galleryList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 根据ID获取团建活动详情
     * @param id 活动ID
     * @return 活动详情
     */
    @GetMapping("/detail/{id}")
    public Map<String, Object> getGalleryDetail(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Gallery gallery = galleryService.getById(id);
            if (gallery != null) {
                result.put("code", 200);
                result.put("message", "获取成功");
                result.put("data", gallery);
            } else {
                result.put("code", 404);
                result.put("message", "活动不存在");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 根据年份获取团建活动列表
     * @param year 年份
     * @return 活动列表
     */
    @GetMapping("/list/year/{year}")
    public Map<String, Object> getGalleryListByYear(@PathVariable Integer year) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 使用MyBatis-Plus的查询条件构造器
            List<Gallery> galleryList = galleryService.lambdaQuery()
                    .apply("YEAR(time) = {0}", year)
                    .list();

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", galleryList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 根据类型获取团建活动列表
     * @param type 活动类型
     * @return 活动列表
     */
    @GetMapping("/list/type/{type}")
    public Map<String, Object> getGalleryListByType(@PathVariable String type) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Gallery> galleryList = galleryService.lambdaQuery()
                    .eq(Gallery::getType, type)
                    .list();

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", galleryList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 搜索团建活动
     * @param keyword 关键词
     * @return 搜索结果
     */
    @GetMapping("/search")
    public Map<String, Object> searchGallery(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Gallery> galleryList = galleryService.lambdaQuery()
                    .like(Gallery::getTitle, keyword)
                    .or()
                    .like(Gallery::getSummary, keyword)
                    .or()
                    .like(Gallery::getContent, keyword)
                    .list();

            result.put("code", 200);
            result.put("message", "搜索成功");
            result.put("data", galleryList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "搜索失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 添加团建活动
     * @param gallery 活动信息
     * @return 添加结果
     */
    @PostMapping("/add")
    public Map<String, Object> addGallery(@RequestBody Gallery gallery) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = galleryService.save(gallery);
            if (success) {
                result.put("code", 200);
                result.put("message", "添加成功");
                result.put("data", gallery.getId());
            } else {
                result.put("code", 500);
                result.put("message", "添加失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "添加失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 更新团建活动
     * @param gallery 活动信息
     * @return 更新结果
     */
    @PutMapping("/update")
    public Map<String, Object> updateGallery(@RequestBody Gallery gallery) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = galleryService.updateById(gallery);
            if (success) {
                result.put("code", 200);
                result.put("message", "更新成功");
            } else {
                result.put("code", 500);
                result.put("message", "更新失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新失败: " + e.getMessage());
        }
        return result;
    }

    /**
     * 删除团建活动
     * @param id 活动ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteGallery(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = galleryService.removeById(id);
            if (success) {
                result.put("code", 200);
                result.put("message", "删除成功");
            } else {
                result.put("code", 500);
                result.put("message", "删除失败");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除失败: " + e.getMessage());
        }
        return result;
    }
}
