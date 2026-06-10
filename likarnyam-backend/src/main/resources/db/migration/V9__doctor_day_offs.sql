CREATE TABLE doctor_day_offs (
    id        BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT NOT NULL REFERENCES doctors(id) ON DELETE CASCADE,
    date      DATE NOT NULL,
    reason    TEXT,
    UNIQUE (doctor_id, date)
);