package com.bizkredit.service;

import com.bizkredit.entity.User;
import com.bizkredit.exception.ResourceNotFoundException;
import com.bizkredit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserScopeService {

    private final UserRepository userRepository;

    // Principal name = email (CustomUserDetailsService sets email as principal)
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Transactional(readOnly = true)
    public String getCurrentUserBranchId() {
        return getCurrentUser().getBranchId();
    }

    @Transactional(readOnly = true)
    public String getCurrentUserRegion() {
        return getCurrentUser().getRegion();
    }

    // null branchId = ADMIN, sees all. Otherwise scoped to own branch.
    @Transactional(readOnly = true)
    public boolean isInSameBranch(User targetUser) {
        String myBranch = getCurrentUserBranchId();
        if (myBranch == null) return true;
        return myBranch.equals(targetUser.getBranchId());
    }

    @Transactional(readOnly = true)
    public boolean isInSameRegion(User targetUser) {
        String myRegion = getCurrentUserRegion();
        if (myRegion == null) return true;
        return myRegion.equalsIgnoreCase(targetUser.getRegion());
    }

    @Transactional
    public User assignScope(Long userId, String branchId, String region) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setBranchId(branchId);
        user.setRegion(region);
        log.info("RBAC scope assigned: user={} branchId={} region={}", user.getEmail(), branchId, region);
        return userRepository.save(user);
    }
}
