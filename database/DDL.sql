CREATE DATABASE IF NOT EXISTS plugandplay;
USE plugandplay;

SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS media_assets;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS rental_contracts;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS rate_plans;
DROP TABLE IF EXISTS vehicle_status;
DROP TABLE IF EXISTS vehicles;
DROP TABLE IF EXISTS locations;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS=1;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    username VARCHAR(80) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    rating REAL,
    phone VARCHAR(32),
    role ENUM('ADMIN', 'CUSTOMER', 'DEFAULT') DEFAULT 'DEFAULT',
    created_at TIMESTAMP,
    birth_date DATE,
    driver_license_number VARCHAR(64),
    address VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120),
    address VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS vehicle_status (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(40) NOT NULL, -- AVAILABLE, RENTED, MAINTENANCE
    owner_id BIGINT,
    owner_type VARCHAR(40),
    uri VARCHAR(255),
    checksum VARCHAR(128),
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    make VARCHAR(80) NOT NULL,
    model VARCHAR(80) NOT NULL,
    year INT NOT NULL,
    category VARCHAR(80),
    seats INT NOT NULL,
    range_km INT,
    license_plate VARCHAR(32) UNIQUE NOT NULL,
    location_id BIGINT,
    owner_user_id BIGINT,
    photo_path VARCHAR(255),
    total_yearly_kilometers BIGINT,
    tco DECIMAL(10, 2),
    FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS rental_contracts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT UNIQUE,
    pickup_odometer INT,
    dropoff_odometer INT,
    pickup_time DATETIME,
    return_time DATETIME,
    signed_at DATETIME,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS rate_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rental_contract_id BIGINT,
    name VARCHAR(120) UNIQUE,
    price_per_day DECIMAL(10,2),
    price_per_km DECIMAL(10,2),
    deposit DECIMAL(10,2),
    cancellation_policy TEXT,
    FOREIGN KEY (rental_contract_id) REFERENCES rental_contracts(id)/* ON DELETE SET NULL ON UPDATE CASCADE*/
);

CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    vehicle_id BIGINT,
    rate_plan_id BIGINT,
    staff_id BIGINT,
    start_at TIMESTAMP,
    end_at TIMESTAMP,
    status VARCHAR(32),
    total_amount DECIMAL(12,2),
    pickup_location_id BIGINT,
    dropoff_location_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (rate_plan_id) REFERENCES rate_plans(id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (staff_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (pickup_location_id) REFERENCES locations(id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (dropoff_location_id) REFERENCES locations(id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT,
    amount DECIMAL(11,2),
    currency CHAR(3),
    provider VARCHAR(40),
    status VARCHAR(32),
    authorized_at TIMESTAMP,
    captured_at TIMESTAMP,
    refunded_at TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    rating INT,
    comment TEXT,
    review_date TIMESTAMP,
    target_type VARCHAR(40),
    target_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    type VARCHAR(40),
    message TEXT,
    timestamp TIMESTAMP,
    is_read BOOLEAN,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS media_assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    owner_type VARCHAR(40),
    owner_id BIGINT,
    kind VARCHAR(40),
    uri VARCHAR(255),
    checksum VARCHAR(128),
    created_at TIMESTAMP
);