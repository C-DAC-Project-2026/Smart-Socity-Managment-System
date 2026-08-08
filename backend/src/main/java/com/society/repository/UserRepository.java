package com.society.repository;
import com.society.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface UserRepository extends JpaRepository<User, Long> {
    // Email is unique globally (not per-society) so login can resolve a
    // user without first knowing which society they belong to.
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole_RoleNameAndSociety_SocietyId(String roleName, Long societyId);
    List<User> findAllBySociety_SocietyId(Long societyId);
    // SUPER_ADMIN accounts have no society, so they can only be looked up by role alone.
    List<User> findByRole_RoleName(String roleName);
}
