package com.example.mapper;

import com.example.entity.Direction;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author example.demo
 * @since 2025-11-04
 */
public interface DirectionMapper extends BaseMapper<Direction> {

    @Select("SELECT * FROM direction WHERE level = 1")
    List<Direction> selectParentDirections();

    @Select("SELECT * FROM direction WHERE level = 2 AND parentId = #{parentId}")
    List<Direction> selectChildDirections(Integer parentId);

    @Select("SELECT d.* FROM direction d " +
            "JOIN memberdirection md ON d.id = md.directionId " +
            "WHERE md.memberId = #{memberId}")
    List<Direction> selectDirectionsByMemberId(Integer memberId);
}
