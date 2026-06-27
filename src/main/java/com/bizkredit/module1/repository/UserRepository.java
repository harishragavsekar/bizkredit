
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

    // Finds a user using email address
    Optional<User> findByEmail(String email);

    // Checks whether an email already exists
    boolean existsByEmail(String email);

    // Finds users based on their role
    List<User> findByRole(Role role);

    // Finds users based on account status
    List<User> findByStatus(String status);

    // Finds users assigned to a specific branch
    List<User> findByBranchId(String branchId);
}
