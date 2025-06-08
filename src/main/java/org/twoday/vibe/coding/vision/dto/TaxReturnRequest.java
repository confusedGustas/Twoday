package org.twoday.vibe.coding.vision.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.twoday.vibe.coding.vision.enums.ApprovalType;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaxReturnRequest {

    @NotNull(message = "Document ID is required")
    private Long documentId;

    @NotBlank(message = "Supplier name is required")
    private String supplierName;

    @NotNull(message = "Total amount is required")
    private BigDecimal totalAmount;

    @NotBlank(message = "Purchase date is required")
    private String purchaseDate;

    @NotNull(message = "User selected approval type is required")
    private ApprovalType userSelectedApproval; // BASIC or COMITET

    private String notes;
} 