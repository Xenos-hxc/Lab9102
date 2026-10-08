package com.example.service.impl;

import com.example.entity.Direction;
import com.example.mapper.DirectionMapper;
import com.example.service.IDirectionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DirectionServiceImpl extends ServiceImpl<DirectionMapper, Direction> implements IDirectionService {

    @Autowired
    private DirectionMapper directionMapper;

    @Override
    public List<Direction> getParentDirections() {
        return directionMapper.selectParentDirections();
    }

    @Override
    public List<Direction> getChildDirections(Integer parentId) {
        return directionMapper.selectChildDirections(parentId);
    }

    @Override
    public List<Direction> getDirectionsByMemberId(Integer memberId) {
        return directionMapper.selectDirectionsByMemberId(memberId);
    }

    @Override
    public List<Direction> getAllDirectionsWithMembers() {
        List<Direction> directions = directionMapper.selectList(null);
        // 这里可以添加获取关联成员信息的逻辑
        return directions;
    }
}
