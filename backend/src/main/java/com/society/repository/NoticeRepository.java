package com.society.repository;
import com.society.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
public interface NoticeRepository extends JpaRepository<Notice, Long> {
    Page<Notice> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
