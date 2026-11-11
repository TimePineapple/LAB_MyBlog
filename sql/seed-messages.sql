-- 可选、仅供人工验收：每次执行都会插入新的 10 条，不要重复播种。
-- 在 schema.sql 已创建的 myblog 数据库中手动执行。
USE myblog;
INSERT INTO message (content) VALUES
('hello world from alice'),
('hello again, dear blog'),
('hello is a warm word'),
('my blog journey begins'),
('blog writing tips'),
('today i learned spring boot'),
('mysql is my memory palace'),
('quiet night, quiet thoughts'),
('testing pagination window'),
('the last seed message');
