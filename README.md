<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# MedTime - Smart Medical Reminder & Healthcare Management App
### Powered by Jetpack Compose, Room SQLite & Supabase PostgreSQL

MedTime is an intelligent medical reminder and healthcare coordination platform featuring real-time medicine schedules, dose adherence tracking, doctor appointment scheduling, caretaker-patient linking, medical document vaults, and an AI health assistant.

---

## ⚡ Supabase PostgreSQL Database Setup

MedTime connects to **Supabase PostgreSQL** for cloud persistence, multi-device sync, and backup while keeping Room SQLite as the offline-first local cache.

### Step 1: Create a Free Supabase Project
1. Go to [supabase.com](https://supabase.com) and create an account or sign in.
2. Click **New Project**, choose an organization, set a project name (e.g., `MedTime`), database password, and region.

### Step 2: Initialize Database Schema in Supabase
1. In your Supabase Project Dashboard, navigate to the **SQL Editor** tab (terminal icon on left sidebar).
2. Click **New query**.
3. Copy the entire contents of [`supabase_schema.sql`](./supabase_schema.sql) and paste it into the editor.
4. Click **Run** (or `Ctrl+Enter`).
   - This creates all **16 healthcare tables** (`users`, `medicines`, `medications`, `medicine_reminders`, `medicine_history`, `appointments`, `messages`, `caretaker_links`, `medical_documents`, `notifications`, `audit_logs`, `announcements`, `patient_saved_addresses`, `caretaker_assistance_requests`, `wallets`, `wallet_transactions`).
   - Sets up indexes and Row Level Security (RLS) policies.
   - Populates initial healthcare seed data matching the demo accounts (Vijay Kumar, Dr. Sarah Mitchell, appointments, reminders).

### Step 3: Configure Project Credentials
You can connect the Android app to Supabase in **either of two easy ways**:

#### Option A: Via `.env` file (Recommended for development)
1. In your Supabase Dashboard, go to **Project Settings** -> **API**.
2. Copy your **Project URL** and **anon / public key**.
3. Open or create the `.env` file in the root of this project:
   ```env
   GEMINI_API_KEY=your_gemini_api_key
   SUPABASE_URL=https://your-project-id.supabase.co
   SUPABASE_ANON_KEY=eyJhbGciOi...
   ```
4. Build or rebuild the project in Android Studio.

#### Option B: Directly from the App UI (Zero rebuilds)
1. Run the MedTime app on your device or emulator.
2. Open **Settings** -> Scroll down to **Supabase PostgreSQL Database**.
3. Tap **Configure URL & Anon Key**.
4. Paste your Project URL and Anon Key and tap **Save & Connect**.
5. Tap **Test Ping** to verify connectivity with a live HTTP ping!
6. Tap **Sync Now** to immediately synchronize records with your cloud PostgreSQL database.

---

## 📱 Run Locally

**Prerequisites:** [Android Studio](https://developer.android.com/studio) (Koala / Ladybug or newer with JDK 17/21).

1. Open Android Studio.
2. Select **Open** and choose the directory containing this project (`remix-remix-medtime (1)`).
3. Allow Android Studio to sync Gradle and download dependencies.
4. Set up `.env` with your `GEMINI_API_KEY`, `SUPABASE_URL`, and `SUPABASE_ANON_KEY`.
5. Remove this line from `app/build.gradle.kts` if you do not have the custom debug keystore: `signingConfig = signingConfigs.getByName("debugConfig")`.
6. Click **Run** on an emulator or physical Android device.

---

## 🗄️ Database Architecture

MedTime uses a **hybrid offline-first synchronization engine**:
- **Room SQLite (`AppDatabase`)**: Local fast-response single source of truth for all Jetpack Compose UI flows.
- **Supabase PostgreSQL via PostgREST**: Cloud relational database hosting all tables with RLS and conflict-free bulk upserts (`Prefer: resolution=merge-duplicates`).
- **WorkManager (`SupabaseSyncWorker`)**: Automatically performs two-way sync in the background whenever the device is connected to the internet.
- **Settings Screen Sync Controls**: Manual "Test Ping" and "Sync Now" triggers with live status badges.
