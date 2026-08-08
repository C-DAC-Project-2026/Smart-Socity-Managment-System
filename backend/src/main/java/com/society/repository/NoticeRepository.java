package com.society.repository;
import com.society.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findAllBySociety_SocietyIdOrderByCreatedAtDesc(Long societyId, Pageable pageable);
    Optional<Notice> findByNoticeIdAndSociety_SocietyId(Long noticeId, Long societyId);
}
