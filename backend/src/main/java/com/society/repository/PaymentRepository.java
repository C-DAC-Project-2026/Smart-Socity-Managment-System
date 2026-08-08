package com.society.repository;
import com.society.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentIdAndSociety_SocietyId(Long paymentId, Long societyId);
    Optional<Payment> findByBill_BillIdAndSociety_SocietyId(Long billId, Long societyId);
    Page<Payment> findByBill_Resident_ResidentIdAndSociety_SocietyId(Long residentId, Long societyId, Pageable pageable);
    Page<Payment> findBySociety_SocietyId(Long societyId, Pageable pageable);
    long countBySociety_SocietyId(Long societyId);
}
