package org.twoday.vibe.coding.vision.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoogleVisionService {

    @Autowired
    private ImageAnnotatorClient imageAnnotatorClient;

    @Autowired
    private InvoiceDataExtractor invoiceDataExtractor;

    public Map<String, Object> analyzeImage(String imagePath) throws IOException {
        byte[] data = Files.readAllBytes(Path.of(imagePath));
        ByteString imgBytes = ByteString.copyFrom(data);

        List<AnnotateImageRequest> requests = new ArrayList<>();
        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .build();
        requests.add(request);

        BatchAnnotateImagesResponse response = imageAnnotatorClient.batchAnnotateImages(requests);
        List<AnnotateImageResponse> responses = response.getResponsesList();

        Map<String, Object> result = new HashMap<>();
        
        for (AnnotateImageResponse res : responses) {
            if (res.hasError()) {
                result.put("error", res.getError().getMessage());
                return result;
            }

            String fullText = "";
            
            for (int i = 0; i < res.getTextAnnotationsList().size(); i++) {
                EntityAnnotation annotation = res.getTextAnnotationsList().get(i);
                if (i == 0) {
                    fullText = annotation.getDescription();
                }
            }
            
            result.put("invoiceData", invoiceDataExtractor.extractInvoiceData(fullText));
            result.put("fullText", fullText);
        }

        return result;
    }

    public Map<String, Object> analyzeImageFromBytes(byte[] imageData) {
        ByteString imgBytes = ByteString.copyFrom(imageData);

        List<AnnotateImageRequest> requests = new ArrayList<>();
        Image img = Image.newBuilder().setContent(imgBytes).build();
        Feature feat = Feature.newBuilder().setType(Feature.Type.TEXT_DETECTION).build();
        AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                .addFeatures(feat)
                .setImage(img)
                .build();
        requests.add(request);

        BatchAnnotateImagesResponse response = imageAnnotatorClient.batchAnnotateImages(requests);
        List<AnnotateImageResponse> responses = response.getResponsesList();

        Map<String, Object> result = new HashMap<>();
        
        for (AnnotateImageResponse res : responses) {
            if (res.hasError()) {
                result.put("error", res.getError().getMessage());
                return result;
            }

            String fullText = "";
            
            for (int i = 0; i < res.getTextAnnotationsList().size(); i++) {
                EntityAnnotation annotation = res.getTextAnnotationsList().get(i);
                if (i == 0) {
                    fullText = annotation.getDescription();
                }
            }
            
            result.put("invoiceData", invoiceDataExtractor.extractInvoiceData(fullText));
            result.put("fullText", fullText);
        }

        return result;
    }
} 