package com.resumeparser.backend;

import com.resumeparser.backend.dto.ResumeData;
import com.resumeparser.backend.service.ResumeParserService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ResumeParserServiceTest {

    private final ResumeParserService service = new ResumeParserService();

    @Test
    public void testParseText() {
        String mockResumeText = "Jane Doe\n" +
                "jane.doe@example.com | (555) 123-4567 | San Francisco, CA\n\n" +
                "Professional Summary\n" +
                "Experienced software engineer specializing in backend systems.\n\n" +
                "Skills\n" +
                "Java, Spring Boot, React, SQL, AWS, Git\n\n" +
                "Experience\n" +
                "Senior Software Engineer - Tech Solutions (2022 - Present)\n" +
                "- Built microservices using Spring Boot and Java\n" +
                "- Designed database schemas in PostgreSQL\n" +
                "- Led a team of 4 developers\n\n" +
                "Education\n" +
                "B.S. in Computer Science - State University (2018 - 2022)\n\n" +
                "Projects\n" +
                "E-commerce Platform\n" +
                "- Developed a scalable e-commerce website using React and Spring Boot\n\n" +
                "Certifications\n" +
                "AWS Certified Solutions Architect\n";

        ResumeData data = service.parseText(mockResumeText);

        assertEquals("Jane Doe", data.name());
        assertEquals("jane.doe@example.com", data.email());
        assertEquals("(555) 123-4567", data.phone());
        
        // Check skills (including exact and keyword scanning)
        assertTrue(data.skills().stream().anyMatch(s -> s.equalsIgnoreCase("Java")));
        assertTrue(data.skills().stream().anyMatch(s -> s.equalsIgnoreCase("Spring Boot")));
        assertTrue(data.skills().stream().anyMatch(s -> s.equalsIgnoreCase("React")));
        assertTrue(data.skills().stream().anyMatch(s -> s.equalsIgnoreCase("SQL")));
        
        // Check education
        assertFalse(data.education().isEmpty());
        assertTrue(data.education().get(0).contains("B.S. in Computer Science"));
        
        // Check experience
        assertFalse(data.experience().isEmpty());
        assertTrue(data.experience().get(0).contains("Senior Software Engineer"));
        
        // Check projects
        assertFalse(data.projects().isEmpty());
        assertTrue(data.projects().get(0).contains("E-commerce Platform"));
        
        // Check certifications
        assertFalse(data.certifications().isEmpty());
        assertTrue(data.certifications().get(0).contains("AWS Certified Solutions Architect"));
    }
}
