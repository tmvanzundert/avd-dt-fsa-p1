USE plugandplay;

-- Insert users
INSERT INTO users (
    first_name,
    last_name,
    username,
    address,
    role,
    phone,
    password,
    email,
    rating,
    created_at,
    birth_date,
    driver_license_number,
    avatar_path
)
VALUES
('John', 'Doe', 'johndoe', 'Stationsplein 1, 4811 BB Breda', 'CUSTOMER', '0612345678', 'hashedpassword123', 'johndoe@example.com', 4.5, NOW(), '1990-01-01', 'D1234567890', ''),
('Sanne', 'Jansen', 'sannej', 'Grote Markt 38, 4811 XS Breda', 'CUSTOMER', '0623456789', 'hashedpassword456', 'sannej@example.com', 4.8, NOW(), '1985-05-15', 'J9876543210', ''),
('Pieter', 'de Vries', 'pieterv', 'Haagweg 334, 4813 XE Amsterdam', 'CUSTOMER', '0634567890', 'hashedpassword789', 'pieterv@example.com', 4.7, NOW(), '1992-09-21', 'V1122334455', '');

-- Insert vehicles
INSERT INTO vehicles (
    range_km,
    license_plate,
    status,
    longitude,
    latitude,
    owner_user_id,
    begin_available,
    end_available,
    price_per_day,
    photo_path
)
VALUES
(491, 'NL-BR-01', 'AVAILABLE', 4.7683, 51.5880, 1, '2025-01-01 00:00:00', '2026-01-31 00:00:00', 80.00, '[]'),
(533, 'NL-BR-02', 'AVAILABLE', 4.7683, 51.5880, 2, '2025-01-01 00:00:00', '2026-01-31 00:00:00', 90.00, '[]'),
(455, 'NL-BR-03', 'AVAILABLE', 4.7683, 51.5880, 3, '2025-01-01 00:00:00', '2026-01-31 00:00:00', 70.00, '[]'),
(484, 'NL-BR-04', 'AVAILABLE', 4.7683, 51.5880, 2, '2025-01-01 00:00:00', '2026-01-31 00:00:00', 75.00, '[]'),
(426, 'NL-BR-05', 'AVAILABLE', 4.7683, 51.5880, 1, '2025-01-01 00:00:00', '2026-01-31 00:00:00', 72.50, '[]'),
(520, 'NL-BR-06', 'AVAILABLE', 4.7683, 51.5880, 3, '2025-01-01 00:00:00', '2026-01-31 00:00:00', 88.00, '[]'),
(395, 'NL-BR-07', 'AVAILABLE', 4.7683, 51.5880, 3, '2025-01-01 00:00:00', '2026-01-31 00:00:00', 60.00, '[]'),
(270, 'NL-BR-08', 'AVAILABLE', 4.7683, 51.5880, 2, '2025-01-01 00:00:00', '2026-01-31 00:00:00', 55.00, '[]'),
(362, 'NL-BR-09', 'AVAILABLE', 4.7683, 51.5880, 1, '2025-01-01 00:00:00', '2026-01-31 00:00:00', 65.00, '[]'),
(476, 'NL-BR-10', 'AVAILABLE', 4.7683, 51.5880, 2, '2025-01-01 00:00:00', '2026-01-31 00:00:00', 95.00, '[]'),
(590, 'NL-BR-11', 'AVAILABLE', 4.7683, 51.5880, 1, '2025-01-01 00:00:00', '2026-01-31 00:00:00', 110.00, '[]'),
(520, 'NL-BR-12', 'AVAILABLE', 4.7683, 51.5880, 3, '2025-01-01 00:00:00', '2026-01-31 00:00:00', 105.00, '[]');

-- Insert a reservation
INSERT INTO reservations (
    user_id,
    vehicle_id,
    start_at,
    end_at,
    status,
    total_amount
) VALUES (
    1, 1, '2025-11-02 10:00:00', '2025-11-04 10:00:00', 'CONFIRMED', 320.00
);