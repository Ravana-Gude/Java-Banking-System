import java.awt.*;
import java.sql.*;
import java.util.Vector;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

public class Dashboard extends JFrame {
    private int userId;
    private String username;
    private JLabel lblBalance;
    private JTable tblTransactions;
    private DefaultTableModel tableModel;

    public Dashboard(int userId, String username) {
        this.userId = userId;
        this.username = username;

        setTitle("Bank Dashboard - " + username);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        add(mainPanel);

        JLabel welcomeLabel = new JLabel("Welcome, " + username);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(new Color(30, 144, 255));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        // Balance Panel
        JPanel balancePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblBalance = new JLabel("Balance: ₹ 0.00");
        lblBalance.setFont(new Font("Segoe UI", Font.BOLD, 18));
        balancePanel.add(lblBalance);

        JButton btnDeposit = new JButton("Deposit");
        JButton btnWithdraw = new JButton("Withdraw");
        JButton btnProfile = new JButton("Profile");
        JButton btnLogout = new JButton("Logout");

        balancePanel.add(btnDeposit);
        balancePanel.add(btnWithdraw);
        balancePanel.add(btnProfile);
        balancePanel.add(btnLogout);

        mainPanel.add(balancePanel, BorderLayout.CENTER);

        // Transaction History Table
        tableModel = new DefaultTableModel(new String[]{"Date", "Type", "Amount"}, 0);
        tblTransactions = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tblTransactions);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Transaction History"));
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        btnDeposit.addActionListener(e -> depositMoney());
        btnWithdraw.addActionListener(e -> withdrawMoney());
        btnLogout.addActionListener(e -> logout());
        btnProfile.addActionListener(e -> openProfile());

        loadBalanceAndTransactions();

        setVisible(true);
    }

    private void loadBalanceAndTransactions() {
        try (Connection conn = DBConnection.getConnection()) {
            // Load balance
            String sqlBalance = "SELECT balance FROM users WHERE id = ?";
            PreparedStatement pstBalance = conn.prepareStatement(sqlBalance);
            pstBalance.setInt(1, userId);
            ResultSet rsBalance = pstBalance.executeQuery();
            if (rsBalance.next()) {
                double balance = rsBalance.getDouble("balance");
                lblBalance.setText(String.format("Balance: ₹ %.2f", balance));
            }

            // Load transactions
            String sqlTrans = "SELECT trans_date, type, amount FROM transactions WHERE user_id = ? ORDER BY trans_date DESC";
            PreparedStatement pstTrans = conn.prepareStatement(sqlTrans);
            pstTrans.setInt(1, userId);
            ResultSet rsTrans = pstTrans.executeQuery();

            tableModel.setRowCount(0); // clear old data
            while (rsTrans.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rsTrans.getTimestamp("trans_date"));
                row.add(rsTrans.getString("type"));
                row.add(String.format("₹ %.2f", rsTrans.getDouble("amount")));
                tableModel.addRow(row);
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load data");
            ex.printStackTrace();
        }
    }

    private void depositMoney() {
        String input = JOptionPane.showInputDialog(this, "Enter amount to deposit:");
        if (input != null && !input.trim().isEmpty()) {
            try {
                double amount = Double.parseDouble(input);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Enter positive amount");
                    return;
                }
                updateBalanceAndLogTransaction(amount, "Deposit");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid amount");
            }
        }
    }

    private void withdrawMoney() {
        String input = JOptionPane.showInputDialog(this, "Enter amount to withdraw:");
        if (input != null && !input.trim().isEmpty()) {
            try {
                double amount = Double.parseDouble(input);
                if (amount <= 0) {
                    JOptionPane.showMessageDialog(this, "Enter positive amount");
                    return;
                }
try (Connection conn = DBConnection.getConnection()) {
    String sql = "SELECT balance FROM users WHERE id = ?";
    PreparedStatement pst = conn.prepareStatement(sql);
    pst.setInt(1, userId);
    ResultSet rs = pst.executeQuery();
    
    if (rs.next()) {
        double balance = rs.getDouble("balance");
        lblBalance.setText("Balance: ₹" + balance);

    }
} catch (SQLException e) {
    e.printStackTrace(); // Or show a message dialog
}

                updateBalanceAndLogTransaction(amount, "Withdraw");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid amount");
            }
        }
    }

    private void updateBalanceAndLogTransaction(double amount, String type) {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            String updateSql = type.equals("Deposit")
                    ? "UPDATE users SET balance = balance + ? WHERE id = ?"
                    : "UPDATE users SET balance = balance - ? WHERE id = ?";

            try (PreparedStatement pstUpdate = conn.prepareStatement(updateSql)) {
                pstUpdate.setDouble(1, amount);
                pstUpdate.setInt(2, userId);
                pstUpdate.executeUpdate();
            }

            String insertSql = "INSERT INTO transactions (user_id, type, amount) VALUES (?, ?, ?)";
            try (PreparedStatement pstInsert = conn.prepareStatement(insertSql)) {
                pstInsert.setInt(1, userId);
                pstInsert.setString(2, type);
                pstInsert.setDouble(3, amount);
                pstInsert.executeUpdate();
            }

            conn.commit();

            JOptionPane.showMessageDialog(this, type + " successful");
            loadBalanceAndTransactions();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, type + " failed");
            ex.printStackTrace();
        }
    }

    private void logout() {
        this.dispose();
        new LoginForm();
    }

    private void openProfile() {
        new ProfileForm(userId, username, this);
    }
}
