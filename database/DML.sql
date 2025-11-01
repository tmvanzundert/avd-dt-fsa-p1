USE plugandplay;

-- Insert locations that are referenced
INSERT INTO locations (name, address) VALUES
('Breda', 'Stationsplein 1, 4811 BB Breda'),
('Breda Centrum', 'Grote Markt 38, 4811 XS Breda'),
('Breda Princenhage', 'Haagweg 334, 4813 XE Breda');

-- Insert users (all in Breda, with different addresses)
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
    driver_license_number
)
VALUES
('John', 'Doe', 'johndoe', 'Stationsplein 1, 4811 BB Breda', 'CUSTOMER', '0612345678', 'hashedpassword123', 'johndoe@example.com', 4.5, NOW(), '1990-01-01', 'D1234567890'),
('Sanne', 'Jansen', 'sannej', 'Grote Markt 38, 4811 XS Breda', 'CUSTOMER', '0623456789', 'hashedpassword456', 'sannej@example.com', 4.8, NOW(), '1985-05-15', 'J9876543210'),
('Pieter', 'de Vries', 'pieterv', 'Haagweg 334, 4813 XE Breda', 'CUSTOMER', '0634567890', 'hashedpassword789', 'pieterv@example.com', 4.7, NOW(), '1992-09-21', 'V1122334455');

-- Insert vehicles (all in Breda, using different location_ids)
INSERT INTO vehicles (
    make,
    model,
    year,
    category,
    seats,
    range_km,
    begin_odometer,
    end_odometer,
    license_plate,
    status,
    location_id,
    price_day,
    photo_path,
    begin_reservation,
    end_reservation,
    total_yearly_usage_km,
    owner_user_id
)
VALUES
('Toyota', 'Corolla', 2020, 'Sedan', 5, 600, 0, 20000, 'NL-BR-01', 'AVAILABLE', 1, 150.00, '/path/to/photo1.jpg', '2025-10-01 10:00:00', '2025-10-10 10:00:00', 12000.0, 1),
('Volkswagen', 'Golf', 2021, 'Hatchback', 5, 650, 10000, 25000, 'NL-BR-02', 'AVAILABLE', 2, 160.00, '/path/to/photo2.jpg', '2025-11-01 09:00:00', '2025-11-05 09:00:00', 10000.0, 2),
('Tesla', 'Model 3', 2022, 'Electric', 5, 400, 5000, 15000, 'NL-BR-03', 'AVAILABLE', 3, 200.00, '/path/to/photo3.jpg', '2025-12-01 08:00:00', '2025-12-07 08:00:00', 8000.0, 3);

-- Insert a rate plan
INSERT INTO rate_plans (name, price_per_day, price_per_km, deposit, cancellation_policy)
VALUES ('Standaard Plan', 100.00, 0.50, 500.00, 'Volledige restitutie tot 24 uur voor aanvang');

-- Insert a reservation (user 1 reserves vehicle 2, staff is user 2, pickup and dropoff at Breda Centrum)
INSERT INTO reservations (
    user_id, vehicle_id, rate_plan_id, staff_id, start_at, end_at, status, total_amount, pickup_location_id, dropoff_location_id
) VALUES (
    1, 2, 1, 2, '2025-11-02 10:00:00', '2025-11-04 10:00:00', 'CONFIRMED', 320.00, 2, 2
);