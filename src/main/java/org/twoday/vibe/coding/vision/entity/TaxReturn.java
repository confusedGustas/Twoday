package org.twoday.vibe.coding.vision.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.twoday.vibe.coding.vision.enums.ApprovalType;
import org.twoday.vibe.coding.vision.enums.TaxReturnStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tax_returns")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxReturn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId; // Reference to UploadedDocument

    @Column(name = "supplier_name", length = 500, nullable = false)
    private String supplierName;

    @Column(name = "total_amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "purchase_date", length = 50, nullable = false)
    private String purchaseDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_selected_approval", length = 20, nullable = false)
    private ApprovalType userSelectedApproval; // BASIC or COMITET

    @Enumerated(EnumType.STRING)
    @Column(name = "final_approval_type", length = 50, nullable = false)
    private ApprovalType finalApprovalType; // BASIC, COMITET, BASIC_DIRECTOR, COMITET_DIRECTOR

    @Column(name = "requires_director_approval", nullable = false)
    private Boolean requiresDirectorApproval;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private TaxReturnStatus status; // PENDING, APPROVED, REJECTED

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
} 