# Lab9102

[![CI](https://github.com/Xenos-hxc/Lab9102/actions/workflows/ci.yml/badge.svg)](https://github.com/Xenos-hxc/Lab9102/actions/workflows/ci.yml)

面向实验室内容展示与日常维护的前后端分离网站，包含公开门户、研究人员个人页面和后台管理。

## 功能

- 展示实验室介绍、研究方向、人员、新闻、讲座、论文和科研项目。
- 维护设备分类、成员与导师关系、个人主页及后台菜单权限。
- 研学社区支持分类、文章编辑、审核、评论、点赞与收藏。
- 上传图片和论文附件，支持 BibTeX 引用内容与页面缩放规则。

## 技术与目录

| 目录 | 内容 |
| --- | --- |
| `front/` | Vue 3、TypeScript、Vite、Element Plus、Pinia、WangEditor |
| `springboot/` | Java 8、Spring Boot 2.5、MyBatis Plus、MySQL |
| `sql/schema.sql` | 空数据库的完整表结构 |
| `sql/demo.sql` | 虚构的本地演示账号和基础信息 |
| `scripts/smoke.py` | 后端启动后的接口检查 |

## 本地运行

需要 JDK 8、Maven 3.8+、Node.js 22.12+、MySQL 8。下面的数据库密码和管理员密码仅供本地演示使用。

1. 在仓库根目录执行 `docker compose up -d` 启动演示数据库；首次启动会自动导入表结构和演示数据。已有 MySQL 时，创建 `lab9102_demo` 数据库，依次导入 `sql/schema.sql` 和 `sql/demo.sql`，并通过环境变量提供数据库账号。
2. 启动后端：

```sh
cd springboot
mvn clean package
java -jar target/springboot-0.0.1-SNAPSHOT.jar
```

3. 在另一个终端启动前端：

```sh
cd front
npm ci
npm run dev
```

访问 `http://localhost:9000`，演示管理员为 `admin / local-demo-change-me`。后端默认端口为 `9001`。

| 环境变量 | 用途 | 默认值 |
| --- | --- | --- |
| `DB_URL` | JDBC 连接地址 | 本地 `lab9102_demo` 数据库 |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库账号 | `demo` / `local-demo-change-me` |
| `UPLOAD_DIR` | 运行时上传目录 | `../upload/files`（从后端目录启动） |
| `PUBLIC_BASE_URL` | 对外文件访问地址 | 自动推导 |
| `VITE_API_BASE_URL` | 开发代理目标或生产 API 地址 | 开发时 `http://localhost:9001` |

生产环境可在构建前设置 `VITE_API_BASE_URL`，也可以用反向代理把 `/api` 转发到后端并去掉前缀。`npm run build` 会执行类型检查并生成 `front/dist/`。

## 验证与数据

`mvn clean package` 检查后端构建，`npm run build` 检查前端类型与生产构建。后端和数据库启动后，在仓库根目录执行 `python scripts/smoke.py` 可检查基础信息接口及演示登录。GitHub Actions 会从空 MySQL 数据库执行同样的检查。

仓库中的联系信息和演示记录均为虚构。照片、业务附件、真实数据库记录、云服务凭据、构建产物和开发工具文件不在仓库中。当前登录和权限机制适用于功能演示；正式部署前需补齐服务端身份认证和密码散列，并更换演示账号。
