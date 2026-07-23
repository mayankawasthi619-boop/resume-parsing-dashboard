# ResumeParser Dashboard 🚀

An intelligence-driven resume parsing system built with **Spring Boot** and **React**. It extracts key structural parameters from PDF resumes in real-time using custom heuristics, presenting them through a premium glassmorphic dashboard interface and a formatted JSON structure.

---

## 🌟 Key Features

*   **Dynamic Drag & Drop Upload**: A sleek, dash-bordered dropzone that supports dragging PDF resumes directly into the browser.
*   **Intelligent Extraction Heuristics**:
    *   **Contact Info**: Extracted via high-accuracy regex patterns for Email and Phone numbers.
    *   **Candidate Name**: Extracted using layout-based heuristics filtering (excluding contact info and common keywords).
    *   **Core Technical Skills**: Extracted from a designated skills block and augmented by an automated keyword scanner scanning for 70+ technical skill strings (Java, React, Docker, AWS, etc.).
    *   **Timelines**: Section-parsing filters that map Education, Work Experience, Projects, and Certifications into clean timeline elements.
*   **Dual-View Presentation**:
    *   **Interactive Profile**: A modern dashboard view organizing candidate stats into cards, tags, and timeline lists with quick copy badges.
    *   **Raw JSON Structure**: A dark-terminal styled JSON syntax display with copy-to-clipboard and file download options.
*   **In-Memory Session History**: Toggle instantly between recently parsed CVs during the active session.
*   **Responsive Dark Tech Theme**: Beautifully crafted from scratch with Vanilla CSS variables, glassmorphic blur filters, custom scrollbars, and key micro-animations.

---

## 🛠️ Technology Stack

*   **Frontend**: React (Vite), Vanilla CSS, Lucide Icons
*   **Backend**: Spring Boot 3.2.5 (Java 17, Maven), Apache PDFBox 2.0.31
*   **Database**: In-memory caching (history list)

---

## 📂 Project Architecture

```text
Resume Parsing Dashboard/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/resumeparser/backend/
│   │   │   │   ├── controller/ResumeParserController.java
│   │   │   │   ├── dto/ResumeData.java
│   │   │   │   ├── service/ResumeParserService.java
│   │   │   │   └── BackendApplication.java
│   │   │   └── resources/application.properties
│   │   └── test/java/com/resumeparser/backend/ResumeParserServiceTest.java
│   └── pom.xml
├── frontend/
│   ├── src/
│   │   ├── App.css
│   │   ├── App.jsx
│   │   ├── index.css
│   │   └── main.jsx
│   ├── index.html
│   └── package.json
└── .gitignore
```

---

## 🚀 Getting Started

### Prerequisites
- **Java**: JDK 17 or higher
- **Maven**: 3.8+
- **Node.js**: 18+ and `npm`

---

### Step 1: Run the Backend Server
1.  Navigate into the `backend` directory:
    ```bash
    cd backend
    ```
2.  Build and boot the Spring Boot application:
    ```bash
    mvn spring-boot:run
    ```
3.  The server will start listening on **[http://localhost:8080](http://localhost:8080)**.

> [!TIP]
> On boot, a mock resume named `sample_resume.pdf` is automatically generated in the backend directory. You can use this file immediately to test the parsing engine!

---

### Step 2: Run the Frontend App
1.  Navigate into the `frontend` directory:
    ```bash
    cd frontend
    ```
2.  Install dependencies:
    ```bash
    npm install
    ```
3.  Launch the Vite development server:
    ```bash
    npm run dev
    ```
4.  Open your browser and navigate to **[http://localhost:5173](http://localhost:5173)** to access the dashboard.

---

## 🧪 Running Tests
To run the backend heuristics tests, execute the following command from the `backend/` directory:
```bash
mvn test
```

---

## 📡 REST API Specifications

### 1. Parse Resume Document
-   **Endpoint**: `POST /api/resume/parse`
-   **Consumes**: `multipart/form-data`
-   **Payload**: `file` (PDF file)
-   **Response Format**: `application/json`
-   **Response Example**:
    ```json
    {
      "name": "John Doe",
      "email": "john.doe@example.com",
      "phone": "(123) 456-7890",
      "skills": ["Java", "Spring Boot", "React", "SQL", "AWS"],
      "education": ["B.S. in Computer Science - University of Washington (2017 - 2021)"],
      "experience": ["Senior Software Engineer - CloudTech Solutions (2021 - Present)"],
      "projects": ["E-Commerce Microservices Cluster"],
      "certifications": ["AWS Certified Solutions Architect Associate"],
      "rawText": "..."
    }
    ```

### 2. Fetch Session History
-   **Endpoint**: `GET /api/resume/history`
-   **Response Format**: `application/json` (list of parsed profiles)
