USE plugandplay;

-- Insert users
INSERT INTO users (
    id,
    first_name,
    last_name,
    username,
    password,
    email,
    rating,
    phone,
    role,
    created_at,
    birth_date,
    driver_license_number,
    address,
    avatar_path
)
VALUES
(1, 'Henk', 'de Vries', 'hdevries', '$2a$10$ouC/BX19GxcB6NTgOKpXjejWWyiZfEpqM7mrR/iFniAiJH2WETHGK', 'hdevries@gmail.com', 5, NULL, 'CUSTOMER', NULL, NULL, NULL, 'Markendaalseweg 131, 4811 KW Breda', NULL),
(2, 'Sanne', 'van den Broek', 'svdbroek', '$2a$10$8FFMYG12cQNryzEmrQrptO/ULm7699GE4QRBDwAV5O/ctV2cUVkva', 'svdbroek@gmail.com', 5, NULL, 'CUSTOMER', NULL, NULL, NULL, 'Fellenoordstraat 97, 4811 TH Breda', NULL),
(3, 'John', 'Halsema', 'jhalsema', '$2a$10$G6mXAVwycEvBnAtC8dCOyOfT3CrR6UTqEPS7Cd3Xjve1FTPixIj7m', 'jhalsema@gmail.com', 5, NULL, 'CUSTOMER', NULL, NULL, NULL, 'Eindstraat 15-17, 4811 KK Breda', NULL);

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
    (491, '1XZL27', 'AVAILABLE', 4.78090, 51.58710, 1, '2026-01-01 00:00:00', '2026-01-13 00:00:00', 80.00, '[]'),
    (533, 'KK596K', 'AVAILABLE', 4.79910, 51.58810, 2, '2026-01-10 00:00:00', '2026-01-16 00:00:00', 90.00, '[]'),
    (455, 'NG431D', 'AVAILABLE', 4.82550, 51.59030, 3, '2026-01-20 00:00:00', '2026-01-31 00:00:00', 70.00, '[]');


-- Insert a reservation
INSERT INTO reservations (
    user_id,
    vehicle_id,
    start_at,
    end_at,
    status,
    total_amount
) VALUES (
    1, 2, '2025-11-02 10:00:00', '2025-11-04 10:00:00', 'CONFIRMED', 320.00
);