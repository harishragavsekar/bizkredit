package com.bizkredit.repository;

import com.bizkredit.entity.User;
import com.bizkredit.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByStatus(String status);

    List<User> findByBranchId(String branchId);
}
