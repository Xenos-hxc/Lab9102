package com.example.controller;

import com.example.common.Result;
import com.example.entity.Carousel;
import com.example.service.ICarouselService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author example.demo
 * @since 2025-11-03
 */
@RestController
@RequestMapping("/carousel")
public class CarouselController {

    @Autowired
    private ICarouselService carouselService;

    /**
     * 获取所有走马灯数据
     */
    @GetMapping("/list")
    public Result getCarouselList() {
        return Result.success(carouselService.list());
    }

    /**
     * 根据ID获取走马灯数据
     */
    @GetMapping("/{id}")
    public Result getCarouselById(@PathVariable Integer id) {
        return Result.success(carouselService.getById(id));
    }

    /**
     * 添加走马灯
     */
    @PostMapping("/add")
    public Result addCarousel(@RequestBody Carousel carousel) {
        try {
            boolean success = carouselService.save(carousel);
            if (success) {
                return Result.success("添加成功");
            } else {
                return Result.error("添加失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("添加失败: " + e.getMessage());
        }
    }

    /**
     * 更新走马灯
     */
    @PutMapping("/update")
    public Result updateCarousel(@RequestBody Carousel carousel) {
        try {
            if (carousel.getId() == null) {
                return Result.error("ID不能为空");
            }
            boolean success = carouselService.updateById(carousel);
            if (success) {
                return Result.success("更新成功");
            } else {
                return Result.error("更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除走马灯
     */
    @DeleteMapping("/{id}")
    public Result deleteCarousel(@PathVariable Integer id) {
        try {
            boolean success = carouselService.removeById(id);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
