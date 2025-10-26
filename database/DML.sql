INSERT INTO `User` (
    `id`,
    `firstName`,
    `lastName`,
    `username`,
    `address`,
    `role`,
    `phone`,
    `password`,
    `email`,
    `rating`,
    `createdAt`,
    `birthDate`,
    `driverLicenseNumber`
)
VALUES (
    1,                        -- id (set to 1)
    'John',                   -- firstName
    'Doe',                    -- lastName
    'johndoe',                -- username
    '123 Main St, Anytown',    -- address
    'USER',                   -- role (could also be 'ADMIN', or 'NULL')
    '555-1234',               -- phone
    'hashedpassword123',      -- password (hashed, never store plain text!)
    'johndoe@example.com',    -- email
    4.5,                      -- rating (you can set this value based on your needs)
    NOW(),                    -- createdAt (current timestamp)
    '1990-01-01 00:00:00',    -- birthDate (replace with actual birth date)
    'D1234567890'             -- driverLicenseNumber
);

INSERT INTO Vehicle (
    make,
    model,
    year,
    category,
    seats,
    `range`,
    beginOdometer,
    endOdometer,
    licensePlate,
    status,
    location,
    price,
    photoPath,
    beginReservation,
    endReservation,
    totalYearlyUsageKilometers,
    ownerId
)
VALUES (
    'Toyota',  -- make
    'Corolla', -- model
    2020,      -- year
    'Sedan',   -- category
    5,         -- seats
    600,       -- range (in kilometers)
    0,         -- beginOdometer (in km)
    20000,     -- endOdometer (in km)
    'ABC1234', -- licensePlate
    'AVAILABLE', -- status
    'New York',  -- location
    15000.00,    -- price
    '/path/to/photo.jpg',  -- photoPath
    '2025-10-01 10:00:00', -- beginReservation
    '2025-10-10 10:00:00', -- endReservation
    12000.00,    -- totalYearlyUsageKilometers
    1            -- ownerId (valid id from the User table)
);
