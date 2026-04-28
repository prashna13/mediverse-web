package com.mediverse.repository;

import com.mediverse.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {
    
    List<Branch> findByIsActiveTrue();
    
    @Query("SELECT b FROM Branch b WHERE b.isActive = true ORDER BY b.name")
    List<Branch> findActiveBranchesOrderByName();
    
    boolean existsByName(String name);
}
