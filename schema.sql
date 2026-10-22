-- CREDORA: Smart Credit & Loan Management System
-- MySQL 8.0+
-- 9 entity tables based on the ER diagram

DROP DATABASE IF EXISTS credora_db;
CREATE DATABASE credora_db;
USE credora_db;

-- 1. EMPLOYEE (supertype)
CREATE TABLE employee (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    role ENUM('LOAN_OFFICER', 'MANAGER') NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE
);

-- 2. LOAN_OFFICER (subtype of EMPLOYEE)
CREATE TABLE loan_officer (
    employee_id INT PRIMARY KEY,
    officer_specialization VARCHAR(100),
    CONSTRAINT fk_loan_officer_employee
        FOREIGN KEY (employee_id)
        REFERENCES employee(employee_id)
        ON DELETE CASCADE
);

-- 3. MANAGER (subtype of EMPLOYEE)
CREATE TABLE manager (
    employee_id INT PRIMARY KEY,
    department VARCHAR(100) NOT NULL,
    CONSTRAINT fk_manager_employee
        FOREIGN KEY (employee_id)
        REFERENCES employee(employee_id)
        ON DELETE CASCADE
);

-- 4. CUSTOMER
CREATE TABLE customer (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    dob DATE,
    gender VARCHAR(20),
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    address VARCHAR(255),
    employment_status VARCHAR(50),
    monthly_income DECIMAL(15,2),
    credit_score INT,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL
);

-- 5. LOAN_TYPE
CREATE TABLE loan_type (
    loan_type_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_type_name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    min_amount DECIMAL(15,2) NOT NULL,
    max_amount DECIMAL(15,2) NOT NULL,
    base_interest_rate DECIMAL(5,2) NOT NULL,
    CONSTRAINT chk_loan_type_amounts
        CHECK (min_amount >= 0 AND max_amount >= min_amount),
    CONSTRAINT chk_loan_type_interest
        CHECK (base_interest_rate >= 0)
);

-- 6. LOAN
CREATE TABLE loan (
    loan_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    loan_type_id INT NOT NULL,
    employee_id INT NOT NULL,
    loan_amount DECIMAL(15,2) NOT NULL,
    interest_rate DECIMAL(5,2) NOT NULL,
    tenure_months INT NOT NULL,
    application_date DATE NOT NULL,
    approval_date DATE,
    loan_status ENUM('PENDING', 'APPROVED', 'REJECTED', 'CLOSED', 'DEFAULTED')
        NOT NULL DEFAULT 'PENDING',
    CONSTRAINT chk_loan_amount CHECK (loan_amount > 0),
    CONSTRAINT chk_loan_interest CHECK (interest_rate >= 0),
    CONSTRAINT chk_loan_tenure CHECK (tenure_months > 0),
    CONSTRAINT fk_loan_customer
        FOREIGN KEY (customer_id)
        REFERENCES customer(customer_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_loan_type
        FOREIGN KEY (loan_type_id)
        REFERENCES loan_type(loan_type_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_loan_employee
        FOREIGN KEY (employee_id)
        REFERENCES employee(employee_id)
        ON DELETE RESTRICT
);

-- 7. PAYMENT
CREATE TABLE payment (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT NOT NULL,
    payment_date DATE NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    payment_mode ENUM('CASH', 'BANK_TRANSFER', 'UPI', 'CARD', 'OTHER') NOT NULL,
    payment_status ENUM('PENDING', 'SUCCESSFUL', 'FAILED') NOT NULL DEFAULT 'PENDING',
    CONSTRAINT chk_payment_amount CHECK (amount > 0),
    CONSTRAINT fk_payment_loan
        FOREIGN KEY (loan_id)
        REFERENCES loan(loan_id)
        ON DELETE RESTRICT
);

-- 8. DOCUMENT
CREATE TABLE document (
    document_id INT AUTO_INCREMENT PRIMARY KEY,
    loan_id INT NOT NULL,
    document_type VARCHAR(100) NOT NULL,
    document_number VARCHAR(100),
    upload_date DATE NOT NULL,
    verification_status ENUM('PENDING', 'VERIFIED', 'REJECTED')
        NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_document_loan
        FOREIGN KEY (loan_id)
        REFERENCES loan(loan_id)
        ON DELETE RESTRICT
);

-- 9. DELINQUENCY
-- Implemented as a regular entity with its own primary key,
-- matching the SQL key shown here. It is linked to LOAN.
CREATE TABLE delinquency (
    loan_id INT NOT NULL,
    delinquency_id INT NOT NULL,
    days_overdue INT NOT NULL,
    outstanding_amount DECIMAL(15,2) NOT NULL,
    delinquency_date DATE NOT NULL,
    risk_level ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL,
    resolution_status ENUM('OPEN', 'RESOLVED') NOT NULL DEFAULT 'OPEN',
    -- Weak-entity identifying key: owner key + partial key
    PRIMARY KEY (loan_id, delinquency_id),
    CONSTRAINT chk_days_overdue CHECK (days_overdue >= 0),
    CONSTRAINT chk_outstanding_amount CHECK (outstanding_amount >= 0),
    CONSTRAINT fk_delinquency_loan
        FOREIGN KEY (loan_id)
        REFERENCES loan(loan_id)
        ON DELETE RESTRICT
);

-- Optional sample data to demonstrate the relationships
INSERT INTO employee (name, email, password_hash, phone, role, username) VALUES
('Aarav Sharma', 'aarav@example.com', 'REPLACE_WITH_HASH', '9876500001', 'LOAN_OFFICER', 'aarav_officer'),
('Meera Patil', 'meera@example.com', 'REPLACE_WITH_HASH', '9876500002', 'MANAGER', 'meera_manager');

INSERT INTO loan_officer (employee_id, officer_specialization)
VALUES (1, 'Education Loans');

INSERT INTO manager (employee_id, department)
VALUES (2, 'Credit and Loans');

INSERT INTO customer
(dob, gender, phone, email, address, employment_status, monthly_income, credit_score, first_name, last_name)
VALUES
('2003-04-15', 'Other', '9876500010', 'customer1@example.com', 'Pune', 'Employed', 45000.00, 720, 'Rahul', 'Sharma');

INSERT INTO loan_type
(loan_type_name, description, min_amount, max_amount, base_interest_rate)
VALUES
('Education Loan', 'Loan for education-related expenses', 10000.00, 2000000.00, 9.50),
('Personal Loan', 'Loan for personal expenses', 10000.00, 1000000.00, 12.00);

INSERT INTO loan
(customer_id, loan_type_id, employee_id, loan_amount, interest_rate, tenure_months,
 application_date, approval_date, loan_status)
VALUES
(1, 1, 1, 200000.00, 9.50, 36, '2026-10-01', '2026-10-05', 'APPROVED');

INSERT INTO payment
(loan_id, payment_date, amount, payment_mode, payment_status)
VALUES
(1, '2026-10-15', 6000.00, 'UPI', 'SUCCESSFUL');

INSERT INTO document
(loan_id, document_type, document_number, upload_date, verification_status)
VALUES
(1, 'PAN', 'PAN-EXAMPLE-001', '2026-10-01', 'VERIFIED');

INSERT INTO delinquency
(loan_id, delinquency_id, days_overdue, outstanding_amount, delinquency_date, risk_level, resolution_status)
VALUES
(1, 1, 0, 194000.00, '2026-10-22', 'LOW', 'RESOLVED');

-- Verification
SHOW TABLES;
SELECT * FROM employee;
SELECT * FROM loan_officer;
SELECT * FROM manager;
SELECT * FROM customer;
SELECT * FROM loan_type;
SELECT * FROM loan;
SELECT * FROM payment;
SELECT * FROM document;
SELECT * FROM delinquency;

