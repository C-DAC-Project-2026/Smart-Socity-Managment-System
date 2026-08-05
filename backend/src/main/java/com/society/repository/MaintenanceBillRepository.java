package com.society.repository;
import com.society.entity.MaintenanceBill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface MaintenanceBillRepository extends JpaRepository<MaintenanceBill, Long> {
    Page<MaintenanceBill> findByResident_ResidentId(Long residentId, Pageable pageable);
    List<MaintenanceBill> findByResident_ResidentId(Long residentId);
    Optional<MaintenanceBill> findByResident_ResidentIdAndMonthAndYear(Long residentId, int month, int year);
    long countByStatus(MaintenanceBill.BillStatus status);
    Page<MaintenanceBill> findByStatus(MaintenanceBill.BillStatus status, Pageable pageable);
}
