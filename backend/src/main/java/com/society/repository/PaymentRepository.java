package com.society.repository;
import com.society.entity.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBill_BillId(Long billId);
    Page<Payment> findByBill_Resident_ResidentId(Long residentId, Pageable pageable);
}
