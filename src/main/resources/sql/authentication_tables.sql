-- 认证信息表
CREATE TABLE authentication (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  auth_type VARCHAR(20) NOT NULL COMMENT '认证类型：natural(自然人), individual(个体工商户), enterprise(企业法人)',
  auth_method VARCHAR(20) NOT NULL COMMENT '认证方式：biometric(生物特征), national(国家平台), document(证件对比)',
  auth_status INT DEFAULT 0 COMMENT '认证状态：0-待审核，1-已通过，2-未通过',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX (user_id)
) COMMENT '用户认证信息表';

-- 自然人认证信息表
CREATE TABLE natural_person (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  auth_id BIGINT NOT NULL COMMENT '认证ID',
  name VARCHAR(50) NOT NULL COMMENT '姓名',
  id_card VARCHAR(20) NOT NULL COMMENT '身份证号码',
  phone VARCHAR(20) NOT NULL COMMENT '手机号码',
  bank_card VARCHAR(30) NOT NULL COMMENT '银行卡号',
  promise_file VARCHAR(255) COMMENT '自然人承诺书文件路径',
  delegate_file VARCHAR(255) COMMENT '税费代办委托协议文件路径',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX (auth_id)
) COMMENT '自然人认证信息表';

-- 个体工商户认证信息表
CREATE TABLE individual_business (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  auth_id BIGINT NOT NULL COMMENT '认证ID',
  name VARCHAR(100) NOT NULL COMMENT '注册名称',
  tax_no VARCHAR(50) NOT NULL COMMENT '税号',
  address VARCHAR(255) NOT NULL COMMENT '注册地址',
  license_file VARCHAR(255) COMMENT '营业执照文件路径',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX (auth_id)
) COMMENT '个体工商户认证信息表';

-- 企业法人认证信息表
CREATE TABLE enterprise (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  auth_id BIGINT NOT NULL COMMENT '认证ID',
  name VARCHAR(100) NOT NULL COMMENT '注册名称',
  tax_no VARCHAR(50) NOT NULL COMMENT '税号',
  address VARCHAR(255) NOT NULL COMMENT '注册地址',
  license_file VARCHAR(255) COMMENT '营业执照文件路径',
  create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX (auth_id)
) COMMENT '企业法人认证信息表'; 