package com.society.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * The tenant root of the application. Every user, resident, staff member,
 * notice, complaint, bill, payment and notification belongs to exactly one
 * Society. All tenant-scoped queries filter on societyId.
 */
@Entity
@Table(name = "societies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Society {

    /** Lifecycle state controlled exclusively by SUPER_ADMIN. */
    public enum Status { PENDING, ACTIVE, SUSPENDED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "society_id")
    private Long societyId;

    @Column(nullable = false, length = 150)
    private String name;

    /**
     * Short, unique, human-shareable code for the society (e.g. "GRNVW01").
     * Not a secret — used for support/reference, never for authorization.
     */
    @Column(name = "society_code", nullable = false, unique = true, length = 30)
    private String societyCode;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 20)
    private String pincode;

    @Column(name = "contact_email", nullable = false, length = 150)
    private String contactEmail;

    @Column(name = "contact_phone", length = 15)
    private String contactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
