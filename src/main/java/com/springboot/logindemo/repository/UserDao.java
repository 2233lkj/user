package com.springboot.logindemo.repository;

import com.springboot.logindemo.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<User, Long> {
    User findByUname(String uname);

    User findByUnameAndPassword(String uname, String password);

    User findByPhonenum(String phonenum);

    User findByPhonenumAndPassword(String phonenum, String password);
}
