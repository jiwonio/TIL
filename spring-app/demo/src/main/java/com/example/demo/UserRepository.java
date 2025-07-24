package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // 기본적인 save(), findAll(), findById(), deleteById() 등이 자동으로 제공됩니다.
}