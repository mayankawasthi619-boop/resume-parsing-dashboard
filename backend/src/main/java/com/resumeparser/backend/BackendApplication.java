package com.resumeparser.backend;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.File;

@SpringBootApplication
public class BackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }

    @Bean
    public CommandLineRunner createSampleResume() {
        return args -> {
            File pdfFile = new File("sample_resume.pdf");
            if (!pdfFile.exists()) {
                try (PDDocument doc = new PDDocument()) {
                    PDPage page = new PDPage();
                    doc.addPage(page);
                    
                    try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
                        contentStream.setLeading(18f);
                        // Start offset
                        contentStream.newLineAtOffset(50, 750);
                        
                        // Header
                        contentStream.showText("John Doe");
                        contentStream.newLine();
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        contentStream.showText("john.doe@example.com | (123) 456-7890 | Seattle, WA");
                        contentStream.newLine();
                        contentStream.newLine();
                        
                        // Skills
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                        contentStream.showText("Skills");
                        contentStream.newLine();
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        contentStream.showText("Java, Spring Boot, React, JavaScript, SQL, AWS, Docker, Git");
                        contentStream.newLine();
                        contentStream.newLine();
                        
                        // Experience
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                        contentStream.showText("Experience");
                        contentStream.newLine();
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                        contentStream.showText("Senior Software Engineer - CloudTech Solutions (2021 - Present)");
                        contentStream.newLine();
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        contentStream.showText("- Led development of high-throughput Spring Boot microservices");
                        contentStream.newLine();
                        contentStream.showText("- Designed and deployed React dashboards on AWS CloudFront");
                        contentStream.newLine();
                        contentStream.showText("- Migrated database schemas to PostgreSQL, improving search latency by 35%");
                        contentStream.newLine();
                        contentStream.newLine();
                        
                        // Education
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                        contentStream.showText("Education");
                        contentStream.newLine();
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        contentStream.showText("B.S. in Computer Science - University of Washington (2017 - 2021)");
                        contentStream.newLine();
                        contentStream.newLine();
                        
                        // Projects
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                        contentStream.showText("Projects");
                        contentStream.newLine();
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
                        contentStream.showText("E-Commerce Microservices Cluster");
                        contentStream.newLine();
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        contentStream.showText("- Developed Spring Boot REST APIs with Docker Compose and Redis cache");
                        contentStream.newLine();
                        contentStream.newLine();
                        
                        // Certifications
                        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                        contentStream.showText("Certifications");
                        contentStream.newLine();
                        contentStream.setFont(PDType1Font.HELVETICA, 10);
                        contentStream.showText("AWS Certified Solutions Architect Associate");
                        contentStream.newLine();
                        
                        contentStream.endText();
                    }
                    doc.save(pdfFile);
                    System.out.println("Generated sample resume PDF at: " + pdfFile.getAbsolutePath());
                } catch (Exception e) {
                    System.err.println("Could not generate sample resume PDF: " + e.getMessage());
                }
            }
        };
    }
}
