-- ==============================================================================
-- MedTime - Supabase PostgreSQL Database Schema
-- Complete relational schema, indexes, RLS policies, and healthcare seed data
-- Compatible with Supabase SQL Editor and PostgREST API
-- ==============================================================================

-- Enable UUID extension if not already enabled
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ------------------------------------------------------------------------------
-- 1. TABLE: users
-- Core user entity supporting Patients, Doctors, Caretakers, and Admins
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.users (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE,
    role TEXT NOT NULL CHECK (role IN ('PATIENT', 'DOCTOR', 'CARETAKER', 'ADMIN')),
    phone TEXT DEFAULT '',
    blood_group TEXT DEFAULT 'O+',
    allergies TEXT DEFAULT '',
    emergency_contact_name TEXT DEFAULT '',
    emergency_contact_phone TEXT DEFAULT '',
    emergency_contact_relation TEXT DEFAULT '',
    is_doctor_verified BOOLEAN DEFAULT FALSE,
    doctor_verification_status TEXT DEFAULT 'APPROVED' CHECK (doctor_verification_status IN ('PENDING', 'UNDER_REVIEW', 'APPROVED', 'REJECTED')),
    doctor_specialty TEXT DEFAULT '',
    doctor_hospital TEXT DEFAULT '',
    doctor_license TEXT DEFAULT '',
    doctor_license_image_url TEXT DEFAULT '',
    doctor_profile_photo_url TEXT DEFAULT '',
    doctor_issuing_council TEXT DEFAULT 'State Medical Council',
    doctor_years_experience INTEGER DEFAULT 8,
    doctor_verification_submitted_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    doctor_bio TEXT DEFAULT '',
    caretaker_linking_code TEXT DEFAULT '',
    password TEXT DEFAULT 'password123',
    date_of_birth TEXT DEFAULT '1985-05-12',
    address TEXT DEFAULT '',
    latitude DOUBLE PRECISION DEFAULT 12.9716,
    longitude DOUBLE PRECISION DEFAULT 77.5946,
    auth_provider TEXT DEFAULT 'LOCAL' CHECK (auth_provider IN ('LOCAL', 'GOOGLE', 'FACEBOOK', 'APPLE')),
    provider_user_id TEXT DEFAULT '',
    profile_photo_url TEXT DEFAULT '',
    is_email_verified BOOLEAN DEFAULT FALSE,
    account_status TEXT DEFAULT 'ACTIVE' CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'PROFILE_INCOMPLETE')),
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_users_role ON public.users(role);
CREATE INDEX IF NOT EXISTS idx_users_email ON public.users(email);
CREATE INDEX IF NOT EXISTS idx_users_caretaker_code ON public.users(caretaker_linking_code);

-- ------------------------------------------------------------------------------
-- 2. TABLE: medicines
-- Prescription and OTC medication catalog with dosage, schedule, and stock levels
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.medicines (
    id TEXT PRIMARY KEY,
    patient_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    name TEXT NOT NULL,
    dosage TEXT NOT NULL,
    form TEXT NOT NULL DEFAULT 'Tablet',
    instructions TEXT NOT NULL DEFAULT 'Take with water',
    frequency TEXT NOT NULL DEFAULT 'Daily',
    reminder_times TEXT NOT NULL DEFAULT '08:00 AM',
    start_date TEXT NOT NULL,
    end_date TEXT NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 30,
    low_stock_threshold INTEGER NOT NULL DEFAULT 6,
    notes TEXT DEFAULT '',
    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'PAUSED', 'COMPLETED')),
    color_hex BIGINT DEFAULT 1402304, -- 0xFF1565C0
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_medicines_patient ON public.medicines(patient_id);
CREATE INDEX IF NOT EXISTS idx_medicines_status ON public.medicines(status);

-- ------------------------------------------------------------------------------
-- 3. TABLE: medications
-- Additional / legacy calendar medication schedules
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.medications (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    dosage TEXT NOT NULL,
    frequency TEXT NOT NULL,
    time_slots TEXT NOT NULL,
    form TEXT DEFAULT 'Tablet',
    instructions TEXT DEFAULT 'Take with water',
    start_date TEXT DEFAULT '',
    end_date TEXT DEFAULT '',
    stock_quantity INTEGER DEFAULT 30,
    notes TEXT DEFAULT '',
    is_active BOOLEAN DEFAULT TRUE,
    patient_id TEXT DEFAULT '',
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_medications_patient ON public.medications(patient_id);

-- ------------------------------------------------------------------------------
-- 4. TABLE: medicine_reminders
-- Daily and scheduled dose alerts with intake status and snooze tracking
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.medicine_reminders (
    id TEXT PRIMARY KEY,
    medicine_id TEXT NOT NULL REFERENCES public.medicines(id) ON DELETE CASCADE,
    patient_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    medicine_name TEXT NOT NULL,
    dosage TEXT NOT NULL,
    form TEXT NOT NULL DEFAULT 'Tablet',
    instructions TEXT NOT NULL DEFAULT '',
    scheduled_date TEXT NOT NULL, -- YYYY-MM-DD
    scheduled_time TEXT NOT NULL, -- e.g. "08:00 AM"
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'TAKEN', 'MISSED', 'SKIPPED', 'SNOOZED')),
    taken_at_timestamp BIGINT,
    snooze_until_time TEXT,
    attempt_count INTEGER DEFAULT 0,
    snooze_count INTEGER DEFAULT 0,
    last_alarm_timestamp BIGINT,
    skip_reason TEXT
);

CREATE INDEX IF NOT EXISTS idx_reminders_patient_date ON public.medicine_reminders(patient_id, scheduled_date);
CREATE INDEX IF NOT EXISTS idx_reminders_status ON public.medicine_reminders(status);
CREATE INDEX IF NOT EXISTS idx_reminders_medicine ON public.medicine_reminders(medicine_id);

-- ------------------------------------------------------------------------------
-- 5. TABLE: medicine_history
-- Historical adherence log recording every dose action taken by the patient
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.medicine_history (
    id TEXT PRIMARY KEY,
    medicine_id TEXT NOT NULL,
    patient_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    medicine_name TEXT NOT NULL,
    dosage TEXT NOT NULL,
    scheduled_time TEXT NOT NULL,
    action TEXT NOT NULL CHECK (action IN ('TAKEN', 'SKIPPED', 'MISSED', 'SNOOZED')),
    action_timestamp BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    notes TEXT DEFAULT ''
);

CREATE INDEX IF NOT EXISTS idx_history_patient ON public.medicine_history(patient_id);
CREATE INDEX IF NOT EXISTS idx_history_timestamp ON public.medicine_history(action_timestamp DESC);

-- ------------------------------------------------------------------------------
-- 6. TABLE: appointments
-- Clinical doctor consultations with scheduling, status, and clinical notes
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.appointments (
    id TEXT PRIMARY KEY,
    patient_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    patient_name TEXT NOT NULL,
    doctor_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    doctor_name TEXT NOT NULL,
    doctor_specialty TEXT NOT NULL,
    appointment_date TEXT NOT NULL, -- YYYY-MM-DD
    appointment_time TEXT NOT NULL,
    reason TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'COMPLETED', 'CANCELLED')),
    doctor_notes TEXT DEFAULT '',
    reminder_enabled BOOLEAN DEFAULT TRUE,
    reminder_minutes_before INTEGER DEFAULT 60,
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_appointments_patient ON public.appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_appointments_doctor ON public.appointments(doctor_id);
CREATE INDEX IF NOT EXISTS idx_appointments_date ON public.appointments(appointment_date);

-- ------------------------------------------------------------------------------
-- 7. TABLE: messages
-- Secure in-app doctor, patient, and caretaker clinical communications
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.messages (
    id TEXT PRIMARY KEY,
    conversation_id TEXT NOT NULL,
    sender_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    sender_name TEXT NOT NULL,
    sender_role TEXT NOT NULL,
    receiver_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    receiver_name TEXT NOT NULL,
    content TEXT NOT NULL,
    timestamp BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    is_read BOOLEAN DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_messages_conversation ON public.messages(conversation_id);
CREATE INDEX IF NOT EXISTS idx_messages_sender ON public.messages(sender_id);
CREATE INDEX IF NOT EXISTS idx_messages_receiver ON public.messages(receiver_id);

-- ------------------------------------------------------------------------------
-- 8. TABLE: caretaker_links
-- Patient-caretaker permission-based guardian relationships and linking codes
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.caretaker_links (
    id TEXT PRIMARY KEY,
    patient_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    patient_name TEXT NOT NULL,
    caretaker_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    caretaker_name TEXT NOT NULL,
    linking_code TEXT NOT NULL,
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    can_view_medicines BOOLEAN DEFAULT TRUE,
    can_view_adherence BOOLEAN DEFAULT TRUE,
    can_view_appointments BOOLEAN DEFAULT TRUE,
    can_view_documents BOOLEAN DEFAULT TRUE,
    can_view_location BOOLEAN DEFAULT TRUE,
    can_receive_alerts BOOLEAN DEFAULT TRUE,
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_caretaker_links_patient ON public.caretaker_links(patient_id);
CREATE INDEX IF NOT EXISTS idx_caretaker_links_caretaker ON public.caretaker_links(caretaker_id);

-- ------------------------------------------------------------------------------
-- 9. TABLE: medical_documents
-- Prescriptions, diagnostic reports, and medical file records
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.medical_documents (
    id TEXT PRIMARY KEY,
    patient_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    type TEXT NOT NULL, -- Prescription, Lab Report, Scan, Insurance, Doctor Notes, Discharge Summary
    doctor_or_clinic TEXT NOT NULL,
    date_added TEXT NOT NULL,
    file_size TEXT DEFAULT '1.0 MB',
    file_size_bytes BIGINT DEFAULT 0,
    file_name TEXT DEFAULT '',
    file_format TEXT DEFAULT 'PDF',
    mime_type TEXT DEFAULT 'application/pdf',
    file_uri TEXT DEFAULT '',
    page_count INTEGER DEFAULT 1,
    resolution TEXT DEFAULT '',
    tags TEXT DEFAULT '',
    is_favorite BOOLEAN DEFAULT FALSE,
    is_recycle_bin BOOLEAN DEFAULT FALSE,
    deleted_at BIGINT,
    upload_status TEXT DEFAULT 'COMPLETED',
    notes TEXT DEFAULT '',
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_documents_patient ON public.medical_documents(patient_id);
CREATE INDEX IF NOT EXISTS idx_documents_type ON public.medical_documents(type);

-- ------------------------------------------------------------------------------
-- 10. TABLE: notifications
-- System alerts, emergency SOS, missed dosage, and appointment reminders
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.notifications (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    type TEXT NOT NULL, -- MEDICINE, LOW_STOCK, MISSED_DOSE, EMERGENCY_SOS, APPOINTMENT, CARETAKER, DOCTOR, SYSTEM
    is_read BOOLEAN DEFAULT FALSE,
    timestamp BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    recipient_role TEXT DEFAULT 'PATIENT_AND_CARETAKER',
    recipient_name TEXT DEFAULT '',
    delivery_channels TEXT DEFAULT 'PUSH,SMS',
    severity TEXT DEFAULT 'NORMAL' CHECK (severity IN ('INFO', 'NORMAL', 'WARNING', 'CRITICAL')),
    related_entity_id TEXT DEFAULT '',
    action_taken TEXT DEFAULT ''
);

CREATE INDEX IF NOT EXISTS idx_notifications_user ON public.notifications(user_id);
CREATE INDEX IF NOT EXISTS idx_notifications_unread ON public.notifications(user_id, is_read);

-- ------------------------------------------------------------------------------
-- 11. TABLE: audit_logs
-- HIPAA compliance, user activity tracking, and system audit trail
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.audit_logs (
    id TEXT PRIMARY KEY,
    action TEXT NOT NULL,
    performed_by TEXT NOT NULL,
    target_resource TEXT NOT NULL,
    details TEXT NOT NULL,
    timestamp BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_audit_logs_timestamp ON public.audit_logs(timestamp DESC);

-- ------------------------------------------------------------------------------
-- 12. TABLE: announcements
-- Clinic notices, healthcare alerts, and system broadcasts
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.announcements (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    target_role TEXT DEFAULT 'ALL' CHECK (target_role IN ('ALL', 'PATIENT', 'DOCTOR', 'CARETAKER')),
    priority TEXT DEFAULT 'NORMAL' CHECK (priority IN ('INFO', 'NORMAL', 'URGENT', 'CRITICAL')),
    author_name TEXT DEFAULT 'System Administrator',
    is_active BOOLEAN DEFAULT TRUE,
    expires_at TEXT DEFAULT '',
    timestamp BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- ------------------------------------------------------------------------------
-- 13. TABLE: patient_saved_addresses
-- Patient physical addresses for doctor home visits and caretaker assistance
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.patient_saved_addresses (
    id TEXT PRIMARY KEY,
    patient_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    address_type TEXT DEFAULT 'HOME',
    address_line1 TEXT NOT NULL,
    address_line2 TEXT DEFAULT '',
    city TEXT DEFAULT 'Bengaluru',
    state TEXT DEFAULT 'Karnataka',
    pincode TEXT DEFAULT '560038',
    landmark TEXT DEFAULT '',
    full_address TEXT DEFAULT '',
    latitude DOUBLE PRECISION DEFAULT 12.9716,
    longitude DOUBLE PRECISION DEFAULT 77.5946,
    is_default BOOLEAN DEFAULT FALSE,
    allow_caretaker_visit BOOLEAN DEFAULT TRUE,
    allow_doctor BOOLEAN DEFAULT TRUE,
    is_private BOOLEAN DEFAULT FALSE,
    is_temporary BOOLEAN DEFAULT FALSE,
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_patient_addresses ON public.patient_saved_addresses(patient_id);

-- ------------------------------------------------------------------------------
-- 14. TABLE: caretaker_assistance_requests
-- On-demand caretaker call & visit requests with billing and location tracking
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.caretaker_assistance_requests (
    id TEXT PRIMARY KEY,
    caretaker_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    caretaker_name TEXT NOT NULL,
    caretaker_phone TEXT DEFAULT '',
    patient_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    patient_name TEXT NOT NULL,
    patient_phone TEXT DEFAULT '',
    request_type TEXT NOT NULL, -- CALL or VISIT
    reason TEXT NOT NULL,
    notes TEXT DEFAULT '',
    status TEXT NOT NULL DEFAULT 'PAID_PENDING_ADMIN',
    caretaker_latitude DOUBLE PRECISION,
    caretaker_longitude DOUBLE PRECISION,
    caretaker_location_name TEXT DEFAULT '',
    selected_address_id TEXT DEFAULT '',
    address_snapshot TEXT DEFAULT '',
    patient_address TEXT DEFAULT '',
    patient_latitude DOUBLE PRECISION,
    patient_longitude DOUBLE PRECISION,
    distance_km DOUBLE PRECISION DEFAULT 0.0,
    billable_km INTEGER DEFAULT 3,
    base_charge DOUBLE PRECISION DEFAULT 30.0,
    additional_distance_charge DOUBLE PRECISION DEFAULT 0.0,
    total_visit_charge DOUBLE PRECISION DEFAULT 30.0,
    payment_status TEXT DEFAULT 'UNPAID',
    payment_id TEXT DEFAULT '',
    transaction_id TEXT DEFAULT '',
    paid_amount DOUBLE PRECISION DEFAULT 0.0,
    paid_at BIGINT,
    refund_transaction_id TEXT DEFAULT '',
    refunded_at BIGINT,
    admin_notes TEXT DEFAULT '',
    rejection_reason TEXT DEFAULT '',
    admin_id TEXT DEFAULT '',
    admin_name TEXT DEFAULT '',
    is_emergency BOOLEAN DEFAULT FALSE,
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT,
    updated_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_assistance_caretaker ON public.caretaker_assistance_requests(caretaker_id);
CREATE INDEX IF NOT EXISTS idx_assistance_patient ON public.caretaker_assistance_requests(patient_id);
CREATE INDEX IF NOT EXISTS idx_assistance_status ON public.caretaker_assistance_requests(status);

-- ------------------------------------------------------------------------------
-- 15. TABLE: wallets
-- Caretaker in-app payment wallet for visit earnings and service credits
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.wallets (
    caretaker_id TEXT PRIMARY KEY REFERENCES public.users(id) ON DELETE CASCADE,
    balance DOUBLE PRECISION DEFAULT 450.0,
    max_balance DOUBLE PRECISION DEFAULT 1000.0,
    currency TEXT DEFAULT 'INR',
    updated_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

-- ------------------------------------------------------------------------------
-- 16. TABLE: wallet_transactions
-- Ledger of wallet credits, visit charges, and refund transactions
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.wallet_transactions (
    transaction_id TEXT PRIMARY KEY,
    caretaker_id TEXT NOT NULL REFERENCES public.users(id) ON DELETE CASCADE,
    type TEXT NOT NULL, -- TOP_UP, VISIT_PAYMENT, REFUND
    amount DOUBLE PRECISION NOT NULL,
    balance_before DOUBLE PRECISION NOT NULL,
    balance_after DOUBLE PRECISION NOT NULL,
    status TEXT NOT NULL, -- PENDING, SUCCESS, FAILED, CANCELLED, REFUNDED
    description TEXT NOT NULL,
    payment_provider TEXT DEFAULT 'Razorpay',
    provider_payment_id TEXT DEFAULT '',
    order_id TEXT DEFAULT '',
    related_request_id TEXT DEFAULT '',
    patient_name TEXT DEFAULT '',
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM NOW()) * 1000)::BIGINT
);

CREATE INDEX IF NOT EXISTS idx_wallet_txn_caretaker ON public.wallet_transactions(caretaker_id);

-- ==============================================================================
-- ROW LEVEL SECURITY (RLS) POLICIES
-- Enables open access for authenticated mobile app clients & anon key
-- ==============================================================================

ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.medicines ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.medications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.medicine_reminders ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.medicine_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.appointments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.caretaker_links ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.medical_documents ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.audit_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.announcements ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.patient_saved_addresses ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.caretaker_assistance_requests ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wallets ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.wallet_transactions ENABLE ROW LEVEL SECURITY;

-- Permissive policy for Anon and Authenticated access via Supabase PostgREST
DO $$
DECLARE
    tbl text;
BEGIN
    FOR tbl IN
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_type = 'BASE TABLE'
    LOOP
        EXECUTE format('DROP POLICY IF EXISTS "Allow full access to %I" ON public.%I', tbl, tbl);
        EXECUTE format('CREATE POLICY "Allow full access to %I" ON public.%I FOR ALL USING (true) WITH CHECK (true)', tbl, tbl);
    END LOOP;
END
$$;

-- ==============================================================================
-- INITIAL HEALTHCARE SEED DATA
-- Matches the default MedTime Android app demonstration accounts and schedules
-- ==============================================================================

-- 1. Seed Users
INSERT INTO public.users (
    id, name, email, role, phone, blood_group, allergies,
    emergency_contact_name, emergency_contact_phone, emergency_contact_relation,
    is_doctor_verified, doctor_verification_status, doctor_specialty, doctor_hospital,
    doctor_license, doctor_bio, caretaker_linking_code
) VALUES
('patient_1', 'Vijay Kumar', 'patient@medtime.com', 'PATIENT', '+1 (555) 234-5678', 'O+', 'Amoxicillin, Pollen', 'Ananya Kumar', '+1 (555) 987-6543', 'Spouse', FALSE, 'APPROVED', '', '', '', '', 'MED-7842'),
('doctor_1', 'Dr. Sarah Mitchell, MD', 'doctor@medtime.com', 'DOCTOR', '+1 (555) 876-5432', 'A+', '', '', '', '', TRUE, 'APPROVED', 'Cardiologist', 'St. Jude Heart & Vascular Hospital', 'MED-LIC-94821', 'Chief of Cardiology with over 15 years treating hypertension, arrhythmias, and preventive care.', ''),
('doctor_2', 'Dr. Robert Chen, MD', 'robert.chen@medtime.com', 'DOCTOR', '+1 (555) 345-6789', 'B+', '', '', '', '', TRUE, 'APPROVED', 'Endocrinologist', 'Memorial General Care', 'MED-LIC-32910', 'Specialist in diabetes management, endocrine metabolic disorders, and thyroid therapy.', ''),
('caretaker_1', 'Emily Davis', 'caretaker@medtime.com', 'CARETAKER', '+1 (555) 654-3210', 'O-', '', '', '', '', FALSE, 'APPROVED', '', '', '', '', ''),
('admin_1', 'System Administrator', 'admin@medtime.com', 'ADMIN', '+1 (555) 000-1111', 'AB+', '', '', '', '', FALSE, 'APPROVED', '', '', '', '', '')
ON CONFLICT (id) DO NOTHING;

-- 2. Seed Medicines
INSERT INTO public.medicines (
    id, patient_id, name, dosage, form, instructions, frequency, reminder_times,
    start_date, end_date, stock_quantity, low_stock_threshold, notes, color_hex
) VALUES
('med_1', 'patient_1', 'Atorvastatin', '20 mg', 'Tablet', 'After food with water', 'Daily', '08:00 AM', TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD'), '2026-12-31', 28, 6, 'For cholesterol regulation', 1402304),
('med_2', 'patient_1', 'Metformin HCl', '500 mg', 'Tablet', 'With breakfast & dinner', 'Twice a day', '08:00 AM,08:00 PM', TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD'), '2026-12-31', 52, 6, 'Blood glucose stabilization', 3046706),
('med_3', 'patient_1', 'Lisinopril', '10 mg', 'Tablet', 'Morning before food', 'Daily', '07:30 AM', TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD'), '2026-12-31', 14, 6, 'Blood pressure maintenance', 27004),
('med_4', 'patient_1', 'Omega-3 Fish Oil', '1000 mg', 'Capsule', 'After lunch', 'Daily', '01:30 PM', TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD'), '2026-12-31', 45, 6, 'Cardiovascular dietary supplement', 16088855)
ON CONFLICT (id) DO NOTHING;

-- 3. Seed Today's Reminders
INSERT INTO public.medicine_reminders (
    id, medicine_id, patient_id, medicine_name, dosage, form, instructions,
    scheduled_date, scheduled_time, status, taken_at_timestamp
) VALUES
('rem_1', 'med_3', 'patient_1', 'Lisinopril', '10 mg', 'Tablet', 'Morning before food', TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD'), '07:30 AM', 'TAKEN', (EXTRACT(EPOCH FROM NOW()) * 1000 - 7200000)::BIGINT),
('rem_2', 'med_1', 'patient_1', 'Atorvastatin', '20 mg', 'Tablet', 'After food with water', TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD'), '08:00 AM', 'TAKEN', (EXTRACT(EPOCH FROM NOW()) * 1000 - 3600000)::BIGINT),
('rem_3', 'med_4', 'patient_1', 'Omega-3 Fish Oil', '1000 mg', 'Capsule', 'After lunch', TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD'), '01:30 PM', 'PENDING', NULL),
('rem_4', 'med_2', 'patient_1', 'Metformin HCl', '500 mg', 'Tablet', 'With dinner', TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD'), '08:00 PM', 'PENDING', NULL)
ON CONFLICT (id) DO NOTHING;

-- 4. Seed Medicine History
INSERT INTO public.medicine_history (
    id, medicine_id, patient_id, medicine_name, dosage, scheduled_time, action, action_timestamp, notes
) VALUES
('hist_1', 'med_3', 'patient_1', 'Lisinopril', '10 mg', '07:30 AM', 'TAKEN', (EXTRACT(EPOCH FROM NOW()) * 1000 - 7200000)::BIGINT, 'Taken on schedule with water'),
('hist_2', 'med_1', 'patient_1', 'Atorvastatin', '20 mg', '08:00 AM', 'TAKEN', (EXTRACT(EPOCH FROM NOW()) * 1000 - 3600000)::BIGINT, 'Taken after breakfast')
ON CONFLICT (id) DO NOTHING;

-- 5. Seed Appointments
INSERT INTO public.appointments (
    id, patient_id, patient_name, doctor_id, doctor_name, doctor_specialty,
    appointment_date, appointment_time, reason, status, doctor_notes
) VALUES
('appt_1', 'patient_1', 'Vijay Kumar', 'doctor_1', 'Dr. Sarah Mitchell, MD', 'Cardiologist', TO_CHAR(CURRENT_DATE + INTERVAL '2 days', 'YYYY-MM-DD'), '10:30 AM', 'Routine quarterly blood pressure review and ECG checkup', 'ACCEPTED', 'Please bring previous ECG reports and 7-day BP tracking log.'),
('appt_2', 'patient_1', 'Vijay Kumar', 'doctor_2', 'Dr. Robert Chen, MD', 'Endocrinologist', TO_CHAR(CURRENT_DATE + INTERVAL '5 days', 'YYYY-MM-DD'), '02:15 PM', 'HbA1c diabetes medication dosage adjustment review', 'PENDING', 'Fasting blood sugar test recommended prior to appointment.')
ON CONFLICT (id) DO NOTHING;

-- 6. Seed Caretaker Link
INSERT INTO public.caretaker_links (
    id, patient_id, patient_name, caretaker_id, caretaker_name, linking_code, status
) VALUES
('link_1', 'patient_1', 'Vijay Kumar', 'caretaker_1', 'Emily Davis', 'MED-7842', 'APPROVED')
ON CONFLICT (id) DO NOTHING;

-- 7. Seed Wallet
INSERT INTO public.wallets (caretaker_id, balance, max_balance, currency) VALUES
('caretaker_1', 450.0, 1000.0, 'INR')
ON CONFLICT (caretaker_id) DO NOTHING;

-- 8. Seed Patient Address
INSERT INTO public.patient_saved_addresses (
    id, patient_id, title, address_type, address_line1, address_line2, city, state, pincode, full_address, latitude, longitude, is_default
) VALUES
('addr_1', 'patient_1', 'Patient''s Home', 'HOME', '124 Indiranagar 100ft Rd', 'Near Metro Pillar 42', 'Bengaluru', 'Karnataka', '560038', '124 Indiranagar 100ft Rd, Near Metro Pillar 42, Bengaluru, Karnataka 560038', 12.9716, 77.5946, TRUE)
ON CONFLICT (id) DO NOTHING;

-- 9. Seed Announcements
INSERT INTO public.announcements (
    id, title, content, target_role, priority, author_name
) VALUES
('ann_1', 'Seasonal Flu Vaccine Clinic Available', 'Annual influenza vaccines are available at Memorial Clinic weekdays 9 AM - 4 PM. Contact your doctor to schedule.', 'ALL', 'INFO', 'Medical Director'),
('ann_2', 'Emergency Assistance Hotline Active', 'Caretakers and patients can use the in-app SOS feature for real-time location dispatch during medication emergencies.', 'ALL', 'NORMAL', 'Chief Medical Officer')
ON CONFLICT (id) DO NOTHING;

-- Verification query
SELECT 'Supabase MedTime PostgreSQL schema initialized successfully!' AS status;
