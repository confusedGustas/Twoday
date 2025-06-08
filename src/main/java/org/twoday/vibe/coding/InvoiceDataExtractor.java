package org.twoday.vibe.coding;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class InvoiceDataExtractor {

    public Map<String, Object> extractInvoiceData(String fullText) {
        Map<String, Object> invoiceData = new HashMap<>();
        
        invoiceData.put("supplierName", extractSupplierName(fullText));
        invoiceData.put("totalAmount", extractTotalAmount(fullText));
        invoiceData.put("purchaseDate", extractPurchaseDate(fullText));
        
        return invoiceData;
    }

    private String extractSupplierName(String text) {
        Pattern[] patterns = {
            Pattern.compile("UAB\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("AB\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("IĮ\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("VšĮ\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("SIA\\s*[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(MAXIMA\\s*LT)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(RIMI\\s*LIETUVA)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(IKI\\s*LT)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(LIDL\\s*LIETUVA)", Pattern.CASE_INSENSITIVE)
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                if (pattern.pattern().contains("UAB") || pattern.pattern().contains("AB") || 
                    pattern.pattern().contains("IĮ") || pattern.pattern().contains("VšĮ") || 
                    pattern.pattern().contains("SIA")) {
                    return matcher.group(1).trim();
                } else {
                    return matcher.group(1).trim();
                }
            }
        }
        
        return "Unknown";
    }

    private String extractTotalAmount(String text) {
        Pattern totalSectionPattern = Pattern.compile("IŠ\\s*VISO\\s*:?\\s*([\\s\\S]*?)(?=\\n\\n|$)", Pattern.CASE_INSENSITIVE);
        Matcher totalMatcher = totalSectionPattern.matcher(text);
        
        if (totalMatcher.find()) {
            String totalSection = totalMatcher.group(1);
            Pattern amountPattern = Pattern.compile("([0-9]+[,.]\\d{2})");
            Matcher amountMatcher = amountPattern.matcher(totalSection);
            
            String largestAmount = "0,00";
            double largestValue = 0.0;
            
            while (amountMatcher.find()) {
                String amount = amountMatcher.group(1);
                double value = Double.parseDouble(amount.replace(",", "."));
                if (value > largestValue) {
                    largestValue = value;
                    largestAmount = amount;
                }
            }
            
            if (!largestAmount.equals("0,00")) {
                return largestAmount + " EUR";
            }
        }
        
        Pattern[] fallbackPatterns = {
            Pattern.compile("(?:TOTAL|VISO|Total|SUMA|SUM|MOKĖTI)\\s*:?\\s*([0-9]+[,.]\\d{2})\\s*(?:EUR|€)?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("([0-9]+[,.]\\d{2})\\s*(?:EUR|€)\\s*$", Pattern.MULTILINE),
            Pattern.compile("\\b([0-9]{2,4}[,.]\\d{2})\\s*(?:EUR|€)", Pattern.CASE_INSENSITIVE)
        };
        
        String largestAmount = "0,00";
        double largestValue = 0.0;
        
        for (Pattern pattern : fallbackPatterns) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                String amount = matcher.group(1);
                double value = Double.parseDouble(amount.replace(",", "."));
                if (value > largestValue) {
                    largestValue = value;
                    largestAmount = amount;
                }
            }
        }
        
        return largestAmount.equals("0,00") ? "Unknown" : largestAmount + " EUR";
    }

    private String extractPurchaseDate(String text) {
        Pattern[] patterns = {
            Pattern.compile("(\\d{4}-\\d{2}-\\d{2})"),
            Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4})"),
            Pattern.compile("(\\d{2}/\\d{2}/\\d{4})"),
            Pattern.compile("(\\d{2}-\\d{2}-\\d{4})"),
            Pattern.compile("(\\d{1,2}\\.\\d{1,2}\\.\\d{4})"),
            Pattern.compile("(\\d{1,2}/\\d{1,2}/\\d{4})")
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        
        return "Unknown";
    }
} 