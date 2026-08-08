package com.society.serviceimpl;

import com.society.dto.MaintenanceBillDTO;
import com.society.entity.MaintenanceBill;
import com.society.entity.Resident;
import com.society.exception.BadRequestException;
import com.society.exception.ResourceNotFoundException;
import com.society.repository.MaintenanceBillRepository;
import com.society.repository.ResidentRepository;
import com.society.security.SecurityUtils;
import com.society.service.EmailService;
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
    private final EmailService emailService;
    private final SecurityUtils securityUtils;

    @Override @Transactional
    public MaintenanceBillDTO createBill(MaintenanceBillDTO dto) {
        Long societyId = securityUtils.getCurrentSocietyId();
        Resident resident = residentRepository.findByResidentIdAndSociety_SocietyId(dto.getResidentId(), societyId)
            .orElseThrow(() -> new ResourceNotFoundException("Resident not found: " + dto.getResidentId()));
        if (billRepository.findByResident_ResidentIdAndMonthAndYearAndSociety_SocietyId(
                dto.getResidentId(), dto.getMonth(), dto.getYear(), societyId).isPresent())
            throw new BadRequestException("Bill already exists for this resident for " + dto.getMonth() + "/" + dto.getYear());
        MaintenanceBill bill = MaintenanceBill.builder().amount(dto.getAmount())
            .dueDate(dto.getDueDate()).month(dto.getMonth()).year(dto.getYear())
            .status(MaintenanceBill.BillStatus.PENDING).resident(resident).society(resident.getSociety()).build();
        bill = billRepository.save(bill);
        notificationService.sendNotification(resident.getUser().getUserId(),
            "Maintenance bill of \u20b9" + dto.getAmount() + " for " + dto.getMonth() + "/" + dto.getYear() + " generated.", "PAYMENT");
        emailBillNotice(resident, dto.getAmount(), dto.getMonth(), dto.getYear(), dto.getDueDate());
        return toDTO(bill);
    }

    @Override public Page<MaintenanceBillDTO> getAllBills(Pageable pageable) {
        return billRepository.findBySociety_SocietyId(securityUtils.getCurrentSocietyId(), pageable).map(this::toDTO);
    }
    @Override public Page<MaintenanceBillDTO> getBillsByResident(Long residentId, Pageable pageable) {
        return billRepository.findByResident_ResidentIdAndSociety_SocietyId(
            residentId, securityUtils.getCurrentSocietyId(), pageable).map(this::toDTO);
    }
    @Override public MaintenanceBillDTO getBillById(Long id) {
        return toDTO(billRepository.findByBillIdAndSociety_SocietyId(id, securityUtils.getCurrentSocietyId())
            .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + id)));
    }
    @Override @Transactional
    public void generateBillsForAll(int month, int year, double amount) {
        Long societyId = securityUtils.getCurrentSocietyId();
        List<Resident> residents = residentRepository.findAllBySociety_SocietyId(societyId);
        for (Resident r : residents) {
            if (billRepository.findByResident_ResidentIdAndMonthAndYearAndSociety_SocietyId(
                    r.getResidentId(), month, year, societyId).isEmpty()) {
                MaintenanceBill bill = MaintenanceBill.builder().amount(BigDecimal.valueOf(amount))
                    .dueDate(LocalDate.of(year, month, 15)).month(month).year(year)
                    .status(MaintenanceBill.BillStatus.PENDING).resident(r).society(r.getSociety()).build();
                billRepository.save(bill);
                notificationService.sendNotification(r.getUser().getUserId(),
                    "Maintenance bill of \u20b9" + amount + " for " + month + "/" + year + " is due on 15th.", "PAYMENT");
                emailBillNotice(r, BigDecimal.valueOf(amount), month, year, LocalDate.of(year, month, 15));
            }
        }
    }

    private void emailBillNotice(Resident resident, BigDecimal amount, int month, int year, LocalDate dueDate) {
        emailService.send(resident.getUser().getEmail(),
            "New maintenance bill - " + month + "/" + year,
            "Hi " + resident.getUser().getName() + ",\n\n" +
            "A new maintenance bill has been generated for your flat " + resident.getFlatNo() + ":\n\n" +
            "Amount: \u20b9" + amount + "\n" +
            "Period: " + month + "/" + year + "\n" +
            "Due date: " + dueDate + "\n\n" +
            "Please log in to Smart Society to view and pay this bill.\n\n" +
            "- " + resident.getSociety().getName()
        );
    }

    private MaintenanceBillDTO toDTO(MaintenanceBill b) {
        return MaintenanceBillDTO.builder().billId(b.getBillId()).amount(b.getAmount())
            .dueDate(b.getDueDate()).month(b.getMonth()).year(b.getYear()).status(b.getStatus().name())
            .createdAt(b.getCreatedAt()).residentId(b.getResident().getResidentId())
            .residentName(b.getResident().getUser().getName()).flatNo(b.getResident().getFlatNo())
            .paid(b.getStatus() == MaintenanceBill.BillStatus.PAID).build();
    }
}
