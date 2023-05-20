package com.example.project_ee297.Dao;

import com.example.project_ee297.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
     Optional<User> findByUsername(String username) ;

}
