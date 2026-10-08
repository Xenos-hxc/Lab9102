package com.example.controller;

import com.example.common.Result;
import com.example.entity.Member;
import com.example.service.IMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IMemberService memberService;

    @PostMapping("/login")
    public Result login(@RequestBody LoginRequest request) {
        // 验证用户名和密码
        Member member = memberService.getMemberByUsername(request.getUsername());

        if (member == null) {
            return Result.error("用户名不存在");
        }

        // 验证密码
        if (!request.getPassword().equals(member.getPassword())) {
            return Result.error("密码错误");
        }

        // 登录成功，返回用户信息（不包含密码）
        LoginResponse response = new LoginResponse();
        response.setId(member.getId());
        response.setUsername(member.getUsername());
        response.setName(member.getName());
        response.setIdentity(member.getIdentity());
        response.setIsAdmin(Boolean.TRUE.equals(member.getIsAdmin()) || "admin".equals(member.getUsername()));
        response.setAdminPermissions("admin".equals(member.getUsername()) ? "*" : member.getAdminPermissions());

        return Result.success(response);
    }

    @PostMapping("/logout")
    public Result logout() {
        // 在实际项目中，这里可能需要清除token或session
        // 由于当前是简单的登录验证，直接返回成功即可
        return Result.success("退出登录成功");
    }

    @GetMapping("/current-user")
    public Result getCurrentUser(@RequestParam(required = false) String username) {
        if (username == null || username.trim().isEmpty()) {
            return Result.error("用户名不能为空");
        }

        Member member = memberService.getMemberByUsername(username);
        if (member == null) {
            return Result.error("用户不存在");
        }

        // 返回用户信息（不包含密码）
        CurrentUserResponse response = new CurrentUserResponse();
        response.setId(member.getId());
        response.setUsername(member.getUsername());
        response.setName(member.getName());
        response.setIdentity(member.getIdentity());
        response.setIsAdmin(Boolean.TRUE.equals(member.getIsAdmin()) || "admin".equals(member.getUsername()));
        response.setAdminPermissions("admin".equals(member.getUsername()) ? "*" : member.getAdminPermissions());

        return Result.success(response);
    }

    // 登录请求DTO
    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    // 登录响应DTO
    public static class LoginResponse {
        private Integer id;
        private String username;
        private String name;
        private String identity;
        private Boolean isAdmin;
        private String adminPermissions;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
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

        public Boolean getIsAdmin() {
            return isAdmin;
        }

        public void setIsAdmin(Boolean admin) {
            isAdmin = admin;
        }

        public String getAdminPermissions() {
            return adminPermissions;
        }

        public void setAdminPermissions(String adminPermissions) {
            this.adminPermissions = adminPermissions;
        }
    }

    // 当前用户信息响应DTO
    public static class CurrentUserResponse {
        private Integer id;
        private String username;
        private String name;
        private String identity;
        private Boolean isAdmin;
        private String adminPermissions;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
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

        public Boolean getIsAdmin() {
            return isAdmin;
        }

        public void setIsAdmin(Boolean admin) {
            isAdmin = admin;
        }

        public String getAdminPermissions() {
            return adminPermissions;
        }

        public void setAdminPermissions(String adminPermissions) {
            this.adminPermissions = adminPermissions;
        }
    }
}
