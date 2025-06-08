package org.twoday.vibe.coding;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vision")
public class GoogleVisionController {

    @Autowired
    private GoogleVisionService googleVisionService;

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, Object>> analyzeImage(@RequestParam("image") MultipartFile file) {
        try {
            Map<String, Object> result = googleVisionService.analyzeImageFromBytes(file.getBytes());
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to process image: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Vision API error: " + e.getMessage()));
        }
    }

    @GetMapping("/analyze-static")
    public ResponseEntity<Map<String, Object>> analyzeAllStaticImages() {
        try {
            Path staticDir = Paths.get("src/main/resources/static/");
            if (!Files.exists(staticDir)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Static directory not found"));
            }
            
            Map<String, Object> results = new HashMap<>();
            
            Files.list(staticDir)
                .filter(path -> {
                    String filename = path.getFileName().toString().toLowerCase();
                    return filename.endsWith(".jpg") || filename.endsWith(".jpeg") || 
                           filename.endsWith(".png") || filename.endsWith(".bmp") || 
                           filename.endsWith(".gif") || filename.endsWith(".tiff");
                })
                .forEach(imagePath -> {
                    try {
                        String filename = imagePath.getFileName().toString();
                        Map<String, Object> result = googleVisionService.analyzeImage(imagePath.toString());
                        results.put(filename, result);
                    } catch (Exception e) {
                        Map<String, Object> errorResult = new HashMap<>();
                        errorResult.put("error", "Failed to process: " + e.getMessage());
                        results.put(imagePath.getFileName().toString(), errorResult);
                    }
                });
            
            if (results.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "No image files found in static directory"));
            }
            
            return ResponseEntity.ok(results);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to read static directory: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Vision API error: " + e.getMessage()));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<Map<String, Object>> testEndpoint() {
        return ResponseEntity.ok(Map.of(
                "status", "Google Vision API is configured",
                "endpoints", Map.of(
                    "upload", "/api/vision/analyze (POST with image file)",
                    "static", "/api/vision/analyze-static (GET)"
                )
        ));
    }
} 