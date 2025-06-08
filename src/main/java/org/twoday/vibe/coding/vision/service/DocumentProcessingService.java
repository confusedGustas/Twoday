package org.twoday.vibe.coding.vision.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.twoday.vibe.coding.vision.dto.DocumentListResponse;
import org.twoday.vibe.coding.vision.dto.DocumentUploadResponse;
import org.twoday.vibe.coding.vision.entity.UploadedDocument;
import org.twoday.vibe.coding.vision.repository.UploadedDocumentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class DocumentProcessingService {

    @Autowired
    private UploadedDocumentRepository documentRepository;

    @Autowired
    private GoogleVisionService visionService;

    public DocumentUploadResponse uploadAndProcessDocument(MultipartFile file) {
        try {
            log.info("Starting document upload and processing for file: {}", file.getOriginalFilename());

            // Create initial document record
            UploadedDocument document = new UploadedDocument();
            document.setOriginalFilename(file.getOriginalFilename());
            document.setContentType(file.getContentType());
            document.setFileSize(file.getSize());
            document.setImageData(file.getBytes());
            document.setProcessingStatus("PROCESSING");

            // Save to database
            document = documentRepository.save(document);
            log.info("Document saved with ID: {}", document.getId());

            try {
                // Process with Google Vision
                Map<String, Object> visionResult = visionService.analyzeImageFromBytes(file.getBytes());
                
                if (visionResult.containsKey("error")) {
                    document.setProcessingStatus("FAILED");
                    document.setErrorMessage(visionResult.get("error").toString());
                } else {
                    // Extract invoice data
                    Map<String, Object> invoiceData = (Map<String, Object>) visionResult.get("invoiceData");
                    if (invoiceData != null) {
                        document.setSupplierName((String) invoiceData.get("supplierName"));
                        document.setTotalAmount((String) invoiceData.get("totalAmount"));
                        document.setPurchaseDate((String) invoiceData.get("purchaseDate"));
                    }
                    
                    // Store full extracted text if available
                    if (visionResult.containsKey("fullText")) {
                        document.setFullExtractedText((String) visionResult.get("fullText"));
                    }
                    
                    document.setProcessingStatus("COMPLETED");
                }

            } catch (Exception e) {
                log.error("Error processing document with Google Vision: ", e);
                document.setProcessingStatus("FAILED");
                document.setErrorMessage("Vision processing failed: " + e.getMessage());
            }

            // Update document with processing results
            document = documentRepository.save(document);
            log.info("Document processing completed for ID: {}", document.getId());

            return mapToUploadResponse(document);

        } catch (Exception e) {
            log.error("Error uploading document: ", e);
            throw new RuntimeException("Failed to upload and process document: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<DocumentListResponse> getAllDocuments() {
        List<UploadedDocument> documents = documentRepository.findAll();
        return documents.stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentListResponse> getDocumentsByStatus(String status) {
        List<UploadedDocument> documents = documentRepository.findByProcessingStatusOrderByCreatedAtDesc(status);
        return documents.stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UploadedDocument> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<DocumentListResponse> searchDocumentsBySupplier(String supplierName) {
        List<UploadedDocument> documents = documentRepository.findBySupplierNameContainingIgnoreCase(supplierName);
        return documents.stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DocumentListResponse> getDocumentsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<UploadedDocument> documents = documentRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
        return documents.stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    public void deleteDocument(Long id) {
        if (!documentRepository.existsById(id)) {
            throw new RuntimeException("Document not found with ID: " + id);
        }
        documentRepository.deleteById(id);
        log.info("Document deleted with ID: {}", id);
    }

    private DocumentUploadResponse mapToUploadResponse(UploadedDocument document) {
        return new DocumentUploadResponse(
                document.getId(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getFileSize(),
                document.getProcessingStatus(),
                document.getSupplierName(),
                document.getTotalAmount(),
                document.getPurchaseDate(),
                document.getErrorMessage(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    private DocumentListResponse mapToListResponse(UploadedDocument document) {
        return new DocumentListResponse(
                document.getId(),
                document.getOriginalFilename(),
                document.getContentType(),
                document.getFileSize(),
                document.getSupplierName(),
                document.getTotalAmount(),
                document.getPurchaseDate(),
                document.getProcessingStatus(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }
} 