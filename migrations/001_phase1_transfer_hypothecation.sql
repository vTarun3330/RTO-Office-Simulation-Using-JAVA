-- RTO System - Phase 1 Database Migration
-- Transfer of Ownership & Hypothecation Features

-- 1. Add new columns to existing vehicles table
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS is_blacklisted BOOLEAN DEFAULT FALSE;
ALTER TABLE vehicles ADD COLUMN IF NOT EXISTS hypothecation_flag BOOLEAN DEFAULT FALSE;

-- 2. Create hypothecations table
CREATE TABLE IF NOT EXISTS hypothecations (
    id VARCHAR(50) PRIMARY KEY,
    vehicle_vin VARCHAR(50) NOT NULL,
    bank_name VARCHAR(100) NOT NULL,
    loan_account VARCHAR(50),
    loan_amount DECIMAL(10,2),
    start_date DATE,
    is_active BOOLEAN DEFAULT TRUE,
    noc_issued BOOLEAN DEFAULT FALSE,
    noc_date DATE,
    FOREIGN KEY (vehicle_vin) REFERENCES vehicles(registration_number)
);

-- 3. Create transfer_requests table
CREATE TABLE IF NOT EXISTS transfer_requests (
    transfer_id VARCHAR(50) PRIMARY KEY,
    vehicle_vin VARCHAR(50) NOT NULL,
    seller_id VARCHAR(50) NOT NULL,
    buyer_id VARCHAR(50),
    buyer_mobile VARCHAR(20),
    transfer_token VARCHAR(20) UNIQUE,
    transfer_fee DECIMAL(10,2) DEFAULT 500.00,
    status VARCHAR(20) DEFAULT 'INITIATED',
    -- Status: INITIATED, BUYER_PENDING, PAYMENT_DONE, RTO_PENDING, COMPLETED, REJECTED
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_date TIMESTAMP,
    approved_by VARCHAR(50),
    rejection_reason VARCHAR(500),
    FOREIGN KEY (vehicle_vin) REFERENCES vehicles(registration_number),
    FOREIGN KEY (seller_id) REFERENCES users(id),
    FOREIGN KEY (buyer_id) REFERENCES users(id),
    FOREIGN KEY (approved_by) REFERENCES users(id)
);

-- 4. Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_transfer_token ON transfer_requests(transfer_token);
CREATE INDEX IF NOT EXISTS idx_hypothecation_vehicle ON hypothecations(vehicle_vin);
CREATE INDEX IF NOT EXISTS idx_vehicle_blacklist ON vehicles(is_blacklisted);

-- 5. Insert sample hypothecation data for testing
-- (Uncomment to test blocking scenario)
-- INSERT INTO hypothecations (id, vehicle_vin, bank_name, loan_account, loan_amount, start_date, is_active, noc_issued)
-- VALUES ('HYP001', 'KA-01-C-1234', 'HDFC Bank', 'LA123456', 500000, CURRENT_DATE, TRUE, FALSE);

-- 6. Update vehicles table to set hypothecation_flag
-- UPDATE vehicles SET hypothecation_flag = TRUE WHERE registration_number = 'KA-01-C-1234';

COMMIT;
