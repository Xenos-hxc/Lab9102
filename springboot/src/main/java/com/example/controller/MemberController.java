package com.example.controller;

import com.example.common.Result;
import com.example.entity.Member;
import com.example.entity.dto.MemberDetailDTO;
import com.example.entity.MemberMentor;
import com.example.mapper.MemberMentorMapper;
import com.example.service.IMemberService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Year;
import java.util.List;
import java.util.ArrayList;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private IMemberService memberService;

    @Autowired
    private MemberMentorMapper memberMentorMapper;

    @GetMapping("/list")
    public Result list() {
        List<Member> members = memberService.list();
        return Result.success(members);
    }

    @GetMapping("/identity/{identity}")
    public Result getMembersByIdentity(@PathVariable String identity) {
        List<Member> members = memberService.getMembersByIdentity(identity);
        return Result.success(members);
    }

    @GetMapping("/direction/{directionId}")
    public Result getMembersByDirectionId(@PathVariable Integer directionId) {
        List<Member> members = memberService.getMembersByDirectionId(directionId);
        return Result.success(members);
    }

    @GetMapping("/identities")
    public Result getAllIdentities() {
        List<String> identities = memberService.getAllIdentities();
        return Result.success(identities);
    }

    @GetMapping("/{id}")
    public Result getById(@PathVariable Integer id) {
        Member member = memberService.getById(id);
        return member != null ? Result.success(member) : Result.error("成员不存在");
    }

    @GetMapping("/detail/{id}")
    public Result getMemberDetail(@PathVariable Integer id) {
        MemberDetailDTO memberDetail = memberService.getMemberDetail(id);
        if (memberDetail == null) {
            return Result.error("成员不存在");
        }
        return Result.success(memberDetail);
    }

    @GetMapping("/current")
    public Result getCurrentMember(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }

        Member member = memberService.getMemberByUsername(username);
        if (member == null) {
            return Result.error("用户不存在");
        }

        // 返回完整的成员信息（包含密码字段，但前端应该不显示密码）
        return Result.success(member);
    }

    @PostMapping("/save")
    public Result save(@RequestBody Member member) {
        boolean success = memberService.saveOrUpdate(member);
        if (memberService.lambdaQuery().eq(Member::getUsername, member.getUsername()).list().size() >= 2) {
            memberService.removeById(member.getId());
            return Result.error("用户名已存在");
        }
        return success ? Result.success(member) : Result.error("保存失败");
    }

    @PutMapping("/update")
    public Result update(@RequestBody MemberUpdateRequest request) {
        try {
            Member member = memberService.getById(request.getId());
            if (member == null) {
                return Result.error("用户不存在");
            }

            // 更新字段
            if (request.getName() != null) {
                member.setName(request.getName());
            }
            if (request.getIdentity() != null) {
                member.setIdentity(request.getIdentity());
            }
            if (request.getIntroduction() != null) {
                member.setIntroduction(request.getIntroduction());
            }
            if (request.getEntrytime() != null) {
                member.setEntrytime(Year.parse(request.getEntrytime()));
            }
            if (request.getEmail() != null) {
                member.setEmail(request.getEmail());
            }
            if (request.getWorkplace() != null) {
                member.setWorkplace(request.getWorkplace());
            }
            if (request.getProfileUrl() != null) {
                member.setProfileUrl(request.getProfileUrl());
            }
            if (request.getProfileLabel() != null) {
                member.setProfileLabel(request.getProfileLabel());
            }

            boolean success = memberService.updateById(member);
            return success ? Result.success(member) : Result.error("更新失败");
        } catch (Exception e) {
            return Result.error("更新过程中出现错误: " + e.getMessage());
        }
    }

    @PutMapping("/update-avatar")
    public Result updateAvatar(@RequestBody AvatarUpdateRequest request) {
        try {
            Member member = memberService.getById(request.getId());
            if (member == null) {
                return Result.error("用户不存在");
            }

            if (request.getPictureurl() != null) {
                member.setPictureurl(request.getPictureurl());
            }

            boolean success = memberService.updateById(member);
            return success ? Result.success(member) : Result.error("头像更新失败");
        } catch (Exception e) {
            return Result.error("头像更新过程中出现错误: " + e.getMessage());
        }
    }

    @PutMapping("/update-password")
    public Result updatePassword(@RequestBody PasswordUpdateRequest request) {
        try {
            Member member = memberService.getById(request.getId());
            if (member == null) {
                return Result.error("用户不存在");
            }

            // 验证原密码
            if (!member.getPassword().equals(request.getOldPassword())) {
                return Result.error("原密码错误");
            }

            // 更新密码
            member.setPassword(request.getNewPassword());

            boolean success = memberService.updateById(member);
            return success ? Result.success("密码修改成功") : Result.error("密码修改失败");
        } catch (Exception e) {
            return Result.error("密码修改过程中出现错误: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Result delete(@PathVariable Integer id) {
        memberMentorMapper.delete(new QueryWrapper<MemberMentor>()
                .eq("mentorId", id).or().eq("studentId", id));
        boolean success = memberService.removeById(id);
        return success ? Result.success(true) : Result.error("删除失败");
    }

    @GetMapping("/relationships/{id}")
    public Result getRelationships(@PathVariable Integer id) {
        Member member = memberService.getById(id);
        if (member == null) {
            return Result.error("成员不存在");
        }
        List<Integer> mentorIds = new ArrayList<>();
        List<Integer> studentIds = new ArrayList<>();
        for (MemberMentor relation : memberMentorMapper.selectList(
                new QueryWrapper<MemberMentor>().eq("studentId", id))) {
            mentorIds.add(relation.getMentorId());
        }
        for (MemberMentor relation : memberMentorMapper.selectList(
                new QueryWrapper<MemberMentor>().eq("mentorId", id))) {
            studentIds.add(relation.getStudentId());
        }
        RelationshipResponse response = new RelationshipResponse();
        response.setMentorIds(mentorIds);
        response.setStudentIds(studentIds);
        return Result.success(response);
    }

    @PutMapping("/relationships/{id}")
    @Transactional
    public Result updateRelationships(@PathVariable Integer id, @RequestBody RelationshipRequest request) {
        if (memberService.getById(id) == null) {
            return Result.error("成员不存在");
        }

        memberMentorMapper.delete(new QueryWrapper<MemberMentor>()
                .eq("studentId", id).or().eq("mentorId", id));

        if (request.getMentorIds() != null) {
            for (Integer mentorId : request.getMentorIds()) {
                if (mentorId == null || mentorId.equals(id) || memberService.getById(mentorId) == null) continue;
                MemberMentor relation = new MemberMentor();
                relation.setMentorId(mentorId);
                relation.setStudentId(id);
                memberMentorMapper.insert(relation);
            }
        }
        if (request.getStudentIds() != null) {
            for (Integer studentId : request.getStudentIds()) {
                if (studentId == null || studentId.equals(id) || memberService.getById(studentId) == null) continue;
                MemberMentor relation = new MemberMentor();
                relation.setMentorId(id);
                relation.setStudentId(studentId);
                try {
                    memberMentorMapper.insert(relation);
                } catch (Exception ignored) {
                    // 同一关系可能同时由导师列表和学生列表提交，唯一索引负责去重。
                }
            }
        }
        return getRelationships(id);
    }

    @PutMapping("/permissions/{id}")
    public Result updatePermissions(@PathVariable Integer id, @RequestBody PermissionUpdateRequest request) {
        Member member = memberService.getById(id);
        if (member == null) {
            return Result.error("成员不存在");
        }
        if ("admin".equals(member.getUsername())) {
            member.setIsAdmin(true);
            member.setAdminPermissions("*");
        } else {
            member.setIsAdmin(Boolean.TRUE.equals(request.getIsAdmin()));
            member.setAdminPermissions(Boolean.TRUE.equals(request.getIsAdmin())
                    ? request.getAdminPermissions() : "");
        }
        return memberService.updateById(member) ? Result.success(member) : Result.error("权限保存失败");
    }

    // 更新用户信息请求DTO
    public static class MemberUpdateRequest {
        private Integer id;
        private String name;
        private String identity;
        private String introduction;
        private String entrytime;
        private String email;
        private String workplace;
        private String profileUrl;
        private String profileLabel;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getIdentity() {
            return identity;
        }

        public void setIdentity(String identity) {
            this.identity = identity;
        }

        public String getIntroduction() {
            return introduction;
        }

        public void setIntroduction(String introduction) {
            this.introduction = introduction;
        }

        public String getEntrytime() {
            return entrytime;
        }

        public void setEntrytime(String entrytime) {
            this.entrytime = entrytime;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getWorkplace() {
            return workplace;
        }

        public void setWorkplace(String workplace) {
            this.workplace = workplace;
        }

        public String getProfileUrl() {
            return profileUrl;
        }

        public void setProfileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
        }

        public String getProfileLabel() {
            return profileLabel;
        }

        public void setProfileLabel(String profileLabel) {
            this.profileLabel = profileLabel;
        }
    }

    public static class RelationshipRequest {
        private List<Integer> mentorIds;
        private List<Integer> studentIds;

        public List<Integer> getMentorIds() { return mentorIds; }
        public void setMentorIds(List<Integer> mentorIds) { this.mentorIds = mentorIds; }
        public List<Integer> getStudentIds() { return studentIds; }
        public void setStudentIds(List<Integer> studentIds) { this.studentIds = studentIds; }
    }

    public static class RelationshipResponse {
        private List<Integer> mentorIds;
        private List<Integer> studentIds;

        public List<Integer> getMentorIds() { return mentorIds; }
        public void setMentorIds(List<Integer> mentorIds) { this.mentorIds = mentorIds; }
        public List<Integer> getStudentIds() { return studentIds; }
        public void setStudentIds(List<Integer> studentIds) { this.studentIds = studentIds; }
    }

    public static class PermissionUpdateRequest {
        private Boolean isAdmin;
        private String adminPermissions;

        public Boolean getIsAdmin() { return isAdmin; }
        public void setIsAdmin(Boolean admin) { isAdmin = admin; }
        public String getAdminPermissions() { return adminPermissions; }
        public void setAdminPermissions(String adminPermissions) { this.adminPermissions = adminPermissions; }
    }

    // 更新头像请求DTO
    public static class AvatarUpdateRequest {
        private Integer id;
        private String pictureurl;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getPictureurl() {
            return pictureurl;
        }

        public void setPictureurl(String pictureurl) {
            this.pictureurl = pictureurl;
        }
    }

    // 在文件末尾添加密码更新请求DTO
    public static class PasswordUpdateRequest {
        private Integer id;
        private String oldPassword;
        private String newPassword;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
