package com.society.repository;
import com.society.entity.Complaint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    Optional<Complaint> findByComplaintIdAndSociety_SocietyId(Long complaintId, Long societyId);
    Page<Complaint> findByResident_ResidentIdAndSociety_SocietyId(Long residentId, Long societyId, Pageable pageable);
    List<Complaint> findByResident_ResidentIdAndSociety_SocietyId(Long residentId, Long societyId);
    Page<Complaint> findByAssignedStaff_StaffIdAndSociety_SocietyId(Long staffId, Long societyId, Pageable pageable);
    Page<Complaint> findByStatusAndSociety_SocietyId(Complaint.Status status, Long societyId, Pageable pageable);
    Page<Complaint> findBySociety_SocietyId(Long societyId, Pageable pageable);
    long countBySociety_SocietyId(Long societyId);
    long countByStatusAndSociety_SocietyId(Complaint.Status status, Long societyId);
}
