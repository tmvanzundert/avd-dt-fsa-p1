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
    avatar_uri
)
VALUES
('John', 'Doe', 'johndoe', 'Stationsplein 1, 4811 BB Breda', 'CUSTOMER', '0612345678', 'hashedpassword123', 'johndoe@example.com', 4.5, NOW(), '1990-01-01', 'D1234567890', ''),
('Sanne', 'Jansen', 'sannej', 'Grote Markt 38, 4811 XS Breda', 'CUSTOMER', '0623456789', 'hashedpassword456', 'sannej@example.com', 4.8, NOW(), '1985-05-15', 'J9876543210', ''),
('Pieter', 'de Vries', 'pieterv', 'Haagweg 334, 4813 XE Amsterdam', 'CUSTOMER', '0634567890', 'hashedpassword789', 'pieterv@example.com', 4.7, NOW(), '1992-09-21', 'V1122334455', '');

-- Insert vehicles
INSERT INTO vehicles (
    make,
    model,
    year,
    category,
    seats,
    range_km,
    license_plate,
    location_id,
    owner_user_id,
    photo_path,
    total_yearly_kilometers,
    begin_available,
    end_available
)
VALUES
('Tesla', 'Model 3', 2022, 'Electric', 5, 491, 'NL-BR-01', 1, 1, "[]", 123456, "2025-01-01T00:00:00", "2026-01-31T00:00:00"),
('Tesla', 'Model Y', 2023, 'Electric', 5, 533, 'NL-BR-02', 2, 2, "[]", 21450, "2025-01-01T00:00:00", "2026-01-31T00:00:00"),
('Kia', 'Niro EV', 2021, 'Electric', 5, 455, 'NL-BR-03', 3, 3, "[]", 19800, "2025-01-01T00:00:00", "2026-01-31T00:00:00"),
('Hyundai', 'Kona Electric', 2020, 'Electric', 5, 484, 'NL-BR-04', 1, 2, "[]", 18500, "2025-01-01T00:00:00", "2026-01-31T00:00:00"),
('Volkswagen', 'ID.3', 2021, 'Electric', 5, 426, 'NL-BR-05', 2, 1, "[]", 22000, "2025-01-01T00:00:00", "2026-01-31T00:00:00"),
('Volkswagen', 'ID.4', 2022, 'Electric', 5, 520, 'NL-BR-06', 3, 3, "[]", 17000, "2025-01-01T00:00:00", "2026-01-31T00:00:00"),
('Renault', 'Zoe', 2019, 'Electric', 5, 395, 'NL-BR-07', 2, 3, "[]", 19000, "2025-01-01T00:00:00", "2026-01-31T00:00:00"),
('Nissan', 'Leaf', 2020, 'Electric', 5, 270, 'NL-BR-08', 3, 2, "[]", 21000, "2025-01-01T00:00:00", "2026-01-31T00:00:00"),
('Peugeot', 'e-208', 2022, 'Electric', 5, 362, 'NL-BR-09', 1, 1, "[]", 16500, "2025-01-01T00:00:00", "2026-01-31T00:00:00"),
('Volvo', 'EX30', 2024, 'Electric', 5, 476, 'NL-BR-10', 2, 2, "[]", 14000, "2025-01-01T00:00:00", "2026-01-31T00:00:00"),
('BMW', 'i4', 2022, 'Electric', 5, 590, 'NL-BR-11', 3, 1, "[]", 20000, "2025-01-01T00:00:00", "2026-01-31T00:00:00"),
('Audi', 'Q4 e-tron', 2022, 'Electric', 5, 520, 'NL-BR-12', 1, 3, "[]", 26000, "2025-01-01T00:00:00", "2026-01-31T00:00:00");

-- Insert a reservation (user 1 reserves vehicle 2, staff is user 2, pickup and dropoff at Breda Centrum)
INSERT INTO reservations (
    user_id, vehicle_id, rate_plan_id, staff_id, start_at, end_at, status, total_amount, pickup_location_id, dropoff_location_id
) VALUES (
    1, 1, 1, 2, '2025-11-02 10:00:00', '2025-11-04 10:00:00', 'CONFIRMED', 320.00, 2, 2
);