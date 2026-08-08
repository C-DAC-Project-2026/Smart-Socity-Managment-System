package com.society.repository;
import com.society.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface StaffRepository extends JpaRepository<Staff, Long> {
    List<Staff> findAllBySociety_SocietyId(Long societyId);
    long countBySociety_SocietyId(Long societyId);
    Optional<Staff> findByStaffIdAndSociety_SocietyId(Long staffId, Long societyId);
    Optional<Staff> findByUser_UserIdAndSociety_SocietyId(Long userId, Long societyId);
    List<Staff> findByDepartmentIgnoreCaseAndSociety_SocietyId(String department, Long societyId);

    // Self-registered staff awaiting Society Admin approval (user.active = false).
    List<Staff> findAllBySociety_SocietyIdAndUser_ActiveFalse(Long societyId);
}
