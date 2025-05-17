package View;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import Model.Admin;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class LoginAGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel w_panel;
    private JTextField fld_username;
    private JPasswordField fld_password;

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                LoginAGUI frame = new LoginAGUI();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public LoginAGUI() {
        setTitle("Admin Giriş Ekranı");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 420, 320);
        setResizable(false);

        w_panel = new JPanel();
        w_panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        w_panel.setLayout(null);
        w_panel.setBackground(new Color(245, 245, 245));
        setContentPane(w_panel);

        // Görünmez buton (sağ üst)
        JButton hiddenBtn = new JButton("");
        hiddenBtn.setBounds(370, 10, 30, 30);
        // Aşağıdaki 2 satır butonun görünmez olmasını sağlar:
        hiddenBtn.setBorder(null);
        hiddenBtn.setContentAreaFilled(false);
        // Cursor eklemek isterseniz (el):
        hiddenBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Buton tıklanınca direkt Admin ekranına geç
        hiddenBtn.addActionListener(e -> {
            try {
                Admin admin = new Admin(); // ya da veritabanından bir admin nesnesi
                AdminGUI adminGui = new AdminGUI(admin);
                adminGui.setVisible(true);
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        w_panel.add(hiddenBtn);

        // Başlık
        JLabel lblTitle = new JLabel("Admin Giriş Ekranı");
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 16));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitle.setBounds(0, 10, 420, 30);
        w_panel.add(lblTitle);

        JLabel lbl_username = new JLabel("Kullanıcı Adı:");
        lbl_username.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lbl_username.setBounds(50, 80, 100, 25);
        w_panel.add(lbl_username);

        fld_username = new JTextField();
        fld_username.setBounds(150, 80, 180, 25);
        w_panel.add(fld_username);
        fld_username.setColumns(10);

        JLabel lbl_password = new JLabel("Şifre:");
        lbl_password.setFont(new Font("Tahoma", Font.PLAIN, 14));
        lbl_password.setBounds(50, 120, 100, 25);
        w_panel.add(lbl_password);

        fld_password = new JPasswordField();
        fld_password.setBounds(150, 120, 180, 25);
        w_panel.add(fld_password);

        JButton btn_login = new JButton("Giriş");
        btn_login.setFont(new Font("Tahoma", Font.BOLD, 14));
        btn_login.setBackground(new Color(60, 179, 113));
        btn_login.setForeground(Color.WHITE);
        btn_login.setFocusPainted(false);
        btn_login.setBounds(150, 170, 100, 30);
        w_panel.add(btn_login);

        // Giriş butonu işlemleri
        btn_login.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = fld_username.getText().trim();
                String password = new String(fld_password.getPassword()).trim();

                // 1) admin123 / admin123 ise giriş yap
                if (username.equals("admin123") && password.equals("admin123")) {
                    openAdminGUI();
                } 
                // 2) Veya veritabanında type='admin' olan kullanıcı ise giriş yap
                else {
                    Admin admin = new Admin();
                    try {
                        // Örnek: checkDbAdmin metodu ile DB kontrol
                        boolean dbAdmin = admin.checkDbAdmin(username, password);
                        if (dbAdmin) {
                            openAdminGUI();
                        } else {
                            JOptionPane.showMessageDialog(null, 
                                "Hatalı kullanıcı adı veya şifre!",
                                "Hata", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(null, 
                            "Veritabanı hatası: " + ex.getMessage(),
                            "Hata", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
    }

    /**
     * Admin ekranını açar ve bu Login penceresini kapatır.
     */
    private void openAdminGUI() {
        try {
            Admin admin = new Admin();
            AdminGUI adminGui = new AdminGUI(admin);
            adminGui.setVisible(true);
            dispose();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
