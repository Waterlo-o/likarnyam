CREATE TABLE schedule_change_requests (
    id              BIGSERIAL PRIMARY KEY,
    doctor_id       BIGINT NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    day_of_week     INTEGER NOT NULL,
    requested_start TIME,
    requested_end   TIME,
    reason          TEXT,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    admin_comment   TEXT,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    reviewed_at     TIMESTAMP,
    reviewed_by     BIGINT REFERENCES doctors(id) ON DELETE SET NULL
);

-- Тестовый админ
INSERT INTO users (email, password_hash, role_id, is_active, created_at, updated_at)
SELECT 'admin@likarnyam.com',
       '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lHHi',
       (SELECT id FROM roles WHERE name = 'ADMIN'),
       true,
       NOW(),
       NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@likarnyam.com'
);