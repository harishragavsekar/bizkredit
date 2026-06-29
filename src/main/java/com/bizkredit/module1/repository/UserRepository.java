
package com.bizkredit.module1.repository;

import com.bizkredit.module1.entity.User;
import com.bizkredit.common.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Repository layer for User database CRUD operations
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByStatus(String status);

    List<User> findByBranchId(String branchId);
}
