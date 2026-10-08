package com.example.controller;

import com.example.entity.News;
import com.example.service.INewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
@RequestMapping("/news")
@CrossOrigin(origins = "*") // 允许跨域请求
public class NewsController {

    @Autowired
    private INewsService newsService;

    /**
     * 获取新闻列表
     * @return 新闻列表
     */
    @GetMapping("/list")
    public Map<String, Object> getNewsList() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<News> newsList = newsService.list();
            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", newsList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 根据ID获取新闻详情
     * @param id 新闻ID
     * @return 新闻详情
     */
    @GetMapping("/detail/{id}")
    public Map<String, Object> getNewsDetail(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            News news = newsService.getById(id);
            if (news != null) {
                result.put("code", 200);
                result.put("message", "获取成功");
                result.put("data", news);
            } else {
                result.put("code", 404);
                result.put("message", "新闻不存在");
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
     * 根据年份获取新闻列表
     * @param year 年份
     * @return 新闻列表
     */
    @GetMapping("/list/year/{year}")
    public Map<String, Object> getNewsListByYear(@PathVariable Integer year) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 使用MyBatis-Plus的查询条件构造器
            List<News> newsList = newsService.lambdaQuery()
                    .apply("YEAR(time) = {0}", year)
                    .list();

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", newsList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 根据类型获取新闻列表
     * @param type 新闻类型
     * @return 新闻列表
     */
    @GetMapping("/list/type/{type}")
    public Map<String, Object> getNewsListByType(@PathVariable String type) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<News> newsList = newsService.lambdaQuery()
                    .eq(News::getType, type)
                    .list();

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", newsList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 搜索新闻
     * @param keyword 关键词
     * @return 搜索结果
     */
    @GetMapping("/search")
    public Map<String, Object> searchNews(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<News> newsList = newsService.lambdaQuery()
                    .like(News::getTitle, keyword)
                    .or()
                    .like(News::getSummary, keyword)
                    .or()
                    .like(News::getContent, keyword)
                    .list();

            result.put("code", 200);
            result.put("message", "搜索成功");
            result.put("data", newsList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "搜索失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 添加新闻
     * @param news 新闻信息
     * @return 添加结果
     */
    @PostMapping("/add")
    public Map<String, Object> addNews(@RequestBody News news) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = newsService.save(news);
            if (success) {
                result.put("code", 200);
                result.put("message", "添加成功");
                result.put("data", news.getId());
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
     * 更新新闻
     * @param news 新闻信息
     * @return 更新结果
     */
    @PutMapping("/update")
    public Map<String, Object> updateNews(@RequestBody News news) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = newsService.updateById(news);
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
     * 删除新闻
     * @param id 新闻ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteNews(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = newsService.removeById(id);
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

    /**
     * 获取最新五条新闻
     * @return 最新五条新闻列表
     */
    @GetMapping("/latest")
    public Map<String, Object> getLatestNews() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<News> newsList = newsService.lambdaQuery()
                    .orderByDesc(News::getTime)  // 按时间降序排列
                    .last("LIMIT 5")  // 限制返回5条
                    .list();

            result.put("code", 200);
            result.put("message", "获取成功");
            result.put("data", newsList);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }
}
