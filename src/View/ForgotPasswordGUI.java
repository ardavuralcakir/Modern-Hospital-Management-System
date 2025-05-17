package View;

import dbhelper.DBConnection;
import dbhelper.Helper;
import java.awt.*;
import java.sql.*;
import java.util.Random;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.mail.MessagingException;
import dbhelper.EmailHelper;

public class ForgotPasswordGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel w_panel;
    private JTextField fld_tcno;
    private JTextField fld_email;
    private JTextField fld_verificationCode;
    private JPasswordField fld_newPassword;
    private DBConnection conn = new DBConnection();
    private int userId;
    private JButton btnSendCode;
    private JButton btnUpdatePassword;
    
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // Modern renk paleti
    private Color primaryColor = new Color(6, 95, 70);      // Koyu yeşil
    private Color secondaryColor = new Color(240, 253, 244); // Açık yeşil
    private Color grayTextColor = new Color(75, 85, 99);    // Gri yazı rengi

    public ForgotPasswordGUI() {
        setResizable(false);
        setTitle("Hastane Otomasyonu - Şifre Yenileme");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Ana panel
        w_panel = new JPanel();
        w_panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        w_panel.setBackground(secondaryColor);
        w_panel.setLayout(null);
        setContentPane(w_panel);

        // Sol panel (form)
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(secondaryColor);
        leftPanel.setBounds(0, 0, 500, 600);
        leftPanel.setLayout(null);
        w_panel.add(leftPanel);

        JLabel lblWelcome = new JLabel("Şifre Yenileme");
        lblWelcome.setFont(new Font("Inter", Font.BOLD, 32));
        lblWelcome.setForeground(primaryColor);
        lblWelcome.setBounds(50, 50, 400, 40);
        leftPanel.add(lblWelcome);

        // Form paneli
        JPanel formPanel = new JPanel();
        formPanel.setBackground(secondaryColor);
        formPanel.setBounds(50, 100, 400, 450);
        formPanel.setLayout(null);
        leftPanel.add(formPanel);

        // TC No
        JLabel lblTc = new JLabel("T.C. Numarası");
        lblTc.setFont(new Font("Inter", Font.PLAIN, 14));
        lblTc.setForeground(grayTextColor);
        lblTc.setBounds(0, 0, 180, 25);
        formPanel.add(lblTc);

        fld_tcno = new JTextField();
        fld_tcno.setBounds(0, 25, 380, 40);
        stylizeField(fld_tcno);
        fld_tcno.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c)) evt.consume();
                if (fld_tcno.getText().length() >= 11 && evt.getKeyChar() != '\b') {
                    evt.consume();
                }
            }
        });
        formPanel.add(fld_tcno);

        // Email
        JLabel lblEmail = new JLabel("E-mail");
        lblEmail.setFont(new Font("Inter", Font.PLAIN, 14));
        lblEmail.setForeground(grayTextColor);
        lblEmail.setBounds(0, 60, 180, 25);
        formPanel.add(lblEmail);

        fld_email = new JTextField();
        fld_email.setBounds(0, 85, 380, 40);
        stylizeField(fld_email);
        formPanel.add(fld_email);

        // Doğrulama Kodu Gönder Butonu
        btnSendCode = createStyledButton("Doğrulama Kodu Gönder", primaryColor);
        btnSendCode.setBounds(0, 135, 380, 45);
        btnSendCode.addActionListener(e -> sendVerificationCode());
        formPanel.add(btnSendCode);

        // Doğrulama Kodu
        JLabel lblVerification = new JLabel("Doğrulama Kodu");
        lblVerification.setFont(new Font("Inter", Font.PLAIN, 14));
        lblVerification.setForeground(grayTextColor);
        lblVerification.setBounds(0, 190, 180, 25);
        formPanel.add(lblVerification);

        fld_verificationCode = new JTextField();
        fld_verificationCode.setBounds(0, 215, 380, 40);
        stylizeField(fld_verificationCode);
        formPanel.add(fld_verificationCode);

        // Yeni Şifre
        JLabel lblNewPassword = new JLabel("Yeni Şifre");
        lblNewPassword.setFont(new Font("Inter", Font.PLAIN, 14));
        lblNewPassword.setForeground(grayTextColor);
        lblNewPassword.setBounds(0, 265, 180, 25);
        formPanel.add(lblNewPassword);

        fld_newPassword = new JPasswordField();
        fld_newPassword.setBounds(0, 290, 380, 40);
        stylizeField(fld_newPassword);
        formPanel.add(fld_newPassword);

        // Şifre Güncelle Butonu
        btnUpdatePassword = createStyledButton("Şifreyi Güncelle", primaryColor);
        btnUpdatePassword.setBounds(0, 340, 380, 45);
        btnUpdatePassword.addActionListener(e -> verifyAndUpdatePassword());
        formPanel.add(btnUpdatePassword);

        // Giriş Sayfasına Dön butonu
        JButton btnBack = new JButton("Giriş Sayfasına Dön");
        btnBack.setFont(new Font("Inter", Font.BOLD, 14));
        btnBack.setBounds(0, 395, 380, 45);
        btnBack.setBackground(secondaryColor);
        btnBack.setForeground(primaryColor);
        btnBack.setBorder(BorderFactory.createLineBorder(primaryColor, 2, true));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            LoginGUI login = new LoginGUI();
            login.setVisible(true);
            dispose();
        });
        formPanel.add(btnBack);

        // Sağ panel (koyu yeşil)
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(primaryColor);
        rightPanel.setBounds(500, 0, 500, 600);
        rightPanel.setLayout(null);
        w_panel.add(rightPanel);

        // Logo
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/images/Logo1.png"));
            Image resizedImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(resizedImage));
            logoLabel.setBounds(371, 30, 80, 80);
            rightPanel.add(logoLabel);
        } catch (Exception e) {
            System.out.println("Logo yüklenemedi: " + e.getMessage());
        }

        // Hastane adı
        JLabel lblHastaneAdi = new JLabel("Agas KHİG");
        lblHastaneAdi.setHorizontalAlignment(SwingConstants.RIGHT);
        lblHastaneAdi.setFont(new Font("Inter", Font.BOLD, 24));
        lblHastaneAdi.setForeground(Color.WHITE);
        lblHastaneAdi.setBounds(61, 51, 300, 30);
        rightPanel.add(lblHastaneAdi);

        // Büyük başlık
        JLabel lblMainTitle = new JLabel("<html>&nbsp;&nbsp;&nbsp;Sağlınız,<br>Görevimiz</html>");
        lblMainTitle.setHorizontalAlignment(SwingConstants.RIGHT);
        lblMainTitle.setFont(new Font("Inter", Font.BOLD, 48));
        lblMainTitle.setForeground(Color.WHITE);
        lblMainTitle.setBounds(37, 152, 400, 120);
        rightPanel.add(lblMainTitle);

        // Alt başlık
        JLabel lblSubTitle = new JLabel("Her ihtiyaca cevap veriyoruz");
        lblSubTitle.setHorizontalAlignment(SwingConstants.RIGHT);
        lblSubTitle.setFont(new Font("Inter", Font.PLAIN, 20));
        lblSubTitle.setForeground(new Color(255, 255, 255, 220));
        lblSubTitle.setBounds(37, 282, 400, 30);
        rightPanel.add(lblSubTitle);

        // Frame kapatıldığında executor'ı temizle
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                executor.shutdown();
            }
        });
    }

    private void stylizeField(JTextField field) {
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6 haneli kod
        return String.valueOf(code);
    }

    private void saveVerificationCode(String code, int userId) throws SQLException {
        Connection con = conn.connDb();
        
        // Önce verification_codes tablosunu oluştur (eğer yoksa)
        try {
            Statement st = con.createStatement();
            st.execute(
                "CREATE TABLE IF NOT EXISTS verification_codes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "code VARCHAR(10) NOT NULL," +
                "expiration_time TIMESTAMP NOT NULL," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")"
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Eski kodları temizle
        PreparedStatement cleanPst = con.prepareStatement(
            "DELETE FROM verification_codes WHERE code = ?"
        );
        cleanPst.setString(1, code);
        cleanPst.executeUpdate();

        // Yeni kodu kaydet (20 saniye geçerli)
        PreparedStatement pst = con.prepareStatement(
            "INSERT INTO verification_codes (code, expiration_time) VALUES (?, DATE_ADD(NOW(), INTERVAL 20 SECOND))"
        );
        pst.setString(1, code);
        pst.executeUpdate();
    }

    private boolean verifyCode(String enteredCode, int userId) throws SQLException {
        Connection con = conn.connDb();
        PreparedStatement pst = con.prepareStatement(
            "SELECT * FROM verification_codes " +
            "WHERE code = ? " +
            "AND expiration_time > NOW()"
        );
        pst.setString(1, enteredCode);
        
        ResultSet rs = pst.executeQuery();
        return rs.next();
    }

    private void sendVerificationCode() {
        String tcno = fld_tcno.getText();
        String email = fld_email.getText();

        if (tcno.isEmpty() || email.isEmpty()) {
            Helper.showMsg("fill");
            return;
        }

        btnSendCode.setEnabled(false);
        btnSendCode.setText("Kod Gönderiliyor...");

        executor.submit(() -> {
            try {
                Connection con = conn.connDb();
                PreparedStatement pst = con.prepareStatement(
                    "SELECT * FROM user WHERE tcno = ? AND email = ?"
                );
                pst.setString(1, tcno);
                pst.setString(2, email);
                ResultSet rs = pst.executeQuery();

                if (rs.next()) {
                    userId = rs.getInt("id");
                    String verificationCode = generateVerificationCode();
                    saveVerificationCode(verificationCode, userId);
                    
                    try {
                        EmailHelper.sendVerificationCode(email, verificationCode);
                        
                        SwingUtilities.invokeLater(() -> {
                            Helper.showMsg("Doğrulama kodu e-posta adresinize gönderildi.\n20 saniye içinde giriniz!");
                            btnSendCode.setText("Doğrulama Kodu Gönder");
                            btnSendCode.setEnabled(true);
                        });
                    } catch (MessagingException me) {
                        SwingUtilities.invokeLater(() -> {
                            Helper.showMsg("Email gönderilemedi: " + me.getMessage());
                            btnSendCode.setText("Doğrulama Kodu Gönder");
                            btnSendCode.setEnabled(true);
                        });
                    }
                } else {
                    SwingUtilities.invokeLater(() -> {
                        Helper.showMsg("Bu bilgilerle eşleşen kullanıcı bulunamadı!");
                        btnSendCode.setText("Doğrulama Kodu Gönder");
                        btnSendCode.setEnabled(true);
                    });
                }
            } catch (SQLException e) {
                SwingUtilities.invokeLater(() -> {
                    Helper.showMsg("Veritabanı hatası: " + e.getMessage());
                    btnSendCode.setText("Doğrulama Kodu Gönder");
                    btnSendCode.setEnabled(true);
                });
            }
        });
    }

    private void verifyAndUpdatePassword() {
        String enteredCode = fld_verificationCode.getText();
        String newPassword = new String(fld_newPassword.getPassword());

        if (enteredCode.isEmpty() || newPassword.isEmpty()) {
            Helper.showMsg("fill");
            return;
        }

        btnUpdatePassword.setEnabled(false);
        btnUpdatePassword.setText("İşleniyor...");

        executor.submit(() -> {
            try {
                // Doğrulama kodunu kontrol et
                if (!verifyCode(enteredCode, userId)) {
                    SwingUtilities.invokeLater(() -> {
                        Helper.showMsg("Doğrulama kodu geçersiz veya süresi dolmuş!");
                        btnUpdatePassword.setText("Şifreyi Güncelle");
                        btnUpdatePassword.setEnabled(true);
                    });
                    return;
                }

                // Şifreyi güncelle
                Connection con = conn.connDb();
                PreparedStatement pst = con.prepareStatement(
                    "UPDATE user SET password = ? WHERE id = ?"
                );
                pst.setString(1, newPassword);
                pst.setInt(2, userId);
                pst.executeUpdate();

                // Kullanılan doğrulama kodunu sil
                PreparedStatement cleanPst = con.prepareStatement(
                    "DELETE FROM verification_codes WHERE code = ?"
                );
                cleanPst.setString(1, enteredCode);
                cleanPst.executeUpdate();

                SwingUtilities.invokeLater(() -> {
                    Helper.showMsg("success");
                    dispose();
                    LoginGUI login = new LoginGUI();
                    login.setVisible(true);
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    Helper.showMsg("error");
                    btnUpdatePassword.setText("Şifreyi Güncelle");
                    btnUpdatePassword.setEnabled(true);
                });
            }
        });
    }
}