# MyBlog 课程整合项目

这是在原 `.MyBlog` 上连续升级的第 1～8 周最终后端：个人资料、MySQL 留言、关键词搜索和分页，以及文章发布、修改、删除。没有引入 Vue、登录或 AI 服务。课程原文和根目录确认表保持原样。

本次只完成文件实现和静态检查，没有安装依赖、编译、运行测试、启动应用或连接数据库。以下结果均需手动验证，不能据此将课程确认表全部勾选。

## 环境与依赖

- Java 17+、Spring Boot 3.5.6、MyBatis-Plus 3.5.17（Boot 3 starter 与同版本 jsqlparser）、MySQL 驱动。
- Maven 3.6.3+ 或 IDEA 自带 Maven；未附带 Maven Wrapper。
- MySQL 8+，先手动创建数据库和表。版本固定沿用课程方案，不代表已验证依赖下载或当前最新版本。

## 人工配置和启动

1. 在自己的 MySQL 客户端检查当前服务器及库，手动执行 `sql/schema.sql`。脚本只建不存在的库表，不修改已有表、不清空数据；旧表结构若不一致，应先备份并人工核对，不要直接覆盖。
2. 在 IDEA 的 Run Configuration 环境变量中填写：

| 环境变量 | 用途 |
| --- | --- |
| `MYBLOG_DB_URL` | JDBC 地址；省略时使用下方本机默认地址 |
| `MYBLOG_DB_USERNAME` | 自己的数据库账号，必填 |
| `MYBLOG_DB_PASSWORD` | 自己的数据库密码，必填；不写入源码或提交 |

默认 JDBC 地址：`jdbc:mysql://localhost:3306/myblog?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai`。若使用其他端口或库名，用环境变量覆盖。未配置凭据时不会自动回退内存仓库。

3. IDEA 打开 `pom.xml`，手动导入 Maven，使用 JDK 17+ 运行 `com.myblog.MyBlogApplication`。
4. 浏览器查看 `http://localhost:8080/hello`、`/profile` 和 `/me`。根路径没有页面，404 是预期结果。

已安装命令行 Maven、并在当前终端配置好环境变量时，也可以手动执行：

```powershell
Set-Location -LiteralPath 'C:\develop\WEB_Programming\.MyBlog'
mvn spring-boot:run
```

该命令会下载依赖并编译。本次没有执行。程序不会自动运行建表或播种脚本；端口默认 8080，可在 `application.properties` 修改。

## 最终接口与兼容变化

| 方法 | 路径 | 成功状态与响应 |
| --- | --- | --- |
| GET | `/hello` | 200，原有纯文本 |
| GET | `/profile` | 200，五字段个人资料，原有结构 |
| GET | `/me` | 200，个人介绍，原有结构 |
| POST | `/messages` | 201，`{id,content,createdAt}` |
| GET | `/messages?keyword=&page=1&size=5` | 200，`{items,total,page,size}` |
| GET | `/messages/{id}` | 200，单条留言对象 |
| POST | `/articles` | 200，`{code:0,message:"ok",data:文章}` |
| GET | `/articles?keyword=&page=1&size=5` | 200，`{code:0,message:"ok",data:分页对象}` |
| GET | `/articles/{id}` | 200，Result 内单篇文章 |
| PUT | `/articles/{id}` | 200，Result 内修改后的文章 |
| DELETE | `/articles/{id}` | 200，`{code:0,message:"ok",data:null}` |

从第 4 周版本升级时，请同步调整客户端：留言列表不再是裸数组；详情路径的数值是数据库自增 ID，不是零基列表下标；留言 POST 改为 201，重启后数据保留。文章功能不再额外包装旧的身份/留言成功体，便于第 7→8 周连续性验收。全局错误响应统一为 `{code,message,data:null}`，400/404/405/415/500 使用对应的真实 HTTP 状态。

分页默认 page=1、size=5；page 必须大于 0，size 为 1～100；超大偏移量、非整数参数返回 400。空白关键词视为无过滤，其他关键词按原输入匹配；无匹配、超出末页返回空 items 和正确 total。查询在 MySQL 执行，按创建时间降序、ID 降序稳定排列；不是先把全表取到 Java。关键词中的 `%`、`_` 当普通字符处理，大小写/重音规则服从数据库 collation，默认 `utf8mb4_unicode_ci` 通常不区分大小写。

留言 JSON：`{"content":"Hello, MyBlog"}`，内容必填，不能全空白，最多 1000 个 Java 字符单元，与校验规则一致。

文章发布 JSON：`{"title":"我的第一篇文章","content":"正文"}`。标题不能全空白，最多 200 个字符单元；正文不能全空白，UTF-8 编码最多 65535 字节（MySQL TEXT 容量）。

文章 PUT 按教程支持部分字段更新，例如 `{"title":"新标题"}`；未提供字段保持原值，显式 null、空白字段和空对象均拒绝。请求 DTO 不包含 ID 或创建时间，客户端传入这些额外字段不会覆盖系统值。删除为硬删除，只作用于指定的一篇文章。

## 分层和文件

留言链路：Controller → MessageService → MessageRepository → DbMessageRepository → MessageMapper → MySQL。

文章遵循课程示例：Controller → ArticleService → ArticleMapper → MySQL。两种资源共用分页配置和错误处理。MyBatis-Plus 数据库查询由留言的 DbRepository 和文章 Service 构造，Controller 不拼查询条件、不持有集合。教学内存仓库没有 Spring 组件注解，生产只注册数据库版仓库。

- `src/main/`：应用代码、配置。
- `src/test/`：独立测试、MVC 契约测试、显式启用的 MySQL 集成测试。
- `sql/`：人工建表、一次性可选播种和 SQL 检查脚本。
- `requests.http`：基础接口及错误请求。
- `requests-week07.http`：课程八项搜索分页矩阵。
- `requests-week08.http`：文章生命周期和操作前后连续性核对。
- [详细实验与验收步骤](docs/lab-verification.md)：历史实验、恢复方式、证据边界和测试命令。

## 个性化与待完成项

`ProfileController.createProfile()` 的姓名、简介、兴趣、愿景仍为占位值；请自行填写，并修改 `HelloController` 的问候使其含真实姓名。个人化前不能认定 Week01 和 Week03 全部达标。

代码和请求文件中的示例、断言都不是已执行证据。仍需本人手动构建和启动，完成真实 MySQL 直插、分页 SQL、重启存活、Recognition Test、破坏实验、AI 交流记录、截图和 GitHub 提交。旧周历史行为与最终版不同，按详细步骤在独立学习副本中补做，并如实说明日期；不伪造旧版基线。

## 手动测试

配置无需数据库的测试使用 MVC 切片或独立对象，不自动连接 MySQL：手动执行 `mvn test`。

真实数据库入口：先在独立测试库建表，设置 `MYBLOG_TEST_DB_URL`、`MYBLOG_TEST_DB_USERNAME`、`MYBLOG_TEST_DB_PASSWORD`，再手动执行 `mvn -Pmysql-it verify`。测试缺少配置时失败，不回退开发库；数据通过事务回滚，但自增 ID 的消耗不会回滚。跨进程重启仍需手动验证。

未做自动构建，依赖解析、编译、测试和运行结果均尚未验证。临时工作产物仅允许放在已忽略的 `.agent_tmp/`；不提交凭据、测试日志或临时报告。
