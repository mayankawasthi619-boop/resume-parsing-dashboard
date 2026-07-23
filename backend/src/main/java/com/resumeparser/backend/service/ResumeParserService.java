package com.resumeparser.backend.service;

import com.resumeparser.backend.dto.ResumeData;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ResumeParserService {

    private enum Section {
        NONE, EDUCATION, EXPERIENCE, PROJECTS, SKILLS, CERTIFICATIONS
    }

    private static final List<String> COMMON_TECH_SKILLS = List.of(
        "Java", "Spring Boot", "Spring", "React", "Angular", "Vue", "JavaScript", "TypeScript", 
        "HTML", "CSS", "SQL", "MySQL", "PostgreSQL", "MongoDB", "Oracle", "SQLite", "Redis", 
        "Python", "Django", "Flask", "FastAPI", "C++", "C#", "C", "Go", "Golang", "Rust", 
        "Kotlin", "Swift", "PHP", "Laravel", "Ruby", "Rails", "AWS", "Azure", "GCP", "Docker", 
        "Kubernetes", "Git", "GitHub", "GitLab", "CI/CD", "Jenkins", "Terraform", "Ansible", 
        "Linux", "Unix", "Hibernate", "JPA", "REST API", "GraphQL", "Microservices", "Kafka", 
        "RabbitMQ", "Node.js", "Express", "JUnit", "Mockito", "Selenium", "Maven", "Gradle", 
        "Agile", "Scrum", "Jira", "Figma", "Redux", "Webpack", "Bootstrap", "Tailwind"
    );

    public ResumeData parseResume(MultipartFile file) throws IOException {
        String rawText = extractTextFromPdf(file.getInputStream());
        return parseText(rawText);
    }

    private String extractTextFromPdf(InputStream inputStream) throws IOException {
        try (PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper stripper = new PDFTextStripper();
            // Sort by position to get a more coherent layout
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    public ResumeData parseText(String rawText) {
        if (rawText == null || rawText.trim().isEmpty()) {
            return new ResumeData("Unknown", "N/A", "N/A", List.of(), List.of(), List.of(), List.of(), List.of(), "");
        }

        String[] lines = rawText.split("\\r?\\n");
        List<String> cleanedLines = new ArrayList<>();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                cleanedLines.add(trimmed);
            }
        }

        // 1. Extract Email
        String email = extractEmail(rawText);

        // 2. Extract Phone
        String phone = extractPhone(rawText);

        // 3. Extract Name
        String name = extractName(cleanedLines);

        // 4. Parse Sections
        List<String> education = new ArrayList<>();
        List<String> experience = new ArrayList<>();
        List<String> projects = new ArrayList<>();
        List<String> skills = new ArrayList<>();
        List<String> certifications = new ArrayList<>();

        Section currentSection = Section.NONE;

        for (String line : cleanedLines) {
            String lowerLine = line.toLowerCase();

            if (isHeader(lowerLine, "education", "academic background", "academic history", "academics", "qualifications")) {
                currentSection = Section.EDUCATION;
                continue;
            } else if (isHeader(lowerLine, "experience", "work experience", "employment history", "professional experience", "work history", "career history")) {
                currentSection = Section.EXPERIENCE;
                continue;
            } else if (isHeader(lowerLine, "projects", "personal projects", "academic projects", "key projects")) {
                currentSection = Section.PROJECTS;
                continue;
            } else if (isHeader(lowerLine, "skills", "technical skills", "key skills", "core competencies", "expertise", "languages & tools", "technical expertise")) {
                currentSection = Section.SKILLS;
                continue;
            } else if (isHeader(lowerLine, "certifications", "certificates", "licenses & certifications", "credentials", "licenses")) {
                currentSection = Section.CERTIFICATIONS;
                continue;
            }

            String cleanedLine = cleanBulletPoints(line);
            if (cleanedLine.isEmpty()) {
                continue;
            }

            switch (currentSection) {
                case EDUCATION -> education.add(cleanedLine);
                case EXPERIENCE -> experience.add(cleanedLine);
                case PROJECTS -> projects.add(cleanedLine);
                case SKILLS -> {
                    // Split skills by commas, slashes, pipes, bullets, tabs
                    String[] splitSkills = cleanedLine.split("[,;/|•·\t]");
                    for (String s : splitSkills) {
                        String trimmedSkill = s.trim();
                        // Remove prefix labels like "Languages:" or "Backend:"
                        if (trimmedSkill.contains(":")) {
                            String[] parts = trimmedSkill.split(":");
                            if (parts.length > 1) {
                                trimmedSkill = parts[1].trim();
                            }
                        }
                        if (!trimmedSkill.isEmpty() && trimmedSkill.length() < 30) {
                            skills.add(trimmedSkill);
                        }
                    }
                }
                case CERTIFICATIONS -> certifications.add(cleanedLine);
                default -> {
                    // Do nothing for header meta
                }
            }
        }

        // 5. Keyword Scanning for Skills (augmentation)
        for (String skill : COMMON_TECH_SKILLS) {
            String regex = "\\b" + Pattern.quote(skill) + "\\b";
            Pattern pattern = skill.length() <= 1 
                ? Pattern.compile(regex) 
                : Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(rawText).find()) {
                boolean alreadyExists = false;
                for (String existing : skills) {
                    if (existing.equalsIgnoreCase(skill)) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists) {
                    skills.add(skill);
                }
            }
        }

        return new ResumeData(
            name,
            email,
            phone,
            skills,
            education,
            experience,
            projects,
            certifications,
            rawText
        );
    }

    private String extractEmail(String text) {
        Pattern emailPattern = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}\\b");
        Matcher matcher = emailPattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "Not Found";
    }

    private String extractPhone(String text) {
        // Match standard format: (123) 456-7890, 123-456-7890, +1 123-456-7890, etc.
        Pattern phonePattern = Pattern.compile(
            "(?:\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}"
        );
        Matcher matcher = phonePattern.matcher(text);
        if (matcher.find()) {
            return matcher.group();
        }
        return "Not Found";
    }

    private String extractName(List<String> lines) {
        // Name is usually the first non-empty text line
        // Avoid common noise: files with headers, email addresses, phone numbers
        for (int i = 0; i < Math.min(lines.size(), 10); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            
            // Skip contact info
            if (line.contains("@") || line.matches(".*\\d{5,}.*")) continue; // Skip email and lines with long numbers (like phone or address)
            
            String lower = line.toLowerCase();
            // Skip common section headers or metadata keyword
            if (lower.contains("resume") || lower.contains("cv") || lower.contains("curriculum") || 
                lower.contains("portfolio") || lower.contains("profile") || lower.contains("contact") ||
                lower.contains("email") || lower.contains("phone") || lower.contains("address") ||
                lower.contains("page") || lower.contains("page 1") || lower.contains("git") ||
                lower.contains("http") || lower.contains("linkedin") || lower.contains("github")) {
                continue;
            }

            // Clean the name (remove any extra titles or characters)
            String cleaned = line.replaceAll("^[^a-zA-Z]+", "") // remove leading non-alphabetic characters
                                 .replaceAll("\\s*\\|.*", "") // remove trailing pipe sections
                                 .trim();
            
            // Check if name has at least a few letters and is relatively short (likely a name)
            if (cleaned.length() > 2 && cleaned.length() < 40 && cleaned.split("\\s+").length <= 4) {
                return cleaned;
            }
        }
        return "Unknown Candidate";
    }

    private boolean isHeader(String lowerLine, String... keywords) {
        if (lowerLine.length() > 35) return false;
        
        // Strip numbers and bullet characters before comparison
        String normalized = lowerLine.replaceAll("^[^a-z]+", "").trim();
        if (normalized.endsWith(":")) {
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }

        for (String keyword : keywords) {
            if (normalized.equals(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String cleanBulletPoints(String text) {
        // Remove leading bullets and numbering (e.g. "1. ", "- ", "• ")
        return text.replaceAll("^\\s*[-•*▪◦■➔✓|•·+]\\s*", "")
                   .replaceAll("^\\s*\\d+\\.\\s+", "")
                   .trim();
    }
}
