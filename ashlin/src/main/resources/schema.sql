CREATE TABLE IF NOT EXISTS specializations (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(40) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_specialization_name (name)
);

CREATE TABLE IF NOT EXISTS patients (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(80) NOT NULL,
    email VARCHAR(120) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    date_of_birth DATE NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_patient_email (email)
);

CREATE TABLE IF NOT EXISTS doctors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(80) NOT NULL,
    email VARCHAR(120) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    specialization_id BIGINT NOT NULL,
    room VARCHAR(16) NOT NULL,
    active TINYINT NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uq_doctor_email (email),
    KEY idx_doctor_specialization (specialization_id),
    CONSTRAINT fk_doctor_specialization FOREIGN KEY (specialization_id) REFERENCES specializations (id)
);

CREATE TABLE IF NOT EXISTS admins (
    id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(80) NOT NULL,
    email VARCHAR(120) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_admin_email (email)
);

CREATE TABLE IF NOT EXISTS appointments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    starts_at DATETIME NOT NULL,
    reason VARCHAR(200) NOT NULL,
    status VARCHAR(16) NOT NULL,
    created_at DATETIME NOT NULL,
    active_doctor_slot VARCHAR(48) GENERATED ALWAYS AS (
        IF(status = 'CANCELLED', NULL, CONCAT(doctor_id, '-', DATE_FORMAT(starts_at, '%Y%m%d%H%i')))
    ) STORED,
    active_patient_slot VARCHAR(48) GENERATED ALWAYS AS (
        IF(status = 'CANCELLED', NULL, CONCAT(patient_id, '-', DATE_FORMAT(starts_at, '%Y%m%d%H%i')))
    ) STORED,
    PRIMARY KEY (id),
    UNIQUE KEY uq_active_doctor_slot (active_doctor_slot),
    UNIQUE KEY uq_active_patient_slot (active_patient_slot),
    KEY idx_appt_doctor_time (doctor_id, starts_at),
    KEY idx_appt_patient_time (patient_id, starts_at),
    CONSTRAINT fk_appt_patient FOREIGN KEY (patient_id) REFERENCES patients (id),
    CONSTRAINT fk_appt_doctor FOREIGN KEY (doctor_id) REFERENCES doctors (id),
    CONSTRAINT chk_appt_status CHECK (status IN ('BOOKED', 'CANCELLED'))
);
