package com.society.repository;
import com.society.entity.Resident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface ResidentRepository extends JpaRepository<Resident, Long> {
    // All lookups are scoped by societyId so one society's residents are
    // never visible to, or resolvable by, another society's users.
    List<Resident> findAllBySociety_SocietyId(Long societyId);
    long countBySociety_SocietyId(Long societyId);
    Optional<Resident> findByResidentIdAndSociety_SocietyId(Long residentId, Long societyId);
    Optional<Resident> findByUser_UserIdAndSociety_SocietyId(Long userId, Long societyId);
    boolean existsByFlatNoAndSociety_SocietyId(String flatNo, Long societyId);

    // Self-registered residents awaiting Society Admin approval (user.active = false).
    List<Resident> findAllBySociety_SocietyIdAndUser_ActiveFalse(Long societyId);

    @Query("SELECT r FROM Resident r JOIN r.user u WHERE r.society.societyId = :societyId " +
           "AND (LOWER(u.name) LIKE LOWER(CONCAT('%',:q,'%')) OR LOWER(r.flatNo) LIKE LOWER(CONCAT('%',:q,'%')))")
    List<Resident> searchByNameOrFlatNo(@Param("societyId") Long societyId, @Param("q") String q);
}
