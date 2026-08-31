# SamadhanX — Unified Web Portal & Command Center

Welcome to the **SamadhanX Unified Web Application**, built for the Smart India Hackathon (SIH Problem Statement 26043).

This portal brings together **Government Officials**, **University Researchers & Faculty**, and **Industry/CSR Sponsors** into a cohesive GovTech command center.

---

## 🌟 Portals & Core Modules

1. **Government Command Center (`/government`)**:
   - Live queue of citizen crowdsourced societal challenges.
   - AI categorization confidence, duplicate detection indicators, and multi-factor priority score (0–100).
   - Triage actions: 1-click standard departmental resolution or escalation to university innovation pipeline.

2. **National GIS Geo-Map (`/map`)**:
   - Interactive Leaflet map displaying societal challenges across Indian districts and states.
   - Priority-coded marker pins (Critical, High, Medium, Low).
   - Filter by domain, priority, and jurisdiction.

3. **University Innovation Hub (`/innovation`)**:
   - Discover challenges escalated to `INNOVATION_REQUIRED`.
   - Multidisciplinary team formation (Faculty Mentors + Student Leads + External Researchers).
   - Solution Proposal submission with TRL progression (Concept → Prototyping → Pilot Ready).

4. **Industry, MSME & CSR Collaboration (`/partnerships`)**:
   - Explainable AI-driven partner matching with transparent match factors and capability gap diagnostics.
   - CSR grant disbursements and milestone management.
   - Real-world pilot deployments and live IoT impact telemetry.

5. **National Impact Analytics Command Center (`/analytics`)**:
   - High-impact visual analytics (Chart.js): domain breakdown, priority distribution, resolution velocity.
   - Real-world outcome counters: Potable Water Treated (L), Clean Energy Generated (kWh), Landfill Waste Diverted (kg), and Beneficiaries Impacted.

---

## 🚀 Running the Web Portal

### 1. Prerequisites
- Node.js v18+ (v20+ recommended)
- Spring Boot Backend running on `http://localhost:8088`

### 2. Install & Start Development Server
```bash
cd web
npm install
npm run dev
```
> Web Portal launches on **`http://localhost:5173`** (or configured Vite port).

### 3. Production Build
```bash
npm run build
npm run preview
```

---

## 🔑 1-Click Demo Accounts

| Role | Email | Password | Primary Portal |
|---|---|---|---|
| **Government Admin** | `admin@samadhanx.gov.in` | `Admin@123456` | Government Command Center & Full Oversight |
| **Department Official** | `official@samadhanx.gov.in` | `Official@123456` | Challenge Triage & Department Resolution |
| **University Faculty Lead** | `faculty@samadhanx.gov.in` | `Faculty@123456` | Innovation Hub & Solution Proposals |
| **Industry / CSR Lead** | `industry@samadhanx.gov.in` | `Industry@123456` | Partner Matching & CSR Grant Deployment |
| **Citizen** | `citizen@samadhanx.gov.in` | `Citizen@123456` | Mobile Citizen App |
