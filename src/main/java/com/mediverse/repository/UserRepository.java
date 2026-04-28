package com.mediverse.repository;

import com.mediverse.entity.User;
import com.mediverse.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    List<User> findByRole(Role role);
    
    List<User> findByIsActiveTrue();
    
    List<User> findByRoleAndIsActiveTrue(Role role);
    
    boolean existsByEmail(String email);
    
    long countByRole(Role role);
}
