package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.entity.Project;
import com.example.service.IProjectService;
import lombok.var;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private IProjectService projectService;

    /**
     * 获取科研项目列表（支持搜索）
     */
    @GetMapping("/list")
    public Map<String, Object> getProjectList(@RequestParam(value = "title", required = false) String title,
                                             @RequestParam(value = "type", required = false) String type,
                                             @RequestParam(value = "endtime", required = false) String endtime,
                                             @RequestParam(value = "company", required = false) String company) {
        Map<String, Object> result = new HashMap<>();
        try {
            var queryWrapper = projectService.lambdaQuery();

            // 添加搜索条件
            if (title != null && !title.trim().isEmpty()) {
                queryWrapper.like(Project::getTitle, title.trim());
            }
            if (type != null && !type.trim().isEmpty()) {
                queryWrapper.eq(Project::getType, type.trim());
            }
            if (company != null && !company.trim().isEmpty()) {
                queryWrapper.like(Project::getCompany, company.trim());
            }
            if (endtime != null) {
                if (endtime.equals("")) {
                    queryWrapper.isNull(Project::getEndtime); // 结题时间为空表示进行中
                } else if (endtime.equals("notnull")) {
                    queryWrapper.isNotNull(Project::getEndtime); // 结题时间不为空表示已结题
                }
            }

            List<Project> projects = queryWrapper.list();
            result.put("code", 200);
            result.put("message", "获取项目列表成功");
            result.put("data", projects);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取项目列表失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 根据项目类型筛选项目
     */
    @GetMapping("/filter")
    public Map<String, Object> getProjectsByType(@RequestParam("type") String type) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Project> projects = projectService.lambdaQuery()
                .eq(Project::getType, type)
                .list();
            result.put("code", 200);
            result.put("message", "筛选项目成功");
            result.put("data", projects);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "筛选项目失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 根据ID获取项目详情
     */
    @GetMapping("/{id}")
    public Map<String, Object> getProjectById(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            Project project = projectService.getById(id);
            if (project != null) {
                result.put("code", 200);
                result.put("message", "获取项目详情成功");
                result.put("data", project);
            } else {
                result.put("code", 404);
                result.put("message", "项目不存在");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取项目详情失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 获取进行中的项目
     */
    @GetMapping("/ongoing")
    public Map<String, Object> getOngoingProjects() {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Project> projects = projectService.lambdaQuery()
                .isNull(Project::getEndtime)  // 结题时间为空表示进行中
                .list();
            result.put("code", 200);
            result.put("message", "获取进行中项目成功");
            result.put("data", projects);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "获取进行中项目失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 添加科研项目
     */
    @PostMapping
    public Map<String, Object> addProject(@RequestBody Project project) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 对于LocalDate类型，不需要trim()方法，直接检查是否为null即可
            // 前端传递空字符串时，Spring会自动转换为null
            // 这里不需要额外处理

            boolean success = projectService.save(project);
            if (success) {
                result.put("code", 200);
                result.put("message", "添加项目成功");
                result.put("data", project);
            } else {
                result.put("code", 500);
                result.put("message", "添加项目失败");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "添加项目失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 编辑科研项目
     */
    @PutMapping
    public Map<String, Object> updateProject(@RequestBody Project project) {
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("更新项目，endtime值为: " + project.getEndtime());

            // 使用UpdateWrapper确保NULL值能被正确更新到数据库
            boolean success = projectService.update(
                new UpdateWrapper<Project>()
                    .eq("id", project.getId())
                    .set("title", project.getTitle())
                    .set("startTime", project.getStarttime())
                    .set("endTime", project.getEndtime())  // 正确设置NULL值
                    .set("type", project.getType())
                    .set("company", project.getCompany())  // 添加合作方字段
            );

            if (success) {
                result.put("code", 200);
                result.put("message", "编辑项目成功");
                result.put("data", project);
            } else {
                result.put("code", 500);
                result.put("message", "编辑项目失败");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "编辑项目失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }

    /**
     * 删除科研项目
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteProject(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = projectService.removeById(id);
            if (success) {
                result.put("code", 200);
                result.put("message", "删除项目成功");
                result.put("data", null);
            } else {
                result.put("code", 500);
                result.put("message", "删除项目失败");
                result.put("data", null);
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "删除项目失败: " + e.getMessage());
            result.put("data", null);
        }
        return result;
    }
}
