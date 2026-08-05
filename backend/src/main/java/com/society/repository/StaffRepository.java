package com.society.repository;
import com.society.entity.Staff;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface StaffRepository extends JpaRepository<Staff, Long> {
    Optional<Staff> findByUser_UserId(Long userId);
    List<Staff> findByDepartmentIgnoreCase(String department);
}
