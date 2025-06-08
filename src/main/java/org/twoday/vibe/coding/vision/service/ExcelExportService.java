package org.twoday.vibe.coding.vision.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.twoday.vibe.coding.vision.dto.TaxReturnResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class ExcelExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] exportTaxReturnsToExcel(List<TaxReturnResponse> taxReturns) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Approved Tax Returns");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {
                "ID", "Document ID", "Supplier Name", "Total Amount", "Purchase Date",
                "User Selected Approval", "Final Approval Type", "Requires Director Approval",
                "Status", "Notes", "Created At", "Updated At"
            };

            // Create header cells
            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Create data rows
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle numberStyle = createNumberStyle(workbook);
            int rowNum = 1;

            for (TaxReturnResponse taxReturn : taxReturns) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(taxReturn.getId());
                row.createCell(1).setCellValue(taxReturn.getDocumentId());
                row.createCell(2).setCellValue(taxReturn.getSupplierName());
                
                Cell amountCell = row.createCell(3);
                amountCell.setCellValue(taxReturn.getTotalAmount().doubleValue());
                amountCell.setCellStyle(numberStyle);
                
                row.createCell(4).setCellValue(taxReturn.getPurchaseDate());
                row.createCell(5).setCellValue(taxReturn.getUserSelectedApproval().toString());
                row.createCell(6).setCellValue(taxReturn.getFinalApprovalType().toString());
                row.createCell(7).setCellValue(taxReturn.getRequiresDirectorApproval());
                row.createCell(8).setCellValue(taxReturn.getStatus().toString());
                row.createCell(9).setCellValue(taxReturn.getNotes());
                
                Cell createdAtCell = row.createCell(10);
                createdAtCell.setCellValue(taxReturn.getCreatedAt().format(DATE_FORMATTER));
                createdAtCell.setCellStyle(dateStyle);
                
                Cell updatedAtCell = row.createCell(11);
                updatedAtCell.setCellValue(taxReturn.getUpdatedAt().format(DATE_FORMATTER));
                updatedAtCell.setCellStyle(dateStyle);
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (IOException e) {
            log.error("Error exporting tax returns to Excel: ", e);
            throw new RuntimeException("Failed to export tax returns to Excel", e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("yyyy-mm-dd hh:mm:ss"));
        return style;
    }

    private CellStyle createNumberStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));
        return style;
    }
} 