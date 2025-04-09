package com.springboot.logindemo.dao;

import com.springboot.logindemo.domain.Enterprise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author 企业法人认证信息DAO
 */
@Repository
public interface EnterpriseDao extends JpaRepository<Enterprise, Long> {

    /**
     * 根据认证ID查找企业法人认证信息
     * 
     * @param authId 认证ID
     * @return 企业法人认证信息
     */
    Enterprise findByAuthId(Long authId);
}