package com.society.security;

import com.society.entity.User;
import com.society.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * @Transactional is required here, not optional. User.society is
     * FetchType.LAZY, and CustomUserDetails's constructor calls
     * society.getStatus() to determine isEnabled() — a real DB hit on the
     * lazy proxy, not just an ID read. This method runs on every single
     * authenticated request (JwtAuthenticationFilter calls it per-request,
     * not just at login), so it needs its own open Hibernate session every
     * time rather than depending on one already being open on the thread.
     * Without this, the lazy load throws LazyInitializationException
     * ("no Session"), which JwtAuthenticationFilter swallows and logs —
     * leaving the SecurityContext empty, so the request falls through as
     * unauthenticated and every authenticated endpoint returns 401. On the
     * frontend, ANY 401 clears the stored session and redirects to /login
     * (see api/axios.js), which is what produced "login succeeds, then the
     * user is logged out again within a second" for every society-scoped
     * account (Admin/Resident/Staff) on their very next API call.
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        return new CustomUserDetails(user);
    }
}
