ALTER TABLE user
ADD COLUMN login_permission INT DEFAULT 1,
ADD COLUMN admin_permission INT DEFAULT 0; 