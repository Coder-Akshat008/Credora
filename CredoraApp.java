import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

// ============================================================================
// ABSTRACT SUPERCLASS: INHERITANCE & BASE GUI TEMPLATE
// ============================================================================
abstract class BaseDatabaseModule extends JPanel {
    protected final Connection con;

    public BaseDatabaseModule(Connection con) {
        this.con = con;
        setLayout(new BorderLayout(10, 10));
    }

    protected <T> void executeAsync(WorkerSupplier<T> bgTask, ResultHandler<T> onSuccess) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return bgTask.doWork();
            }

            @Override
            protected void done() {
                try {
                    T result = get();
                    if (onSuccess != null) {
                        onSuccess.handle(result);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    showError("Operation interrupted.");
                } catch (ExecutionException e) {
                    e.printStackTrace();
                    showError("Database Error: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
                }
            }
        }.execute();
    }

    protected void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    protected void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    protected DefaultTableModel createNonEditableModel(String[] headers) {
        return new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    @FunctionalInterface
    public interface WorkerSupplier<T> {
        T doWork() throws Exception;
    }

    @FunctionalInterface
    public interface ResultHandler<T> {
        void handle(T result);
    }
}

// ============================================================================
// MODULE 1: CUSTOMER OPERATIONS
// ============================================================================
class CustomerModule extends BaseDatabaseModule {
    private final DefaultTableModel tableModel;
    private final JTable table;

    public CustomerModule(Connection con) {
        super(con);
        tableModel = createNonEditableModel(new String[]{"Customer ID", "Full Name", "Email", "Phone"});
        table = new JTable(tableModel);
        initUI();
        loadCustomers();
    }

    private void initUI() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Customer");
        JButton updateBtn = new JButton("Update Customer");
        JButton deleteBtn = new JButton("Soft Delete Customer");
        JButton refreshBtn = new JButton("Refresh");

        btnPanel.add(addBtn);
        btnPanel.add(updateBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);

        add(btnPanel, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        addBtn.addActionListener(e -> addCustomer());
        updateBtn.addActionListener(e -> updateCustomer());
        deleteBtn.addActionListener(e -> softDeleteCustomer());
        refreshBtn.addActionListener(e -> loadCustomers());
    }

    public void loadCustomers() {
        tableModel.setRowCount(0);
        executeAsync(() -> {
            String sql = "SELECT customer_id, full_name, email, phone FROM customer WHERE is_deleted = 0";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                List<Object[]> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getInt("customer_id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        rs.getString("phone")
                    });
                }
                return rows;
            }
        }, rows -> rows.forEach(tableModel::addRow));
    }

    private void addCustomer() {
        JTextField nameF = new JTextField();
        JTextField emailF = new JTextField();
        JTextField phoneF = new JTextField();
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.add(new JLabel("Full Name:")); form.add(nameF);
        form.add(new JLabel("Email:")); form.add(emailF);
        form.add(new JLabel("Phone:")); form.add(phoneF);

        if (JOptionPane.showConfirmDialog(this, form, "Add Customer", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            String name = nameF.getText().trim();
            String email = emailF.getText().trim();
            String phone = phoneF.getText().trim();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty()) {
                showError("All fields are required.");
                return;
            }

            executeAsync(() -> {
                String sql = "INSERT INTO customer (full_name, email, phone) VALUES (?, ?, ?)";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.setString(3, phone);
                    ps.executeUpdate();
                }
                return null;
            }, res -> {
                showInfo("Customer added successfully.");
                loadCustomers();
            });
        }
    }

    private void updateCustomer() {
        int row = table.getSelectedRow();
        if (row == -1) { showError("Select a customer to update."); return; }
        int id = (int) tableModel.getValueAt(row, 0);

        JTextField nameF = new JTextField((String) tableModel.getValueAt(row, 1));
        JTextField emailF = new JTextField((String) tableModel.getValueAt(row, 2));
        JTextField phoneF = new JTextField((String) tableModel.getValueAt(row, 3));
        JPanel form = new JPanel(new GridLayout(3, 2, 5, 5));
        form.add(new JLabel("Full Name:")); form.add(nameF);
        form.add(new JLabel("Email:")); form.add(emailF);
        form.add(new JLabel("Phone:")); form.add(phoneF);

        if (JOptionPane.showConfirmDialog(this, form, "Update Customer", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            executeAsync(() -> {
                String sql = "UPDATE customer SET full_name = ?, email = ?, phone = ? WHERE customer_id = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, nameF.getText().trim());
                    ps.setString(2, emailF.getText().trim());
                    ps.setString(3, phoneF.getText().trim());
                    ps.setInt(4, id);
                    ps.executeUpdate();
                }
                return null;
            }, res -> {
                showInfo("Customer updated successfully.");
                loadCustomers();
            });
        }
    }

    private void softDeleteCustomer() {
        int row = table.getSelectedRow();
        if (row == -1) { showError("Select a customer to soft-delete."); return; }
        int id = (int) tableModel.getValueAt(row, 0);

        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to soft-delete Customer ID: " + id + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            executeAsync(() -> {
                String sql = "UPDATE customer SET is_deleted = 1 WHERE customer_id = ?";
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
                return null;
            }, res -> {
                showInfo("Customer soft-deleted successfully.");
                loadCustomers();
            });
        }
    }
}

// ============================================================================
// MODULE 2: LOAN LIFECYCLE MANAGEMENT
// ============================================================================
class LoanModule extends BaseDatabaseModule {
    private final DefaultTableModel tableModel;

    public LoanModule(Connection con) {
        super(con);
        tableModel = createNonEditableModel(new String[]{"Loan ID", "Customer ID", "Customer Name", "Principal", "Remaining", "Rate (%)", "Tenure (m)", "Start Date", "Status"});
        initUI();
        loadLoans();
    }

    private void initUI() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton createBtn = new JButton("Create New Loan");
        JButton refreshBtn = new JButton("Refresh");

        btnPanel.add(createBtn);
        btnPanel.add(refreshBtn);

        add(btnPanel, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);

        createBtn.addActionListener(e -> createLoan());
        refreshBtn.addActionListener(e -> loadLoans());
    }

    public void loadLoans() {
        tableModel.setRowCount(0);
        executeAsync(() -> {
            String sql = "SELECT l.loan_id, l.customer_id, c.full_name, l.principal_amount, l.remaining_balance, " +
                         "l.interest_rate, l.tenure_months, l.start_date, l.loan_status " +
                         "FROM loan l JOIN customer c ON l.customer_id = c.customer_id";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                List<Object[]> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getInt("loan_id"),
                        rs.getInt("customer_id"),
                        rs.getString("full_name"),
                        rs.getBigDecimal("principal_amount"),
                        rs.getBigDecimal("remaining_balance"),
                        rs.getBigDecimal("interest_rate"),
                        rs.getInt("tenure_months"),
                        rs.getDate("start_date"),
                        rs.getString("loan_status")
                    });
                }
                return rows;
            }
        }, rows -> rows.forEach(tableModel::addRow));
    }

    private void createLoan() {
        JTextField custIdF = new JTextField();
        JTextField principalF = new JTextField();
        JTextField rateF = new JTextField();
        JTextField tenureF = new JTextField();
        JTextField startDateF = new JTextField("2026-01-01");

        JPanel form = new JPanel(new GridLayout(5, 2, 5, 5));
        form.add(new JLabel("Customer ID:")); form.add(custIdF);
        form.add(new JLabel("Principal Amount:")); form.add(principalF);
        form.add(new JLabel("Interest Rate (%):")); form.add(rateF);
        form.add(new JLabel("Tenure (Months):")); form.add(tenureF);
        form.add(new JLabel("Start Date (YYYY-MM-DD):")); form.add(startDateF);

        if (JOptionPane.showConfirmDialog(this, form, "Create Loan Record", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int cid = Integer.parseInt(custIdF.getText().trim());
                BigDecimal principal = new BigDecimal(principalF.getText().trim()).setScale(2, RoundingMode.HALF_UP);
                BigDecimal rate = new BigDecimal(rateF.getText().trim()).setScale(2, RoundingMode.HALF_UP);
                int tenure = Integer.parseInt(tenureF.getText().trim());
                Date startDate = Date.valueOf(startDateF.getText().trim());

                if (principal.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ZERO) < 0 || tenure <= 0) {
                    showError("Please enter valid positive values for loan parameters.");
                    return;
                }

                executeAsync(() -> {
                    String sql = "INSERT INTO loan (customer_id, principal_amount, remaining_balance, interest_rate, tenure_months, start_date, loan_status) VALUES (?, ?, ?, ?, ?, ?, 'APPROVED')";
                    try (PreparedStatement ps = con.prepareStatement(sql)) {
                        ps.setInt(1, cid);
                        ps.setBigDecimal(2, principal);
                        ps.setBigDecimal(3, principal);
                        ps.setBigDecimal(4, rate);
                        ps.setInt(5, tenure);
                        ps.setDate(6, startDate);
                        ps.executeUpdate();
                    }
                    return null;
                }, res -> {
                    showInfo("Loan created successfully.");
                    loadLoans();
                });
            } catch (NumberFormatException ex) {
                showError("Invalid numeric format entered into fields.");
            } catch (IllegalArgumentException ex) {
                showError("Invalid date format. Use YYYY-MM-DD.");
            }
        }
    }
}

// ============================================================================
// MODULE 3: REPAYMENT PROCESSING WITH SQL TRANSACTIONS
// ============================================================================
class RepaymentModule extends BaseDatabaseModule {
    public RepaymentModule(Connection con) {
        super(con);
        initUI();
    }

    private void initUI() {
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        JTextField loanIdF = new JTextField();
        JTextField amountF = new JTextField();
        JButton payBtn = new JButton("Process Payment");

        formPanel.setBorder(BorderFactory.createTitledBorder("Record Repayment"));
        formPanel.add(new JLabel("Loan ID:")); formPanel.add(loanIdF);
        formPanel.add(new JLabel("Payment Amount:")); formPanel.add(amountF);
        formPanel.add(new JLabel("")); formPanel.add(payBtn);

        add(formPanel, BorderLayout.NORTH);

        payBtn.addActionListener(e -> {
            try {
                int loanId = Integer.parseInt(loanIdF.getText().trim());
                BigDecimal amount = new BigDecimal(amountF.getText().trim()).setScale(2, RoundingMode.HALF_UP);

                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    showError("Payment amount must be positive.");
                    return;
                }

                executeAsync(() -> processTransaction(loanId, amount), res -> {
                    showInfo("Payment applied successfully. Balance updated.");
                    loanIdF.setText("");
                    amountF.setText("");
                });
            } catch (NumberFormatException ex) {
                showError("Please enter valid numeric inputs.");
            }
        });
    }

    private Boolean processTransaction(int loanId, BigDecimal amount) throws SQLException {
        boolean originalAutoCommit = con.getAutoCommit();
        try {
            con.setAutoCommit(false);

            String checkSql = "SELECT remaining_balance FROM loan WHERE loan_id = ? FOR UPDATE";
            BigDecimal currentBalance;
            try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                ps.setInt(1, loanId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentBalance = rs.getBigDecimal("remaining_balance");
                    } else {
                        throw new SQLException("Loan ID " + loanId + " does not exist.");
                    }
                }
            }

            if (amount.compareTo(currentBalance) > 0) {
                throw new SQLException("Payment amount (" + amount + ") exceeds remaining balance (" + currentBalance + ").");
            }

            String paySql = "INSERT INTO payment (loan_id, amount_paid) VALUES (?, ?)";
            try (PreparedStatement ps = con.prepareStatement(paySql)) {
                ps.setInt(1, loanId);
                ps.setBigDecimal(2, amount);
                ps.executeUpdate();
            }

            BigDecimal newBalance = currentBalance.subtract(amount);
            String status = (newBalance.compareTo(BigDecimal.ZERO) == 0) ? "CLOSED" : "APPROVED";
            String updateSql = "UPDATE loan SET remaining_balance = ?, loan_status = ? WHERE loan_id = ?";
            try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                ps.setBigDecimal(1, newBalance);
                ps.setString(2, status);
                ps.setInt(3, loanId);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(originalAutoCommit);
        }
    }
}

// ============================================================================
// MODULE 4: DELINQUENCY MONITORING
// ============================================================================
class DelinquencyModule extends BaseDatabaseModule {
    private final DefaultTableModel tableModel;

    public DelinquencyModule(Connection con) {
        super(con);
        tableModel = createNonEditableModel(new String[]{"Delinquency ID", "Loan ID", "Days Overdue", "Outstanding", "Date", "Risk", "Status"});
        initUI();
        loadDelinquencies();
    }

    private void initUI() {
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton auditBtn = new JButton("Run Dynamic Delinquency Audit");
        JButton refreshBtn = new JButton("Refresh Table");

        btnPanel.add(auditBtn);
        btnPanel.add(refreshBtn);

        add(btnPanel, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(tableModel)), BorderLayout.CENTER);

        auditBtn.addActionListener(e -> runAudit());
        refreshBtn.addActionListener(e -> loadDelinquencies());
    }

    public void loadDelinquencies() {
        tableModel.setRowCount(0);
        executeAsync(() -> {
            String sql = "SELECT delinquency_id, loan_id, days_overdue, outstanding_amount, delinquency_date, risk_level, resolution_status FROM delinquency";
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                List<Object[]> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(new Object[]{
                        rs.getInt("delinquency_id"),
                        rs.getInt("loan_id"),
                        rs.getInt("days_overdue"),
                        rs.getBigDecimal("outstanding_amount"),
                        rs.getDate("delinquency_date"),
                        rs.getString("risk_level"),
                        rs.getString("resolution_status")
                    });
                }
                return rows;
            }
        }, rows -> rows.forEach(tableModel::addRow));
    }

    private void runAudit() {
        executeAsync(() -> {
            String auditSql = "SELECT loan_id, remaining_balance, GREATEST(DATEDIFF(CURDATE(), start_date) - 30, 0) AS overdue_days " +
                              "FROM loan WHERE loan_status = 'APPROVED' AND remaining_balance > 0 AND DATEDIFF(CURDATE(), start_date) > 30";
            String insertSql = "INSERT INTO delinquency (loan_id, days_overdue, outstanding_amount, delinquency_date, risk_level, resolution_status) " +
                              "VALUES (?, ?, ?, CURDATE(), ?, 'OPEN')";
            
            int flaggedCount = 0;
            try (Statement st = con.createStatement(); ResultSet rs = st.executeQuery(auditSql)) {
                while (rs.next()) {
                    int loanId = rs.getInt("loan_id");
                    int daysOverdue = rs.getInt("overdue_days");
                    BigDecimal balance = rs.getBigDecimal("remaining_balance");
                    String risk = daysOverdue > 90 ? "HIGH" : (daysOverdue > 60 ? "MEDIUM" : "LOW");

                    try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                        ps.setInt(1, loanId);
                        ps.setInt(2, daysOverdue);
                        ps.setBigDecimal(3, balance);
                        ps.setString(4, risk);
                        ps.executeUpdate();
                        flaggedCount++;
                    }
                }
            }
            return flaggedCount;
        }, count -> {
            showInfo("Audit complete. Flagged " + count + " delinquent accounts.");
            loadDelinquencies();
        });
    }
}

// ============================================================================
// MODULE 5: DASHBOARD & AGGREGATE METRICS
// ============================================================================
class DashboardModule extends BaseDatabaseModule {
    private final DefaultTableModel metricsModel;

    public DashboardModule(Connection con) {
        super(con);
        metricsModel = createNonEditableModel(new String[]{"Metric Name", "Aggregated Value"});
        initUI();
        loadDashboard();
    }

    private void initUI() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Refresh Dashboard Metrics");
        topPanel.add(refreshBtn);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(new JTable(metricsModel)), BorderLayout.CENTER);

        refreshBtn.addActionListener(e -> loadDashboard());
    }

    public void loadDashboard() {
        metricsModel.setRowCount(0);
        executeAsync(() -> {
            List<Object[]> stats = new ArrayList<>();
            String sql1 = "SELECT COALESCE(SUM(remaining_balance), 0) FROM loan WHERE loan_status = 'APPROVED'";
            String sql2 = "SELECT COUNT(*) FROM loan WHERE loan_status = 'APPROVED'";
            String sql3 = "SELECT COUNT(DISTINCT loan_id) FROM delinquency WHERE resolution_status = 'OPEN'";

            try (Statement st = con.createStatement()) {
                try (ResultSet rs = st.executeQuery(sql1)) {
                    if (rs.next()) stats.add(new Object[]{"Total Outstanding Principal", String.format("$%.2f", rs.getBigDecimal(1))});
                }
                try (ResultSet rs = st.executeQuery(sql2)) {
                    if (rs.next()) stats.add(new Object[]{"Active Approved Loans", rs.getInt(1)});
                }
                try (ResultSet rs = st.executeQuery(sql3)) {
                    if (rs.next()) stats.add(new Object[]{"Overdue Accounts (Open Risk)", rs.getInt(1)});
                }
            }
            return stats;
        }, stats -> stats.forEach(metricsModel::addRow));
    }
}

// ============================================================================
// MAIN APPLICATION CONTAINER FRAME & ENTRY POINT
// ============================================================================
public class CredoraApplication extends JFrame {
    private Connection con;

    public CredoraApplication() {
        setTitle("Credora Financial Management System");
        setSize(1000, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initDatabaseConnection();
        initTabs();
    }

    private void initDatabaseConnection() {
        try {
            con = DriverManager.getConnection("jdbc:mysql://localhost:3306/credora_db", "root", "sit@123");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Failed to connect to Database: " + e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void initTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Dashboard", new DashboardModule(con));
        tabbedPane.addTab("Customers", new CustomerModule(con));
        tabbedPane.addTab("Loans", new LoanModule(con));
        tabbedPane.addTab("Repayments", new RepaymentModule(con));
        tabbedPane.addTab("Delinquency", new DelinquencyModule(con));

        add(tabbedPane);
    }

    public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> new CredoraApplication().setVisible(true));
}
}
