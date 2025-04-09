package com.springboot.logindemo.dao;

import com.springboot.logindemo.domain.Authentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author 认证信息DAO
 */
@Repository
public interface AuthenticationDao extends JpaRepository<Authentication, Long> {

    /**
     * 根据用户ID查找认证信息
     * 
     * @param userId 用户ID
     * @return 认证信息列表
     */
    List<Authentication> findByUserId(Long userId);

    /**
     * 根据用户ID和认证状态查找认证信息
     * 
     * @param userId     用户ID
     * @param authStatus 认证状态
     * @return 认证信息列表
     */
    List<Authentication> findByUserIdAndAuthStatus(Long userId, Integer authStatus);
}