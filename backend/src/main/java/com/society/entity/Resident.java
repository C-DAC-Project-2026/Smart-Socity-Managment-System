package com.society.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "residents",
       uniqueConstraints = @UniqueConstraint(name = "uk_resident_flat_per_society", columnNames = {"society_id", "flat_no"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Resident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resident_id")
    private Long residentId;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 15)
    private String mobile;

    // flatNo is unique per-society, not globally — two different societies
    // can each have a "flat A-101". Uniqueness is enforced in the composite
    // constraint below and re-checked in the service layer scoped to society.
    @Column(name = "flat_no", nullable = false, length = 20)
    private String flatNo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", nullable = false)
    private Society society;

    @OneToMany(mappedBy = "resident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Complaint> complaints;

    @OneToMany(mappedBy = "resident", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MaintenanceBill> bills;
}
