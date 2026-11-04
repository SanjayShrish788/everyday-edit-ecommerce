package com.sanjay.Spring_ECOM.repo;

import com.sanjay.Spring_ECOM.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User,Integer> {

    User findByUsername(String username);

}
