package com.society.repository;
import com.society.entity.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface ResidentRepository extends JpaRepository<Resident, Long> {
    Optional<Resident> findByUser_UserId(Long userId);
    Optional<Resident> findByFlatNo(String flatNo);
    boolean existsByFlatNo(String flatNo);
    @Query("SELECT r FROM Resident r JOIN r.user u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(r.flatNo) LIKE LOWER(CONCAT('%',:q,'%'))")
    List<Resident> searchByNameOrFlatNo(@Param("q") String q);
}
