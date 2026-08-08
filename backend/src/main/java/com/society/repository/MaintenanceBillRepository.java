package com.society.repository;
import com.society.entity.MaintenanceBill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface MaintenanceBillRepository extends JpaRepository<MaintenanceBill, Long> {
    Optional<MaintenanceBill> findByBillIdAndSociety_SocietyId(Long billId, Long societyId);
    Page<MaintenanceBill> findByResident_ResidentIdAndSociety_SocietyId(Long residentId, Long societyId, Pageable pageable);
    List<MaintenanceBill> findByResident_ResidentIdAndSociety_SocietyId(Long residentId, Long societyId);
    Optional<MaintenanceBill> findByResident_ResidentIdAndMonthAndYearAndSociety_SocietyId(
            Long residentId, int month, int year, Long societyId);
    long countByStatusAndSociety_SocietyId(MaintenanceBill.BillStatus status, Long societyId);
    Page<MaintenanceBill> findByStatusAndSociety_SocietyId(MaintenanceBill.BillStatus status, Long societyId, Pageable pageable);
    Page<MaintenanceBill> findBySociety_SocietyId(Long societyId, Pageable pageable);
    long countBySociety_SocietyId(Long societyId);
}
