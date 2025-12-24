-- Schema for lab-notification service (MySQL)
-- Run e.g. via: mysql -u root -p labNotifications < database-schema.sql

CREATE TABLE IF NOT EXISTS patients (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mr_number VARCHAR(50) NOT NULL,
    patient_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(30),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_patients_mr UNIQUE (mr_number)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS batches (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    results_date DATE NOT NULL,
    from_time TIME NOT NULL,
    to_time TIME NOT NULL,
    status VARCHAR(50) NOT NULL,
    fetched_at TIMESTAMP NOT NULL,
    payload_hash CHAR(64) NOT NULL,
    CONSTRAINT uq_batches_window UNIQUE (results_date, from_time, to_time, status)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS lab_result_windows (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    results_date CHAR(8) NOT NULL,
    from_time TIME NOT NULL,
    to_time TIME NOT NULL,
    interval_minutes INT NOT NULL DEFAULT 30,
    last_run_at TIMESTAMP NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_date_window UNIQUE (results_date, from_time)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    order_num VARCHAR(50) NOT NULL,
    order_date DATE NOT NULL,
    order_time TIME NOT NULL,
    collected_at DATETIME NULL,
    order_status VARCHAR(10) NOT NULL,
    result_status VARCHAR(10) NOT NULL,
    order_type VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    status VARCHAR(10) NOT NULL,
    status_code INT NOT NULL DEFAULT 100,
    CONSTRAINT uq_orders_order_num UNIQUE (order_num),
    INDEX idx_orders_patient (patient_id),
    INDEX idx_orders_batch (batch_id),
    CONSTRAINT fk_orders_patients FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE RESTRICT,
    CONSTRAINT fk_orders_batches FOREIGN KEY (batch_id) REFERENCES batches (id) ON DELETE RESTRICT
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    profile_code VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_profiles_order_profile UNIQUE (order_id, profile_code),
    CONSTRAINT fk_profiles_orders FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS lab_departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL,
    description VARCHAR(255),
    lab_name VARCHAR(255),
    lab_type VARCHAR(10),
    lab_code VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_lab_departments_code UNIQUE (code, lab_code)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    profile_id BIGINT NULL,
    lab_department_id BIGINT NULL,
    test_code VARCHAR(50) NOT NULL,
    test_name VARCHAR(255) NOT NULL,
    result_status VARCHAR(10) NOT NULL,
    order_status VARCHAR(10) NOT NULL,
    order_type VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    tatTime VARCHAR(255),
    CONSTRAINT uq_tests_order_test UNIQUE (order_id, test_code),
    INDEX idx_tests_profile (profile_id),
    INDEX idx_tests_lab_department (lab_department_id),
    CONSTRAINT fk_tests_orders FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_tests_profiles FOREIGN KEY (profile_id) REFERENCES profiles (id) ON DELETE SET NULL,
    CONSTRAINT fk_tests_lab_departments FOREIGN KEY (lab_department_id) REFERENCES lab_departments (id) ON DELETE SET NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS sms_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    notification_id VARCHAR(50) NOT NULL,
    patient_id BIGINT NULL,
    phone_number VARCHAR(30),
    message_body TEXT NOT NULL,
    provider_message_id VARCHAR(100) DEFAULT 'MLOGANZILA',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    status_code INT NOT NULL DEFAULT 100,
    error_message TEXT,
    sent_at DATETIME NOT NULL,
    delivery_at DATETIME NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    INDEX idx_sms_history_notification (notification_id),
    INDEX idx_sms_history_phone (phone_number),
    INDEX idx_sms_history_patient (patient_id),
    CONSTRAINT fk_sms_history_patients FOREIGN KEY (patient_id) REFERENCES patients (id) ON DELETE SET NULL
) ENGINE=InnoDB;
