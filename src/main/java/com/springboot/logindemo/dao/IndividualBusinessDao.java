package com.springboot.logindemo.dao;

import com.springboot.logindemo.domain.IndividualBusiness;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author 个体工商户认证信息DAO
 */
@Repository
public interface IndividualBusinessDao extends JpaRepository<IndividualBusiness, Long> {

    /**
     * 根据认证ID查找个体工商户认证信息
     * 
     * @param authId 认证ID
     * @return 个体工商户认证信息
     */
    IndividualBusiness findByAuthId(Long authId);
}