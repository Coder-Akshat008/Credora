DROP DATABASE IF EXISTS credora_db;
CREATE DATABASE IF NOT EXISTS credora_db;
USE credora_db;

-- 1. CUSTOMER TABLE
CREATE TABLE IF NOT EXISTS customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    dob DATE,
    gender ENUM('Male', 'Female', 'Other'),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    address TEXT,
    employment_status ENUM('EMPLOYED', 'SELF-EMPLOYED', 'STUDENT', 'UNEMPLOYED'),
    monthly_income DECIMAL(15, 2) DEFAULT 0.00,
    credit_score INT CHECK (credit_score BETWEEN 300 AND 850),
    is_deleted TINYINT(1) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. LOAN TABLE (Includes start_date, application_date, approval_date)
CREATE TABLE IF NOT EXISTS loan (
    loan_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    loan_type_id INT,
    employee_id INT,
    principal_amount DECIMAL(15, 2) NOT NULL CHECK (principal_amount > 0),
    remaining_balance DECIMAL(15, 2) DEFAULT 0.00 CHECK (remaining_balance >= 0),
    interest_rate DECIMAL(5, 2) NOT NULL CHECK (interest_rate >= 0),
    tenure_months INT NOT NULL CHECK (tenure_months > 0),
    start_date DATE NOT NULL,
    application_date DATE,
    approval_date DATE NULL,
    loan_status ENUM('APPROVED', 'PENDING', 'REJECTED', 'CLOSED', 'DEFAULTED') DEFAULT 'PENDING',
    FOREIGN KEY (customer_id) REFERENCES customer(customer_id) ON DELETE RESTRICT
);

-- 3. PAYMENT TABLE
CREATE TABLE IF NOT EXISTS payment (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT NOT NULL,
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    amount_paid DECIMAL(15, 2) NOT NULL CHECK (amount_paid > 0),
    payment_mode ENUM('UPI', 'CARD', 'NET BANKING', 'BANK TRANSFER', 'CASH'),
    payment_status ENUM('SUCCESS', 'PENDING', 'FAILED') DEFAULT 'SUCCESS',
    FOREIGN KEY (loan_id) REFERENCES loan(loan_id) ON DELETE RESTRICT
);

-- 4. DELINQUENCY TABLE
CREATE TABLE IF NOT EXISTS delinquency (
    delinquency_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT NOT NULL,
    days_overdue INT NOT NULL,
    outstanding_amount DECIMAL(15, 2) NOT NULL,
    delinquency_date DATE NOT NULL,
    risk_level ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL,
    resolution_status ENUM('OPEN', 'RESOLVED') DEFAULT 'OPEN',
    FOREIGN KEY (loan_id) REFERENCES loan(loan_id) ON DELETE RESTRICT
);


-- =========================================================
-- INSERT CUSTOMERS
-- =========================================================

INSERT INTO customer
(full_name, dob, gender, phone, email, address, employment_status, monthly_income, credit_score)
VALUES
('Akshay Kulkarni', '2002-04-18', 'Male', '9876500001', 'akshay@gmail.com', 'Pune, Maharashtra', 'EMPLOYED', 65000, 768),
('Sneha Patil', '2001-09-12', 'Female', '9876500002', 'sneha@gmail.com', 'Nashik, Maharashtra', 'EMPLOYED', 58000, 742),
('Rohan Deshmukh', '1999-12-05', 'Male', '9876500003', 'rohan@gmail.com', 'Mumbai, Maharashtra', 'SELF-EMPLOYED', 92000, 781),
('Ananya Shah', '2003-06-21', 'Female', '9876500004', 'ananya@gmail.com', 'Ahmedabad, Gujarat', 'STUDENT', 35000, 720),
('Vivek Joshi', '1997-02-14', 'Male', '9876500005', 'vivek@gmail.com', 'Nagpur, Maharashtra', 'EMPLOYED', 110000, 805),
('Pooja Nair', '2000-11-30', 'Female', '9876500006', 'pooja@gmail.com', 'Bengaluru, Karnataka', 'EMPLOYED', 72000, 755),
('Karan Verma', '1996-08-08', 'Male', '9876500007', 'karan@gmail.com', 'Delhi, India', 'SELF-EMPLOYED', 125000, 790),
('Meera Iyer', '2002-01-25', 'Female', '9876500008', 'meera@gmail.com', 'Chennai, Tamil Nadu', 'EMPLOYED', 67000, 748),
('Aditya Singh', '1998-05-17', 'Male', '9876500009', 'aditya@gmail.com', 'Jaipur, Rajasthan', 'EMPLOYED', 83000, 774),
('Nidhi Gupta', '2001-03-09', 'Female', '9876500010', 'nidhi@gmail.com', 'Indore, Madhya Pradesh', 'EMPLOYED', 61000, 735),
('Sahil Khan', '1995-07-22', 'Male', '9876500011', 'sahil@gmail.com', 'Hyderabad, Telangana', 'SELF-EMPLOYED', 98000, 716),
('Ishita Rao', '2003-10-11', 'Female', '9876500012', 'ishita@gmail.com', 'Pune, Maharashtra', 'STUDENT', 30000, 701);


-- =========================================================
-- INSERT LOANS
-- =========================================================

INSERT INTO loan
(customer_id, loan_type_id, employee_id, principal_amount, remaining_balance, interest_rate, tenure_months, start_date, application_date, approval_date, loan_status)
VALUES
(1, 1, 2, 300000, 300000, 12.50, 36, '2026-01-12', '2026-01-10', '2026-01-12', 'APPROVED'),
(2, 2, 3, 500000, 500000, 9.50, 48, '2026-01-17', '2026-01-15', '2026-01-17', 'APPROVED'),
(3, 5, 4, 1200000, 1200000, 11.25, 60, '2026-02-05', '2026-02-02', '2026-02-05', 'APPROVED'),
(4, 2, 2, 250000, 250000, 9.50, 36, '2026-02-12', '2026-02-12', NULL, 'PENDING'),
(5, 3, 4, 4500000, 4500000, 8.50, 180, '2026-02-24', '2026-02-20', '2026-02-24', 'APPROVED'),
(6, 4, 3, 800000, 800000, 10.00, 60, '2026-03-03', '2026-03-01', '2026-03-03', 'APPROVED'),
(7, 5, 2, 2000000, 2000000, 11.25, 72, '2026-03-08', '2026-03-08', NULL, 'PENDING'),
(8, 1, 5, 150000, 150000, 12.50, 24, '2026-03-16', '2026-03-15', '2026-03-16', 'APPROVED'),
(9, 4, 3, 950000, 950000, 10.00, 60, '2026-03-23', '2026-03-21', '2026-03-23', 'APPROVED'),
(10, 1, 5, 200000, 200000, 12.50, 30, '2026-04-01', '2026-04-01', '2026-04-03', 'REJECTED'),
(11, 6, 2, 300000, 300000, 10.75, 24, '2026-04-07', '2026-04-06', '2026-04-07', 'APPROVED'),
(12, 2, 3, 150000, 150000, 9.50, 24, '2026-04-12', '2026-04-12', NULL, 'PENDING');


-- =========================================================
-- INSERT PAYMENTS
-- =========================================================

INSERT INTO payment
(loan_id, payment_date, amount_paid, payment_mode, payment_status)
VALUES
(1, '2026-02-10 10:00:00', 11000, 'UPI', 'SUCCESS'),
(1, '2026-03-10 10:00:00', 11000, 'UPI', 'SUCCESS'),
(1, '2026-04-10 10:00:00', 11000, 'NET BANKING', 'SUCCESS'),
(2, '2026-02-20 10:00:00', 12500, 'UPI', 'SUCCESS'),
(2, '2026-03-20 10:00:00', 12500, 'CARD', 'SUCCESS'),
(3, '2026-03-05 10:00:00', 27000, 'NET BANKING', 'SUCCESS'),
(5, '2026-03-24 10:00:00', 44300, 'BANK TRANSFER', 'SUCCESS'),
(5, '2026-04-24 10:00:00', 44300, 'BANK TRANSFER', 'SUCCESS'),
(6, '2026-04-05 10:00:00', 17000, 'UPI', 'SUCCESS'),
(8, '2026-04-16 10:00:00', 7100, 'UPI', 'SUCCESS'),
(9, '2026-04-23 10:00:00', 20200, 'CARD', 'SUCCESS'),
(11, '2026-05-07 10:00:00', 14500, 'UPI', 'SUCCESS'),
(11, '2026-06-07 10:00:00', 14500, 'UPI', 'SUCCESS');


-- =========================================================
-- INSERT DELINQUENCY RECORDS
-- =========================================================

INSERT INTO delinquency
(loan_id, days_overdue, outstanding_amount, delinquency_date, risk_level, resolution_status)
VALUES
(3, 8, 1175000, '2026-05-15', 'MEDIUM', 'OPEN'),
(6, 22, 735000, '2026-05-20', 'HIGH', 'OPEN'),
(9, 5, 920000, '2026-05-25', 'LOW', 'RESOLVED'),
(11, 31, 270000, '2026-06-10', 'HIGH', 'OPEN');
SELECT * FROM customer;

-- 2. View all loans
SELECT * FROM loan;

-- 3. View all payment records
SELECT * FROM payment;

-- 4. View all delinquency records
SELECT * FROM delinquency;
