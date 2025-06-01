import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class ProfileForm extends JFrame {
    private int userId;
    private String username;
    private Dashboard dashboard;

    private JPasswordField txtOldPassword, txtNewPassword, txtConfirmPassword;
    private JButton btnChangePassword;

    public ProfileForm(int userId, String username, Dashboard dashboard) {
        this.userId = userId;
        this.username = username;
        this.dashboard = dashboard;

        setTitle("User Profile - " + username);
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        add(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Change Password", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1;

        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel("Old Password:"), gbc);
        txtOldPassword = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(txtOldPassword, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel("New Password:"), gbc);
        txtNewPassword = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(txtNewPassword, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        panel.add(new JLabel("Confirm Password:"), gbc);
        txtConfirmPassword = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(txtConfirmPassword, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        btnChangePassword = new JButton("Update Password");
        btnChangePassword.setBackground(new Color(30, 144, 255));
        btnChangePassword.setForeground(Color.WHITE);
        btnChangePassword.setFocusPainted(false);
        panel.add(btnChangePassword, gbc);

        btnChangePassword.addActionListener(e -> changePassword());

        setVisible(true);
    }

    private void changePassword() {
        String oldPass = new String(txtOldPassword.getPassword());
        String newPass = new String(txtNewPassword.getPassword());
        String confirmPass = new String(txtConfirmPassword.getPassword());

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields");
            return;
        }

        if (!newPass.equals(confirmPass)) {
            JOptionPane.showMessageDialog(this, "New passwords do not match");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT password FROM users WHERE id = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                String storedHashedPass = rs.getString("password");
                String oldPassHashed = PasswordUtils.hashPassword(oldPass);
                if (!storedHashedPass.equals(oldPassHashed)) {
                    JOptionPane.showMessageDialog(this, "Old password is incorrect");
                    return;
                }
            }

            String updateSql = "UPDATE users SET password = ? WHERE id = ?";
            PreparedStatement pstUpdate = conn.prepareStatement(updateSql);
            pstUpdate.setString(1, PasswordUtils.hashPassword(newPass));
            pstUpdate.setInt(2, userId);
            pstUpdate.executeUpdate();

            JOptionPane.showMessageDialog(this, "Password updated successfully");
            this.dispose();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to update password");
            ex.printStackTrace();
        }
    }
}
