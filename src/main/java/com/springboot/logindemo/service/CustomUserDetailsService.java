/**
 * @author 潘楠
 * @cooperators 协作者
 * @date 2025-3-22
 * @description 角色权限管理
 */
package com.springboot.logindemo.service;

import com.springboot.logindemo.domain.User;
import com.springboot.logindemo.repository.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDao userRepository;

    @Autowired
    public CustomUserDetailsService(UserDao userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUname(username);

        List<GrantedAuthority> authorities = new ArrayList<>();

        // 添加角色
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // 添加该角色拥有的所有权限
        authorities.addAll(
                user.getRole().getPermissionNames().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList()));

        return new org.springframework.security.core.userdetails.User(
                user.getUname(),
                user.getPassword(),
                user.getStatus() == 1, // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities);
    }
}