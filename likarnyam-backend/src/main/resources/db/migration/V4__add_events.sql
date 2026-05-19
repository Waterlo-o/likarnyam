CREATE TABLE events (
    id          BIGSERIAL PRIMARY KEY,
    doctor_id   BIGINT NOT NULL REFERENCES doctors(id),
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    event_at    TIMESTAMPTZ NOT NULL,
    location    VARCHAR(200),
    event_type  VARCHAR(50) DEFAULT 'MEETING'
);