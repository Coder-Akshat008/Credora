import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CredoraApp extends JFrame {

    // ==============================
    // DATABASE CONNECTION
    // ==============================

    static final String URL = "jdbc:mysql://localhost:3306/credora";
    static final String USER = "root";
    static final String PASSWORD = "sit@123";

    Connection con;

    // ==============================
    // MAIN
    // ==============================

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new CredoraApp().showLogin();
        });
    }

    // ==============================
    // CONSTRUCTOR
    // ==============================

    public CredoraApp() {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            con = DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD
            );

        } catch (ClassNotFoundException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "MySQL Driver Not Found!\n"
                    + "Check the Connector/J JAR in the lib folder.",
                    "Driver Error",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Database Connection Failed!\n\n"
                    + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ==============================
    // LOGIN
    // ==============================

    void showLogin() {

        JFrame frame = new JFrame("Credora - Login");

        frame.setSize(450, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(
                25, 35, 25, 35
        ));

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(8, 8, 8, 8);

        JLabel title = new JLabel("CREDORA");
        title.setFont(new Font("Arial", Font.BOLD, 28));

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        panel.add(title, gbc);

        JLabel userLabel = new JLabel("Username:");
        JTextField username = new JTextField(18);

        JLabel passLabel = new JLabel("Password:");
        JPasswordField password = new JPasswordField(18);

        gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;

        panel.add(userLabel, gbc);

        gbc.gridx = 1;

        panel.add(username, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;

        panel.add(passLabel, gbc);

        gbc.gridx = 1;

        panel.add(password, gbc);

        JButton login = new JButton("LOGIN");

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;

        panel.add(login, gbc);

        login.addActionListener(e -> {

            String u = username.getText();
            String p = new String(password.getPassword());

            loginUser(u, p, frame);
        });

        frame.add(panel);

        frame.setVisible(true);
    }

    // ==============================
    // LOGIN DATABASE CHECK
    // ==============================

    void loginUser(
            String username,
            String password,
            JFrame loginFrame
    ) {

        if (con == null) {

            JOptionPane.showMessageDialog(
                    loginFrame,
                    "Database is not connected!"
            );

            return;
        }

        String sql =
                "SELECT * FROM employee "
                + "WHERE username = ? AND password = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                JOptionPane.showMessageDialog(
                        loginFrame,
                        "Login Successful!"
                );

                loginFrame.dispose();

                dashboard();

            } else {

                JOptionPane.showMessageDialog(
                        loginFrame,
                        "Invalid username or password!",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE
                );
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    loginFrame,
                    "Database Error:\n" + e.getMessage()
            );
        }
    }

    // ==============================
    // DASHBOARD
    // ==============================

    void dashboard() {

        JFrame frame = new JFrame(
                "Credora - Smart Credit & Loan Management System"
        );

        frame.setSize(1100, 700);

        frame.setLocationRelativeTo(null);

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        JPanel top = new JPanel();

        JLabel title = new JLabel(
                "CREDORA - SMART CREDIT & LOAN MANAGEMENT SYSTEM"
        );

        title.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        top.add(title);

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Dashboard", dashboardPanel());
        tabs.addTab("Customers", customersPanel());
        tabs.addTab("Loan Types", loanTypesPanel());
        tabs.addTab("Loans", loansPanel());
        tabs.addTab("Payments", paymentsPanel());
        tabs.addTab("Delinquency", delinquencyPanel());
        tabs.addTab("Reports", reportsPanel());

        frame.add(top, BorderLayout.NORTH);

        frame.add(tabs, BorderLayout.CENTER);

        frame.setVisible(true);
    }

    // ==============================
    // DASHBOARD PANEL
    // ==============================

    JPanel dashboardPanel() {

        JPanel panel = new JPanel(
                new GridLayout(2, 3, 20, 20)
        );

        panel.setBorder(
                BorderFactory.createEmptyBorder(
                        40, 40, 40, 40
                )
        );

        panel.add(
                createDashboardCard(
                        "CUSTOMERS",
                        "SELECT COUNT(*) FROM customer"
                )
        );

        panel.add(
                createDashboardCard(
                        "LOANS",
                        "SELECT COUNT(*) FROM loan"
                )
        );

        panel.add(
                createDashboardCard(
                        "APPROVED",
                        "SELECT COUNT(*) FROM loan "
                        + "WHERE loan_status='APPROVED'"
                )
        );

        panel.add(
                createDashboardCard(
                        "PENDING",
                        "SELECT COUNT(*) FROM loan "
                        + "WHERE loan_status='PENDING'"
                )
        );

        panel.add(
                createDashboardCard(
                        "PAYMENTS",
                        "SELECT COUNT(*) FROM payment"
                )
        );

        panel.add(
                createDashboardCard(
                        "DELINQUENCIES",
                        "SELECT COUNT(*) FROM delinquency"
                )
        );

        return panel;
    }

    JPanel createDashboardCard(
            String title,
            String sql
    ) {

        JPanel card = new JPanel(
                new BorderLayout()
        );

        card.setBorder(
                BorderFactory.createTitledBorder(title)
        );

        JLabel value = new JLabel(
                "0",
                SwingConstants.CENTER
        );

        value.setFont(
                new Font("Arial", Font.BOLD, 32)
        );

        card.add(value, BorderLayout.CENTER);

        try (
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)
        ) {

            if (rs.next()) {

                value.setText(
                        String.valueOf(rs.getInt(1))
                );
            }

        } catch (SQLException e) {

            value.setText("ERR");
        }

        return card;
    }

    String formatMoney(double value) {
    return String.format("₹%,.2f", value);
    }   
    // ==============================
    // CUSTOMERS
    // ==============================

    JPanel customersPanel() {

        JPanel panel = new JPanel(
                new BorderLayout()
        );

        String[] columns = {
                "ID",
                "First Name",
                "Last Name",
                "Phone",
                "Email",
                "Employment",
                "Income",
                "Credit Score"
        };

        DefaultTableModel model =
                new DefaultTableModel(columns, 0);

        JTable table = new JTable(model);

        loadCustomers(model);

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        JPanel buttons = new JPanel();

        JButton add = new JButton("Add Customer");
        JButton delete = new JButton("Delete");
        JButton refresh = new JButton("Refresh");

        buttons.add(add);
        buttons.add(delete);
        buttons.add(refresh);

        panel.add(
                buttons,
                BorderLayout.SOUTH
        );

        add.addActionListener(e ->
                addCustomer(model)
        );

        delete.addActionListener(e -> {

            int row = table.getSelectedRow();

            if (row == -1) {

                JOptionPane.showMessageDialog(
                        panel,
                        "Select a customer first."
                );

                return;
            }

            int id = (int) model.getValueAt(row, 0);

            try {

                PreparedStatement ps =
                        con.prepareStatement(
                                "DELETE FROM customer "
                                + "WHERE customer_id=?"
                        );

                ps.setInt(1, id);

                ps.executeUpdate();

                loadCustomers(model);

            } catch (SQLException ex) {

                JOptionPane.showMessageDialog(
                        panel,
                        ex.getMessage()
                );
            }
        });

        refresh.addActionListener(e ->
                loadCustomers(model)
        );

        return panel;
    }

    void loadCustomers(DefaultTableModel model) {

        model.setRowCount(0);

        String sql =
                "SELECT customer_id, first_name, last_name, "
                + "phone, email, employment_status, "
                + "monthly_income, credit_score "
                + "FROM customer";

        try (
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)
        ) {

            while (rs.next()) {

                model.addRow(new Object[]{

                        rs.getInt("customer_id"),

                        rs.getString("first_name"),

                        rs.getString("last_name"),

                        rs.getString("phone"),

                        rs.getString("email"),

                        rs.getString("employment_status"),

                        formatMoney(rs.getDouble("monthly_income")),

                        rs.getInt("credit_score")
                });
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage()
            );
        }
    }

    void addCustomer(DefaultTableModel model) {

        JTextField first = new JTextField();
        JTextField last = new JTextField();
        JTextField phone = new JTextField();
        JTextField email = new JTextField();
        JTextField employment = new JTextField();
        JTextField income = new JTextField();
        JTextField score = new JTextField();

        JPanel panel = new JPanel(
                new GridLayout(7, 2, 5, 5)
        );

        panel.add(new JLabel("First Name"));
        panel.add(first);

        panel.add(new JLabel("Last Name"));
        panel.add(last);

        panel.add(new JLabel("Phone"));
        panel.add(phone);

        panel.add(new JLabel("Email"));
        panel.add(email);

        panel.add(new JLabel("Employment"));
        panel.add(employment);

        panel.add(new JLabel("Monthly Income"));
        panel.add(income);

        panel.add(new JLabel("Credit Score"));
        panel.add(score);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Add Customer",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String sql =
                "INSERT INTO customer "
                + "(first_name,last_name,phone,email,"
                + "employment_status,monthly_income,credit_score)"
                + " VALUES (?,?,?,?,?,?,?)";

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, first.getText());
            ps.setString(2, last.getText());
            ps.setString(3, phone.getText());
            ps.setString(4, email.getText());
            ps.setString(5, employment.getText());
            ps.setDouble(6, Double.parseDouble(income.getText()));
            ps.setInt(7, Integer.parseInt(score.getText()));

            ps.executeUpdate();

            loadCustomers(model);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage()
            );
        }
    }

    // ==============================
    // LOAN TYPES
    // ==============================

    JPanel loanTypesPanel() {

        JPanel panel = new JPanel(
                new BorderLayout()
        );

        String[] columns = {
                "ID",
                "Loan Type",
                "Description",
                "Min Amount",
                "Max Amount",
                "Interest Rate"
        };

        DefaultTableModel model =
                new DefaultTableModel(columns, 0);

        JTable table = new JTable(model);

        loadLoanTypes(model);

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        JPanel buttons = new JPanel();

        JButton add = new JButton("Add Loan Type");
        JButton delete = new JButton("Delete");
        JButton refresh = new JButton("Refresh");

        buttons.add(add);
        buttons.add(delete);
        buttons.add(refresh);

        panel.add(buttons, BorderLayout.SOUTH);

        add.addActionListener(e ->
                addLoanType(model)
        );

        delete.addActionListener(e -> {

            int row = table.getSelectedRow();

            if (row == -1) {

                JOptionPane.showMessageDialog(
                        panel,
                        "Select a loan type first."
                );

                return;
            }

            int id =
                    (int) model.getValueAt(row, 0);

            try {

                PreparedStatement ps =
                        con.prepareStatement(
                                "DELETE FROM loan_type "
                                + "WHERE loan_type_id=?"
                        );

                ps.setInt(1, id);

                ps.executeUpdate();

                loadLoanTypes(model);

            } catch (SQLException ex) {

                JOptionPane.showMessageDialog(
                        panel,
                        ex.getMessage()
                );
            }
        });

        refresh.addActionListener(e ->
                loadLoanTypes(model)
        );

        return panel;
    }

    void loadLoanTypes(DefaultTableModel model) {

        model.setRowCount(0);

        try (
                Statement st = con.createStatement();
                ResultSet rs =
                        st.executeQuery(
                                "SELECT * FROM loan_type"
                        )
        ) {

            while (rs.next()) {

                model.addRow(new Object[]{

                        rs.getInt("loan_type_id"),

                        rs.getString("loan_type_name"),

                        rs.getString("description"),

                        rs.getDouble("min_amount"),

                        rs.getDouble("max_amount"),

                        rs.getDouble("base_interest_rate")
                });
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage()
            );
        }
    }

    void addLoanType(DefaultTableModel model) {

        JTextField name = new JTextField();
        JTextField description = new JTextField();
        JTextField min = new JTextField();
        JTextField max = new JTextField();
        JTextField rate = new JTextField();

        JPanel panel = new JPanel(
                new GridLayout(5, 2, 5, 5)
        );

        panel.add(new JLabel("Loan Type"));
        panel.add(name);

        panel.add(new JLabel("Description"));
        panel.add(description);

        panel.add(new JLabel("Minimum Amount"));
        panel.add(min);

        panel.add(new JLabel("Maximum Amount"));
        panel.add(max);

        panel.add(new JLabel("Interest Rate"));
        panel.add(rate);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Add Loan Type",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            PreparedStatement ps =
                    con.prepareStatement(
                            "INSERT INTO loan_type "
                            + "(loan_type_name,description,"
                            + "min_amount,max_amount,"
                            + "base_interest_rate)"
                            + " VALUES (?,?,?,?,?)"
                    );

            ps.setString(1, name.getText());
            ps.setString(2, description.getText());
            ps.setDouble(3, Double.parseDouble(min.getText()));
            ps.setDouble(4, Double.parseDouble(max.getText()));
            ps.setDouble(5, Double.parseDouble(rate.getText()));

            ps.executeUpdate();

            loadLoanTypes(model);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage()
            );
        }
    }

    // ==============================
    // LOANS
    // ==============================

    JPanel loansPanel() {

        JPanel panel = new JPanel(
                new BorderLayout()
        );

        String[] columns = {
                "Loan ID",
                "Customer",
                "Loan Type",
                "Employee",
                "Amount",
                "Interest",
                "Tenure",
                "Status"
        };

        DefaultTableModel model =
                new DefaultTableModel(columns, 0);

        JTable table = new JTable(model);

        loadLoans(model);

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        JPanel buttons = new JPanel();

        JButton create = new JButton("Create Loan");
        JButton approve = new JButton("Approve");
        JButton reject = new JButton("Reject");
        JButton refresh = new JButton("Refresh");

        buttons.add(create);
        buttons.add(approve);
        buttons.add(reject);
        buttons.add(refresh);

        panel.add(buttons, BorderLayout.SOUTH);

        create.addActionListener(e ->
                createLoan(model)
        );

        approve.addActionListener(e ->
                updateLoanStatus(table, model, "APPROVED")
        );

        reject.addActionListener(e ->
                updateLoanStatus(table, model, "REJECTED")
        );

        refresh.addActionListener(e ->
                loadLoans(model)
        );

        return panel;
    }

    void loadLoans(DefaultTableModel model) {

        model.setRowCount(0);

        String sql =
                "SELECT l.loan_id, "
                + "CONCAT(c.first_name,' ',c.last_name) AS customer, "
                + "lt.loan_type_name, "
                + "e.name AS employee, "
                + "l.loan_amount, "
                + "l.interest_rate, "
                + "l.tenure_months, "
                + "l.loan_status "
                + "FROM loan l "
                + "JOIN customer c "
                + "ON l.customer_id=c.customer_id "
                + "JOIN loan_type lt "
                + "ON l.loan_type_id=lt.loan_type_id "
                + "JOIN employee e "
                + "ON l.employee_id=e.employee_id";

        try (
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)
        ) {

            while (rs.next()) {

                model.addRow(new Object[]{

                        rs.getInt("loan_id"),

                        rs.getString("customer"),

                        rs.getString("loan_type_name"),

                        rs.getString("employee"),

                        formatMoney(rs.getDouble("loan_amount")),

                        rs.getDouble("interest_rate"),

                        rs.getInt("tenure_months"),

                        rs.getString("loan_status")
                });
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage()
            );
        }
    }

    void updateLoanStatus(
            JTable table,
            DefaultTableModel model,
            String status
    ) {

        int row = table.getSelectedRow();

        if (row == -1) {

            JOptionPane.showMessageDialog(
                    null,
                    "Select a loan first."
            );

            return;
        }

        int id =
                (int) model.getValueAt(row, 0);

        String sql =
                "UPDATE loan "
                + "SET loan_status=?, approval_date=CURDATE() "
                + "WHERE loan_id=?";

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, id);

            ps.executeUpdate();

            loadLoans(model);

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage()
            );
        }
    }

    void createLoan(DefaultTableModel model) {

        JTextField customer =
                new JTextField();

        JTextField type =
                new JTextField();

        JTextField employee =
                new JTextField();

        JTextField amount =
                new JTextField();

        JTextField rate =
                new JTextField();

        JTextField tenure =
                new JTextField();

        JPanel panel = new JPanel(
                new GridLayout(6, 2, 5, 5)
        );

        panel.add(new JLabel("Customer ID"));
        panel.add(customer);

        panel.add(new JLabel("Loan Type ID"));
        panel.add(type);

        panel.add(new JLabel("Employee ID"));
        panel.add(employee);

        panel.add(new JLabel("Loan Amount"));
        panel.add(amount);

        panel.add(new JLabel("Interest Rate"));
        panel.add(rate);

        panel.add(new JLabel("Tenure (Months)"));
        panel.add(tenure);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Create Loan",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String sql =
                "INSERT INTO loan "
                + "(customer_id,loan_type_id,employee_id,"
                + "loan_amount,interest_rate,tenure_months,"
                + "application_date,loan_status)"
                + " VALUES (?,?,?,?,?,?,CURDATE(),'PENDING')";

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    Integer.parseInt(customer.getText())
            );

            ps.setInt(
                    2,
                    Integer.parseInt(type.getText())
            );

            ps.setInt(
                    3,
                    Integer.parseInt(employee.getText())
            );

            ps.setDouble(
                    4,
                    Double.parseDouble(amount.getText())
            );

            ps.setDouble(
                    5,
                    Double.parseDouble(rate.getText())
            );

            ps.setInt(
                    6,
                    Integer.parseInt(tenure.getText())
            );

            ps.executeUpdate();

            loadLoans(model);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage()
            );
        }
    }

    // ==============================
    // PAYMENTS
    // ==============================

    JPanel paymentsPanel() {

        JPanel panel = new JPanel(
                new BorderLayout()
        );

        String[] columns = {
                "Payment ID",
                "Loan ID",
                "Date",
                "Amount",
                "Mode",
                "Status"
        };

        DefaultTableModel model =
                new DefaultTableModel(columns, 0);

        JTable table = new JTable(model);

        loadPayments(model);

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        JButton add =
                new JButton("Record Payment");

        JButton refresh =
                new JButton("Refresh");

        JPanel buttons = new JPanel();

        buttons.add(add);
        buttons.add(refresh);

        panel.add(
                buttons,
                BorderLayout.SOUTH
        );

        add.addActionListener(e ->
                addPayment(model)
        );

        refresh.addActionListener(e ->
                loadPayments(model)
        );

        return panel;
    }

    void loadPayments(DefaultTableModel model) {

        model.setRowCount(0);

        try (
                Statement st = con.createStatement();
                ResultSet rs =
                        st.executeQuery(
                                "SELECT * FROM payment"
                        )
        ) {

            while (rs.next()) {

                model.addRow(new Object[]{

                        rs.getInt("payment_id"),

                        rs.getInt("loan_id"),

                        rs.getDate("payment_date"),

                        formatMoney(rs.getDouble("amount")),

                        rs.getString("payment_mode"),

                        rs.getString("payment_status")
                });
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage()
            );
        }
    }

    void addPayment(DefaultTableModel model) {

        JTextField loan =
                new JTextField();

        JTextField amount =
                new JTextField();

        JTextField mode =
                new JTextField();

        JPanel panel = new JPanel(
                new GridLayout(3, 2, 5, 5)
        );

        panel.add(new JLabel("Loan ID"));
        panel.add(loan);

        panel.add(new JLabel("Amount"));
        panel.add(amount);

        panel.add(new JLabel("Payment Mode"));
        panel.add(mode);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Record Payment",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String sql =
                "INSERT INTO payment "
                + "(loan_id,payment_date,amount,"
                + "payment_mode,payment_status)"
                + " VALUES (?,CURDATE(),?,?,'SUCCESS')";

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    Integer.parseInt(loan.getText())
            );

            ps.setDouble(
                    2,
                    Double.parseDouble(amount.getText())
            );

            ps.setString(
                    3,
                    mode.getText()
            );

            ps.executeUpdate();

            loadPayments(model);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage()
            );
        }
    }

    // ==============================
    // DELINQUENCY
    // ==============================

    JPanel delinquencyPanel() {

        JPanel panel = new JPanel(
                new BorderLayout()
        );

        String[] columns = {
                "ID",
                "Loan ID",
                "Days Overdue",
                "Outstanding",
                "Date",
                "Risk",
                "Status"
        };

        DefaultTableModel model =
                new DefaultTableModel(columns, 0);

        JTable table = new JTable(model);

        loadDelinquencies(model);

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        JButton add =
                new JButton("Add Delinquency");

        JButton resolve =
                new JButton("Resolve");

        JButton refresh =
                new JButton("Refresh");

        JPanel buttons = new JPanel();

        buttons.add(add);
        buttons.add(resolve);
        buttons.add(refresh);

        panel.add(
                buttons,
                BorderLayout.SOUTH
        );

        add.addActionListener(e ->
                addDelinquency(model)
        );

        resolve.addActionListener(e -> {

            int row = table.getSelectedRow();

            if (row == -1) {

                JOptionPane.showMessageDialog(
                        null,
                        "Select a delinquency first."
                );

                return;
            }

            int id =
                    (int) model.getValueAt(row, 0);

            try {

                PreparedStatement ps =
                        con.prepareStatement(
                                "UPDATE delinquency "
                                + "SET resolution_status='RESOLVED' "
                                + "WHERE delinquency_id=?"
                        );

                ps.setInt(1, id);

                ps.executeUpdate();

                loadDelinquencies(model);

            } catch (SQLException ex) {

                JOptionPane.showMessageDialog(
                        null,
                        ex.getMessage()
                );
            }
        });

        refresh.addActionListener(e ->
                loadDelinquencies(model)
        );

        return panel;
    }

    void loadDelinquencies(
            DefaultTableModel model
    ) {

        model.setRowCount(0);

        try (
                Statement st = con.createStatement();
                ResultSet rs =
                        st.executeQuery(
                                "SELECT * FROM delinquency"
                        )
        ) {

            while (rs.next()) {

                model.addRow(new Object[]{

                        rs.getInt("delinquency_id"),

                        rs.getInt("loan_id"),

                        rs.getInt("days_overdue"),

                        rs.getDouble(
                                "outstanding_amount"
                        ),

                        rs.getDate(
                                "delinquency_date"
                        ),

                        rs.getString("risk_level"),

                        rs.getString(
                                "resolution_status"
                        )
                });
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage()
            );
        }
    }

    void addDelinquency(
            DefaultTableModel model
    ) {

        JTextField loan =
                new JTextField();

        JTextField days =
                new JTextField();

        JTextField outstanding =
                new JTextField();

        JTextField risk =
                new JTextField();

        JPanel panel = new JPanel(
                new GridLayout(4, 2, 5, 5)
        );

        panel.add(new JLabel("Loan ID"));
        panel.add(loan);

        panel.add(new JLabel("Days Overdue"));
        panel.add(days);

        panel.add(new JLabel("Outstanding Amount"));
        panel.add(outstanding);

        panel.add(new JLabel("Risk Level"));
        panel.add(risk);

        int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "Add Delinquency",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        String sql =
                "INSERT INTO delinquency "
                + "(loan_id,days_overdue,"
                + "outstanding_amount,delinquency_date,"
                + "risk_level,resolution_status)"
                + " VALUES (?,?,?,CURDATE(),?,'OPEN')";

        try (PreparedStatement ps =
                con.prepareStatement(sql)) {

            ps.setInt(
                    1,
                    Integer.parseInt(loan.getText())
            );

            ps.setInt(
                    2,
                    Integer.parseInt(days.getText())
            );

            ps.setDouble(
                    3,
                    Double.parseDouble(
                            outstanding.getText()
                    )
            );

            ps.setString(
                    4,
                    risk.getText()
            );

            ps.executeUpdate();

            loadDelinquencies(model);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage()
            );
        }
    }

    // ==============================
    // REPORTS
    // ==============================

    JPanel reportsPanel() {

        JPanel panel = new JPanel(
                new BorderLayout()
        );

        String[] options = {

                "All Customers",

                "Approved Loans",

                "Pending Loans",

                "Rejected Loans",

                "Payment History",

                "High Risk Delinquencies"
        };

        JComboBox<String> combo =
                new JComboBox<>(options);

        JButton generate =
                new JButton("Generate Report");

        JPanel top = new JPanel();

        top.add(combo);
        top.add(generate);

        panel.add(
                top,
                BorderLayout.NORTH
        );

        DefaultTableModel model =
                new DefaultTableModel();

        JTable table = new JTable(model);

        panel.add(
                new JScrollPane(table),
                BorderLayout.CENTER
        );

        generate.addActionListener(e -> {

            int choice =
                    combo.getSelectedIndex();

            String sql;

            if (choice == 0) {

                sql =
                        "SELECT * FROM customer";

            } else if (choice == 1) {

                sql =
                        "SELECT * FROM loan "
                        + "WHERE loan_status='APPROVED'";

            } else if (choice == 2) {

                sql =
                        "SELECT * FROM loan "
                        + "WHERE loan_status='PENDING'";

            } else if (choice == 3) {

                sql =
                        "SELECT * FROM loan "
                        + "WHERE loan_status='REJECTED'";

            } else if (choice == 4) {

                sql =
                        "SELECT * FROM payment";

            } else {

                sql =
                        "SELECT * FROM delinquency "
                        + "WHERE risk_level='HIGH'";
            }

            loadReport(model, sql);
        });

        return panel;
    }

    void loadReport(
            DefaultTableModel model,
            String sql
    ) {

        model.setRowCount(0);
        model.setColumnCount(0);

        try (
                Statement st = con.createStatement();
                ResultSet rs = st.executeQuery(sql)
        ) {

            ResultSetMetaData meta =
                    rs.getMetaData();

            int columns =
                    meta.getColumnCount();

            for (int i = 1; i <= columns; i++) {

                model.addColumn(
                        meta.getColumnName(i)
                );
            }

            while (rs.next()) {

                Object[] row =
                        new Object[columns];

                for (int i = 0; i < columns; i++) {

                    row[i] =
                            rs.getObject(i + 1);
                }

                model.addRow(row);
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    e.getMessage()
            );
        }
    }
}