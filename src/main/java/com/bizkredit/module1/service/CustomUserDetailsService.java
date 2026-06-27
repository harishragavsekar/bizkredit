package com.bizkredit.module1.service;

import com.bizkredit.module1.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Separate UserDetailsService class
// SecurityConfig -> UserDetailsService -> UserRepository
@Service
@RequiredArgsConstructor  // Generates constructor for userRepository
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; // Inject repository

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // Fetch user or throw exception if not found
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        return org.springframework.security.core.userdetails.User
                .builder() // Spring Security's built-in builder
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }
}