# 🚀 SamadhanX — Judgment Day Runbook & Operations Guide

> **Target Time**: 7:00 AM Judgment Demo  
> **Environment**: Windows 11 / Local Development Suite  
> **Backend Port**: `8088` | **Web Port**: `5173` | **Apache/System Port**: `8080` (Preserved)

---

## 📋 1. Startup Sequence (Order of Execution)

Execute these 3 terminal tabs in order before the judging session starts:

### Tab 1 — Spring Boot Backend (Port 8088)
```powershell
cd c:\Users\LENOVO\OneDrive\Desktop\samadhanX\backend
.\maven\apache-maven-3.9.9\bin\mvn.cmd spring-boot:run
```
*Wait until logs display: `Started SamadhanXBackendApplication in X.XXX seconds` (on port 8088).*

### Tab 2 — React / Vite Web Command Portal (Port 5173)
```powershell
cd c:\Users\LENOVO\OneDrive\Desktop\samadhanX\web
npm run dev
```
*Accessible at: `http://localhost:5173`*

### Tab 3 (Optional) — Flutter Mobile Citizen App
```powershell
cd c:\Users\LENOVO\OneDrive\Desktop\samadhanX\mobile
flutter run -d <device-or-emulator-id>
```

---

## ⚡ 2. One-Click Demo Reset & Seeder

If you want to reset the ecosystem to a clean, predictable state with Varanasi and Chandauli testbeds:

### Method A — Via Web UI (Recommended)
1. Open `http://localhost:5173`.
2. Click the floating **"JUDGMENT DEMO COPILOT"** button in the bottom right corner.
3. Click **"Reset & Seed Demo Data"**.

### Method B — Via PowerShell Terminal
```powershell
Invoke-RestMethod -Uri "http://localhost:8088/api/v1/demo/reset-and-seed" -Method POST
```

---

## 🔑 3. Pre-Seeded Demo Accounts (Password for all: `Password@123`)

| Role | Email | Use Case in Demo |
|---|---|---|
| **Citizen** | `citizen@samadhanx.org` | Mobile challenge reporting, tracking status, in-app notifications. |
| **Government Admin** | `admin@samadhanx.gov.in` | Executive Command Center, national escalations, approving tasks. |
| **Government Official** | `official@samadhanx.gov.in` | PWD division triage, SLA tracking, department resolution. |
| **IIT BHU Faculty** | `faculty@iitbhu.ac.in` | Academic mentoring, laboratory resource allocation. |
| **Student Innovator** | `student@iitbhu.ac.in` | Submitting R&D proposals (`PRP-2026-08-001`), prototype milestones. |
| **CSR Funder** | `csr@tatatrusts.org` | Tata Trusts CSR grant commitments, tranche disbursements. |
| **DST Evaluator** | `evaluator@dst.gov.in` | Scoring proposals on TRL feasibility, methodology, and social impact. |

---

## 🌐 4. Critical URLs & Portals

- **Web Command Portal**: [http://localhost:5173](http://localhost:5173)
- **Government Command Center**: [http://localhost:5173/government](http://localhost:5173/government)
- **Geospatial GIS Map**: [http://localhost:5173/government/gis-map](http://localhost:5173/government/gis-map)
- **University Innovation Hub**: [http://localhost:5173/innovation](http://localhost:5173/innovation)
- **Industry & CSR Matching**: [http://localhost:5173/industry](http://localhost:5173/industry)
- **Governance Action Center**: [http://localhost:5173/action-center](http://localhost:5173/action-center)
- **National Impact Analytics**: [http://localhost:5173/analytics](http://localhost:5173/analytics)
- **Backend Swagger API Docs**: [http://localhost:8088/swagger-ui/index.html](http://localhost:8088/swagger-ui/index.html)

---

## ⚠️ 5. What NOT to Touch Immediately Before Judging

- ❌ **Do NOT change backend port** from `8088` (preserves Apache 8080).
- ❌ **Do NOT start PostgreSQL migration** (in-memory H2 profile guarantees zero-friction reliability).
- ❌ **Do NOT alter Firebase service account JSON** permissions.
- ❌ **Do NOT delete the target/dist directories** before running.

---

## 🚨 6. Emergency Live Fallbacks

| Scenario | Behavior / Fallback |
|---|---|
| **No Internet / Gemini AI API Timeout** | System automatically switches to the built-in **Deterministic Fallback Provider**—all confidence scores, priority weights, and R&D blueprints continue working without interruption. |
| **FCM Push Notification Service Unreachable** | In-app notification records and live web navbar notification badges continue to update automatically via polling. |
| **Database or State Inconsistency** | Click **"Reset & Seed Demo Data"** on the Presenter Assistant panel to restore clean state in < 1 second. |
