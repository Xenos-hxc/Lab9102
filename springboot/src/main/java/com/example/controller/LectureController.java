package com.example.controller;

import com.example.entity.Lecture;
import com.example.service.ILectureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author example.demo
 * @since 2025-11-05
 */
@RestController
@RequestMapping("/lecture")
@CrossOrigin(origins = "*")
public class LectureController {

    @Autowired
    private ILectureService lectureService;

    /**
     * 获取所有讲座列表
     */
    @GetMapping("/list")
    public Map<String, Object> getLectureList() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Lecture> lectures = lectureService.list();
            result.put("code", 200);
            result.put("message", "获取讲座列表成功");
            result.put("data", lectures);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取讲座列表失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 根据ID获取讲座详情
     */
    @GetMapping("/detail/{id}")
    public Map<String, Object> getLectureDetail(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Lecture lecture = lectureService.getById(id);
            if (lecture != null) {
                result.put("code", 200);
                result.put("message", "获取讲座详情成功");
                result.put("data", lecture);
            } else {
                result.put("code", 404);
                result.put("message", "讲座不存在");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取讲座详情失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 搜索讲座
     */
    @GetMapping("/search")
    public Map<String, Object> searchLectures(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Lecture> lectures = lectureService.lambdaQuery()
                    .like(Lecture::getTitle, keyword)
                    .or()
                    .like(Lecture::getSpeaker, keyword)
                    .or()
                    .like(Lecture::getHost, keyword)
                    .or()
                    .like(Lecture::getSpeakerfrom, keyword)
                    .list();

            result.put("code", 200);
            result.put("message", "搜索成功");
            result.put("data", lectures);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "搜索失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 按时间范围筛选讲座
     */
    @GetMapping("/filter/time")
    public Map<String, Object> filterLecturesByTime(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Lecture> lectures = lectureService.lambdaQuery()
                    .ge(Lecture::getTime, startTime)
                    .le(Lecture::getTime, endTime)
                    .list();

            result.put("code", 200);
            result.put("message", "筛选成功");
            result.put("data", lectures);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "筛选失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 添加讲座
     */
    @PostMapping("/add")
    public Map<String, Object> addLecture(@RequestBody Lecture lecture) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = lectureService.save(lecture);
            if (success) {
                result.put("code", 200);
                result.put("message", "添加讲座成功");
                result.put("data", lecture);
            } else {
                result.put("code", 500);
                result.put("message", "添加讲座失败");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "添加讲座失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 更新讲座
     */
    @PutMapping("/update")
    public Map<String, Object> updateLecture(@RequestBody Lecture lecture) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = lectureService.updateById(lecture);
            if (success) {
                result.put("code", 200);
                result.put("message", "更新讲座成功");
                result.put("data", lecture);
            } else {
                result.put("code", 500);
                result.put("message", "更新讲座失败");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "更新讲座失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 删除讲座
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteLecture(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = lectureService.removeById(id);
            if (success) {
                result.put("code", 200);
                result.put("message", "删除讲座成功");
                result.put("data", null);
            } else {
                result.put("code", 500);
                result.put("message", "删除讲座失败");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除讲座失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }
}
