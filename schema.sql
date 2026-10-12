DROP DATABASE IF EXISTS credora_db;
CREATE DATABASE IF NOT EXISTS credora_db;
USE credora_db;

-- 1. CUSTOMER TABLE
CREATE TABLE customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    is_deleted TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. LOAN TABLE
CREATE TABLE loan (
    loan_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    principal_amount DECIMAL(15, 2) NOT NULL CHECK (principal_amount > 0),
    remaining_balance DECIMAL(15, 2) NOT NULL CHECK (remaining_balance >= 0),
    interest_rate DECIMAL(5, 2) NOT NULL CHECK (interest_rate >= 0),
    tenure_months INT NOT NULL CHECK (tenure_months > 0),
    start_date DATE NOT NULL,
    loan_status ENUM('APPROVED', 'CLOSED', 'DEFAULTED') DEFAULT 'APPROVED',
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE RESTRICT
);

-- 3. PAYMENT TABLE
CREATE TABLE payment (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT NOT NULL,
    amount_paid DECIMAL(15, 2) NOT NULL CHECK (amount_paid > 0),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (loan_id) REFERENCES loan(loan_id) ON DELETE RESTRICT
);

-- 4. DELINQUENCY TABLE
CREATE TABLE delinquency (
    delinquency_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT NOT NULL,
    days_overdue INT NOT NULL,
    outstanding_amount DECIMAL(15, 2) NOT NULL,
    delinquency_date DATE NOT NULL,
    risk_level ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL,
    resolution_status ENUM('OPEN', 'RESOLVED') DEFAULT 'OPEN',
    FOREIGN KEY (loan_id) REFERENCES loan(loan_id) ON DELETE RESTRICT
);

-- SEED DATA
INSERT INTO customer (full_name, email, phone, is_deleted) VALUES 
('Akshay Kulkarni', 'akshay@gmail.com', '9876500001', 0),
('Sneha Patil', 'sneha@gmail.com', '9876500002', 0),
('Rohan Mehta', 'rohan.m@gmail.com', '9876500003', 0),
('Ananya Sharma', 'ananya.s@yahoo.com', '9876500004', 0),
('Vikram Deshmukh', 'v.deshmukh@outlook.com', '9876500005', 0),
('Priya Nair', 'priya.nair@gmail.com', '9876500006', 0),
('Amitabh Joshi', 'ajoshi@rediffmail.com', '9876500007', 0),
('Neha Verma', 'neha.verma@gmail.com', '9876500008', 0),
('Siddharth Rao', 'sid.rao@gmail.com', '9876500009', 1);

INSERT INTO loan (customer_id, principal_amount, remaining_balance, interest_rate, tenure_months, start_date, loan_status) VALUES 
(1, 300000.00, 250000.00, 12.50, 36, '2025-06-15', 'APPROVED'),
(2, 500000.00, 420000.00, 9.50, 48, '2025-08-10', 'APPROVED'),
(3, 150000.00, 0.00, 11.00, 12, '2024-01-10', 'CLOSED'),
(4, 800000.00, 750000.00, 10.25, 60, '2025-03-01', 'APPROVED'),
(5, 200000.00, 180000.00, 14.00, 24, '2025-04-20', 'DEFAULTED'),
(6, 450000.00, 450000.00, 8.75, 36, '2026-01-05', 'APPROVED'),
(7, 100000.00, 0.00, 13.00, 12, '2024-05-15', 'CLOSED'),
(8, 600000.00, 580000.00, 10.50, 48, '2025-09-12', 'APPROVED');

INSERT INTO payment (loan_id, amount_paid, payment_date) VALUES 
(1, 25000.00, '2025-07-15 10:30:00'),
(1, 25000.00, '2025-08-15 11:15:00'),
(2, 40000.00, '2025-09-10 14:00:00'),
(2, 40000.00, '2025-10-10 09:45:00'),
(3, 75000.00, '2024-07-10 16:20:00'),
(3, 75000.00, '2025-01-10 12:00:00'),
(4, 50000.00, '2025-04-01 10:00:00'),
(5, 20000.00, '2025-05-20 15:30:00'),
(7, 50000.00, '2024-11-15 11:00:00'),
(7, 50000.00, '2025-05-15 10:00:00'),
(8, 20000.00, '2025-10-12 13:10:00');

INSERT INTO delinquency (loan_id, days_overdue, outstanding_amount, delinquency_date, risk_level, resolution_status) VALUES 
(5, 120, 180000.00, '2025-09-01', 'HIGH', 'OPEN'),
(1, 45, 250000.00, '2025-10-01', 'LOW', 'OPEN'),
(4, 75, 750000.00, '2025-09-15', 'MEDIUM', 'OPEN'),
(2, 35, 420000.00, '2024-12-01', 'LOW', 'RESOLVED');

-- VERIFICATION PRINTS
SHOW TABLES;
SELECT * FROM customer;
SELECT * FROM loan;
SELECT * FROM payment;
SELECT * FROM delinquency;
