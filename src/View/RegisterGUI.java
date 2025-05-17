package View;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Date;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.formdev.flatlaf.FlatLightLaf;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import Model.Hasta;
import dbhelper.Helper;

public class RegisterGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel w_panel;
    private Hasta hasta = new Hasta();

    // Form alanları
    private JTextField fld_name, fld_tcno, fld_email, fld_pnumber;
    private JPasswordField fld_pass, fld_passConfirm;
    private JRadioButton rBtn_male, rBtn_female;
    private ButtonGroup genderGroup;
    private JDateChooser dateChooser;

    // Renkler
    private Color primaryColor   = new Color(6, 95, 70);      // Koyu yeşil
    private Color secondaryColor = new Color(240, 253, 244); // Açık yeşil
    private Color grayTextColor  = new Color(75, 85, 99);

    public static void main(String[] args) {
        // FlatLaf ile modern görünüm
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            UIManager.put("defaultFont", new Font("Inter", Font.PLAIN, 14));
            UIManager.put("Button.arc", 999);
            UIManager.put("Component.arc", 999);
            UIManager.put("TextComponent.arc", 999);

        } catch (Exception e) {
            e.printStackTrace();
        }

        EventQueue.invokeLater(() -> {
            try {
                RegisterGUI frame = new RegisterGUI();
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public RegisterGUI() {
        setResizable(false);
        setTitle("Hastane Otomasyonu - Kayıt");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Ana panel
        w_panel = new JPanel();
        w_panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        // Arka plan açık yeşil
        w_panel.setBackground(secondaryColor);
        w_panel.setLayout(null);
        setContentPane(w_panel);

        // Sol panel (form)
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(secondaryColor);
        leftPanel.setBounds(0, 0, 500, 600);
        leftPanel.setLayout(null);
        w_panel.add(leftPanel);

        JLabel lblWelcome = new JLabel("Kayıt Ol");
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

        // Ad Soyad
        JLabel lblName = new JLabel("Ad Soyad");
        lblName.setFont(new Font("Inter", Font.PLAIN, 14));
        lblName.setForeground(grayTextColor);
        lblName.setBounds(0, 0, 180, 25);
        formPanel.add(lblName);

        fld_name = new JTextField();
        fld_name.setBounds(0, 25, 180, 40);
        stylizeField(fld_name);
        formPanel.add(fld_name);

        // T.C. Numarası
        JLabel lblTc = new JLabel("T.C. Numarası");
        lblTc.setFont(new Font("Inter", Font.PLAIN, 14));
        lblTc.setForeground(grayTextColor);
        lblTc.setBounds(200, 0, 180, 25);
        formPanel.add(lblTc);

        fld_tcno = new JTextField();
        fld_tcno.setBounds(200, 25, 180, 40);
        stylizeField(fld_tcno);
        fld_tcno.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                // Sadece rakam, en fazla 11 hane
                if (!Character.isDigit(c)) evt.consume();
                if (fld_tcno.getText().length() >= 11 && evt.getKeyChar() != '\b') {
                    evt.consume();
                }
            }
        });
        formPanel.add(fld_tcno);

        // E-mail
        JLabel lblEmail = new JLabel("E-mail");
        lblEmail.setFont(new Font("Inter", Font.PLAIN, 14));
        lblEmail.setForeground(grayTextColor);
        lblEmail.setBounds(0, 80, 180, 25);
        formPanel.add(lblEmail);

        fld_email = new JTextField();
        fld_email.setBounds(0, 105, 180, 40);
        stylizeField(fld_email);
        formPanel.add(fld_email);

        // Telefon
        JLabel lblPhone = new JLabel("Telefon");
        lblPhone.setFont(new Font("Inter", Font.PLAIN, 14));
        lblPhone.setForeground(grayTextColor);
        lblPhone.setBounds(200, 80, 180, 25);
        formPanel.add(lblPhone);

        fld_pnumber = new JTextField();
        fld_pnumber.setBounds(200, 105, 180, 40);
        stylizeField(fld_pnumber);
        fld_pnumber.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c)) evt.consume();
                if (fld_pnumber.getText().length() >= 10 && evt.getKeyChar() != '\b') {
                    evt.consume();
                }
            }
        });
        fld_pnumber.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = fld_pnumber.getText().trim();
                input = input.replaceAll("[^0-9]", "");
                if (input.length() == 10) {
                    fld_pnumber.setText(formatPhoneNumber(input));
                }
            }
        });
        formPanel.add(fld_pnumber);

        // Doğum Tarihi
        JLabel lblBirthDate = new JLabel("Doğum Tarihi");
        lblBirthDate.setFont(new Font("Inter", Font.PLAIN, 14));
        lblBirthDate.setForeground(grayTextColor);
        lblBirthDate.setBounds(0, 160, 180, 25);
        formPanel.add(lblBirthDate);

        dateChooser = new JDateChooser();
        dateChooser.getCalendarButton().setBackground(new Color(240, 253, 244));
        dateChooser.getCalendarButton().setBorder(new EmptyBorder(0, 0, 0, 0));
        dateChooser.setBounds(0, 185, 180, 40);
        dateChooser.setFont(new Font("Inter", Font.PLAIN, 14));

        // Date editor'un border'ı
        if (dateChooser.getDateEditor() instanceof JTextFieldDateEditor) {
            JTextFieldDateEditor editor = (JTextFieldDateEditor) dateChooser.getDateEditor();
            editor.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            editor.setFont(new Font("Inter", Font.PLAIN, 14));
        }
        dateChooser.getCalendarButton().setContentAreaFilled(false);
        dateChooser.getCalendarButton().setOpaque(false);

        formPanel.add(dateChooser);

        // Cinsiyet
        JLabel lblGender = new JLabel("Cinsiyet");
        lblGender.setFont(new Font("Inter", Font.PLAIN, 14));
        lblGender.setForeground(grayTextColor);
        lblGender.setBounds(200, 160, 180, 25);
        formPanel.add(lblGender);

        rBtn_male   = new JRadioButton("Erkek");
        rBtn_female = new JRadioButton("Kadın");
        rBtn_male.setBounds(200, 185, 80, 40);
        rBtn_female.setBounds(280, 185, 80, 40);
        rBtn_male.setBackground(secondaryColor);
        rBtn_female.setBackground(secondaryColor);

        rBtn_female.putClientProperty("FlatLaf.style", "icon.checkmarkColor: #F06292");
        genderGroup = new ButtonGroup();
        genderGroup.add(rBtn_male);
        genderGroup.add(rBtn_female);
        formPanel.add(rBtn_male);
        formPanel.add(rBtn_female);

        // Şifre
        JLabel lblPass = new JLabel("Şifre");
        lblPass.setFont(new Font("Inter", Font.PLAIN, 14));
        lblPass.setForeground(grayTextColor);
        lblPass.setBounds(0, 240, 180, 25);
        formPanel.add(lblPass);

        fld_pass = new JPasswordField();
        fld_pass.setBounds(0, 265, 180, 40);
        stylizeField(fld_pass);
        formPanel.add(fld_pass);

        // Şifre Tekrar
        JLabel lblPassAgain = new JLabel("Şifre Tekrar");
        lblPassAgain.setFont(new Font("Inter", Font.PLAIN, 14));
        lblPassAgain.setForeground(grayTextColor);
        lblPassAgain.setBounds(200, 240, 180, 25);
        formPanel.add(lblPassAgain);

        fld_passConfirm = new JPasswordField();
        fld_passConfirm.setBounds(200, 265, 180, 40);
        stylizeField(fld_passConfirm);
        formPanel.add(fld_passConfirm);

        // Kayıt Ol butonu
        JButton btnRegister = new JButton("Kayıt Ol");
        btnRegister.setFont(new Font("Inter", Font.BOLD, 14));
        btnRegister.setBounds(0, 330, 380, 45);
        btnRegister.setBackground(primaryColor);
        btnRegister.setForeground(Color.WHITE);
        btnRegister.setBorder(BorderFactory.createEmptyBorder());
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnRegister.addActionListener(e -> register());
        formPanel.add(btnRegister);

        // Geri Dön butonu
        JButton btnBack = new JButton("Giriş Sayfasına Dön");
        btnBack.setFont(new Font("Inter", Font.BOLD, 14));
        btnBack.setBounds(0, 385, 380, 45);
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
    }

    /**
     * Metin alanları ve benzer bileşenlere ortak stil uygulama
     */
    private void stylizeField(JTextField field) {
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        // Diğer alanlarla aynı border stili
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(209, 213, 219), 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
    }

    /**
     * Telefon formatı
     */
    private String formatPhoneNumber(String raw) {
        return "+90 (" 
            + raw.substring(0, 3) + ") "
            + raw.substring(3, 6) + " "
            + raw.substring(6, 8) + " "
            + raw.substring(8, 10);
    }

    /**
     * Kayıt Ol butonuna basılınca
     */
    private void register() {
        if (isAnyFieldEmpty() || !isGenderSelected() || dateChooser.getDate() == null) {
            Helper.showMsg("fill");
            return;
        }

        String tcno       = fld_tcno.getText().trim();
        String email      = fld_email.getText().trim();
        String phone      = fld_pnumber.getText().trim();
        String password   = new String(fld_pass.getPassword());
        String confirmPass= new String(fld_passConfirm.getPassword());

        // TC Kimlik kontrolü
        if (tcno.length() != 11) {
            Helper.showMsg("TC Kimlik numarası 11 haneli olmalıdır!");
            return;
        }

        // Email kontrolü
        if (!isValidEmail(email)) {
            Helper.showMsg("Lütfen geçerli bir email adresi giriniz!");
            return;
        }

        // Doğum tarihi kontrolü
        Calendar selectedDate = Calendar.getInstance();
        selectedDate.setTime(dateChooser.getDate());
        Calendar currentDate = Calendar.getInstance();
        if (selectedDate.after(currentDate)) {
            Helper.showMsg("Lütfen doğum tarihinizi düzgün girin!");
            return;
        }

        // Şifre kontrolü
        if (!password.equals(confirmPass)) {
            Helper.showMsg("Şifreler eşleşmiyor!");
            return;
        }

        try {
            if (hasta.isTCNumberRegistered(tcno)) {
                Helper.showMsg("Bu TC kimlik numarası sistemde kayıtlıdır!");
                return;
            }
            if (hasta.isEmailRegistered(email)) {
                Helper.showMsg("Bu email adresi sistemde kayıtlıdır!");
                return;
            }
            if (hasta.isPhoneRegistered(phone)) {
                Helper.showMsg("Bu telefon numarası sistemde kayıtlıdır!");
                return;
            }

            boolean control = hasta.register(
                tcno,
                password,
                fld_name.getText(),
                email,
                phone,
                rBtn_male.isSelected() ? "Erkek" : "Kadın",
                new Date(dateChooser.getDate().getTime())
            );

            if (control) {
                Helper.showMsg("success");
                // Başarılı kayıt -> LoginGUI'ye dön
                LoginGUI login = new LoginGUI();
                login.setVisible(true);
                dispose();
            } else {
                Helper.showMsg("error");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            Helper.showMsg("Lütfen geçerli bilgiler giriniz!");
        }
    }

    /**
     * Email doğrulama
     */
    private boolean isValidEmail(String email) {
        email = email.toLowerCase();
        String gmailPattern  = "^[A-Za-z0-9+_.-]+@gmail\\.com$";
        String otherPattern  = "^[A-Za-z0-9+_.-]+@(hotmail|outlook|yahoo)\\.com$";
        return email.matches(gmailPattern) || email.matches(otherPattern);
    }

    /**
     * Boş alan kontrolü
     */
    private boolean isAnyFieldEmpty() {
        return fld_tcno.getText().trim().isEmpty() ||
               fld_pass.getPassword().length == 0 ||
               fld_passConfirm.getPassword().length == 0 ||
               fld_name.getText().trim().isEmpty() ||
               fld_email.getText().trim().isEmpty() ||
               fld_pnumber.getText().trim().isEmpty();
    }

    /**
     * Cinsiyet seçili mi
     */
    private boolean isGenderSelected() {
        return rBtn_male.isSelected() || rBtn_female.isSelected();
    }
}
