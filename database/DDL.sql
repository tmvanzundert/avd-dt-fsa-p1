CREATE DATABASE IF NOT EXISTS plugandplay;
USE plugandplay;

SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS driving_details;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS vehicle_status;
DROP TABLE IF EXISTS payments;
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS vehicles;
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
    role ENUM('ADMIN', 'CUSTOMER') DEFAULT 'CUSTOMER',
    created_at TIMESTAMP,
    birth_date DATE,
    driver_license_number VARCHAR(64),
    address VARCHAR(255),
    uri_avatar VARCHAR(255)
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
    status ENUM('AVAILABLE', 'RENTED', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    longitude DECIMAL(10, 7),
    latitude DECIMAL(10, 7),
    owner_user_id BIGINT,
    total_yearly_kilometers BIGINT,
    tco DECIMAL(10, 2),
    begin_available TIMESTAMP,
    end_available TIMESTAMP,
    price_per_day DECIMAL(10,2),
    uri_vehicle VARCHAR(255),
    FOREIGN KEY (owner_user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    vehicle_id BIGINT,
    start_at TIMESTAMP,
    end_at TIMESTAMP,
    status ENUM('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING',
    total_amount DECIMAL(12,2),
    photo_vehicle_before VARCHAR(255),
    photo_vehicle_after VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id BIGINT,
    amount DECIMAL(11,2),
    currency CHAR(3),
    provider ENUM('STRIPE', 'PAYPAL', 'IDEAL', 'BANCONTACT'),
    status VARCHAR(32),
    authorized_at TIMESTAMP,
    captured_at TIMESTAMP,
    refunded_at TIMESTAMP,
    deposit DECIMAL(10,2),
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

CREATE TABLE IF NOT EXISTS driving_details (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    renter_id BIGINT,
    vehicle_id BIGINT,
    timestamp DATETIME,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    max_speed_kmh INT,
    battery_level_percent INT,
    total_kilometers INT,
    FOREIGN KEY (renter_id) REFERENCES users(id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE SET NULL ON UPDATE CASCADE
);