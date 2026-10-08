package com.example.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.entity.Member;
import com.example.entity.dto.MemberDetailDTO;
import com.example.mapper.DirectionMapper;
import com.example.mapper.MemberMapper;
import com.example.mapper.MemberdirectionMapper;
import com.example.mapper.MemberMentorMapper;
import com.example.service.IMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements IMemberService {

    @Autowired
    private MemberMapper memberMapper;

    @Autowired
    private MemberdirectionMapper memberdirectionMapper;

    @Autowired
    private DirectionMapper directionMapper;

    @Autowired
    private MemberMentorMapper memberMentorMapper;

    @Override
    public List<Member> getMembersByIdentity(String identity) {
        return memberMapper.selectByIdentity(identity);
    }

    @Override
    public List<Member> getMembersByDirectionId(Integer directionId) {
        return memberMapper.selectMembersByDirectionId(directionId);
    }

    @Override
    public List<String> getAllIdentities() {
        return memberMapper.selectAllIdentities();
    }

    @Override
    public List<Member> getAllMembersWithDirections() {
        List<Member> members = memberMapper.selectList(null);
        // 这里可以添加获取关联研究方向信息的逻辑
        return members;
    }

    @Override
    public MemberDetailDTO getMemberDetail(Integer memberId) {
        // 获取成员基本信息
        Member member = memberMapper.selectById(memberId);
        if (member == null) {
            return null;
        }

        // 获取研究方向信息
        List<String> researchDirections = memberdirectionMapper.selectByMemberId(memberId)
            .stream()
            .map(relation -> {
                // 这里需要根据directionId获取研究方向名称
                // 在实际项目中，可能需要查询Direction表
                return directionMapper.selectById(relation.getDirectionid()).getName(); // 简化处理
            })
            .collect(Collectors.toList());

        MemberDetailDTO detail = new MemberDetailDTO(member, researchDirections);
        List<Integer> mentorIds = memberMentorMapper.selectList(
                        new QueryWrapper<com.example.entity.MemberMentor>().eq("studentId", memberId))
                .stream().map(com.example.entity.MemberMentor::getMentorId).collect(Collectors.toList());
        List<Integer> studentIds = memberMentorMapper.selectList(
                        new QueryWrapper<com.example.entity.MemberMentor>().eq("mentorId", memberId))
                .stream().map(com.example.entity.MemberMentor::getStudentId).collect(Collectors.toList());
        detail.setMentors(mentorIds.isEmpty() ? Collections.emptyList() : memberMapper.selectBatchIds(mentorIds));
        detail.setStudents(studentIds.isEmpty() ? Collections.emptyList() : memberMapper.selectBatchIds(studentIds));
        return detail;
    }

    @Override
    public Member getMemberByUsername(String username) {
        QueryWrapper<Member> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        return memberMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean updateMemberInfo(Integer id, String name, String identity, String introduction, String entrytime, String email) {
        try {
            Member member = memberMapper.selectById(id);
            if (member == null) {
                return false;
            }

            if (name != null) {
                member.setName(name);
            }
            if (identity != null) {
                member.setIdentity(identity);
            }
            if (introduction != null) {
                member.setIntroduction(introduction);
            }
            if (entrytime != null) {
                member.setEntrytime(Year.parse(entrytime));
            }
            if (email != null) {
                member.setEmail(email);
            }

            return memberMapper.updateById(member) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean updateMemberAvatar(Integer id, String pictureurl) {
        try {
            Member member = memberMapper.selectById(id);
            if (member == null) {
                return false;
            }

            if (pictureurl != null) {
                member.setPictureurl(pictureurl);
            }

            return memberMapper.updateById(member) > 0;
        } catch (Exception e) {
            return false;
        }
    }
}
