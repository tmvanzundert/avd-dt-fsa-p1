CREATE DATABASE IF NOT EXISTS plugandplay;

USE plugandplay;

SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS `User`;
DROP TABLE IF EXISTS `Vehicle`;
SET FOREIGN_KEY_CHECKS=1;

CREATE TABLE
    IF NOT EXISTS `User` (
        `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
        `firstName` VARCHAR(255) NOT NULL,
        `lastName` VARCHAR(255) NOT NULL,
        `username` VARCHAR(255) NOT NULL,
        `address` VARCHAR(255) NOT NULL,
        `role` ENUM (
            "USER",
            "ADMIN",
            "NULL"
        ) DEFAULT 'NULL',
        `phone` VARCHAR(20) NOT NULL,
        `password` VARCHAR(255) NOT NULL,
        `email` VARCHAR(255) NOT NULL,
        `rating` FLOAT,
        `createdAt` DATETIME,
        `birthDate` DATETIME,
        `driverLicenseNumber` VARCHAR(50) NOT NULL
    );


CREATE TABLE
    IF NOT EXISTS `Vehicle` (
        `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
        `make` VARCHAR(50),
        `model` VARCHAR(50),
        `year` INT,
        `category` VARCHAR(50),
        `seats` INT,
        `range` DOUBLE,
        `beginOdometer` INT,
        `endOdometer` INT,
        `licensePlate` VARCHAR(20),
        `status` ENUM (
            "AVAILABLE",
            "RENTED",
            "MAINTENANCE",
            "NULL"
        ) DEFAULT 'NULL',
        `location` VARCHAR(100),
        `price` DOUBLE,
        `photoPath` TEXT,
        `beginReservation` DATETIME,
        `endReservation` DATETIME,
        `totalYearlyUsageKilometers` DOUBLE,

        -- Foreign Key to User table
        `ownerId` BIGINT,
        FOREIGN KEY (`ownerId`) REFERENCES `User`(`id`) ON DELETE CASCADE
    );