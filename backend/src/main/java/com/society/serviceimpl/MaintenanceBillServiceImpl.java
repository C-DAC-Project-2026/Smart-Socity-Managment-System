package com.society.serviceimpl;

import com.society.dto.MaintenanceBillDTO;
import com.society.entity.MaintenanceBill;
import com.society.entity.Resident;
import com.society.exception.BadRequestException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.MaintenanceBillRepository;
import com.society.repository.ResidentRepository;
import com.society.service.MaintenanceBillService;
import com.society.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service @RequiredArgsConstructor
public class MaintenanceBillServiceImpl implements MaintenanceBillService {
    private final MaintenanceBillRepository billRepository;
    private final ResidentRepository residentRepository;
    private final NotificationService notificationService;

    @Override @Transactional
    public MaintenanceBillDTO createBill(MaintenanceBillDTO dto) {
        Resident resident = residentRepository.findById(dto.getResidentId())
            .orElseThrow(() -> new ResourceNotFoundException("Resident not found: " + dto.getResidentId()));
        if (billRepository.findByResident_ResidentIdAndMonthAndYear(dto.getResidentId(), dto.getMonth(), dto.getYear()).isPresent())
            throw new BadRequestException("Bill already exists for this resident for " + dto.getMonth() + "/" + dto.getYear());
        MaintenanceBill bill = MaintenanceBill.builder().amount(dto.getAmount())
            .dueDate(dto.getDueDate()).month(dto.getMonth()).year(dto.getYear())
            .status(MaintenanceBill.BillStatus.PENDING).resident(resident).build();
        bill = billRepository.save(bill);
        notificationService.sendNotification(resident.getUser().getUserId(),
            "Maintenance bill of ₹" + dto.getAmount() + " for " + dto.getMonth() + "/" + dto.getYear() + " generated.", "PAYMENT");
        return toDTO(bill);
    }

    @Override public Page<MaintenanceBillDTO> getAllBills(Pageable pageable) {
        return billRepository.findAll(pageable).map(this::toDTO);
    }
    @Override public Page<MaintenanceBillDTO> getBillsByResident(Long residentId, Pageable pageable) {
        return billRepository.findByResident_ResidentId(residentId, pageable).map(this::toDTO);
    }
    @Override public MaintenanceBillDTO getBillById(Long id) {
        return toDTO(billRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + id)));
    }
    @Override @Transactional
    public void generateBillsForAll(int month, int year, double amount) {
        List<Resident> residents = residentRepository.findAll();
        for (Resident r : residents) {
            if (billRepository.findByResident_ResidentIdAndMonthAndYear(r.getResidentId(), month, year).isEmpty()) {
                MaintenanceBill bill = MaintenanceBill.builder().amount(BigDecimal.valueOf(amount))
                    .dueDate(LocalDate.of(year, month, 15)).month(month).year(year)
                    .status(MaintenanceBill.BillStatus.PENDING).resident(r).build();
                billRepository.save(bill);
                notificationService.sendNotification(r.getUser().getUserId(),
                    "Maintenance bill of ₹" + amount + " for " + month + "/" + year + " is due on 15th.", "PAYMENT");
            }
        }
    }
    private MaintenanceBillDTO toDTO(MaintenanceBill b) {
        return MaintenanceBillDTO.builder().billId(b.getBillId()).amount(b.getAmount())
            .dueDate(b.getDueDate()).month(b.getMonth()).year(b.getYear()).status(b.getStatus().name())
            .createdAt(b.getCreatedAt()).residentId(b.getResident().getResidentId())
            .residentName(b.getResident().getUser().getName()).flatNo(b.getResident().getFlatNo())
            .paid(b.getStatus() == MaintenanceBill.BillStatus.PAID).build();
    }
}
