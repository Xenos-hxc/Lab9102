-- Empty demo schema. Original rows and database credentials are excluded.
SET NAMES utf8mb4;

CREATE TABLE `carousel`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '走马灯标题',
  `pictureUrl` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '走马灯图片路径',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;


CREATE TABLE `direction`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '研究方向id',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '研究方向名称',
  `pictureUrl` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '研究方向图片路径',
  `introduction` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '研究方向简介',
  `level` int(0) NULL DEFAULT NULL COMMENT '层级：1：父级研究方向；2：子级研究方向',
  `parentId` int(0) NULL DEFAULT NULL COMMENT '若level=2,则有parentid',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;


CREATE TABLE `gallery`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '团队建设id',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '团建标题',
  `summary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动摘要',
  `content` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动内容，内容是富文本',
  `pictureUrl` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动图片路径',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动类型',
  `time` date NULL DEFAULT NULL COMMENT '活动时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;


CREATE TABLE `labinfo`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `lab_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '实验室名称',
  `address_province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '省份',
  `address_city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '城市',
  `address_district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '区县',
  `address_street` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '街道地址',
  `address_detail` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `university` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属大学',
  `college` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '所属学院',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `contact_person1_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人1姓名',
  `contact_person1_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人1邮箱',
  `contact_person2_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人2姓名',
  `contact_person2_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人2邮箱',
  `contact_person3_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人3姓名',
  `contact_person3_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人3邮箱',
  `contact_person4_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人4姓名',
  `contact_person4_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人4邮箱',
  `contact_person5_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人5姓名（预留）',
  `contact_person5_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系人5邮箱（预留）',
  `workday_hours` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '工作日工作时间',
  `weekend_hours` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '周末工作时间',
  `holiday_note` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '节假日说明',
  `map_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地图链接',
  `created_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updated_at` timestamp(0) NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `introduction` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '实验室介绍',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_lab_name`(`lab_name`) USING BTREE,
  INDEX `idx_university`(`university`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '实验室信息表' ROW_FORMAT = Dynamic;


CREATE TABLE `lecture`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '讲座id',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '讲座标题',
  `speaker` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '主讲人',
  `speakerFrom` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '主讲人来自哪儿：如西南交通大学信息学院',
  `time` datetime(0) NULL DEFAULT NULL COMMENT '讲座时间',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '讲座地点',
  `host` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '主持人',
  `lectureIntroduction` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '讲座简介',
  `speakerIntroduction` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '主讲人简介',
  `pictureUrl` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '图片路径',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;


CREATE TABLE `member`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名字',
  `identity` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份：负责人、导师、博士研究生、硕士研究生、毕业生',
  `introduction` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人介绍',
  `entryTime` year NULL DEFAULT NULL COMMENT '入学/职年份',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `pictureUrl` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '个人图片路径',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '账号',
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  PRIMARY KEY (`id`, `username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;


CREATE TABLE `memberdirection`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '人员研究方向关联表id',
  `memberId` int(0) NULL DEFAULT NULL COMMENT '人员id',
  `directionId` int(0) NULL DEFAULT NULL COMMENT '研究方向id',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;


CREATE TABLE `news`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '新闻id',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '新闻标题',
  `summary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '新闻摘要',
  `content` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '新闻内容，内容是富文本',
  `pictureUrl` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '新闻图片路径',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '新闻类型',
  `time` date NULL DEFAULT NULL COMMENT '新闻时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;


CREATE TABLE `paper`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '论文id',
  `title` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '论文标题',
  `authors` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '作者',
  `time` year NULL DEFAULT NULL COMMENT '发表年份',
  `place` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发表的期刊或者会议',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;


CREATE TABLE `project`  (
  `id` int(0) NOT NULL AUTO_INCREMENT COMMENT '科研项目id',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '科研项目标题',
  `startTime` date NULL DEFAULT NULL COMMENT '开始时间',
  `endTime` date NULL DEFAULT NULL COMMENT '结题时间：如果为空则代表还在进行中',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '项目类型：横向项目、纵向项目',
  `company` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '合作方',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;



CREATE TABLE IF NOT EXISTS `forum_category` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `description` varchar(255) DEFAULT NULL,
  `sort_order` int DEFAULT 0,
  `status` tinyint DEFAULT 1,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_forum_category_name` (`name`),
  KEY `idx_forum_category_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='研学社区分类';

CREATE TABLE IF NOT EXISTS `forum_post` (
  `id` int NOT NULL AUTO_INCREMENT,
  `author_id` int NOT NULL,
  `category_id` int DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `summary` varchar(500) DEFAULT NULL,
  `content` longtext,
  `cover_url` varchar(1000) DEFAULT NULL,
  `visibility` varchar(20) DEFAULT 'MEMBER',
  `status` varchar(20) DEFAULT 'DRAFT',
  `reject_reason` varchar(500) DEFAULT NULL,
  `view_count` int DEFAULT 0,
  `like_count` int DEFAULT 0,
  `favorite_count` int DEFAULT 0,
  `is_top` tinyint DEFAULT 0,
  `is_featured` tinyint DEFAULT 0,
  `deleted` tinyint DEFAULT 0,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `submitted_at` datetime DEFAULT NULL,
  `published_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_forum_post_status_publish` (`status`, `published_at`),
  KEY `idx_forum_post_author` (`author_id`, `status`),
  KEY `idx_forum_post_category` (`category_id`, `status`),
  KEY `idx_forum_post_visibility` (`visibility`, `status`),
  KEY `idx_forum_post_featured` (`is_featured`, `is_top`, `published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='研学社区文章';

CREATE TABLE IF NOT EXISTS `forum_tag` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `status` tinyint DEFAULT 1,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_forum_tag_name` (`name`),
  KEY `idx_forum_tag_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='研学社区标签';

CREATE TABLE IF NOT EXISTS `forum_post_tag` (
  `id` int NOT NULL AUTO_INCREMENT,
  `post_id` int NOT NULL,
  `tag_id` int NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_forum_post_tag` (`post_id`, `tag_id`),
  KEY `idx_forum_post_tag_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章标签关系';

CREATE TABLE IF NOT EXISTS `forum_like` (
  `id` int NOT NULL AUTO_INCREMENT,
  `post_id` int NOT NULL,
  `member_id` int NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_forum_like_member_post` (`post_id`, `member_id`),
  KEY `idx_forum_like_member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章点赞';

CREATE TABLE IF NOT EXISTS `forum_favorite` (
  `id` int NOT NULL AUTO_INCREMENT,
  `post_id` int NOT NULL,
  `member_id` int NOT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_forum_favorite_member_post` (`post_id`, `member_id`),
  KEY `idx_forum_favorite_member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章收藏';

CREATE TABLE IF NOT EXISTS `forum_attachment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `post_id` int NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_url` varchar(1000) NOT NULL,
  `file_type` varchar(100) DEFAULT NULL,
  `file_size` bigint DEFAULT 0,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_forum_attachment_post` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章附件';

CREATE TABLE IF NOT EXISTS `forum_audit_log` (
  `id` int NOT NULL AUTO_INCREMENT,
  `post_id` int NOT NULL,
  `action` varchar(30) NOT NULL,
  `reason` varchar(500) DEFAULT NULL,
  `operator_name` varchar(50) DEFAULT 'admin',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_forum_audit_post` (`post_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文章审核记录';

ALTER TABLE `member` ADD COLUMN `workplace` varchar(500) DEFAULT NULL COMMENT '毕业去向';

ALTER TABLE `paper` ADD COLUMN `url` varchar(1000) DEFAULT NULL COMMENT '论文在线链接';

ALTER TABLE `paper` ADD COLUMN `pdfurl` varchar(1000) DEFAULT NULL COMMENT '论文PDF地址';

ALTER TABLE `paper` ADD COLUMN `level` varchar(1000) DEFAULT NULL COMMENT '论文级别';

CREATE TABLE IF NOT EXISTS `member_mentor` (
  `id` int NOT NULL AUTO_INCREMENT,
  `mentorId` int NOT NULL COMMENT '导师成员ID',
  `studentId` int NOT NULL COMMENT '学生成员ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_member_mentor` (`mentorId`, `studentId`),
  KEY `idx_member_mentor_student` (`studentId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='导师学生多对多关系';

CREATE TABLE IF NOT EXISTS `equipment_category` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `description` varchar(255) DEFAULT NULL COMMENT '分类说明',
  `sortOrder` int NOT NULL DEFAULT 0 COMMENT '显示顺序',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_equipment_category_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备分类';

CREATE TABLE IF NOT EXISTS `equipment` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL COMMENT '设备名称',
  `categoryId` int DEFAULT NULL COMMENT '设备分类ID',
  `purchaseDate` date NOT NULL COMMENT '引入时间',
  `introduction` varchar(2000) DEFAULT NULL COMMENT '设备简介',
  `pictureUrl` varchar(500) DEFAULT NULL COMMENT '设备图片',
  PRIMARY KEY (`id`),
  KEY `idx_equipment_purchase_date` (`purchaseDate`),
  KEY `idx_equipment_category` (`categoryId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实验室设备';

CREATE TABLE IF NOT EXISTS `resolution_setting` (
  `id` int NOT NULL AUTO_INCREMENT,
  `width` int NOT NULL,
  `height` int NOT NULL,
  `scale` decimal(4,2) NOT NULL DEFAULT 1.00,
  `description` varchar(100) DEFAULT NULL,
  `enabled` tinyint(1) NOT NULL DEFAULT 1,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_resolution` (`width`, `height`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='屏幕分辨率缩放规则';

ALTER TABLE `member` ADD COLUMN `profileUrl` varchar(500) DEFAULT NULL COMMENT '个人主页链接' AFTER `workplace`;

ALTER TABLE `member` ADD COLUMN `profileLabel` varchar(80) DEFAULT NULL COMMENT '个人主页名称' AFTER `profileUrl`;

ALTER TABLE `member` ADD COLUMN `isAdmin` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否管理员' AFTER `profileLabel`;

ALTER TABLE `member` ADD COLUMN `adminPermissions` varchar(500) DEFAULT NULL COMMENT '管理员菜单权限' AFTER `isAdmin`;

ALTER TABLE `news` ADD COLUMN `sourceGalleryId` int DEFAULT NULL COMMENT '迁移来源团队建设ID', ADD UNIQUE KEY `uk_news_source_gallery` (`sourceGalleryId`);

ALTER TABLE `paper` ADD COLUMN `bib` MEDIUMTEXT DEFAULT NULL COMMENT 'BibTeX引用内容' AFTER `level`;

ALTER TABLE `lecture` MODIFY COLUMN `lectureIntroduction` LONGTEXT NULL, MODIFY COLUMN `speakerIntroduction` LONGTEXT NULL;
