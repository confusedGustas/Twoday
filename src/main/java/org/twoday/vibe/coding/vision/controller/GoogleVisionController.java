package org.twoday.vibe.coding.vision.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.twoday.vibe.coding.vision.dto.DocumentListResponse;
import org.twoday.vibe.coding.vision.dto.DocumentUploadResponse;
import org.twoday.vibe.coding.vision.entity.UploadedDocument;
import org.twoday.vibe.coding.vision.service.DocumentProcessingService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@Tag(name = "Document Processing", description = "APIs for document upload, processing, and retrieval with Google Vision OCR")
public class GoogleVisionController {

    @Autowired
    private DocumentProcessingService documentProcessingService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a document", 
               description = "Upload an image document for OCR processing with Google Vision API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document uploaded successfully",
                     content = @Content(schema = @Schema(implementation = DocumentUploadResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid file or empty file"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<DocumentUploadResponse> uploadDocument(
            @Parameter(description = "Image file to upload", required = true, 
                      content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE))
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            log.info("Uploading document: {} (size: {} bytes)", file.getOriginalFilename(), file.getSize());
            DocumentUploadResponse response = documentProcessingService.uploadAndProcessDocument(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error uploading document: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all documents", 
               description = "Retrieve a list of all uploaded documents")
    @ApiResponse(responseCode = "200", description = "List of documents retrieved successfully")
    public ResponseEntity<List<DocumentListResponse>> getAllDocuments() {
        try {
            List<DocumentListResponse> documents = documentProcessingService.getAllDocuments();
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Error retrieving documents: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get documents by status", 
               description = "Retrieve documents filtered by processing status (PROCESSING, COMPLETED, FAILED)")
    @Parameter(name = "status", description = "Processing status", example = "COMPLETED")
    public ResponseEntity<List<DocumentListResponse>> getDocumentsByStatus(@PathVariable String status) {
        try {
            List<DocumentListResponse> documents = documentProcessingService.getDocumentsByStatus(status.toUpperCase());
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Error retrieving documents by status: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get document details", 
               description = "Retrieve detailed information about a specific document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<DocumentUploadResponse> getDocumentDetails(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id) {
        try {
            Optional<UploadedDocument> documentOpt = documentProcessingService.getDocumentById(id);
            if (documentOpt.isPresent()) {
                UploadedDocument document = documentOpt.get();
                DocumentUploadResponse response = new DocumentUploadResponse(
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
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error retrieving document details: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download document image", 
               description = "Download the original image file of a document")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document image downloaded successfully"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<byte[]> downloadDocumentImage(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id) {
        try {
            Optional<UploadedDocument> documentOpt = documentProcessingService.getDocumentById(id);
            if (documentOpt.isPresent()) {
                UploadedDocument document = documentOpt.get();
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(document.getContentType()));
                headers.setContentDispositionFormData("attachment", document.getOriginalFilename());
                headers.setContentLength(document.getImageData().length);
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(document.getImageData());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error downloading document image: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/preview")
    @Operation(summary = "Preview document image", 
               description = "Preview the document image in browser")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Document image preview loaded successfully"),
        @ApiResponse(responseCode = "404", description = "Document not found")
    })
    public ResponseEntity<byte[]> previewDocumentImage(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id) {
        try {
            Optional<UploadedDocument> documentOpt = documentProcessingService.getDocumentById(id);
            if (documentOpt.isPresent()) {
                UploadedDocument document = documentOpt.get();
                
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.parseMediaType(document.getContentType()));
                headers.setCacheControl("max-age=3600");
                
                return ResponseEntity.ok()
                        .headers(headers)
                        .body(document.getImageData());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error previewing document image: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search documents by supplier", 
               description = "Search documents by supplier name (case-insensitive partial match)")
    public ResponseEntity<List<DocumentListResponse>> searchDocuments(
            @Parameter(description = "Supplier name to search for", required = true, example = "ACME Corp")
            @RequestParam String supplier) {
        try {
            List<DocumentListResponse> documents = documentProcessingService.searchDocumentsBySupplier(supplier);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Error searching documents: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get documents by date range", 
               description = "Retrieve documents within a specific date range")
    public ResponseEntity<List<DocumentListResponse>> getDocumentsByDateRange(
            @Parameter(description = "Start date", required = true, example = "2023-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date", required = true, example = "2023-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<DocumentListResponse> documents = documentProcessingService.getDocumentsByDateRange(startDate, endDate);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Error retrieving documents by date range: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document", 
               description = "Delete a document and its associated data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Document deleted successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteDocument(
            @Parameter(description = "Document ID", required = true) @PathVariable Long id) {
        try {
            documentProcessingService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Error deleting document: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 