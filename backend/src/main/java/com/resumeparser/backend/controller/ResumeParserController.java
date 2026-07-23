package com.resumeparser.backend.controller;

import com.resumeparser.backend.dto.ResumeData;
import com.resumeparser.backend.service.ResumeParserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "*")
public class ResumeParserController {

    private final ResumeParserService resumeParserService;
    private final List<ResumeData> history = new CopyOnWriteArrayList<>();

    public ResumeParserController(ResumeParserService resumeParserService) {
        this.resumeParserService = resumeParserService;
    }

    @PostMapping("/parse")
    public ResponseEntity<?> parseResume(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Please upload a valid PDF file."));
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
                return ResponseEntity.badRequest().body(Map.of("error", "Only PDF resumes are supported."));
            }
        }

        try {
            ResumeData parsedData = resumeParserService.parseResume(file);
            
            // Add to history (avoid duplicates in recent history)
            history.removeIf(data -> data.email().equalsIgnoreCase(parsedData.email()) && 
                                     data.name().equalsIgnoreCase(parsedData.name()));
            history.add(0, parsedData);
            if (history.size() > 10) {
                history.remove(history.size() - 1);
            }
            return ResponseEntity.ok(parsedData);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error parsing PDF: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<List<ResumeData>> getHistory() {
        return ResponseEntity.ok(history);
    }
}
