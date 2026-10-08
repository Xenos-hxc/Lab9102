package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.Member;
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
public interface MemberMapper extends BaseMapper<Member> {

    @Select("SELECT * FROM member WHERE identity = #{identity}")
    List<Member> selectByIdentity(String identity);

    @Select("SELECT m.* FROM member m " +
            "JOIN memberdirection md ON m.id = md.memberId " +
            "WHERE md.directionId = #{directionId}")
    List<Member> selectMembersByDirectionId(Integer directionId);

    @Select("SELECT DISTINCT identity FROM member")
    List<String> selectAllIdentities();

    @Select("SELECT d.name FROM direction d " +
            "JOIN memberdirection md ON d.id = md.directionId " +
            "WHERE md.memberId = #{memberId}")
    List<String> selectResearchDirectionsByMemberId(Integer memberId);
}
