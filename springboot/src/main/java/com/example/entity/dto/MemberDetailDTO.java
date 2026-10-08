package com.example.entity.dto;

import com.example.entity.Member;
import java.util.List;

/**
 * 研究人员详情数据传输对象
 */
public class MemberDetailDTO {
    private Member member;
    private List<String> researchDirections;
    private List<Member> mentors;
    private List<Member> students;

    public MemberDetailDTO() {}

    public MemberDetailDTO(Member member, List<String> researchDirections) {
        this.member = member;
        this.researchDirections = researchDirections;
    }

    // Getters and Setters
    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public List<String> getResearchDirections() {
        return researchDirections;
    }

    public void setResearchDirections(List<String> researchDirections) {
        this.researchDirections = researchDirections;
    }

    public List<Member> getMentors() {
        return mentors;
    }

    public void setMentors(List<Member> mentors) {
        this.mentors = mentors;
    }

    public List<Member> getStudents() {
        return students;
    }

    public void setStudents(List<Member> students) {
        this.students = students;
    }
}
