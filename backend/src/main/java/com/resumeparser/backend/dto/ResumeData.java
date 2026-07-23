package com.resumeparser.backend.dto;

import java.util.List;

public record ResumeData(
    String name,
    String email,
    String phone,
    List<String> skills,
    List<String> education,
    List<String> experience,
    List<String> projects,
    List<String> certifications,
    String rawText
) {}
