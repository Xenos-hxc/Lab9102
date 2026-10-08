package com.example.service;

import com.example.entity.Direction;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface IDirectionService extends IService<Direction> {

    List<Direction> getParentDirections();

    List<Direction> getChildDirections(Integer parentId);

    List<Direction> getDirectionsByMemberId(Integer memberId);

    List<Direction> getAllDirectionsWithMembers();
}
