-- =========================================================
-- CREDORA - SMART CREDIT & LOAN MANAGEMENT SYSTEM
-- =========================================================

DROP DATABASE IF EXISTS credora;

CREATE DATABASE credora;

USE credora;


-- =========================================================
-- 1. EMPLOYEE TABLE
-- =========================================================

CREATE TABLE employee (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(15),
    role VARCHAR(50) NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL
);


-- =========================================================
-- 2. CUSTOMER TABLE
-- =========================================================

CREATE TABLE customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    dob DATE,
    gender VARCHAR(20),
    phone VARCHAR(15),
    email VARCHAR(100),
    address VARCHAR(255),
    employment_status VARCHAR(50),
    monthly_income DECIMAL(12,2),
    credit_score INT
);


-- =========================================================
-- 3. LOAN TYPE TABLE
-- =========================================================

CREATE TABLE loan_type (
    loan_type_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_type_name VARCHAR(80) NOT NULL,
    description VARCHAR(255),
    min_amount DECIMAL(12,2),
    max_amount DECIMAL(12,2),
    base_interest_rate DECIMAL(5,2)
);


-- =========================================================
-- 4. LOAN TABLE
-- =========================================================

CREATE TABLE loan (
    loan_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    loan_type_id INT NOT NULL,
    employee_id INT NOT NULL,
    loan_amount DECIMAL(12,2) NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    tenure_months INT NOT NULL,
    application_date DATE NOT NULL,
    approval_date DATE,
    loan_status VARCHAR(30) DEFAULT 'PENDING',

    FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id),

    FOREIGN KEY (loan_type_id)
        REFERENCES loan_type(loan_type_id),

    FOREIGN KEY (employee_id)
        REFERENCES employee(employee_id)
);


-- =========================================================
-- 5. PAYMENT TABLE
-- =========================================================

CREATE TABLE payment (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_mode VARCHAR(30),
    payment_status VARCHAR(30) DEFAULT 'SUCCESS',

    FOREIGN KEY (loan_id)
        REFERENCES loan(loan_id)
);


-- =========================================================
-- 6. DELINQUENCY TABLE
-- =========================================================

CREATE TABLE delinquency (
    delinquency_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT NOT NULL,
    days_overdue INT NOT NULL,
    outstanding_amount DECIMAL(12,2) NOT NULL,
    delinquency_date DATE NOT NULL,
    risk_level VARCHAR(30),
    resolution_status VARCHAR(30) DEFAULT 'OPEN',

    FOREIGN KEY (loan_id)
        REFERENCES loan(loan_id)
);


-- =========================================================
-- INSERT EMPLOYEES
-- =========================================================

INSERT INTO employee
(name, email, phone, role, username, password)
VALUES
('System Admin',
 'admin@credora.com',
 '9999999999',
 'ADMIN',
 'admin',
 'admin123'),

('Priya Sharma',
 'priya@credora.com',
 '9876543210',
 'LOAN OFFICER',
 'priya',
 'priya123'),

('Rahul Mehta',
 'rahul@credora.com',
 '9867001122',
 'LOAN OFFICER',
 'rahul',
 'rahul123'),

('Neha Patil',
 'neha@credora.com',
 '9822003344',
 'MANAGER',
 'neha',
 'neha123'),

('Amit Joshi',
 'amit@credora.com',
 '9819005566',
 'LOAN OFFICER',
 'amit',
 'amit123');


-- =========================================================
-- INSERT LOAN TYPES
-- =========================================================

INSERT INTO loan_type
(loan_type_name, description, min_amount, max_amount, base_interest_rate)
VALUES
('Personal Loan',
 'General personal-purpose loan',
 10000,
 1000000,
 12.50),

('Education Loan',
 'Loan for education expenses',
 50000,
 2000000,
 9.50),

('Home Loan',
 'Loan for home purchase or construction',
 500000,
 10000000,
 8.50),

('Vehicle Loan',
 'Loan for vehicle purchase',
 50000,
 3000000,
 10.00),

('Business Loan',
 'Loan for small business requirements',
 100000,
 5000000,
 11.25),

('Medical Loan',
 'Loan for medical and healthcare expenses',
 25000,
 1500000,
 10.75);


-- =========================================================
-- INSERT CUSTOMERS
-- =========================================================

INSERT INTO customer
(first_name, last_name, dob, gender, phone, email, address,
 employment_status, monthly_income, credit_score)
VALUES

('Akshay',
 'Kulkarni',
 '2002-04-18',
 'Male',
 '9876500001',
 'akshay@gmail.com',
 'Pune, Maharashtra',
 'EMPLOYED',
 65000,
 768),

('Sneha',
 'Patil',
 '2001-09-12',
 'Female',
 '9876500002',
 'sneha@gmail.com',
 'Nashik, Maharashtra',
 'EMPLOYED',
 58000,
 742),

('Rohan',
 'Deshmukh',
 '1999-12-05',
 'Male',
 '9876500003',
 'rohan@gmail.com',
 'Mumbai, Maharashtra',
 'SELF-EMPLOYED',
 92000,
 781),

('Ananya',
 'Shah',
 '2003-06-21',
 'Female',
 '9876500004',
 'ananya@gmail.com',
 'Ahmedabad, Gujarat',
 'STUDENT',
 35000,
 720),

('Vivek',
 'Joshi',
 '1997-02-14',
 'Male',
 '9876500005',
 'vivek@gmail.com',
 'Nagpur, Maharashtra',
 'EMPLOYED',
 110000,
 805),

('Pooja',
 'Nair',
 '2000-11-30',
 'Female',
 '9876500006',
 'pooja@gmail.com',
 'Bengaluru, Karnataka',
 'EMPLOYED',
 72000,
 755),

('Karan',
 'Verma',
 '1996-08-08',
 'Male',
 '9876500007',
 'karan@gmail.com',
 'Delhi, India',
 'SELF-EMPLOYED',
 125000,
 790),

('Meera',
 'Iyer',
 '2002-01-25',
 'Female',
 '9876500008',
 'meera@gmail.com',
 'Chennai, Tamil Nadu',
 'EMPLOYED',
 67000,
 748),

('Aditya',
 'Singh',
 '1998-05-17',
 'Male',
 '9876500009',
 'aditya@gmail.com',
 'Jaipur, Rajasthan',
 'EMPLOYED',
 83000,
 774),

('Nidhi',
 'Gupta',
 '2001-03-09',
 'Female',
 '9876500010',
 'nidhi@gmail.com',
 'Indore, Madhya Pradesh',
 'EMPLOYED',
 61000,
 735),

('Sahil',
 'Khan',
 '1995-07-22',
 'Male',
 '9876500011',
 'sahil@gmail.com',
 'Hyderabad, Telangana',
 'SELF-EMPLOYED',
 98000,
 716),

('Ishita',
 'Rao',
 '2003-10-11',
 'Female',
 '9876500012',
 'ishita@gmail.com',
 'Pune, Maharashtra',
 'STUDENT',
 30000,
 701);


-- =========================================================
-- INSERT LOANS
-- =========================================================

INSERT INTO loan
(customer_id, loan_type_id, employee_id,
 loan_amount, interest_rate, tenure_months,
 application_date, approval_date, loan_status)
VALUES

(1, 1, 2,
 300000, 12.50, 36,
 '2026-01-10', '2026-01-12', 'APPROVED'),

(2, 2, 3,
 500000, 9.50, 48,
 '2026-01-15', '2026-01-17', 'APPROVED'),

(3, 5, 4,
 1200000, 11.25, 60,
 '2026-02-02', '2026-02-05', 'APPROVED'),

(4, 2, 2,
 250000, 9.50, 36,
 '2026-02-12', NULL, 'PENDING'),

(5, 3, 4,
 4500000, 8.50, 180,
 '2026-02-20', '2026-02-24', 'APPROVED'),

(6, 4, 3,
 800000, 10.00, 60,
 '2026-03-01', '2026-03-03', 'APPROVED'),

(7, 5, 2,
 2000000, 11.25, 72,
 '2026-03-08', NULL, 'PENDING'),

(8, 1, 5,
 150000, 12.50, 24,
 '2026-03-15', '2026-03-16', 'APPROVED'),

(9, 4, 3,
 950000, 10.00, 60,
 '2026-03-21', '2026-03-23', 'APPROVED'),

(10, 1, 5,
 200000, 12.50, 30,
 '2026-04-01', '2026-04-03', 'REJECTED'),

(11, 6, 2,
 300000, 10.75, 24,
 '2026-04-06', '2026-04-07', 'APPROVED'),

(12, 2, 3,
 150000, 9.50, 24,
 '2026-04-12', NULL, 'PENDING');


-- =========================================================
-- INSERT PAYMENTS
-- =========================================================

INSERT INTO payment
(loan_id, payment_date, amount, payment_mode, payment_status)
VALUES

(1, '2026-02-10', 11000, 'UPI', 'SUCCESS'),
(1, '2026-03-10', 11000, 'UPI', 'SUCCESS'),
(1, '2026-04-10', 11000, 'NET BANKING', 'SUCCESS'),

(2, '2026-02-20', 12500, 'UPI', 'SUCCESS'),
(2, '2026-03-20', 12500, 'CARD', 'SUCCESS'),

(3, '2026-03-05', 27000, 'NET BANKING', 'SUCCESS'),

(5, '2026-03-24', 44300, 'BANK TRANSFER', 'SUCCESS'),
(5, '2026-04-24', 44300, 'BANK TRANSFER', 'SUCCESS'),

(6, '2026-04-05', 17000, 'UPI', 'SUCCESS'),

(8, '2026-04-16', 7100, 'UPI', 'SUCCESS'),

(9, '2026-04-23', 20200, 'CARD', 'SUCCESS'),

(11, '2026-05-07', 14500, 'UPI', 'SUCCESS'),
(11, '2026-06-07', 14500, 'UPI', 'SUCCESS');


-- =========================================================
-- INSERT DELINQUENCY RECORDS
-- =========================================================

INSERT INTO delinquency
(loan_id, days_overdue, outstanding_amount,
 delinquency_date, risk_level, resolution_status)
VALUES

(3,
 8,
 1175000,
 '2026-05-15',
 'MEDIUM',
 'OPEN'),

(6,
 22,
 735000,
 '2026-05-20',
 'HIGH',
 'OPEN'),

(9,
 5,
 920000,
 '2026-05-25',
 'LOW',
 'RESOLVED'),

(11,
 31,
 270000,
 '2026-06-10',
 'HIGH',
 'OPEN');


-- =========================================================
-- VERIFY DATABASE
-- =========================================================

USE credora;

SHOW TABLES;

SELECT * FROM employee;

SELECT * FROM customer;

SELECT * FROM loan_type;

SELECT * FROM loan;

SELECT * FROM payment;

SELECT * FROM delinquency;