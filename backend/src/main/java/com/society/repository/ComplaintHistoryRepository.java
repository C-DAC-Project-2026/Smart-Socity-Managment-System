package com.society.repository;
import com.society.entity.ComplaintHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ComplaintHistoryRepository extends JpaRepository<ComplaintHistory, Long> {
    List<ComplaintHistory> findByComplaint_ComplaintIdOrderByUpdatedAtAsc(Long complaintId);
}
