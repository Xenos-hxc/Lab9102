package com.example.controller;

import com.example.common.Result;
import com.example.entity.Direction;
import com.example.service.IDirectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/direction")
public class DirectionController {

    @Autowired
    private IDirectionService directionService;

    @GetMapping("/list")
    public Result list() {
        List<Direction> directions = directionService.list();
        return Result.success(directions);
    }

    @GetMapping("/parent")
    public Result getParentDirections() {
        List<Direction> directions = directionService.getParentDirections();
        return Result.success(directions);
    }

    @GetMapping("/child/{parentId}")
    public Result getChildDirections(@PathVariable Integer parentId) {
        List<Direction> directions = directionService.getChildDirections(parentId);
        return Result.success(directions);
    }

    @GetMapping("/member/{memberId}")
    public Result getDirectionsByMemberId(@PathVariable Integer memberId) {
        List<Direction> directions = directionService.getDirectionsByMemberId(memberId);
        return Result.success(directions);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        Direction direction = directionService.getById(id);
        return direction != null ? Result.success(direction) : Result.error("研究方向不存在");
    }

    @PostMapping("/save")
    public Result save(@RequestBody Direction direction) {
        boolean success = directionService.saveOrUpdate(direction);
        return success ? Result.success(direction) : Result.error("保存失败");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        boolean success = directionService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }
}
