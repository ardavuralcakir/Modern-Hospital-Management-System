package View;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.JToggleButton;

import com.formdev.flatlaf.FlatLightLaf;

import Model.Admin;
import Model.Doctor;
import Model.Hasta;
import dbhelper.DBConnection;
import dbhelper.Helper;

public class LoginGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel w_panel; // Ana panel
    private JTextField fld_hastaLogin;
    private JPasswordField fld_hastaSifre;
    private JTextField fld_doctorTC;
    private JPasswordField fld_doctorPass;
    private DBConnection conn = new DBConnection();
    private JToggleButton btnShowHastaPassword;
    private JToggleButton btnShowDoctorPassword;

    // Renkler (RegisterGUI ile uyumlu)
    private Color primaryColor   = new Color(6, 95, 70);      // Koyu yeşil
    private Color secondaryColor = new Color(240, 253, 244); // Açık yeşil
    private Color grayTextColor  = new Color(75, 85, 99);

    public static void main(String[] args) {
        // FlatLaf ile modern görünüm kazandır
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            // Varsayılan font ve ARC değerleri
            UIManager.put("defaultFont", new Font("Inter", Font.PLAIN, 14));
            UIManager.put("Button.arc", 999);
            UIManager.put("Component.arc", 999);
            UIManager.put("TextComponent.arc", 999);

        } catch (Exception e) {
            e.printStackTrace();
        }

        EventQueue.invokeLater(() -> {
            try {
                LoginGUI frame = new LoginGUI();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public LoginGUI() {
        setResizable(false);
        setTitle("Hastane Otomasyonu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1000, 600);
        setLocationRelativeTo(null);

        // Ana panel (arka planı açık yeşil)
        w_panel = new JPanel();
        w_panel.setBackground(secondaryColor);
        w_panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        w_panel.setLayout(null);
        setContentPane(w_panel);

        // Sol panel (koyu yeşil)
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(primaryColor);
        leftPanel.setBounds(0, 0, 500, 600);
        leftPanel.setLayout(null);
        w_panel.add(leftPanel);

        // Logo
        try {
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/images/logo1.png"));
            Image resizedImage = originalIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            JLabel logoLabel = new JLabel(new ImageIcon(resizedImage));
            logoLabel.setBounds(50, 30, 80, 80);
            leftPanel.add(logoLabel);
        } catch (Exception e) {
            System.out.println("Logo yüklenemedi: " + e.getMessage());
        }

        // Hastane adı
        JLabel lblHastaneAdi = new JLabel("Agas KHİG");
        lblHastaneAdi.setFont(new Font("Inter", Font.BOLD, 24));
        lblHastaneAdi.setForeground(Color.WHITE);
        lblHastaneAdi.setBounds(140, 50, 300, 30);
        leftPanel.add(lblHastaneAdi);

        // Büyük başlık
        JLabel lblMainTitle = new JLabel("<html>Sağlınız,<br>Görevimiz</html>");
        lblMainTitle.setFont(new Font("Inter", Font.BOLD, 48));
        lblMainTitle.setForeground(Color.WHITE);
        lblMainTitle.setBounds(50, 150, 400, 120);
        leftPanel.add(lblMainTitle);

        // Alt başlık
        JLabel lblSubTitle = new JLabel("Her ihtiyaca cevap veriyoruz");
        lblSubTitle.setFont(new Font("Inter", Font.PLAIN, 20));
        lblSubTitle.setForeground(new Color(255, 255, 255, 220));
        lblSubTitle.setBounds(50, 280, 400, 30);
        leftPanel.add(lblSubTitle);
        
        JButton btnAdminLogin_2 = new JButton("");
        btnAdminLogin_2.setFocusPainted(false);
        btnAdminLogin_2.setContentAreaFilled(false);
        btnAdminLogin_2.setBorderPainted(false);
        btnAdminLogin_2.setBackground(new Color(240, 253, 244));
        btnAdminLogin_2.setBounds(0, 0, 85, 21);
        leftPanel.add(btnAdminLogin_2);

        // Sağ panel (arka planı açık yeşil)
        JPanel rightPanel = new JPanel();
        rightPanel.setBackground(secondaryColor);
        rightPanel.setBounds(500, 0, 500, 600);
        rightPanel.setLayout(null);
        w_panel.add(rightPanel);

        // Hoşgeldiniz
        JLabel lblWelcome = new JLabel("Hoşgeldiniz");
        lblWelcome.setFont(new Font("Inter", Font.BOLD, 32));
        lblWelcome.setForeground(primaryColor);
        lblWelcome.setBounds(50, 50, 400, 40);
        rightPanel.add(lblWelcome);

        // Sekme yapısı
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBounds(50, 100, 400, 450);
        tabbedPane.setFont(new Font("Inter", Font.BOLD, 14));
        // Sekmenin de arka planını açık yeşil yapmak isterseniz:
        tabbedPane.setBackground(secondaryColor);
        tabbedPane.setForeground(primaryColor);
        rightPanel.add(tabbedPane);

        // Hasta Girişi sekmesi
        tabbedPane.addTab("Hasta Girişi", createHastaLoginPanel());

        // Doktor Girişi sekmesii
        tabbedPane.addTab("Doktor Girişi", createDoctorLoginPanel());

        // Yönetici girişi (gizli buton)
        JButton btnAdminLogin = new JButton("");
        btnAdminLogin.setBackground(secondaryColor);
        btnAdminLogin.setBounds(405, 0, 85, 21);
        btnAdminLogin.setFocusPainted(false);
        btnAdminLogin.setBorderPainted(false);
        btnAdminLogin.setContentAreaFilled(false);
        btnAdminLogin.addActionListener(e -> {
            try {
                LoginAGUI loginAGUI = new LoginAGUI();
                loginAGUI.setVisible(true);
                dispose();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        rightPanel.add(btnAdminLogin);
    }

    /**
     * Hasta Girişi Paneli (arka plan artık açık yeşil)
     */
    private JPanel createHastaLoginPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(secondaryColor); // beyaz yerine açık yeşil
        panel.setLayout(null);

        JLabel lblLogin = new JLabel("T.C. No veya E-mail");
        lblLogin.setFont(new Font("Inter", Font.PLAIN, 14));
        lblLogin.setForeground(grayTextColor);
        lblLogin.setBounds(30, 30, 340, 25);
        panel.add(lblLogin);

        fld_hastaLogin = new JTextField();
        fld_hastaLogin.setBounds(30, 60, 340, 45);
        fld_hastaLogin.setFont(new Font("Inter", Font.PLAIN, 14));
        fld_hastaLogin.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panel.add(fld_hastaLogin);

        JLabel lblSifre = new JLabel("Şifre");
        lblSifre.setFont(new Font("Inter", Font.PLAIN, 14));
        lblSifre.setForeground(grayTextColor);
        lblSifre.setBounds(30, 120, 340, 25);
        panel.add(lblSifre);

        fld_hastaSifre = new JPasswordField();
        fld_hastaSifre.setBounds(30, 150, 300, 45);
        fld_hastaSifre.setFont(new Font("Inter", Font.PLAIN, 14));
        fld_hastaSifre.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panel.add(fld_hastaSifre);

        // Şifre göster/gizle butonu
        btnShowHastaPassword = new JToggleButton("O");
        btnShowHastaPassword.setBounds(335, 150, 35, 45);
        btnShowHastaPassword.setBackground(Color.WHITE);
        btnShowHastaPassword.setBorderPainted(true);
        btnShowHastaPassword.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true));
        btnShowHastaPassword.setFocusPainted(false);
        btnShowHastaPassword.setFont(new Font("Inter", Font.BOLD, 14));
        btnShowHastaPassword.setForeground(grayTextColor);
        btnShowHastaPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnShowHastaPassword.addActionListener(e -> {
            if (btnShowHastaPassword.isSelected()) {
                fld_hastaSifre.setEchoChar((char) 0);
                btnShowHastaPassword.setText("X");
            } else {
                fld_hastaSifre.setEchoChar('•');
                btnShowHastaPassword.setText("O");
            }
        });
        panel.add(btnShowHastaPassword);

        JButton btnGiris = new JButton("Giriş Yap");
        btnGiris.setFont(new Font("Inter", Font.BOLD, 14));
        btnGiris.setBounds(30, 220, 340, 45);
        btnGiris.setBackground(primaryColor);
        btnGiris.setForeground(Color.WHITE);
        btnGiris.setBorder(BorderFactory.createEmptyBorder());
        btnGiris.setFocusPainted(false);
        btnGiris.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGiris.addActionListener(e -> hastaLogin());
        panel.add(btnGiris);

        JButton btnKayit = new JButton("Yeni Kayıt Oluştur");
        btnKayit.setFont(new Font("Inter", Font.BOLD, 14));
        btnKayit.setBounds(30, 280, 340, 45);
        // Arka planı beyaz yerine açık yeşil isterseniz:
        btnKayit.setBackground(secondaryColor);
        btnKayit.setForeground(primaryColor);
        btnKayit.setBorder(BorderFactory.createLineBorder(primaryColor, 2, true));
        btnKayit.setFocusPainted(false);
        btnKayit.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnKayit.addActionListener(e -> {
            RegisterGUI register = new RegisterGUI();
            register.setVisible(true);
            dispose();
        });
        panel.add(btnKayit);

        JButton btnForgotPassword = new JButton("Şifremi Unuttum");
        btnForgotPassword.setFont(new Font("Inter", Font.PLAIN, 14));
        btnForgotPassword.setBounds(30, 340, 340, 30);
        btnForgotPassword.setBackground(secondaryColor);
        btnForgotPassword.setForeground(primaryColor);
        btnForgotPassword.setBorder(null);
        btnForgotPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForgotPassword.addActionListener(e -> {
        	dispose(); 
            ForgotPasswordGUI forgotPassword = new ForgotPasswordGUI();
            forgotPassword.setVisible(true);
        });
        panel.add(btnForgotPassword);

        return panel;
    }

    /**
     * Doktor Girişi Paneli (arka plan artık açık yeşil)
     */
    private JPanel createDoctorLoginPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(secondaryColor); // beyaz yerine açık yeşil
        panel.setLayout(null);

        JLabel lblTC = new JLabel("T.C. Numarası");
        lblTC.setFont(new Font("Inter", Font.PLAIN, 14));
        lblTC.setForeground(grayTextColor);
        lblTC.setBounds(30, 30, 340, 25);
        panel.add(lblTC);

        fld_doctorTC = new JTextField();
        fld_doctorTC.setBounds(30, 60, 340, 45);
        fld_doctorTC.setFont(new Font("Inter", Font.PLAIN, 14));
        fld_doctorTC.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        fld_doctorTC.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                // Sadece rakam ve en fazla 11 hane
                if (!Character.isDigit(c) || fld_doctorTC.getText().length() >= 11) {
                    e.consume();
                }
            }
        });
        panel.add(fld_doctorTC);

        JLabel lblSifre = new JLabel("Şifre");
        lblSifre.setFont(new Font("Inter", Font.PLAIN, 14));
        lblSifre.setForeground(grayTextColor);
        lblSifre.setBounds(30, 120, 340, 25);
        panel.add(lblSifre);

        fld_doctorPass = new JPasswordField();
        fld_doctorPass.setBounds(30, 150, 300, 45);
        fld_doctorPass.setFont(new Font("Inter", Font.PLAIN, 14));
        fld_doctorPass.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panel.add(fld_doctorPass);

        // Şifre göster/gizle butonu (Doktor)
        btnShowDoctorPassword = new JToggleButton("O");
        btnShowDoctorPassword.setBounds(335, 150, 35, 45);
        btnShowDoctorPassword.setBackground(Color.WHITE);
        btnShowDoctorPassword.setBorderPainted(true);
        btnShowDoctorPassword.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true));
        btnShowDoctorPassword.setFocusPainted(false);
        btnShowDoctorPassword.setFont(new Font("Inter", Font.BOLD, 14));
        btnShowDoctorPassword.setForeground(grayTextColor);
        btnShowDoctorPassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnShowDoctorPassword.addActionListener(e -> {
            if (btnShowDoctorPassword.isSelected()) {
                fld_doctorPass.setEchoChar((char) 0);
                btnShowDoctorPassword.setText("X");
            } else {
                fld_doctorPass.setEchoChar('•');
                btnShowDoctorPassword.setText("O");
            }
        });
        panel.add(btnShowDoctorPassword);

        JButton btnGiris = new JButton("Giriş Yap");
        btnGiris.setFont(new Font("Inter", Font.BOLD, 14));
        btnGiris.setBounds(30, 220, 340, 45);
        btnGiris.setBackground(primaryColor);
        btnGiris.setForeground(Color.WHITE);
        btnGiris.setBorder(BorderFactory.createEmptyBorder());
        btnGiris.setFocusPainted(false);
        btnGiris.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGiris.addActionListener(e -> doctorLogin());
        panel.add(btnGiris);

        return panel;
    }

    /**
     * Hasta Girişi
     */
    private void hastaLogin() {
        if (fld_hastaLogin.getText().length() == 0 || fld_hastaSifre.getPassword().length == 0) {
            Helper.showMsg("fill");
            return;
        }

        try {
            Connection con = conn.connDb();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM user WHERE type='hasta' AND (tcno='" +
                fld_hastaLogin.getText() + "' OR email='" + fld_hastaLogin.getText() + "')");

            if (!rs.next()) {
                Helper.showMsg("Böyle bir kullanıcı bulunamadı!");
                return;
            }

            if (!String.valueOf(fld_hastaSifre.getPassword()).equals(rs.getString("password"))) {
                Helper.showMsg("Şifre yanlış!");
                return;
            }

            // Bilgileri al ve Hasta nesnesi oluştur
            Hasta hasta = new Hasta();
            hasta.setId(rs.getInt("id"));
            hasta.setPassword(rs.getString("password"));
            hasta.setTcno(rs.getString("tcno"));
            hasta.setName(rs.getString("name"));
            hasta.setType(rs.getString("type"));

            HomepageGUI hGUI = new HomepageGUI(hasta);
            hGUI.setVisible(true);
            dispose();

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Doktor Girişi
     */
    private void doctorLogin() {
        if (fld_doctorTC.getText().length() == 0 || fld_doctorPass.getPassword().length == 0) {
            Helper.showMsg("fill");
            return;
        }

        if (fld_doctorTC.getText().length() != 11) {
            Helper.showMsg("Lütfen 11 haneli TC kimlik numaranızı giriniz!");
            return;
        }

        try (Connection con = conn.connDb();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM user WHERE tcno='" + fld_doctorTC.getText() + 
                "' AND (type='doktor' OR type='bashekim')")) {

            if (!rs.next()) {
                Helper.showMsg("TC kimlik numarası ile kayıtlı doktor bulunamadı!");
                return;
            }

            if (!String.valueOf(fld_doctorPass.getPassword()).equals(rs.getString("password"))) {
                Helper.showMsg("Şifre yanlış!");
                return;
            }

            // başhekim ise
            if ("bashekim".equals(rs.getString("type"))) {
                Admin admin = new Admin();
                admin.setId(rs.getInt("id"));
                admin.setPassword(rs.getString("password"));
                admin.setTcno(rs.getString("tcno"));
                admin.setName(rs.getString("name"));
                admin.setType(rs.getString("type"));

                AdminGUI aGUI = new AdminGUI(admin);
                aGUI.setVisible(true);
                dispose();

            } else if ("doktor".equals(rs.getString("type"))) {
                Doctor doctor = new Doctor();
                doctor.setId(rs.getInt("id"));
                doctor.setPassword(rs.getString("password"));
                doctor.setTcno(rs.getString("tcno"));
                doctor.setName(rs.getString("name"));
                doctor.setType(rs.getString("type"));

                DoctorGUI dGUI = new DoctorGUI(doctor);
                dGUI.setVisible(true);
                dispose();
            }

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}