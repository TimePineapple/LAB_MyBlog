-- 手动采集真实 SQL 结果；这里只读查询，不代表已经验收。
USE myblog;
SHOW CREATE TABLE message;
SHOW CREATE TABLE article;
SELECT COUNT(*) AS message_count FROM message;
SELECT COUNT(*) AS hello_count FROM message WHERE content LIKE '%hello%';
SELECT id, content, created_at FROM message ORDER BY created_at DESC, id DESC LIMIT 5;
SELECT id, title, content, created_at FROM article ORDER BY created_at DESC, id DESC LIMIT 5;

-- Week06 数据库直插检查：人工单独执行下一行，再调用 GET /messages?keyword=direct-db-insert。
-- INSERT INTO message (content) VALUES ('direct-db-insert');
