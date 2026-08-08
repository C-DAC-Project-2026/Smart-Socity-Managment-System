package com.society.repository;

import com.society.entity.Society;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SocietyRepository extends JpaRepository<Society, Long> {
    boolean existsBySocietyCode(String societyCode);
    boolean existsByContactEmail(String contactEmail);
    Optional<Society> findBySocietyCode(String societyCode);
    List<Society> findByStatus(Society.Status status);
}
