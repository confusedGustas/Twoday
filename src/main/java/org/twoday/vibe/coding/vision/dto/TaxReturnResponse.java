package org.twoday.vibe.coding.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.twoday.vibe.coding.vision.enums.ApprovalType;
import org.twoday.vibe.coding.vision.enums.TaxReturnStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxReturnResponse {

    private Long id;
    private Long documentId;
    private String supplierName;
    private BigDecimal totalAmount;
    private String purchaseDate;
    private ApprovalType userSelectedApproval;
    private ApprovalType finalApprovalType;
    private Boolean requiresDirectorApproval;
    private TaxReturnStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 