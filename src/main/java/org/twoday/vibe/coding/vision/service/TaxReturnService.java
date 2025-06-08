package org.twoday.vibe.coding.vision.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.twoday.vibe.coding.auth.enums.UserRole;
import org.twoday.vibe.coding.vision.dto.TaxReturnRequest;
import org.twoday.vibe.coding.vision.dto.TaxReturnResponse;
import org.twoday.vibe.coding.vision.entity.TaxReturn;
import org.twoday.vibe.coding.vision.entity.UploadedDocument;
import org.twoday.vibe.coding.vision.enums.ApprovalType;
import org.twoday.vibe.coding.vision.enums.TaxReturnStatus;
import org.twoday.vibe.coding.vision.repository.TaxReturnRepository;
import org.twoday.vibe.coding.vision.repository.UploadedDocumentRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class TaxReturnService {

    private static final BigDecimal DIRECTOR_APPROVAL_THRESHOLD = new BigDecimal("1000");

    @Autowired
    private TaxReturnRepository taxReturnRepository;

    @Autowired
    private UploadedDocumentRepository documentRepository;

    public TaxReturnResponse createTaxReturn(TaxReturnRequest request) {
        log.info("Creating tax return for document ID: {}", request.getDocumentId());

        // Validate document exists
        Optional<UploadedDocument> documentOpt = documentRepository.findById(request.getDocumentId());
        if (documentOpt.isEmpty()) {
            throw new RuntimeException("Document not found with ID: " + request.getDocumentId());
        }

        // Validate approval type
        if (!isValidApprovalType(request.getUserSelectedApproval())) {
            throw new RuntimeException("Invalid approval type. Must be BASIC or COMITET");
        }

        // Calculate final approval type based on business rules
        ApprovalType finalApprovalType = calculateFinalApprovalType(request.getTotalAmount(), request.getUserSelectedApproval());
        boolean requiresDirectorApproval = request.getTotalAmount().compareTo(DIRECTOR_APPROVAL_THRESHOLD) >= 0;

        // Create tax return entity
        TaxReturn taxReturn = new TaxReturn();
        taxReturn.setDocumentId(request.getDocumentId());
        taxReturn.setSupplierName(request.getSupplierName());
        taxReturn.setTotalAmount(request.getTotalAmount());
        taxReturn.setPurchaseDate(request.getPurchaseDate());
        taxReturn.setUserSelectedApproval(request.getUserSelectedApproval());
        taxReturn.setFinalApprovalType(finalApprovalType);
        taxReturn.setRequiresDirectorApproval(requiresDirectorApproval);
        taxReturn.setStatus(TaxReturnStatus.PENDING);
        taxReturn.setNotes(request.getNotes());

        // Save to database
        taxReturn = taxReturnRepository.save(taxReturn);
        log.info("Tax return created with ID: {} and approval type: {}", taxReturn.getId(), finalApprovalType);

        return mapToResponse(taxReturn);
    }

    @Transactional(readOnly = true)
    public List<TaxReturnResponse> getAllTaxReturns() {
        List<TaxReturn> taxReturns = taxReturnRepository.findAll();
        return taxReturns.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<TaxReturnResponse> getTaxReturnById(Long id) {
        return taxReturnRepository.findById(id)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<TaxReturnResponse> getTaxReturnsByStatus(TaxReturnStatus status) {
        List<TaxReturn> taxReturns = taxReturnRepository.findByStatusOrderByCreatedAtDesc(status);
        return taxReturns.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaxReturnResponse> getTaxReturnsByDirectorApprovalRequired(Boolean requiresDirectorApproval) {
        List<TaxReturn> taxReturns = taxReturnRepository.findByRequiresDirectorApprovalOrderByCreatedAtDesc(requiresDirectorApproval);
        return taxReturns.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaxReturnResponse> searchTaxReturnsBySupplier(String supplierName) {
        List<TaxReturn> taxReturns = taxReturnRepository.findBySupplierNameContainingIgnoreCaseOrderByCreatedAtDesc(supplierName);
        return taxReturns.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaxReturnResponse> getTaxReturnsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<TaxReturn> taxReturns = taxReturnRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(startDate, endDate);
        return taxReturns.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TaxReturnResponse updateTaxReturnStatus(Long id, TaxReturnStatus status) {
        Optional<TaxReturn> taxReturnOpt = taxReturnRepository.findById(id);
        if (taxReturnOpt.isEmpty()) {
            throw new RuntimeException("Tax return not found with ID: " + id);
        }

        TaxReturn taxReturn = taxReturnOpt.get();
        taxReturn.setStatus(status);
        taxReturn = taxReturnRepository.save(taxReturn);

        log.info("Tax return {} status updated to: {}", id, status);
        return mapToResponse(taxReturn);
    }

    public TaxReturnResponse approveTaxReturn(Long id, String notes) {
        Optional<TaxReturn> taxReturnOpt = taxReturnRepository.findById(id);
        if (taxReturnOpt.isEmpty()) {
            throw new RuntimeException("Tax return not found with ID: " + id);
        }

        TaxReturn taxReturn = taxReturnOpt.get();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserRole userRole = UserRole.valueOf(auth.getAuthorities().iterator().next().getAuthority());

        // Validate if user can approve based on role and approval type
        if (!canApproveTaxReturn(taxReturn, userRole)) {
            throw new AccessDeniedException("User not authorized to approve this tax return");
        }

        // Update status and notes
        taxReturn.setStatus(TaxReturnStatus.APPROVED);
        if (notes != null && !notes.trim().isEmpty()) {
            taxReturn.setNotes(notes);
        }
        taxReturn.setUpdatedAt(LocalDateTime.now());
        taxReturn = taxReturnRepository.save(taxReturn);

        log.info("Tax return {} approved by {} with role {}", id, auth.getName(), userRole);
        return mapToResponse(taxReturn);
    }

    public TaxReturnResponse rejectTaxReturn(Long id, String reason) {
        Optional<TaxReturn> taxReturnOpt = taxReturnRepository.findById(id);
        if (taxReturnOpt.isEmpty()) {
            throw new RuntimeException("Tax return not found with ID: " + id);
        }

        TaxReturn taxReturn = taxReturnOpt.get();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserRole userRole = UserRole.valueOf(auth.getAuthorities().iterator().next().getAuthority());

        // Validate if user can reject based on role and approval type
        if (!canApproveTaxReturn(taxReturn, userRole)) {
            throw new AccessDeniedException("User not authorized to reject this tax return");
        }

        // Update status and notes
        taxReturn.setStatus(TaxReturnStatus.REJECTED);
        taxReturn.setNotes(reason);
        taxReturn.setUpdatedAt(LocalDateTime.now());
        taxReturn = taxReturnRepository.save(taxReturn);

        log.info("Tax return {} rejected by {} with role {}", id, auth.getName(), userRole);
        return mapToResponse(taxReturn);
    }

    private boolean canApproveTaxReturn(TaxReturn taxReturn, UserRole userRole) {
        ApprovalType finalApprovalType = taxReturn.getFinalApprovalType();
        boolean requiresDirectorApproval = taxReturn.getRequiresDirectorApproval();

        // Director can approve anything
        if (userRole == UserRole.DIRECTOR) {
            return true;
        }

        // Committee lead can approve committee approvals
        if (userRole == UserRole.COMMITTEE_LEAD) {
            return finalApprovalType == ApprovalType.COMITET || 
                   finalApprovalType == ApprovalType.COMITET_DIRECTOR;
        }

        // Coach can only approve basic approvals
        if (userRole == UserRole.COACH) {
            return finalApprovalType == ApprovalType.BASIC;
        }

        return false;
    }

    private ApprovalType calculateFinalApprovalType(BigDecimal totalAmount, ApprovalType userSelectedApproval) {
        if (totalAmount.compareTo(DIRECTOR_APPROVAL_THRESHOLD) >= 0) {
            // Amount >= 1000: Add director approval
            if (userSelectedApproval == ApprovalType.BASIC) {
                return ApprovalType.BASIC_DIRECTOR;
            } else {
                return ApprovalType.COMITET_DIRECTOR;
            }
        } else {
            // Amount < 1000: Use user selection as-is
            return userSelectedApproval;
        }
    }

    private boolean isValidApprovalType(ApprovalType approvalType) {
        return approvalType == ApprovalType.BASIC || approvalType == ApprovalType.COMITET;
    }

    private TaxReturnResponse mapToResponse(TaxReturn taxReturn) {
        return new TaxReturnResponse(
                taxReturn.getId(),
                taxReturn.getDocumentId(),
                taxReturn.getSupplierName(),
                taxReturn.getTotalAmount(),
                taxReturn.getPurchaseDate(),
                taxReturn.getUserSelectedApproval(),
                taxReturn.getFinalApprovalType(),
                taxReturn.getRequiresDirectorApproval(),
                taxReturn.getStatus(),
                taxReturn.getNotes(),
                taxReturn.getCreatedAt(),
                taxReturn.getUpdatedAt()
        );
    }
} 