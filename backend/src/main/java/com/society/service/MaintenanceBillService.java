package com.society.service;
import com.society.dto.MaintenanceBillDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
public interface MaintenanceBillService {
    MaintenanceBillDTO createBill(MaintenanceBillDTO dto);
    Page<MaintenanceBillDTO> getAllBills(Pageable pageable);
    Page<MaintenanceBillDTO> getBillsByResident(Long residentId, Pageable pageable);
    MaintenanceBillDTO getBillById(Long id);
    void generateBillsForAll(int month, int year, double amount);
}
