package com.society.repository;
import com.society.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Page<Complaint> findByResident_ResidentId(Long residentId, Pageable pageable);
    Page<Complaint> findByAssignedStaff_StaffId(Long staffId, Pageable pageable);
    Page<Complaint> findByStatus(Complaint.Status status, Pageable pageable);
    long countByStatus(Complaint.Status status);
    List<Complaint> findByResident_ResidentId(Long residentId);
}
