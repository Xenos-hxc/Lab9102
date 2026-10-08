package com.example.controller;

import com.example.common.Result;
import com.example.entity.Memberdirection;
import com.example.mapper.MemberdirectionMapper;
import com.example.service.IMemberdirectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/memberdirection")
public class MemberdirectionController {

    @Autowired
    private IMemberdirectionService memberdirectionService;

    @Autowired
    private MemberdirectionMapper memberdirectionMapper;

    @GetMapping("/list")
    public Result list() {
        List<Memberdirection> relations = memberdirectionService.list();
        return Result.success(relations);
    }

    @GetMapping("/member/{memberId}")
    public Result getByMemberId(@PathVariable Integer memberId) {
        List<Memberdirection> relations = memberdirectionMapper.selectByMemberId(memberId);
        return Result.success(relations);
    }

    @GetMapping("/direction/{directionId}")
    public Result getByDirectionId(@PathVariable Integer directionId) {
        List<Memberdirection> relations = memberdirectionMapper.selectByDirectionId(directionId);
        return Result.success(relations);
    }

    @PostMapping("/save")
    public Result save(@RequestBody Memberdirection relation) {
        boolean success = memberdirectionService.saveOrUpdate(relation);
        return success ? Result.success(relation) : Result.error("保存失败");
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        boolean success = memberdirectionService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }
}
