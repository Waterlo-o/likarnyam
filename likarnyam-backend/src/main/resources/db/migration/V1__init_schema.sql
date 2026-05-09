-- ============================================================
-- V1 — Полная схема базы данных likarnyam
-- Sprint 1
-- ============================================================

CREATE TABLE roles (
    id   BIGSERIAL  PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL  -- 'DOCTOR', 'ADMIN'
);

CREATE TABLE users (
    id            BIGSERIAL  PRIMARY KEY,
    email         VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role_id       INT NOT NULL REFERENCES roles(id),
    is_active     BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMPTZ DEFAULT NOW(),
    updated_at    TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE specializations (
    id          BIGSERIAL  PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE doctors (
    id                BIGSERIAL  PRIMARY KEY,
    user_id           INT UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    first_name        VARCHAR(100) NOT NULL,
    last_name         VARCHAR(100) NOT NULL,
    middle_name       VARCHAR(100),
    specialization_id INT REFERENCES specializations(id),
    phone             VARCHAR(20),
    photo_url         VARCHAR(255),
    license_number    VARCHAR(50),
    created_at        TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE patients (
    id            BIGSERIAL  PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    middle_name   VARCHAR(100),
    date_of_birth DATE,
    gender        VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE', 'OTHER')),
    phone         VARCHAR(20),
    email         VARCHAR(255),
    address       TEXT,
    blood_type    VARCHAR(5),
    allergies     TEXT,
    notes         TEXT,
    created_at    TIMESTAMPTZ DEFAULT NOW(),
    updated_at    TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE doctor_patients (
    doctor_id   INT NOT NULL REFERENCES doctors(id),
    patient_id  INT NOT NULL REFERENCES patients(id),
    assigned_at TIMESTAMPTZ DEFAULT NOW(),
    is_primary  BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (doctor_id, patient_id)
);

CREATE TABLE schedules (
    id                    BIGSERIAL  PRIMARY KEY,
    doctor_id             INT NOT NULL REFERENCES doctors(id),
    day_of_week           INT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_time            TIME NOT NULL,
    end_time              TIME NOT NULL,
    slot_duration_minutes INT DEFAULT 30,
    is_active             BOOLEAN DEFAULT TRUE
);

CREATE TABLE schedule_exceptions (
    id        BIGSERIAL  PRIMARY KEY,
    doctor_id INT NOT NULL REFERENCES doctors(id),
    date      DATE NOT NULL,
    reason    VARCHAR(255)
);

CREATE TABLE appointments (
    id               BIGSERIAL  PRIMARY KEY,
    doctor_id        INT NOT NULL REFERENCES doctors(id),
    patient_id       INT NOT NULL REFERENCES patients(id),
    appointment_at   TIMESTAMPTZ NOT NULL,
    duration_minutes INT DEFAULT 30,
    status           VARCHAR(20) DEFAULT 'SCHEDULED'
                     CHECK (status IN ('SCHEDULED','COMPLETED','CANCELLED','NO_SHOW')),
    reason           TEXT,
    notes            TEXT,
    created_by       INT REFERENCES users(id),
    created_at       TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE medical_records (
    id             BIGSERIAL  PRIMARY KEY,
    patient_id     INT NOT NULL REFERENCES patients(id),
    doctor_id      INT NOT NULL REFERENCES doctors(id),
    appointment_id INT REFERENCES appointments(id),
    recorded_at    TIMESTAMPTZ DEFAULT NOW(),
    complaints     TEXT,
    diagnosis      TEXT,
    prescription   TEXT
);

CREATE TABLE symptoms (
    id          BIGSERIAL  PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    description TEXT
);

CREATE TABLE diseases (
    id          BIGSERIAL  PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    icd_code    VARCHAR(10),
    description TEXT
);

CREATE TABLE symptom_disease (
    symptom_id INT REFERENCES symptoms(id),
    disease_id INT REFERENCES diseases(id),
    PRIMARY KEY (symptom_id, disease_id)
);

CREATE TABLE disease_specialization (
    disease_id        INT REFERENCES diseases(id),
    specialization_id INT REFERENCES specializations(id),
    PRIMARY KEY (disease_id, specialization_id)
);

CREATE TABLE user_settings (
    user_id               INT PRIMARY KEY REFERENCES users(id),
    theme                 VARCHAR(20) DEFAULT 'LIGHT',
    language              VARCHAR(10) DEFAULT 'uk',
    font_size             VARCHAR(10) DEFAULT 'MEDIUM',
    notifications_enabled BOOLEAN DEFAULT TRUE,
    updated_at            TIMESTAMPTZ DEFAULT NOW()
);
