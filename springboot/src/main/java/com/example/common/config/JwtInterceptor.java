//package com.example.common.config;
//
//import cn.hutool.core.util.ObjectUtil;
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.JWTVerifier;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.auth0.jwt.exceptions.JWTVerificationException;
//import com.example.common.Constants;
//import com.example.common.enums.ResultCodeEnum;
//import com.example.exception.CustomException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
///**
// * jwt拦截器
// */
//@Component
//public class JwtInterceptor implements HandlerInterceptor {
//
//    private static final Logger log = LoggerFactory.getLogger(JwtInterceptor.class);
//
//    @Autowired
//    IUserService iUserService;
//
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
//        // 1. 从http请求的header中获取token
//        String token = request.getHeader(Constants.TOKEN);
//        if (ObjectUtil.isEmpty(token)) {
//            // 如果没拿到，从参数里再拿一次
//            token = request.getParameter(Constants.TOKEN);
//        }
//        // 2. 开始执行认证
//        if (ObjectUtil.isEmpty(token)) {
//
//            throw new CustomException(ResultCodeEnum.TOKEN_INVALID_ERROR);
//        }
//        User user = null;
//        try {
//            // 解析token获取存储的数据
//            String userRole = JWT.decode(token).getAudience().get(0);
//            String userId = userRole.split("-")[0];
//            String role = userRole.split("-")[1];
//            // 根据userId查询数据库
//            user=iUserService.lambdaQuery().eq(User::getId,userId).eq(User::getRole,role).list().get(0);
//        } catch (Exception e) {
//            System.out.println(token+"00000000000000000000000000000000000000000000000000000");
//            throw new CustomException(ResultCodeEnum.TOKEN_CHECK_ERROR);
//        }
//        if (ObjectUtil.isNull(user)) {
//            throw new CustomException(ResultCodeEnum.USER_NOT_EXIST_ERROR);
//        }
//        try {
//            // 用户密码加签验证 token
//            JWTVerifier jwtVerifier = JWT.require(Algorithm.HMAC256(user.getPassword())).build();
//            jwtVerifier.verify(token); // 验证token
//        } catch (JWTVerificationException e) {
//            System.out.println(
//                    token+"1111111111111111111111111111111111111111111111111"
//            );
//            throw new CustomException(ResultCodeEnum.TOKEN_CHECK_ERROR);
//        }
//        return true;
//    }
//}
