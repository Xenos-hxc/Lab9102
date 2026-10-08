package com.example.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.entity.Member;
import com.example.entity.dto.MemberDetailDTO;
import java.util.List;

public interface IMemberService extends IService<Member> {

    List<Member> getMembersByIdentity(String identity);

    List<Member> getMembersByDirectionId(Integer directionId);

    List<String> getAllIdentities();

    List<Member> getAllMembersWithDirections();

    MemberDetailDTO getMemberDetail(Integer memberId);

    // 根据用户名获取成员
    Member getMemberByUsername(String username);

    // 更新成员信息
    boolean updateMemberInfo(Integer id, String name, String identity, String introduction, String entrytime, String email);

    // 更新成员头像
    boolean updateMemberAvatar(Integer id, String pictureurl);
}
