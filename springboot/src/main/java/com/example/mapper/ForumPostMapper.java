package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.entity.ForumPost;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface ForumPostMapper extends BaseMapper<ForumPost> {
    List<Map<String, Object>> selectPostList(
            @Param("keyword") String keyword,
            @Param("categoryId") Integer categoryId,
            @Param("authorId") Integer authorId,
            @Param("status") String status,
            @Param("includeMemberOnly") boolean includeMemberOnly,
            @Param("adminMode") boolean adminMode,
            @Param("sort") String sort);

    Map<String, Object> selectPostDetail(@Param("id") Integer id);

    List<Map<String, Object>> selectRelatedPosts(
            @Param("categoryId") Integer categoryId,
            @Param("excludeId") Integer excludeId,
            @Param("includeMemberOnly") boolean includeMemberOnly);

    List<Map<String, Object>> selectCategoryStats();

    List<Map<String, Object>> selectContributorStats();
}
