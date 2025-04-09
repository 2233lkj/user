-- 修改user_roles表的create_time字段，添加默认值为当前时间戳
ALTER TABLE user_roles MODIFY create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP; 