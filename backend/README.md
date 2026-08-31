# SamadhanX — Backend Ecosystem (Milestones 1, 2, 3 & 4 Completed)

> **Societal Challenge Crowdsourcing and Collaborative Problem-Solving Ecosystem**
> *(Built for SIH Problem Statement 26043)*

SamadhanX is a unified, scalable platform connecting Citizens, Panchayati Raj Institutions (PRIs), Urban Local Bodies (ULBs), Government Departments, Universities, Research Laboratories, Startups, MSMEs, and CSR Partners to crowdsource societal challenges and build measurable real-world solutions.

This repository hosts the **Spring Boot Modular Monolith Backend** — the single source of truth providing unified REST APIs for all client portals:
1. **Citizen & Community Mobile App** (Flutter)
2. **Government Portal** (React/Next.js)
3. **University Portal** (React/Next.js)
4. **Industry & Partner Portal** (React/Next.js)
5. **Super Admin Portal** (React/Next.js)

---

## 🛠️ Technology Stack

- **Language**: Java 17+ (Java 21 LTS compatible)
- **Framework**: Spring Boot 3.3.4
- **Security**: Spring Security 6 + Stateless JWT (JJWT 0.12.6) + BCrypt (strength 12)
- **Database**: PostgreSQL 16
- **ORM / Persistence**: Spring Data JPA / Hibernate
- **Validation**: Jakarta Bean Validation (Hibernate Validator)
- **API Documentation**: Springdoc OpenAPI 3 / Swagger UI
- **Build Tool**: Apache Maven
- **Containerization**: Docker Compose (PostgreSQL 16)
- **Testing**: JUnit 5, Mockito, Spring Boot Test, H2 (test scope)

---

## 📁 Modular Monolith Architecture

```
backend/
├── pom.xml                                  # Maven dependencies and build configuration
├── docker-compose.yml                       # Local PostgreSQL 16 service
├── README.md                                # This document
└── src/
    ├── main/
    │   ├── java/com/samadhanx/
    │   │   ├── SamadhanXApplication.java   # Spring Boot entry point
    │   │   ├── config/                      # Security & JPA Auditing & OpenAPI configs
    │   │   ├── common/                      # Common BaseAuditEntity, ApiResponse, PageResponse, GlobalExceptionHandler
    │   │   ├── infrastructure/              # JWT filter, custom UserDetailsService, DataInitializer
    │   │   └── module/
    │   │       ├── auth/                    # Authentication & JWT issuance (M1)
    │   │       ├── user/                    # User identity & profile management (M1)
    │   │       ├── role/                    # Role definitions (14 platform roles) (M1)
    │   │       ├── organization/            # Institutional Ecosystem (M2)
    │   │       ├── challenge/               # Challenge Lifecycle & AI Engines (M3)
    │   │       └── solution/                # Solution Proposals, Teams & Hackathons (M4)
    │   │           ├── controller/          # Team, Proposal, Hackathon, Dashboard controllers
    │   │           ├── dto/                 # Request & Response DTOs
    │   │           ├── entity/              # Team, TeamMember, Proposal, ProposalEvaluation, Hackathon
    │   │           ├── repository/          # Spring Data JPA repositories
    │   │           └── service/             # TeamService, ProposalService, EvaluationService, HackathonService
    │   └── resources/
    │       ├── application.yml & application-dev.yml
    │       └── db/schema.sql & data.sql
    └── test/
        └── java/com/samadhanx/              # 42 automated tests (100% pass rate)
```

---

## 🏛️ Ecosystem Subsystems Implemented

### 1. Solution Development, Teams & Hackathons (Milestone 4)
- **Multidisciplinary Project Teams**: Cross-departmental and inter-university team formation (`TEAM_LEAD`, `FACULTY_MENTOR`, `STUDENT`, `RESEARCHER`) with invitations and acceptance workflow.
- **R&D Solution Proposals**: Submissions linked to innovation challenges with technical approach, expected impact, budget in INR, sustainability model, and CAD/document attachments.
- **Guarded Proposal Lifecycle**: `PROPOSED` → `UNDER_REVIEW` → `SHORTLISTED` → `PROTOTYPING` → `PILOT_READY` (or `REJECTED`).
- **Multi-Dimensional Evaluation Scorecard Engine**: 8-dimension weighted scorecard (Innovation 20%, Feasibility 20%, Impact 15%, Problem Understanding 10%, Scalability 10%, Cost 10%, Readiness 10%, Sustainability 5%) with transparent explainability (*"Why this proposal scored X/100"*) and conflict-of-interest prevention.
- **Hackathon & Problem Statement Challenge Mode**: Competition publishing (`SMX-HACK-...`), jury assignment, challenge mapping, and milestone countdowns.
- **Role-Specific Dashboards**: Personalized metrics for University Admin, Faculty, Student, and Evaluator.

### 2. Challenge Crowdsourcing & AI Pipeline (Milestone 3)
- **Geotagged Crowdsourcing**: GIS coordinates, jurisdiction level, and multimedia evidence attachments.
- **Free-First AI Categorization**: Rule-based taxonomy keyword extraction predicting domains with confidence scoring.
- **Priority Scoring Engine**: Multi-factor weighted formula $(\text{Severity} \times 0.35 + \text{Urgency} \times 0.25 + \text{Population} \times 0.25 + \text{Endorsements} \times 0.15)$.
- **Spatial Deduplication Engine**: Haversine distance ($\le 2.0\text{ km}$) + Jaccard token similarity clustering.
- **Automated Department Routing & Dual Resolution**: Routing to local government departments with standard repair vs `INNOVATION_REQUIRED` escalation.

### 3. Institutional Ecosystem & Verification (Milestone 2)
- Multi-tier Government Departments, Higher Education Institutions, Faculty research profiles, Lab facilities, and Startups/MSMEs with auditable verification lifecycle.

### 4. Authentication & Security Foundation (Milestone 1)
- Stateless JWT authentication filter with 14 platform roles.

---

## 🚀 Getting Started Locally

### Step 1: Start PostgreSQL
```powershell
cd backend
docker-compose up -d
```

### Step 2: Run Application
```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
- Base API URL: **`http://localhost:8080`**
- Interactive Swagger UI: **`http://localhost:8080/swagger-ui.html`**

### Step 3: Run Automated Tests
```powershell
mvn clean test
```
**Test Results: 42 passed, 0 failures, 0 errors (100% pass rate).**

---

## 🗺️ Roadmap & Milestones

- **Milestone 1**: Backend Foundation, JWT Security, Users & Roles ✅
- **Milestone 2**: Organization & Institutional Ecosystem (Gov, Univ, Industry, Verification) ✅
- **Milestone 3**: Challenge Crowdsourcing Lifecycle, AI Engines, Department Triage & Academic Pipeline ✅
- **Milestone 4**: Solution Proposals, Multidisciplinary Teams, Evaluation Scorecards & Hackathons ✅
- **Milestone 5**: Industry/CSR Mentorship, Grant Allocation & Field Pilot Impact Tracking
- **Milestone 6**: Client Portals (Flutter Mobile App + Web Portals)
