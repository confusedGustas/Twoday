package org.twoday.vibe.coding.vision.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.twoday.vibe.coding.vision.dto.TaxReturnRequest;
import org.twoday.vibe.coding.vision.dto.TaxReturnResponse;
import org.twoday.vibe.coding.vision.enums.TaxReturnStatus;
import org.twoday.vibe.coding.vision.service.ExcelExportService;
import org.twoday.vibe.coding.vision.service.TaxReturnService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/tax-returns")
@Tag(name = "Tax Return Management", description = "APIs for creating and managing tax return forms with approval workflow")
public class TaxReturnController {

    @Autowired
    private TaxReturnService taxReturnService;

    @Autowired
    private ExcelExportService excelExportService;

    @PostMapping
    @Operation(summary = "Create a tax return form", 
               description = "Create a tax return form with automatic approval type calculation based on amount")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tax return created successfully",
                     content = @Content(schema = @Schema(implementation = TaxReturnResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Referenced document not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<TaxReturnResponse> createTaxReturn(
            @Valid @RequestBody TaxReturnRequest request) {
        try {
            log.info("Creating tax return for document ID: {} with amount: {}", 
                     request.getDocumentId(), request.getTotalAmount());
            TaxReturnResponse response = taxReturnService.createTaxReturn(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating tax return: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping
    @Operation(summary = "Get all tax returns", 
               description = "Retrieve a list of all tax return forms")
    @ApiResponse(responseCode = "200", description = "List of tax returns retrieved successfully")
    public ResponseEntity<List<TaxReturnResponse>> getAllTaxReturns() {
        try {
            List<TaxReturnResponse> taxReturns = taxReturnService.getAllTaxReturns();
            return ResponseEntity.ok(taxReturns);
        } catch (Exception e) {
            log.error("Error retrieving tax returns: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tax return by ID", 
               description = "Retrieve a specific tax return form by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tax return retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Tax return not found")
    })
    public ResponseEntity<TaxReturnResponse> getTaxReturnById(
            @Parameter(description = "Tax return ID", required = true) @PathVariable Long id) {
        try {
            Optional<TaxReturnResponse> taxReturn = taxReturnService.getTaxReturnById(id);
            if (taxReturn.isPresent()) {
                return ResponseEntity.ok(taxReturn.get());
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error retrieving tax return: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get tax returns by status", 
               description = "Retrieve tax returns filtered by status (PENDING, APPROVED, REJECTED)")
    @Parameter(name = "status", description = "Tax return status", example = "PENDING")
    public ResponseEntity<List<TaxReturnResponse>> getTaxReturnsByStatus(@PathVariable String status) {
        try {
            TaxReturnStatus statusEnum = TaxReturnStatus.fromValue(status.toUpperCase());
            List<TaxReturnResponse> taxReturns = taxReturnService.getTaxReturnsByStatus(statusEnum);
            return ResponseEntity.ok(taxReturns);
        } catch (Exception e) {
            log.error("Error retrieving tax returns by status: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/director-approval/{required}")
    @Operation(summary = "Get tax returns by director approval requirement", 
               description = "Retrieve tax returns that require or don't require director approval")
    @Parameter(name = "required", description = "Whether director approval is required", example = "true")
    public ResponseEntity<List<TaxReturnResponse>> getTaxReturnsByDirectorApproval(@PathVariable Boolean required) {
        try {
            List<TaxReturnResponse> taxReturns = taxReturnService.getTaxReturnsByDirectorApprovalRequired(required);
            return ResponseEntity.ok(taxReturns);
        } catch (Exception e) {
            log.error("Error retrieving tax returns by director approval: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search tax returns by supplier", 
               description = "Search tax returns by supplier name (case-insensitive partial match)")
    public ResponseEntity<List<TaxReturnResponse>> searchTaxReturnsBySupplier(
            @Parameter(description = "Supplier name to search for", required = true, example = "ACME Corp")
            @RequestParam String supplier) {
        try {
            List<TaxReturnResponse> taxReturns = taxReturnService.searchTaxReturnsBySupplier(supplier);
            return ResponseEntity.ok(taxReturns);
        } catch (Exception e) {
            log.error("Error searching tax returns: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get tax returns by date range", 
               description = "Retrieve tax returns within a specific date range")
    public ResponseEntity<List<TaxReturnResponse>> getTaxReturnsByDateRange(
            @Parameter(description = "Start date", required = true, example = "2023-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date", required = true, example = "2023-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<TaxReturnResponse> taxReturns = taxReturnService.getTaxReturnsByDateRange(startDate, endDate);
            return ResponseEntity.ok(taxReturns);
        } catch (Exception e) {
            log.error("Error retrieving tax returns by date range: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update tax return status", 
               description = "Update the status of a tax return (PENDING, APPROVED, REJECTED)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tax return status updated successfully"),
        @ApiResponse(responseCode = "404", description = "Tax return not found")
    })
    public ResponseEntity<TaxReturnResponse> updateTaxReturnStatus(
            @Parameter(description = "Tax return ID", required = true) @PathVariable Long id,
            @Parameter(description = "New status", required = true, example = "APPROVED")
            @RequestParam String status) {
        try {
            TaxReturnStatus statusEnum = TaxReturnStatus.fromValue(status.toUpperCase());
            TaxReturnResponse response = taxReturnService.updateTaxReturnStatus(id, statusEnum);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating tax return status: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/export/approved")
    @Operation(summary = "Export approved tax returns to Excel", 
               description = "Download approved tax returns as an Excel file")
    @ApiResponse(responseCode = "200", description = "Excel file generated successfully")
    public ResponseEntity<byte[]> exportApprovedTaxReturnsToExcel() {
        try {
            List<TaxReturnResponse> approvedTaxReturns = taxReturnService.getTaxReturnsByStatus(TaxReturnStatus.APPROVED);
            byte[] excelFile = excelExportService.exportTaxReturnsToExcel(approvedTaxReturns);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "approved_tax_returns.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelFile);
        } catch (Exception e) {
            log.error("Error exporting tax returns to Excel: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('DIRECTOR', 'COMMITTEE_LEAD', 'COACH')")
    @Operation(summary = "Approve a tax return", 
               description = "Approve a tax return based on user role and approval type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tax return approved successfully"),
        @ApiResponse(responseCode = "403", description = "User not authorized to approve this tax return"),
        @ApiResponse(responseCode = "404", description = "Tax return not found"),
        @ApiResponse(responseCode = "400", description = "Invalid approval request")
    })
    public ResponseEntity<TaxReturnResponse> approveTaxReturn(
            @Parameter(description = "Tax return ID", required = true) @PathVariable Long id,
            @Parameter(description = "Approval notes", required = false) @RequestParam(required = false) String notes) {
        try {
            TaxReturnResponse response = taxReturnService.approveTaxReturn(id, notes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error approving tax return: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('DIRECTOR', 'COMMITTEE_LEAD', 'COACH')")
    @Operation(summary = "Reject a tax return", 
               description = "Reject a tax return with optional rejection reason")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tax return rejected successfully"),
        @ApiResponse(responseCode = "403", description = "User not authorized to reject this tax return"),
        @ApiResponse(responseCode = "404", description = "Tax return not found")
    })
    public ResponseEntity<TaxReturnResponse> rejectTaxReturn(
            @Parameter(description = "Tax return ID", required = true) @PathVariable Long id,
            @Parameter(description = "Rejection reason", required = true) @RequestParam String reason) {
        try {
            TaxReturnResponse response = taxReturnService.rejectTaxReturn(id, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error rejecting tax return: ", e);
            return ResponseEntity.internalServerError().build();
        }
    }
} 