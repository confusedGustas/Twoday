package org.twoday.vibe.coding.vision.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twoday.vibe.coding.vision.entity.UploadedDocument;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UploadedDocumentRepository extends JpaRepository<UploadedDocument, Long> {

    List<UploadedDocument> findByProcessingStatusOrderByCreatedAtDesc(String processingStatus);

    List<UploadedDocument> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT d FROM UploadedDocument d WHERE d.supplierName LIKE %:supplierName% ORDER BY d.createdAt DESC")
    List<UploadedDocument> findBySupplierNameContainingIgnoreCase(@Param("supplierName") String supplierName);

    @Query("SELECT d.id, d.originalFilename, d.contentType, d.fileSize, d.supplierName, d.totalAmount, d.purchaseDate, d.processingStatus, d.createdAt, d.updatedAt FROM UploadedDocument d ORDER BY d.createdAt DESC")
    List<Object[]> findAllDocumentMetadata();

    Optional<UploadedDocument> findByIdAndProcessingStatus(Long id, String processingStatus);
} 