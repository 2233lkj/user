package com.springboot.logindemo.dao;

import com.springboot.logindemo.domain.NaturalPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author 自然人认证信息DAO
 */
@Repository
public interface NaturalPersonDao extends JpaRepository<NaturalPerson, Long> {

    /**
     * 根据认证ID查找自然人认证信息
     * 
     * @param authId 认证ID
     * @return 自然人认证信息
     */
    NaturalPerson findByAuthId(Long authId);
}