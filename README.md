# SamadhanX: Societal Challenge Crowdsourcing & Collaborative Problem-Solving Ecosystem

**Team Name**: VisionX_26  
**Architecture**: 100% Supabase Native (Auth, PostgreSQL RLS, RPCs, Storage, Realtime, Gemini Edge Functions) + Flutter Mobile App + React/Vite/TypeScript Web Portal

---

## 🏛️ Ecosystem Overview

SamadhanX connects:
```
Citizens
  ↓
Societal Problems / Challenges
  ↓
Government / Departments
  ↓
Universities / Faculty / Students
  ↓
Startups / MSMEs / Industry
  ↓
Research Labs / Innovation Hubs
  ↓
CSR / Funding Partners
  ↓
Prototype / Pilot
  ↓
Implementation
  ↓
Impact Tracking
```

---

## 📂 Architecture Structure

```
/samadhanX
  /web           # React + Vite + TypeScript + Tailwind CSS Ecosystem Command Portal
  /mobile        # Flutter Mobile Application for Citizens & Community
  /supabase      # Pure Supabase Architecture
    /config.toml # Local Supabase dev config
    /migrations  # Idempotent database migrations (Schema, RPCs, RLS, Storage)
      001_initial_schema.sql
      002_functions_and_triggers.sql
      003_rls_policies.sql
      004_storage_policies.sql
    /seed        # Seed data (14 roles, 8 societal domains)
      seed.sql
    /functions   # Supabase Edge Functions with Gemini AI (Server-Side)
      ai-categorize-challenge/index.ts
      ai-summarize-challenge/index.ts
      ai-match-partners/index.ts
  README.md
```

---

## 🔒 Security & Authorization

- **Supabase Auth**: Strict email/password auth with automatic profile generation and role enforcement triggers.
- **Row Level Security (RLS)**: Enforced across all user-accessible tables.
- **Helper Functions**: `public.get_user_roles()`, `public.has_role()`, `public.is_admin()`, `public.is_verified_user()`.
- **Zero Secret Leakage**: Service role keys and Gemini API keys are confined exclusively to Supabase Edge Functions and the server environment.

---

## 🚀 Getting Started

### 1. Web Portal (`/web`)
```bash
cd web
npm install
npm run build   # Production build
npm run dev     # Local development server
```

### 2. Flutter Mobile Application (`/mobile`)
```bash
cd mobile
flutter pub get
flutter analyze # Verify zero lint errors
flutter test    # Run unit & widget test suite
flutter run     # Run on simulator or device
```

### 3. Supabase Migrations & Functions (`/supabase`)
```bash
supabase db push
supabase functions deploy ai-categorize-challenge
supabase functions deploy ai-summarize-challenge
supabase functions deploy ai-match-partners
```

---

## 🌐 Environment Configuration

### Web (`web/.env`)
```env
VITE_SUPABASE_URL=https://<your-project-id>.supabase.co
VITE_SUPABASE_PUBLISHABLE_KEY=<your-publishable-key>
```

### Mobile
Build-time environment parameters:
```bash
flutter build apk --dart-define=SUPABASE_URL=https://<your-project-id>.supabase.co --dart-define=SUPABASE_PUBLISHABLE_KEY=<your-publishable-key>
```
