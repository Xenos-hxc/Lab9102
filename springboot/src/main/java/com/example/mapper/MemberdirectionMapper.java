package com.example.mapper;

import com.example.entity.Memberdirection;
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
public interface MemberdirectionMapper extends BaseMapper<Memberdirection> {

    @Select("SELECT * FROM memberdirection WHERE memberId = #{memberId}")
    List<Memberdirection> selectByMemberId(Integer memberId);

    @Select("SELECT * FROM memberdirection WHERE directionId = #{directionId}")
    List<Memberdirection> selectByDirectionId(Integer directionId);
}
