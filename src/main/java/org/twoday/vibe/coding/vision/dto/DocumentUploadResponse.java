package org.twoday.vibe.coding.vision.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadResponse {
    private Long documentId;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private String processingStatus;
    private String supplierName;
    private String totalAmount;
    private String purchaseDate;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 