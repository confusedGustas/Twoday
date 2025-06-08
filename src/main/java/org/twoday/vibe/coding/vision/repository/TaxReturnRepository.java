package org.twoday.vibe.coding.vision.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.twoday.vibe.coding.vision.entity.TaxReturn;
import org.twoday.vibe.coding.vision.enums.TaxReturnStatus;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaxReturnRepository extends JpaRepository<TaxReturn, Long> {

    List<TaxReturn> findByStatusOrderByCreatedAtDesc(TaxReturnStatus status);

    List<TaxReturn> findBySupplierNameContainingIgnoreCaseOrderByCreatedAtDesc(String supplierName);

    List<TaxReturn> findByRequiresDirectorApprovalOrderByCreatedAtDesc(Boolean requiresDirectorApproval);

    List<TaxReturn> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT tr FROM TaxReturn tr WHERE tr.documentId = :documentId")
    List<TaxReturn> findByDocumentId(@Param("documentId") Long documentId);
} 