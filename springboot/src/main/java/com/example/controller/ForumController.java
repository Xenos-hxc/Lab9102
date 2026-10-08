package com.example.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.example.common.Result;
import com.example.entity.ForumAttachment;
import com.example.entity.ForumAuditLog;
import com.example.entity.ForumCategory;
import com.example.entity.ForumFavorite;
import com.example.entity.ForumLike;
import com.example.entity.ForumPost;
import com.example.entity.ForumPostTag;
import com.example.entity.ForumTag;
import com.example.entity.Member;
import com.example.mapper.ForumAttachmentMapper;
import com.example.mapper.ForumAuditLogMapper;
import com.example.mapper.ForumCategoryMapper;
import com.example.mapper.ForumFavoriteMapper;
import com.example.mapper.ForumLikeMapper;
import com.example.mapper.ForumPostMapper;
import com.example.mapper.ForumPostTagMapper;
import com.example.mapper.ForumTagMapper;
import com.example.mapper.MemberMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/forum")
public class ForumController {

    private static final String DRAFT = "DRAFT";
    private static final String PENDING = "PENDING";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String REJECTED = "REJECTED";
    private static final String OFFLINE = "OFFLINE";

    @Autowired
    private ForumPostMapper forumPostMapper;
    @Autowired
    private ForumCategoryMapper forumCategoryMapper;
    @Autowired
    private ForumTagMapper forumTagMapper;
    @Autowired
    private ForumPostTagMapper forumPostTagMapper;
    @Autowired
    private ForumLikeMapper forumLikeMapper;
    @Autowired
    private ForumFavoriteMapper forumFavoriteMapper;
    @Autowired
    private ForumAttachmentMapper forumAttachmentMapper;
    @Autowired
    private ForumAuditLogMapper forumAuditLogMapper;
    @Autowired
    private MemberMapper memberMapper;

    @GetMapping("/categories")
    public Result categories() {
        return Result.success(forumPostMapper.selectCategoryStats());
    }

    @GetMapping("/tags")
    public Result tags() {
        List<ForumTag> tags = forumTagMapper.selectList(
                new QueryWrapper<ForumTag>()
                        .eq("status", 1)
                        .orderByAsc("name"));
        return Result.success(tags);
    }

    @GetMapping("/overview")
    public Result overview(@RequestParam(required = false) String username) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("categories", forumPostMapper.selectCategoryStats());
        data.put("contributors", forumPostMapper.selectContributorStats());
        PageHelper.startPage(1, 5);
        List<Map<String, Object>> featured = forumPostMapper.selectPostList(
                null, null, null, null, resolveMember(username) != null, false, "featured");
        normalizePostRows(featured);
        data.put("featured", featured);
        return Result.success(data);
    }

    @GetMapping("/posts")
    public Result posts(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Integer categoryId,
                        @RequestParam(defaultValue = "latest") String sort,
                        @RequestParam(defaultValue = "1") Integer pageNum,
                        @RequestParam(defaultValue = "10") Integer pageSize,
                        @RequestParam(required = false) String username) {
        Member member = resolveMember(username);
        PageHelper.startPage(safePage(pageNum), safePageSize(pageSize));
        List<Map<String, Object>> rows = forumPostMapper.selectPostList(
                clean(keyword), categoryId, null, null, member != null, false, sort);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(rows);
        normalizePostRows(rows);
        return Result.success(pageData(rows, pageInfo));
    }

    @GetMapping("/posts/{id}")
    public Result detail(@PathVariable Integer id,
                         @RequestParam(required = false) String username,
                         @RequestParam(defaultValue = "false") boolean admin) {
        Map<String, Object> detail = forumPostMapper.selectPostDetail(id);
        if (detail == null) {
            return fail("文章不存在或已删除");
        }
        Member member = resolveMember(username);
        String status = String.valueOf(detail.get("status"));
        String visibility = String.valueOf(detail.get("visibility"));
        Integer authorId = number(detail.get("authorId"));
        boolean isAuthor = member != null && member.getId().equals(authorId);
        if (!admin && !PUBLISHED.equals(status) && !isAuthor) {
            return fail("文章尚未发布");
        }
        if (!admin && "MEMBER".equals(visibility) && member == null) {
            return fail("该文章仅实验室成员可见，请先登录");
        }

        ForumPost post = forumPostMapper.selectById(id);
        if (PUBLISHED.equals(status) && post != null) {
            post.setViewCount(defaultInt(post.getViewCount()) + 1);
            forumPostMapper.updateById(post);
            detail.put("viewCount", post.getViewCount());
        }
        detail.put("tags", tagNames(id));
        detail.put("attachments", forumAttachmentMapper.selectList(
                new QueryWrapper<ForumAttachment>()
                        .eq("post_id", id)
                        .orderByAsc("id")));
        detail.put("liked", member != null && forumLikeMapper.selectCount(
                new QueryWrapper<ForumLike>()
                        .eq("post_id", id)
                        .eq("member_id", member.getId())) > 0);
        detail.put("favorited", member != null && forumFavoriteMapper.selectCount(
                new QueryWrapper<ForumFavorite>()
                        .eq("post_id", id)
                        .eq("member_id", member.getId())) > 0);
        detail.put("related", forumPostMapper.selectRelatedPosts(
                number(detail.get("categoryId")), id, member != null));
        return Result.success(detail);
    }

    @GetMapping("/my")
    public Result myPosts(@RequestParam String username,
                          @RequestParam(required = false) String status,
                          @RequestParam(defaultValue = "1") Integer pageNum,
                          @RequestParam(defaultValue = "10") Integer pageSize) {
        Member member = resolveMember(username);
        if (member == null) {
            return fail("请先登录");
        }
        PageHelper.startPage(safePage(pageNum), safePageSize(pageSize));
        List<Map<String, Object>> rows = forumPostMapper.selectPostList(
                null, null, member.getId(), clean(status), true, false, "latest");
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(rows);
        normalizePostRows(rows);
        return Result.success(pageData(rows, pageInfo));
    }

    @GetMapping("/favorites")
    public Result favorites(@RequestParam String username) {
        Member member = resolveMember(username);
        if (member == null) {
            return fail("请先登录");
        }
        List<ForumFavorite> relations = forumFavoriteMapper.selectList(
                new QueryWrapper<ForumFavorite>()
                        .eq("member_id", member.getId())
                        .orderByDesc("created_at"));
        List<Map<String, Object>> rows = new ArrayList<>();
        for (ForumFavorite relation : relations) {
            Map<String, Object> row = forumPostMapper.selectPostDetail(relation.getPostId());
            if (row != null && PUBLISHED.equals(String.valueOf(row.get("status")))) {
                rows.add(row);
            }
        }
        normalizePostRows(rows);
        return Result.success(rows);
    }

    @PostMapping("/posts/save")
    @Transactional
    public Result savePost(@RequestBody ForumPostRequest request) {
        Member member = resolveMember(request.getUsername());
        if (member == null) {
            return fail("请先登录后再发表文章");
        }
        String title = clean(request.getTitle());
        String content = clean(request.getContent());
        if (title == null || title.length() > 200) {
            return fail("文章标题不能为空且不能超过200个字符");
        }
        if (request.getSubmit() && content == null) {
            return fail("提交审核前请填写文章正文");
        }
        if (request.getCategoryId() == null) {
            return fail("请选择文章分类");
        }
        ForumCategory category = forumCategoryMapper.selectById(request.getCategoryId());
        if (category == null || defaultInt(category.getStatus()) != 1) {
            return fail("所选分类不存在或已停用");
        }

        ForumPost post;
        if (request.getId() == null) {
            post = new ForumPost();
            post.setAuthorId(member.getId());
            post.setViewCount(0);
            post.setLikeCount(0);
            post.setFavoriteCount(0);
            post.setIsTop(0);
            post.setIsFeatured(0);
            post.setDeleted(0);
        } else {
            post = forumPostMapper.selectById(request.getId());
            if (post == null || defaultInt(post.getDeleted()) == 1) {
                return fail("文章不存在");
            }
            if (!member.getId().equals(post.getAuthorId())) {
                return fail("只能编辑自己发表的文章");
            }
        }
        post.setCategoryId(request.getCategoryId());
        post.setTitle(title);
        post.setContent(content == null ? "" : content);
        post.setSummary(buildSummary(request.getSummary(), content));
        post.setCoverUrl(clean(request.getCoverUrl()));
        post.setVisibility("PUBLIC".equals(request.getVisibility()) ? "PUBLIC" : "MEMBER");
        post.setRejectReason(null);
        if (request.getSubmit()) {
            post.setStatus(PENDING);
            post.setSubmittedAt(LocalDateTime.now());
        } else {
            post.setStatus(DRAFT);
        }

        if (post.getId() == null) {
            forumPostMapper.insert(post);
        } else {
            forumPostMapper.updateById(post);
        }
        clearRejectReason(post.getId());
        syncTags(post.getId(), request.getTags());
        syncAttachments(post.getId(), request.getAttachments());
        return Result.success(forumPostMapper.selectPostDetail(post.getId()));
    }

    @PostMapping("/posts/{id}/submit")
    public Result submit(@PathVariable Integer id, @RequestParam String username) {
        Member member = resolveMember(username);
        ForumPost post = forumPostMapper.selectById(id);
        if (member == null || post == null || !member.getId().equals(post.getAuthorId())) {
            return fail("文章不存在或无权操作");
        }
        if (clean(post.getTitle()) == null || clean(post.getContent()) == null) {
            return fail("标题和正文填写完整后才能提交");
        }
        post.setStatus(PENDING);
        post.setRejectReason(null);
        post.setSubmittedAt(LocalDateTime.now());
        forumPostMapper.updateById(post);
        clearRejectReason(id);
        return Result.success("已提交管理员审核");
    }

    @DeleteMapping("/posts/{id}")
    public Result deleteOwnPost(@PathVariable Integer id, @RequestParam String username) {
        Member member = resolveMember(username);
        ForumPost post = forumPostMapper.selectById(id);
        if (member == null || post == null || !member.getId().equals(post.getAuthorId())) {
            return fail("文章不存在或无权操作");
        }
        post.setDeleted(1);
        forumPostMapper.updateById(post);
        return Result.success("文章已删除");
    }

    @PostMapping("/posts/{id}/like")
    public Result toggleLike(@PathVariable Integer id, @RequestParam String username) {
        return toggleRelation(id, username, true);
    }

    @PostMapping("/posts/{id}/favorite")
    public Result toggleFavorite(@PathVariable Integer id, @RequestParam String username) {
        return toggleRelation(id, username, false);
    }

    @GetMapping("/admin/posts")
    public Result adminPosts(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer categoryId,
                             @RequestParam(required = false) String status,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageHelper.startPage(safePage(pageNum), safePageSize(pageSize));
        List<Map<String, Object>> rows = forumPostMapper.selectPostList(
                clean(keyword), categoryId, null, clean(status), true, true, "latest");
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(rows);
        normalizePostRows(rows);
        return Result.success(pageData(rows, pageInfo));
    }

    @GetMapping("/admin/stats")
    public Result adminStats() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("total", forumPostMapper.selectCount(new QueryWrapper<ForumPost>().eq("deleted", 0)));
        data.put("pending", countStatus(PENDING));
        data.put("published", countStatus(PUBLISHED));
        data.put("draft", countStatus(DRAFT));
        data.put("offline", countStatus(OFFLINE));
        data.put("rejected", countStatus(REJECTED));
        data.put("views", sumField("view_count"));
        data.put("favorites", forumFavoriteMapper.selectCount(null));
        return Result.success(data);
    }

    @GetMapping("/admin/tags")
    public Result adminTags() {
        List<ForumTag> tags = forumTagMapper.selectList(
                new QueryWrapper<ForumTag>()
                        .orderByDesc("status")
                        .orderByAsc("name"));
        return Result.success(tags);
    }

    @PutMapping("/admin/posts/{id}/review")
    public Result review(@PathVariable Integer id, @RequestBody ReviewRequest request) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null || defaultInt(post.getDeleted()) == 1) {
            return fail("文章不存在");
        }
        String action = request.getAction() == null ? "" : request.getAction().toUpperCase();
        if ("APPROVE".equals(action) || "PUBLISH".equals(action)) {
            post.setStatus(PUBLISHED);
            post.setRejectReason(null);
            post.setPublishedAt(LocalDateTime.now());
        } else if ("REJECT".equals(action)) {
            String reason = clean(request.getReason());
            if (reason == null) {
                return fail("请填写驳回原因");
            }
            post.setStatus(REJECTED);
            post.setRejectReason(reason);
        } else if ("OFFLINE".equals(action)) {
            post.setStatus(OFFLINE);
            post.setRejectReason(clean(request.getReason()));
        } else {
            return fail("不支持的审核操作");
        }
        forumPostMapper.updateById(post);
        if ("APPROVE".equals(action) || "PUBLISH".equals(action)) {
            clearRejectReason(id);
        }
        saveAudit(id, action, request.getReason());
        return Result.success(forumPostMapper.selectPostDetail(id));
    }

    @PutMapping("/admin/posts/{id}/flag")
    public Result flag(@PathVariable Integer id, @RequestBody FlagRequest request) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null || defaultInt(post.getDeleted()) == 1) {
            return fail("文章不存在");
        }
        int value = Boolean.TRUE.equals(request.getValue()) ? 1 : 0;
        if ("TOP".equalsIgnoreCase(request.getType())) {
            post.setIsTop(value);
        } else if ("FEATURED".equalsIgnoreCase(request.getType())) {
            post.setIsFeatured(value);
        } else {
            return fail("不支持的标记类型");
        }
        forumPostMapper.updateById(post);
        return Result.success(post);
    }

    @DeleteMapping("/admin/posts/{id}")
    public Result adminDelete(@PathVariable Integer id) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) {
            return fail("文章不存在");
        }
        post.setDeleted(1);
        forumPostMapper.updateById(post);
        saveAudit(id, "DELETE", "管理员删除");
        return Result.success("文章已删除");
    }

    @PostMapping("/admin/categories")
    public Result saveCategory(@RequestBody CategoryRequest request) {
        String name = clean(request.getName());
        if (name == null || name.length() > 50) {
            return fail("分类名称不能为空且不能超过50个字符");
        }
        QueryWrapper<ForumCategory> duplicate = new QueryWrapper<ForumCategory>().eq("name", name);
        if (request.getId() != null) {
            duplicate.ne("id", request.getId());
        }
        if (forumCategoryMapper.selectCount(duplicate) > 0) {
            return fail("分类名称已存在");
        }
        ForumCategory category = request.getId() == null
                ? new ForumCategory()
                : forumCategoryMapper.selectById(request.getId());
        if (category == null) {
            return fail("分类不存在");
        }
        category.setName(name);
        category.setDescription(clean(request.getDescription()));
        category.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        category.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        if (category.getId() == null) {
            forumCategoryMapper.insert(category);
        } else {
            forumCategoryMapper.updateById(category);
        }
        return Result.success(category);
    }

    @DeleteMapping("/admin/categories/{id}")
    public Result deleteCategory(@PathVariable Integer id) {
        if (forumPostMapper.selectCount(new QueryWrapper<ForumPost>()
                .eq("category_id", id)
                .eq("deleted", 0)) > 0) {
            return fail("该分类下仍有文章，不能删除");
        }
        forumCategoryMapper.deleteById(id);
        return Result.success("分类已删除");
    }

    @PutMapping("/admin/tags/{id}/disable")
    public Result disableTag(@PathVariable Integer id) {
        ForumTag tag = forumTagMapper.selectById(id);
        if (tag == null) {
            return fail("标签不存在");
        }
        tag.setStatus(defaultInt(tag.getStatus()) == 1 ? 0 : 1);
        forumTagMapper.updateById(tag);
        return Result.success(tag);
    }

    private Result toggleRelation(Integer postId, String username, boolean like) {
        Member member = resolveMember(username);
        ForumPost post = forumPostMapper.selectById(postId);
        if (member == null) {
            return fail("请先登录");
        }
        if (post == null || !PUBLISHED.equals(post.getStatus()) || defaultInt(post.getDeleted()) == 1) {
            return fail("文章不存在或尚未发布");
        }
        boolean active;
        if (like) {
            QueryWrapper<ForumLike> query = new QueryWrapper<ForumLike>()
                    .eq("post_id", postId)
                    .eq("member_id", member.getId());
            ForumLike relation = forumLikeMapper.selectOne(query);
            if (relation == null) {
                relation = new ForumLike();
                relation.setPostId(postId);
                relation.setMemberId(member.getId());
                forumLikeMapper.insert(relation);
                active = true;
            } else {
                forumLikeMapper.deleteById(relation.getId());
                active = false;
            }
            post.setLikeCount(Math.toIntExact(forumLikeMapper.selectCount(
                    new QueryWrapper<ForumLike>().eq("post_id", postId))));
        } else {
            QueryWrapper<ForumFavorite> query = new QueryWrapper<ForumFavorite>()
                    .eq("post_id", postId)
                    .eq("member_id", member.getId());
            ForumFavorite relation = forumFavoriteMapper.selectOne(query);
            if (relation == null) {
                relation = new ForumFavorite();
                relation.setPostId(postId);
                relation.setMemberId(member.getId());
                forumFavoriteMapper.insert(relation);
                active = true;
            } else {
                forumFavoriteMapper.deleteById(relation.getId());
                active = false;
            }
            post.setFavoriteCount(Math.toIntExact(forumFavoriteMapper.selectCount(
                    new QueryWrapper<ForumFavorite>().eq("post_id", postId))));
        }
        forumPostMapper.updateById(post);
        Map<String, Object> data = new HashMap<>();
        data.put("active", active);
        data.put("likeCount", post.getLikeCount());
        data.put("favoriteCount", post.getFavoriteCount());
        return Result.success(data);
    }

    private void syncTags(Integer postId, List<String> names) {
        forumPostTagMapper.delete(new QueryWrapper<ForumPostTag>().eq("post_id", postId));
        if (names == null) {
            return;
        }
        List<String> uniqueNames = names.stream()
                .map(this::clean)
                .filter(name -> name != null && name.length() <= 50)
                .distinct()
                .limit(8)
                .collect(Collectors.toList());
        for (String name : uniqueNames) {
            ForumTag tag = forumTagMapper.selectOne(new QueryWrapper<ForumTag>().eq("name", name));
            if (tag == null) {
                tag = new ForumTag();
                tag.setName(name);
                tag.setStatus(1);
                forumTagMapper.insert(tag);
            } else if (defaultInt(tag.getStatus()) == 0) {
                tag.setStatus(1);
                forumTagMapper.updateById(tag);
            }
            ForumPostTag relation = new ForumPostTag();
            relation.setPostId(postId);
            relation.setTagId(tag.getId());
            forumPostTagMapper.insert(relation);
        }
    }

    private void syncAttachments(Integer postId, List<AttachmentRequest> attachments) {
        forumAttachmentMapper.delete(
                new QueryWrapper<ForumAttachment>().eq("post_id", postId));
        if (attachments == null) {
            return;
        }
        for (AttachmentRequest request : attachments.stream().limit(10).collect(Collectors.toList())) {
            if (clean(request.getFileUrl()) == null) {
                continue;
            }
            ForumAttachment attachment = new ForumAttachment();
            attachment.setPostId(postId);
            attachment.setFileName(clean(request.getFileName()) == null ? "附件" : clean(request.getFileName()));
            attachment.setFileUrl(clean(request.getFileUrl()));
            attachment.setFileType(clean(request.getFileType()));
            attachment.setFileSize(request.getFileSize() == null ? 0L : request.getFileSize());
            forumAttachmentMapper.insert(attachment);
        }
    }

    private List<String> tagNames(Integer postId) {
        List<ForumPostTag> relations = forumPostTagMapper.selectList(
                new QueryWrapper<ForumPostTag>().eq("post_id", postId));
        if (relations.isEmpty()) {
            return new ArrayList<>();
        }
        List<Integer> tagIds = relations.stream()
                .map(ForumPostTag::getTagId)
                .collect(Collectors.toList());
        return forumTagMapper.selectBatchIds(tagIds).stream()
                .filter(tag -> defaultInt(tag.getStatus()) == 1)
                .map(ForumTag::getName)
                .collect(Collectors.toList());
    }

    private Member resolveMember(String username) {
        String cleanUsername = clean(username);
        if (cleanUsername == null) {
            return null;
        }
        return memberMapper.selectOne(
                new QueryWrapper<Member>().eq("username", cleanUsername).last("LIMIT 1"));
    }

    private void saveAudit(Integer postId, String action, String reason) {
        ForumAuditLog log = new ForumAuditLog();
        log.setPostId(postId);
        log.setAction(action);
        log.setReason(clean(reason));
        log.setOperatorName("admin");
        forumAuditLogMapper.insert(log);
    }

    private void clearRejectReason(Integer postId) {
        forumPostMapper.update(null,
                new UpdateWrapper<ForumPost>()
                        .eq("id", postId)
                        .set("reject_reason", null));
    }

    private long countStatus(String status) {
        return forumPostMapper.selectCount(new QueryWrapper<ForumPost>()
                .eq("deleted", 0)
                .eq("status", status));
    }

    private long sumField(String field) {
        List<ForumPost> posts = forumPostMapper.selectList(
                new QueryWrapper<ForumPost>().eq("deleted", 0));
        if ("view_count".equals(field)) {
            return posts.stream().mapToLong(post -> defaultInt(post.getViewCount())).sum();
        }
        return 0;
    }

    private Map<String, Object> pageData(List<Map<String, Object>> rows,
                                         PageInfo<Map<String, Object>> pageInfo) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", rows);
        data.put("total", pageInfo.getTotal());
        data.put("pageNum", pageInfo.getPageNum());
        data.put("pageSize", pageInfo.getPageSize());
        data.put("pages", pageInfo.getPages());
        return data;
    }

    private void normalizePostRows(List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            Object tagNames = row.get("tagNames");
            if (tagNames == null || clean(String.valueOf(tagNames)) == null) {
                row.put("tags", new ArrayList<>());
            } else {
                row.put("tags", Arrays.asList(String.valueOf(tagNames).split(",")));
            }
        }
    }

    private String buildSummary(String summary, String content) {
        String value = clean(summary);
        if (value != null) {
            return value.length() > 500 ? value.substring(0, 500) : value;
        }
        if (content == null) {
            return "";
        }
        String plain = content.replaceAll("<[^>]+>", " ")
                .replace("&nbsp;", " ")
                .replaceAll("\\s+", " ")
                .trim();
        return plain.length() > 180 ? plain.substring(0, 180) + "…" : plain;
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Integer number(Object value) {
        return value == null ? null : ((Number) value).intValue();
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private int safePage(Integer value) {
        return value == null || value < 1 ? 1 : value;
    }

    private int safePageSize(Integer value) {
        if (value == null || value < 1) {
            return 10;
        }
        return Math.min(value, 50);
    }

    private Result fail(String message) {
        return Result.error("400", message);
    }

    @Data
    public static class ForumPostRequest {
        private Integer id;
        private String username;
        private Integer categoryId;
        private String title;
        private String summary;
        private String content;
        private String coverUrl;
        private String visibility;
        private Boolean submit = false;
        private List<String> tags;
        private List<AttachmentRequest> attachments;
    }

    @Data
    public static class AttachmentRequest {
        private String fileName;
        private String fileUrl;
        private String fileType;
        private Long fileSize;
    }

    @Data
    public static class ReviewRequest {
        private String action;
        private String reason;
    }

    @Data
    public static class FlagRequest {
        private String type;
        private Boolean value;
    }

    @Data
    public static class CategoryRequest {
        private Integer id;
        private String name;
        private String description;
        private Integer sortOrder;
        private Integer status;
    }
}
