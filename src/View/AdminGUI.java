package View;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.time.LocalTime;  // <-- YENİ: Saat kontrolü için
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.toedter.calendar.JDateChooser;

import Model.Admin;
import Model.Appointment;
import Model.Clinic;
import Model.User;
import dbhelper.Helper;
import dbhelper.Item;

/**
 * AdminGUI:
 * - Main Dashboard
 * - Randevu Dashboard
 * - Kullanıcı Yönetimi (Doktor Yönetimi, Hasta Yönetimi)
 * - Poliklinikler
 * - Randevu İstatistikleri (butonla açılıyor)
 */
public class AdminGUI extends JFrame {
    private static final long serialVersionUID = 1L;

    private Admin admin;
    private Clinic clinic;

    private JPanel w_pane;
    // Randevu istatistikleri Timer
    private Timer statsTimer;

    // ------------------- Doktor Yönetimi bileşenleri -------------------
    private JPanel doktorPanel;
    private JTextField fld_dName, fld_dTcno, fld_doktorID;
    private JPasswordField fld_dPass;
    private JTable table_doktor;
    private DefaultTableModel doktorModel;
    private Object[] doktorData;
    private int currentDoctorPage = 1;
    private final int doctorPageSize = 10;
    private List<User> allDoctors = new ArrayList<>();
    private JLabel lblDoctorPageInfo;

    // ------------------- Hasta Yönetimi bileşenleri -------------------
    private JPanel hastaPanel;
    private JTable table_hasta;
    private DefaultTableModel hastaModel;
    private int currentPatientPage = 1;
    private final int patientPageSize = 10;
    private List<User> allPatients = new ArrayList<>();
    private JLabel lblPatientPageInfo;

    // ------------------- Klinik bileşenleri -------------------
    private JTable table_clinic, table_worker;
    private DefaultTableModel clinicModel;
    private Object[] clinicData;
    private JComboBox<Item> select_doctor;
    private JTextField fld_clinicName;

    // ------------------- Main Dashboard bileşenleri -------------------
    private JLabel lbl_doctorCount;
    private JLabel lbl_clinicCount;
    private JLabel lbl_patientCount;
    private JDateChooser startDateChooser, endDateChooser;
    private GrafikPaneli grafikPaneli;

    // ------------------- Randevu Dashboard bileşenleri -------------------
    private JPanel appointmentPanel;
    private JTable table_appointment;
    private DefaultTableModel appointmentModel;
    private Object[] appointmentData;
    private JTextField fld_filterHasta;
    private JComboBox<Item> cmb_filterDoktor;
    private JDateChooser dateChooserStart, dateChooserEnd;
    private JButton btnPrevPage, btnNextPage;
    private JLabel lblPageInfo;
    private int currentPage = 1;
    private final int pageSize = 10;
    private List<Appointment> allAppointments = new ArrayList<>();

    // ------------------- Renk paleti -------------------
    private final Color primaryColor   = new Color(6, 95, 70);
    private final Color secondaryColor = new Color(240, 253, 244);
    private final Color textColor      = new Color(75, 85, 99);
    private final Color borderColor    = new Color(209, 213, 219);
    // ------------------- Sekmeler -------------------
    private JTabbedPane tabbedPane;

    // ------------------- Randevu İstatistikleri paneli -------------------
    private JPanel appointmentStatsPanel;
    private JLabel lblWeeklyValue, lblMonthlyValue, lblYearlyValue, lblCancelledValue;
    private JDateChooser statsStartDate, statsEndDate;
    private GrafikPaneli grafikPaneliStats;

    // ------------------- Kullanıcı Yönetimi sekmesi (kart) -------------------
    private JPanel userPanel;
    private JPanel userContentPanel; // Doktor/Hasta panelini göstereceğimiz CardLayout panel

    // ------------------- Doluluk Oranı, Atanmış Doktor vb. -------------------
    private JLabel lbl_assignedDoctorCount; 
    private JLabel lblOccupancyRate;        

    // ------------------- Maksimum Kapasite Görüntüleme -------------------
    private JLabel lblMaxCapacity;  // Üst header'daki "Maksimum Kapasite: X" yazısı

    // ------------------------------------------------------------------------
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                AdminGUI frame = new AdminGUI(new Admin());
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    public AdminGUI(Admin admin) throws SQLException {
        this.admin = admin;
        this.clinic = new Clinic();

        setTitle("Hastane Yönetim Sistemi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1000, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        w_pane = new JPanel();
        w_pane.setBackground(secondaryColor);
        w_pane.setBorder(new EmptyBorder(15, 15, 15, 15));
        w_pane.setLayout(null);
        setContentPane(w_pane);

        // Üst Header Panel
        JPanel headerPanel = new JPanel(null);
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBounds(0, 0, 1000, 60);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, borderColor));
        w_pane.add(headerPanel);

        // ----- YENİ: Saat bazlı selamlama + Admin adını gösteren label
        String greeting = getTimeBasedGreeting(); // 06-12 / 12-17 / 17-21 / 21-06
        String userName = admin.getName();
        if (userName == null 
            || userName.trim().isEmpty() 
            || userName.equalsIgnoreCase("null") 
            || userName.equals("admin123")) 
        {
            userName = "Admin";
        }

        JLabel lblWelcome = new JLabel(greeting + ", " + userName);
        lblWelcome.setFont(new Font("Inter", Font.BOLD, 16));
        lblWelcome.setForeground(primaryColor);
        lblWelcome.setBounds(20, 15, 300, 30);
        headerPanel.add(lblWelcome);

        // Yeni: Header'da Maksimum Kapasite göster
        int currentMax = admin.getMaxCapacity(); // Admin sınıfınızda tanımlı olduğunu varsayıyoruz
        lblMaxCapacity = new JLabel("Maksimum Kapasite: " + currentMax);
        lblMaxCapacity.setFont(new Font("Inter", Font.BOLD, 16));
        lblMaxCapacity.setForeground(new Color(100, 100, 100));
        lblMaxCapacity.setBounds(340, 15, 250, 30);
        lblMaxCapacity.setCursor(new Cursor(Cursor.HAND_CURSOR));
        // Çift tıklama ile kapasite güncelleme
        lblMaxCapacity.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Çift tık
                if (e.getClickCount() == 2) {
                    // Mevcut kapasiteyi elde et (label'dan ya da admin'den):
                    int currentMax = 0;
                    try {
                        currentMax = admin.getMaxCapacity();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                    // Kullanıcıdan yeni kapasite değerini al:
                    String input = JOptionPane.showInputDialog(
                        AdminGUI.this,
                        "Yeni maksimum kapasite değerini giriniz:",
                        currentMax
                    );
                    if (input != null && !input.trim().isEmpty()) {
                        try {
                            int newCap = Integer.parseInt(input.trim());
                            boolean success = admin.updateMaxCapacity(newCap);
                            if (success) {
                                lblMaxCapacity.setText("Maksimum Kapasite: " + newCap);
                                Helper.showMsg("success");

                                // YENİ: Başarılı güncelleme sonrası verileri otomatik güncelle
                                refreshAllData(); 

                            } else {
                                Helper.showMsg("error");
                            }
                        } catch (NumberFormatException ex) {
                            Helper.showMsg("Lütfen sayı giriniz!");
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            Helper.showMsg("error");
                        }
                    }
                }
            }
        });
        headerPanel.add(lblMaxCapacity);

        JButton btnRefresh = createHeaderButton("Yenile", 780);
        btnRefresh.addActionListener(e -> refreshAllData());
        headerPanel.add(btnRefresh);

        JButton btnLogout = createHeaderButton("Çıkış", 880);
        btnLogout.addActionListener(e -> logout());
        headerPanel.add(btnLogout);

        // Sekmeler
        tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(15, 75, 955, 470);
        tabbedPane.setFont(new Font("Inter", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);
        w_pane.add(tabbedPane);

        // Önce modelleri oluştur
        setupModels();

        // Sonra panelleri oluştur
        // Main Dashboard sekmesi
        tabbedPane.addTab("Main Dashboard", createMainDashboardPanel());

        // Randevu Dashboard sekmesi
        appointmentPanel = createAppointmentPanel();
        tabbedPane.addTab("Randevu Dashboard", appointmentPanel);

        // Kullanıcı Yönetimi sekmesi (Sadece 2 buton: Doktor, Hasta)
        tabbedPane.addTab("Kullanıcı Yönetimi", createUserPanel());

        // Poliklinikler sekmesi
        tabbedPane.addTab("Poliklinikler", createClinicPanel());

        // Randevu İstatistikleri paneli (gizli, butonla açıyoruz)
        appointmentStatsPanel = createAppointmentStatsPanel();
        appointmentStatsPanel.setVisible(false);

        // En son listener'ları ekle
        if (table_doktor != null) {
            setupTableListeners();
        }
    }

    /**
     * Zaman bazlı selamlama döndürür.
     * 06-12: Günaydın, 12-17: İyi günler, 17-21: İyi akşamlar, 21-06: İyi geceler
     */
    private String getTimeBasedGreeting() {
        int hour = LocalTime.now().getHour();
        if (hour >= 6 && hour < 12) {
            return "Günaydın";
        } else if (hour >= 12 && hour < 17) {
            return "İyi günler";
        } else if (hour >= 17 && hour < 21) {
            return "İyi akşamlar";
        } else {
            return "İyi geceler";
        }
    }

    // ------------------------------------------------------------------------
    // Model setup
    // ------------------------------------------------------------------------
    private void setupModels() throws SQLException {
        // ------------------- Doktor Modeli -------------------
        doktorModel = new DefaultTableModel() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public boolean isCellEditable(int row, int column) {
                return (column != 0); // ID değiştirilemez
            }
        };
        Object[] colDoktor = { "ID", "Ad Soyad", "T.C. No", "Şifre" };
        doktorModel.setColumnIdentifiers(colDoktor);
        doktorData = new Object[4];
        allDoctors = admin.getDoktorList();
        for (User d : allDoctors) {
            doktorData[0] = d.getId();
            doktorData[1] = d.getName();
            doktorData[2] = d.getTcno();
            doktorData[3] = d.getPassword();
            doktorModel.addRow(doktorData);
        }

        // ------------------- Hasta Modeli -------------------
        hastaModel = new DefaultTableModel() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        Object[] colHasta = {
            "ID",
            "Ad Soyad",
            "T.C. No",
            "Şifre",
            "E-posta",
            "Telefon",
            "Cinsiyet"
        };
        hastaModel.setColumnIdentifiers(colHasta);
        allPatients = admin.getHastaList();
        for (User hasta : allPatients) {
            Object[] row = new Object[]{
                hasta.getId(),
                hasta.getName(),
                hasta.getTcno(),
                hasta.getPassword(),
                (hasta.getEmail() != null ? hasta.getEmail() : ""),
                (hasta.getPhone() != null ? hasta.getPhone() : ""),
                (hasta.getGender() != null ? hasta.getGender() : "")
            };
            hastaModel.addRow(row);
        }

        // ------------------- Klinik Modeli -------------------
        clinicModel = new DefaultTableModel() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        Object[] colClinic = { "ID", "Poliklinik Adı" };
        clinicModel.setColumnIdentifiers(colClinic);
        clinicData = new Object[2];
        for (Clinic c : clinic.getList()) {
            clinicData[0] = c.getId();
            clinicData[1] = c.getName();
            clinicModel.addRow(clinicData);
        }

        // ------------------- Randevu Modeli -------------------
        appointmentModel = new DefaultTableModel() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            public boolean isCellEditable(int row, int col) {
                // Randevu ID dışındaki hücreler düzenlenebilir
                return (col != 0);
            }
        };
        Object[] colAppointment = { "Randevu ID", "Hasta Adı", "Doktor Adı", "Tarih", "Durum" };
        appointmentModel.setColumnIdentifiers(colAppointment);
        appointmentData = new Object[colAppointment.length];
    }

    // ------------------------------------------------------------------------
    // Kullanıcı Yönetimi Sekmesi: Doktor - Hasta Kartları
    // ------------------------------------------------------------------------
    private JPanel createUserPanel() {
        // Ana panel
        userPanel = new JPanel(new BorderLayout());
        userPanel.setBackground(Color.WHITE);

        // CardLayout panel for content
        userContentPanel = new JPanel(new CardLayout());
        userContentPanel.setBackground(Color.WHITE);

        // Seçim paneli (doktor/hasta)
        JPanel selectionPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        selectionPanel.setBackground(Color.WHITE);
        selectionPanel.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        // -- Doktor Kartı
        JPanel doctorCard = new JPanel(null) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Gradient arkaplan
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(6, 95, 70),
                    0, getHeight(), new Color(4, 120, 87)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Doktor resmi
                try {
                    ImageIcon icon = new ImageIcon(getClass().getResource("/Images/Doktor.png"));
                    Image img = icon.getImage();
                    int imgWidth = 150;
                    int imgHeight = 150;
                    int x = (getWidth() - imgWidth) / 2;
                    int y = (getHeight() - imgHeight) / 2 - 40;
                    g2d.drawImage(img, x, y, imgWidth, imgHeight, this);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                g2d.dispose();
            }
        };
        doctorCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        doctorCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        doctorCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                doctorCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                doctorCard.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                doctorCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                doctorCard.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                ((CardLayout) userContentPanel.getLayout()).show(userContentPanel, "doktor");
            }
        });

        JLabel lblDoctor = new JLabel("Doktor Yönetimi");
        lblDoctor.setFont(new Font("Inter", Font.BOLD, 28));
        lblDoctor.setForeground(Color.WHITE);
        lblDoctor.setHorizontalAlignment(SwingConstants.CENTER);
        lblDoctor.setBounds(0, 0, 430, 470);
        doctorCard.add(lblDoctor);

        // -- Hasta Kartı
        JPanel patientCard = new JPanel(null) {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(6, 95, 70),
                    0, getHeight(), new Color(4, 120, 87)
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Hasta resmi
                try {
                    ImageIcon icon = new ImageIcon(getClass().getResource("/Images/Hasta.png"));
                    Image img = icon.getImage();
                    int imgWidth = 150;
                    int imgHeight = 150;
                    int x = (getWidth() - imgWidth) / 2;
                    int y = (getHeight() - imgHeight) / 2 - 40;
                    g2d.drawImage(img, x, y, imgWidth, imgHeight, this);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                g2d.dispose();
            }
        };
        patientCard.setCursor(new Cursor(Cursor.HAND_CURSOR));
        patientCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        patientCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                patientCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 255, 255, 100), 2),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                patientCard.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                patientCard.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                patientCard.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                ((CardLayout) userContentPanel.getLayout()).show(userContentPanel, "hasta");
            }
        });

        JLabel lblPatient = new JLabel("Hasta Yönetimi");
        lblPatient.setFont(new Font("Inter", Font.BOLD, 28));
        lblPatient.setForeground(Color.WHITE);
        lblPatient.setHorizontalAlignment(SwingConstants.CENTER);
        lblPatient.setBounds(0, 0, 430, 470);
        patientCard.add(lblPatient);

        selectionPanel.add(doctorCard);
        selectionPanel.add(patientCard);

        doktorPanel = createDoctorPanel();
        hastaPanel = createHastaPanel();

        userContentPanel.add(selectionPanel, "selection");
        userContentPanel.add(doktorPanel, "doktor");
        userContentPanel.add(hastaPanel, "hasta");

        userPanel.add(userContentPanel, BorderLayout.CENTER);
        ((CardLayout) userContentPanel.getLayout()).show(userContentPanel, "selection");

        return userPanel;
    }

    // Doktor Paneli
    private JPanel createDoctorPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        JButton btnBack = new JButton("← Geri");
        btnBack.setBounds(15, 15, 90, 30);
        styleActionButton(btnBack);
        btnBack.addActionListener(e ->
            ((CardLayout) userContentPanel.getLayout()).show(userContentPanel, "selection")
        );
        panel.add(btnBack);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(15, 60, 600, 300);
        table_doktor = new JTable(doktorModel);
        styleTable1(table_doktor);
        scrollPane.setViewportView(table_doktor);
        panel.add(scrollPane);

        JButton btnPrevDoc = new JButton("Önceki");
        styleActionButton(btnPrevDoc);
        btnPrevDoc.setBounds(15, 365, 80, 30);
        btnPrevDoc.addActionListener(e -> prevDoctorPage());
        panel.add(btnPrevDoc);

        lblDoctorPageInfo = new JLabel("Sayfa 1");
        lblDoctorPageInfo.setBounds(110, 365, 100, 30);
        panel.add(lblDoctorPageInfo);

        JButton btnNextDoc = new JButton("Sonraki");
        styleActionButton(btnNextDoc);
        btnNextDoc.setBounds(200, 365, 80, 30);
        btnNextDoc.addActionListener(e -> nextDoctorPage());
        panel.add(btnNextDoc);

        JPanel formPanel = new JPanel(null);
        formPanel.setBackground(Color.WHITE);
        formPanel.setBounds(630, 60, 300, 335);
        panel.add(formPanel);

        addFormField(formPanel, "Ad Soyad:", fld_dName = createTextField(), 0);
        addFormField(formPanel, "T.C. No:", fld_dTcno = createTextField(), 60);
        addFormField(formPanel, "Şifre:", fld_dPass = createPasswordField(), 120);
        addFormField(formPanel, "Kullanıcı ID:", fld_doktorID = createTextField(), 180);

        JButton btnAdd = createActionButton("Doktor Ekle", e -> addDoctor());
        btnAdd.setBounds(0, 240, 300, 35);
        formPanel.add(btnAdd);

        JButton btnDelete = createActionButton("Doktor Sil", e -> deleteDoctor());
        btnDelete.setBounds(0, 285, 300, 35);
        formPanel.add(btnDelete);

        return panel;
    }

    // Hasta Paneli
    private JPanel createHastaPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        JButton btnBack = new JButton("← Geri");
        btnBack.setBounds(15, 15, 90, 30);
        styleActionButton(btnBack);
        btnBack.addActionListener(e ->
            ((CardLayout) userContentPanel.getLayout()).show(userContentPanel, "selection")
        );
        panel.add(btnBack);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(15, 60, 920, 300);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        table_hasta = new JTable(hastaModel);
        styleTable1(table_hasta);

        // Sağ tık menüsü (Hasta paneli)
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem deleteItem = new JMenuItem("Sil");
        deleteItem.addActionListener(e -> deleteSelectedHasta());
        popupMenu.add(deleteItem);

        table_hasta.setComponentPopupMenu(popupMenu);
        table_hasta.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point point = e.getPoint();
                int currentRow = table_hasta.rowAtPoint(point);
                if (currentRow >= 0) {
                    table_hasta.setRowSelectionInterval(currentRow, currentRow);
                }
            }
        });

        scrollPane.setViewportView(table_hasta);
        panel.add(scrollPane);

        JButton btnPrevPatient = new JButton("Önceki");
        styleActionButton(btnPrevPatient);
        btnPrevPatient.setBounds(15, 370, 80, 30);
        btnPrevPatient.addActionListener(e -> prevPatientPage());
        panel.add(btnPrevPatient);

        lblPatientPageInfo = new JLabel("Sayfa 1");
        lblPatientPageInfo.setBounds(110, 370, 100, 30);
        panel.add(lblPatientPageInfo);

        JButton btnNextPatient = new JButton("Sonraki");
        styleActionButton(btnNextPatient);
        btnNextPatient.setBounds(200, 370, 80, 30);
        btnNextPatient.addActionListener(e -> nextPatientPage());
        panel.add(btnNextPatient);

        return panel;
    }

    // ------------------------------------------------------------------------
    // Main Dashboard Panel
    // ------------------------------------------------------------------------
    private JPanel createMainDashboardPanel() throws SQLException {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setBounds(15, 15, 920, 100);
        statsPanel.setBackground(Color.WHITE);

        JPanel doctorCard = createStatsCard("Toplam Doktor",
                String.valueOf(admin.getDoktorList().size()));
        lbl_doctorCount = (JLabel) doctorCard.getComponent(1);

        JPanel clinicCard = createStatsCard("Toplam Poliklinik",
                String.valueOf(clinic.getList().size()));
        lbl_clinicCount = (JLabel) clinicCard.getComponent(1);

        int totalPatients = admin.getHastaCount();
        JPanel patientCard = createStatsCard("Toplam Hasta", String.valueOf(totalPatients));
        lbl_patientCount = (JLabel) patientCard.getComponent(1);

        statsPanel.add(doctorCard);
        statsPanel.add(clinicCard);
        statsPanel.add(patientCard);
        panel.add(statsPanel);

        JLabel lblStartDate = new JLabel("Başlangıç Tarihi:");
        lblStartDate.setFont(new Font("Inter", Font.BOLD, 14));
        lblStartDate.setForeground(textColor);
        lblStartDate.setBounds(20, 130, 120, 30);
        panel.add(lblStartDate);

        startDateChooser = new JDateChooser();
        startDateChooser.setBounds(150, 130, 140, 30);
        startDateChooser.setDateFormatString("dd.MM.yyyy");
        panel.add(startDateChooser);

        JLabel lblEndDate = new JLabel("Bitiş Tarihi:");
        lblEndDate.setFont(new Font("Inter", Font.BOLD, 14));
        lblEndDate.setForeground(textColor);
        lblEndDate.setBounds(320, 130, 80, 30);
        panel.add(lblEndDate);

        endDateChooser = new JDateChooser();
        endDateChooser.setBounds(400, 130, 140, 30);
        endDateChooser.setDateFormatString("dd.MM.yyyy");
        panel.add(endDateChooser);

        JButton btnDoktorlar = new JButton("Doktorlar");
        btnDoktorlar.setBounds(560, 130, 100, 30);
        styleActionButton(btnDoktorlar);
        btnDoktorlar.addActionListener(e -> {
            Date start = startDateChooser.getDate();
            Date end   = endDateChooser.getDate();
            if (start == null || end == null) {
                Helper.showMsg("Lütfen tarih seçiniz!");
                return;
            }
            grafikPaneli.gosterDoktorKayitlari(start, end);
        });
        panel.add(btnDoktorlar);

        JButton btnPoliklinikler = new JButton("Poliklinikler");
        btnPoliklinikler.setBounds(670, 130, 110, 30);
        styleActionButton(btnPoliklinikler);
        btnPoliklinikler.addActionListener(e -> {
            Date start = startDateChooser.getDate();
            Date end   = endDateChooser.getDate();
            if (start == null || end == null) {
                Helper.showMsg("Lütfen tarih seçiniz!");
                return;
            }
            grafikPaneli.gosterKlinikKayitlari(start, end);
        });
        panel.add(btnPoliklinikler);

        JButton btnHastalar = new JButton("Hastalar");
        btnHastalar.setBounds(790, 130, 100, 30);
        styleActionButton(btnHastalar);
        btnHastalar.addActionListener(e -> {
            Date start = startDateChooser.getDate();
            Date end   = endDateChooser.getDate();
            if (start == null || end == null) {
                Helper.showMsg("Lütfen tarih seçiniz!");
                return;
            }
            grafikPaneli.gosterHastaKayitlari(start, end);
        });
        panel.add(btnHastalar);

        grafikPaneli = new GrafikPaneli(admin, clinic);
        JPanel chartPanel = grafikPaneli.getGrafikPaneli();
        chartPanel.setBounds(15, 180, 920, 260);
        panel.add(chartPanel);

        return panel;
    }

    // ------------------------------------------------------------------------
    // Klinik Paneli
    // ------------------------------------------------------------------------
    private JPanel createClinicPanel() {
        // Ana panel
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        // Üst Bilgi Kartları
        JPanel cardsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        cardsPanel.setBounds(15, 15, 920, 100);
        cardsPanel.setBackground(Color.WHITE);

        JPanel clinicCountCard = createStatsCard(
                "Toplam Poliklinik",
                String.valueOf(clinicModel.getRowCount())
        );
        JPanel doctorCountCard = createStatsCard(
                "Atanmış Doktor",
                "0"
        );
        lbl_assignedDoctorCount = (JLabel) doctorCountCard.getComponent(1);

        JPanel occupancyCard = createStatsCard(
                "Doluluk Oranı",
                "%0"
        );
        lblOccupancyRate = (JLabel) occupancyCard.getComponent(1);

        cardsPanel.add(clinicCountCard);
        cardsPanel.add(doctorCountCard);
        cardsPanel.add(occupancyCard);
        panel.add(cardsPanel);

        JPanel leftPanel = new JPanel(null);
        leftPanel.setBounds(15, 130, 450, 320);
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblClinicList = new JLabel("Poliklinikler");
        lblClinicList.setFont(new Font("Inter", Font.BOLD, 16));
        lblClinicList.setBounds(15, 15, 200, 25);
        leftPanel.add(lblClinicList);

        JPanel addClinicPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        addClinicPanel.setBounds(15, 50, 420, 40);
        addClinicPanel.setBackground(Color.WHITE);

        fld_clinicName = new JTextField(20);
        styleTextField(fld_clinicName);
        addClinicPanel.add(fld_clinicName);

        JButton btnAddClinic = new JButton("Ekle");
        styleActionButton(btnAddClinic);
        btnAddClinic.addActionListener(e -> addClinic());
        addClinicPanel.add(btnAddClinic);

        leftPanel.add(addClinicPanel);

     // YENİ: Kaldır butonu ekleniyor
        JButton btnRemoveClinic = new JButton("Kaldır");
        styleActionButton(btnRemoveClinic);
        btnRemoveClinic.setBackground(new Color(220, 38, 38)); // Kırmızı renk
        btnRemoveClinic.addActionListener(e -> {
            int selRow = table_clinic.getSelectedRow();
            if (selRow == -1) {
                Helper.showMsg("Lütfen bir poliklinik seçiniz!");
                return;
            }

            if (Helper.confirm("sure")) {
                try {
                    int clinicId = Integer.parseInt(table_clinic.getValueAt(selRow, 0).toString());
                    if (clinic.deleteClinic(clinicId)) {
                        Helper.showMsg("success");
                        updateClinicModel();
                        updateDashboard();
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });
        addClinicPanel.add(btnRemoveClinic);
        
        
        
        
        JScrollPane scrollClinic = new JScrollPane();
        scrollClinic.setBounds(15, 100, 420, 205);
        table_clinic = new JTable(clinicModel);
        styleTable1(table_clinic);

        // Sağ tık menüsü (Poliklinik güncelle)
        JPopupMenu clinicPopupMenu = new JPopupMenu();
        JMenuItem updateItem = new JMenuItem("Güncelle");
        updateItem.addActionListener(e -> {
            int selectedRow = table_clinic.getSelectedRow();
            if (selectedRow >= 0) {
                int clinicID = Integer.parseInt(table_clinic.getValueAt(selectedRow, 0).toString());
                String clinicName = table_clinic.getValueAt(selectedRow, 1).toString();

                // "UpdateClinicGUI" penceresi açalım
                Clinic c = new Clinic(clinicID, clinicName);
                UpdateClinicGUI updateFrame = new UpdateClinicGUI(c);
                updateFrame.setLocationRelativeTo(null);
                updateFrame.setVisible(true);
            } else {
                Helper.showMsg("Lütfen güncellenecek polikliniği seçiniz!");
            }
        });
        clinicPopupMenu.add(updateItem);

        table_clinic.setComponentPopupMenu(clinicPopupMenu);
        table_clinic.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point point = e.getPoint();
                int row = table_clinic.rowAtPoint(point);
                if (row >= 0) {
                    table_clinic.setRowSelectionInterval(row, row);
                }
            }
        });

        scrollClinic.setViewportView(table_clinic);
        leftPanel.add(scrollClinic);
        panel.add(leftPanel);

        JPanel rightPanel = new JPanel(null);
        rightPanel.setBounds(480, 130, 450, 320);
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblDoctorAssign = new JLabel("Doktor Ataması");
        lblDoctorAssign.setFont(new Font("Inter", Font.BOLD, 16));
        lblDoctorAssign.setBounds(15, 15, 200, 25);
        rightPanel.add(lblDoctorAssign);

        JPanel assignPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        assignPanel.setBounds(15, 50, 420, 40);
        assignPanel.setBackground(Color.WHITE);

        select_doctor = new JComboBox<>();
        styleComboBox(select_doctor);
        select_doctor.setPreferredSize(new Dimension(250, 35));
        assignPanel.add(select_doctor);

        JButton btnAssign = new JButton("Ata");
        styleActionButton(btnAssign);
        btnAssign.addActionListener(e -> assignDoctor());
        assignPanel.add(btnAssign);

        
     // YENİ: Kaldır butonu
        JButton btnRemove = new JButton("Kaldır");
        styleActionButton(btnRemove);
        btnRemove.setBackground(new Color(220, 38, 38)); // Kırmızımsı renk
        btnRemove.addActionListener(e -> removeDoctor());
        assignPanel.add(btnRemove);
        
        rightPanel.add(assignPanel);

        JScrollPane scrollWorker = new JScrollPane();
        scrollWorker.setBounds(15, 100, 420, 205);
        table_worker = new JTable();
        styleTable1(table_worker);
        scrollWorker.setViewportView(table_worker);
        rightPanel.add(scrollWorker);

        panel.add(rightPanel);

        table_clinic.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showWorkers();
            }
        });

        updateDoctorComboBox();
        updateOccupancyRate();
        try {
            int assignedCount = admin.getAssignedDoctorCount();
            lbl_assignedDoctorCount.setText(String.valueOf(assignedCount));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return panel;
    }
    private void removeDoctor() {
        int selClinicRow = table_clinic.getSelectedRow();
        int selDoctorRow = table_worker.getSelectedRow();
        
        if (selClinicRow == -1) {
            Helper.showMsg("Lütfen bir poliklinik seçiniz!");
            return;
        }
        
        if (selDoctorRow == -1) {
            Helper.showMsg("Lütfen kaldırılacak doktoru seçiniz!");
            return;
        }

        if (Helper.confirm("sure")) {
            try {
                int clinicId = Integer.parseInt(table_clinic.getValueAt(selClinicRow, 0).toString());
                int doctorId = Integer.parseInt(table_worker.getValueAt(selDoctorRow, 0).toString());
                
                boolean success = admin.removeWorker(doctorId, clinicId);
                if (success) {
                    Helper.showMsg("success");
                    showWorkers(); // Tabloyu güncelle
                    updateDashboard();
                    // Atanmış doktor sayısını güncelle
                    int assignedCount = admin.getAssignedDoctorCount();
                    lbl_assignedDoctorCount.setText(String.valueOf(assignedCount));
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
    // Doluluk Oranı
    private void updateOccupancyRate() {
        try {
            int maxCapacity = admin.getMaxCapacity(); 
            int todayAppointments = admin.getTodayAppointmentCount();
            double ratio = (double) todayAppointments / maxCapacity * 100;
            int occupancy = (int) ratio;
            lblOccupancyRate.setText("%" + occupancy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------
    // Randevu Dashboard
    // ------------------------------------------------------------------------
    private JPanel createAppointmentPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        JLabel lblFilterHasta = new JLabel("Hasta Adı:");
        lblFilterHasta.setBounds(50, 15, 100, 25);
        panel.add(lblFilterHasta);

        fld_filterHasta = new JTextField();
        fld_filterHasta.setBounds(50, 40, 150, 35);
        styleTextField(fld_filterHasta);
        panel.add(fld_filterHasta);

        JLabel lblFilterDoktor = new JLabel("Doktor Seç:");
        lblFilterDoktor.setBounds(220, 15, 100, 25);
        panel.add(lblFilterDoktor);

        cmb_filterDoktor = new JComboBox<>();
        cmb_filterDoktor.setBounds(220, 40, 150, 35);
        styleComboBox(cmb_filterDoktor);
        panel.add(cmb_filterDoktor);

        JLabel lblBas = new JLabel("Tarih (Başlangıç):");
        lblBas.setBounds(390, 15, 120, 25);
        panel.add(lblBas);

        dateChooserStart = new JDateChooser();
        dateChooserStart.setBounds(390, 40, 150, 35);
        dateChooserStart.setDateFormatString("dd.MM.yyyy");
        panel.add(dateChooserStart);

        JLabel lblBit = new JLabel("Tarih (Bitiş):");
        lblBit.setBounds(560, 15, 100, 25);
        panel.add(lblBit);

        dateChooserEnd = new JDateChooser();
        dateChooserEnd.setBounds(560, 40, 150, 35);
        dateChooserEnd.setDateFormatString("dd.MM.yyyy");
        panel.add(dateChooserEnd);

        JButton btnFilter = new JButton("Filtrele");
        btnFilter.setBounds(730, 40, 100, 35);
        styleActionButton(btnFilter);
        panel.add(btnFilter);

        JScrollPane scrollAppointment = new JScrollPane();
        scrollAppointment.setBounds(15, 90, 735, 300);
        table_appointment = new JTable(appointmentModel);
        styleTable1(table_appointment);
        table_appointment.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        scrollAppointment.setViewportView(table_appointment);
        panel.add(scrollAppointment);

        btnPrevPage = new JButton("Önceki");
        btnPrevPage.setBounds(15, 400, 80, 30);
        styleActionButton(btnPrevPage);
        panel.add(btnPrevPage);

        lblPageInfo = new JLabel("Sayfa 1");
        lblPageInfo.setBounds(105, 400, 100, 30);
        panel.add(lblPageInfo);

        btnNextPage = new JButton("Sonraki");
        btnNextPage.setBounds(210, 400, 80, 30);
        styleActionButton(btnNextPage);
        panel.add(btnNextPage);

        JPanel rightPanel = new JPanel(null);
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBounds(760, 90, 180, 300);
        panel.add(rightPanel);

        JButton btnOnayla = new JButton("Onayla");
        btnOnayla.setBounds(0, 0, 180, 40);
        styleActionButton(btnOnayla);
        btnOnayla.addActionListener(e -> approveSelectedAppointments());
        rightPanel.add(btnOnayla);

        JButton btnIptal = new JButton("İptal");
        btnIptal.setBounds(0, 50, 180, 40);
        styleActionButton(btnIptal);
        btnIptal.addActionListener(e -> cancelSelectedAppointments());
        rightPanel.add(btnIptal);

        JButton btnSelectAll = new JButton("Tümünü Seç");
        btnSelectAll.setBounds(0, 100, 180, 40);
        styleActionButton(btnSelectAll);
        btnSelectAll.addActionListener(e -> selectAllPhysically());
        rightPanel.add(btnSelectAll);

        JButton btnStats = new JButton("Randevu İstatistikleri");
        btnStats.setBounds(0, 150, 180, 40);
        styleActionButton(btnStats);
        btnStats.addActionListener(e -> {
            int idx = tabbedPane.indexOfComponent(panel);
            if (idx != -1) {
                tabbedPane.setComponentAt(idx, appointmentStatsPanel);
                tabbedPane.setTitleAt(idx, "Randevu İstatistikleri");
                showAndStartStats();
            }
        });
        rightPanel.add(btnStats);

        btnFilter.addActionListener(e -> filterAppointments());
        btnPrevPage.addActionListener(e -> prevPage());
        btnNextPage.addActionListener(e -> nextPage());

        updateDoctorComboBox();

        return panel;
    }

    // ------------------------------------------------------------------------
    // Randevu İstatistikleri Paneli
    // ------------------------------------------------------------------------
    private JPanel createAppointmentStatsPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Randevu İstatistikleri");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 18));
        lblTitle.setForeground(primaryColor);
        lblTitle.setBounds(20, 20, 300, 30);
        panel.add(lblTitle);

        JButton btnBack = new JButton("← Geri");
        btnBack.setBounds(800, 20, 90, 30);
        styleActionButton(btnBack);
        btnBack.addActionListener(e -> {
            if (statsTimer != null) {
                statsTimer.stop();
            }
            int idx = tabbedPane.indexOfComponent(panel);
            if (idx != -1) {
                tabbedPane.setComponentAt(idx, appointmentPanel);
                tabbedPane.setTitleAt(idx, "Randevu Dashboard");
            }
        });
        panel.add(btnBack);

        JLabel lblStart = new JLabel("Başlangıç Tarihi:");
        lblStart.setFont(new Font("Inter", Font.BOLD, 14));
        lblStart.setForeground(textColor);
        lblStart.setBounds(20, 60, 120, 30);
        panel.add(lblStart);

        statsStartDate = new JDateChooser();
        statsStartDate.setBounds(150, 60, 140, 30);
        statsStartDate.setDateFormatString("dd.MM.yyyy");
        panel.add(statsStartDate);

        JLabel lblEnd = new JLabel("Bitiş Tarihi:");
        lblEnd.setFont(new Font("Inter", Font.BOLD, 14));
        lblEnd.setForeground(textColor);
        lblEnd.setBounds(320, 60, 80, 30);
        panel.add(lblEnd);

        statsEndDate = new JDateChooser();
        statsEndDate.setBounds(400, 60, 140, 30);
        statsEndDate.setDateFormatString("dd.MM.yyyy");
        panel.add(statsEndDate);

        JButton btnFiltre = new JButton("Filtrele");
        btnFiltre.setBounds(560, 60, 100, 30);
        styleActionButton(btnFiltre);
        btnFiltre.addActionListener(e -> updateAppointmentStats());
        panel.add(btnFiltre);

        JPanel statsCardsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsCardsPanel.setBackground(Color.WHITE);
        statsCardsPanel.setBounds(20, 110, 900, 80);
        panel.add(statsCardsPanel);

        lblWeeklyValue    = new JLabel("0");
        lblMonthlyValue   = new JLabel("0");
        lblYearlyValue    = new JLabel("0");
        lblCancelledValue = new JLabel("0");

        statsCardsPanel.add(createStatsBlock("Haftalık", lblWeeklyValue));
        statsCardsPanel.add(createStatsBlock("Aylık", lblMonthlyValue));
        statsCardsPanel.add(createStatsBlock("Yıllık", lblYearlyValue));
        statsCardsPanel.add(createStatsBlock("İptal",  lblCancelledValue));

        grafikPaneliStats = new GrafikPaneli(admin, clinic);
        JPanel chartPanel = grafikPaneliStats.getGrafikPaneli();
        chartPanel.setBounds(20, 200, 900, 200);
        panel.add(chartPanel);

        JPanel btnPanel = new JPanel(null);
        btnPanel.setBackground(Color.WHITE);
        btnPanel.setBounds(20, 390, 900, 40);
        panel.add(btnPanel);

        JButton btnHaftalik = new JButton("Haftalık");
        btnHaftalik.setBounds(0, 0, 100, 40);
        styleActionButton(btnHaftalik);
        btnHaftalik.addActionListener(e -> {
            try {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                Date start = cal.getTime();
                cal.add(Calendar.DATE, 6);
                Date end = cal.getTime();
                grafikPaneliStats.gosterHaftalikGrafik(start, end);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        btnPanel.add(btnHaftalik);

        JButton btnAylik = new JButton("Aylık");
        btnAylik.setBounds(110, 0, 100, 40);
        styleActionButton(btnAylik);
        btnAylik.addActionListener(e -> {
            try {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 1);
                Date start = cal.getTime();
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                Date end = cal.getTime();
                grafikPaneliStats.gosterAylikGrafik(start, end);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        btnPanel.add(btnAylik);

        JButton btnYillik = new JButton("Yıllık");
        btnYillik.setBounds(220, 0, 100, 40);
        styleActionButton(btnYillik);
        btnYillik.addActionListener(e -> {
            try {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MONTH, 0);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                Date start = cal.getTime();
                cal.set(Calendar.MONTH, 11);
                cal.set(Calendar.DAY_OF_MONTH, 31);
                Date end = cal.getTime();
                grafikPaneliStats.gosterYillikGrafik(start, end);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        btnPanel.add(btnYillik);

        JButton btnIptal = new JButton("İptal");
        btnIptal.setBounds(330, 0, 100, 40);
        styleActionButton(btnIptal);
        btnIptal.addActionListener(e -> {
            Date start = statsStartDate.getDate();
            Date end   = statsEndDate.getDate();
            if (start == null || end == null) {
                Helper.showMsg("Lütfen başlangıç ve bitiş tarihlerini seçiniz!");
                return;
            }
            try {
                grafikPaneliStats.grafigiYenile();
                var stats = admin.getAppointmentStats(start, end);
                lblWeeklyValue.setText(String.valueOf(stats.get("weekly")));
                lblMonthlyValue.setText(String.valueOf(stats.get("monthly")));
                lblYearlyValue.setText(String.valueOf(stats.get("yearly")));
                lblCancelledValue.setText(String.valueOf(stats.get("cancelled")));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        btnPanel.add(btnIptal);

        return panel;
    }

    // Timer kapatma
    @Override
    public void dispose() {
        if (statsTimer != null) {
            statsTimer.stop();
        }
        super.dispose();
    }

    // Doktor tablosu vb. event
    private void setupTableListeners() {
        if (table_doktor == null) return;

        // Satır seçilince
        table_doktor.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                try {
                    int selectedRow = table_doktor.getSelectedRow();
                    if (selectedRow >= 0) {
                        fld_doktorID.setText(table_doktor.getValueAt(selectedRow, 0).toString());
                    }
                } catch (Exception ex) {
                    // Seçim yok
                }
            }
        });

        // Hücre değişince veritabanına yansıt
        table_doktor.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                try {
                    int selectID = Integer.parseInt(table_doktor.getValueAt(row, 0).toString());
                    String selName = table_doktor.getValueAt(row, 1).toString();
                    String selTcno = table_doktor.getValueAt(row, 2).toString();
                    String selPass = table_doktor.getValueAt(row, 3).toString();

                    boolean control = admin.updateDoktor(selectID, selTcno, selName, selPass);
                    if (!control) {
                        Helper.showMsg("error");
                    }
                } catch (SQLException ex2) {
                    ex2.printStackTrace();
                }
            }
        });
    }

    // Doktor Ekle
    private void addDoctor() {
        if (fld_dName.getText().isEmpty()
                || String.valueOf(fld_dPass.getPassword()).isEmpty()
                || fld_dTcno.getText().isEmpty()) {
            Helper.showMsg("fill");
            return;
        }
        try {
            boolean control = admin.addDoktor(
                    fld_dTcno.getText(),
                    String.valueOf(fld_dPass.getPassword()),
                    fld_dName.getText()
            );
            if (control) {
                Helper.showMsg("success");
                fld_dName.setText(null);
                fld_dTcno.setText(null);
                fld_dPass.setText(null);

                allDoctors = admin.getDoktorList();
                currentDoctorPage = 1;
                refreshDoctorTableByPage();
                updateDoctorComboBox();
                updateDashboard();
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    // Doktor Sil
    private void deleteDoctor() {
        if (fld_doktorID.getText().isEmpty()) {
            Helper.showMsg("Lütfen geçerli bir doktor seçiniz!");
            return;
        }
        if (Helper.confirm("sure")) {
            int selectID = Integer.parseInt(fld_doktorID.getText());
            try {
                boolean control = admin.deleteDoktor(selectID);
                if (control) {
                    Helper.showMsg("success");
                    fld_doktorID.setText(null);

                    allDoctors = admin.getDoktorList();
                    currentDoctorPage = 1;
                    refreshDoctorTableByPage();
                    updateDoctorComboBox();
                    updateDashboard();
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    // Hasta Sil
    private void deleteSelectedHasta() {
        int selectedRow = table_hasta.getSelectedRow();
        if (selectedRow >= 0) {
            if (Helper.confirm("sure")) {
                try {
                    int hastaId = (int) table_hasta.getValueAt(selectedRow, 0);

                    if (admin.deleteHasta(hastaId)) {
                        Helper.showMsg("success");
                        allPatients = admin.getHastaList();
                        refreshPatientTableByPage();
                        updateDashboard();
                    } else {
                        Helper.showMsg("error");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    Helper.showMsg("error");
                }
            }
        } else {
            Helper.showMsg("Lütfen bir hasta seçiniz!");
        }
    }

    // Klinik Ekle
    private void addClinic() {
        if (fld_clinicName.getText().isEmpty()) {
            Helper.showMsg("fill");
            return;
        }
        try {
            if (clinic.addClinic(fld_clinicName.getText())) {
                Helper.showMsg("success");
                fld_clinicName.setText(null);
                updateClinicModel();
                updateDashboard();
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    // Çalışanları Göster (Poliklinik altındaki)
    private void showWorkers() {
        int selRow = table_clinic.getSelectedRow();
        if (selRow >= 0) {
            int selClinicID = Integer.parseInt(table_clinic.getValueAt(selRow, 0).toString());
            DefaultTableModel workerModel = new DefaultTableModel();
            workerModel.setColumnIdentifiers(new Object[]{"ID", "Ad Soyad"});

            try {
                for (User doc : admin.getClinicDoctorList(selClinicID)) {
                    workerModel.addRow(new Object[]{doc.getId(), doc.getName()});
                }
                table_worker.setModel(workerModel);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        }
    }

    // Doktoru Polikliniğe Ata
    private void assignDoctor() {
        int selRow = table_clinic.getSelectedRow();
        if (selRow >= 0) {
            int selClinicID = Integer.parseInt(table_clinic.getValueAt(selRow, 0).toString());
            if (select_doctor.getSelectedIndex() == 0) {
                Helper.showMsg("Lütfen bir doktor seçiniz!");
                return;
            }
            Item doctorItem = (Item) select_doctor.getSelectedItem();
            try {
                boolean isAlreadyAssigned = false;
                for (User assignedDoctor : admin.getClinicDoctorList(selClinicID)) {
                    if (assignedDoctor.getId() == doctorItem.getKey()) {
                        isAlreadyAssigned = true;
                        break;
                    }
                }
                if (isAlreadyAssigned) {
                    Helper.showMsg("Bu doktor zaten poliklinikte çalışıyor!");
                    return;
                }

                boolean control = admin.addWorker(doctorItem.getKey(), selClinicID);
                if (control) {
                    Helper.showMsg("success");
                    showWorkers();
                    updateDashboard();
                    int assignedCount = admin.getAssignedDoctorCount();
                    lbl_assignedDoctorCount.setText(String.valueOf(assignedCount));
                }
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        } else {
            Helper.showMsg("Lütfen bir poliklinik seçiniz!");
        }
    }

    // ------------------------------------------------------------------------
    // “Yenile” butonu
    // ------------------------------------------------------------------------
    private void refreshAllData() {
        try {
            // Doktor Modeli
            allDoctors = admin.getDoktorList();
            currentDoctorPage = 1;
            refreshDoctorTableByPage();

            // Hasta Modeli  
            allPatients = admin.getHastaList();
            currentPatientPage = 1;
            refreshPatientTableByPage();

            // Klinik Modeli
            updateClinicModel();

            // Doktor ComboBox
            updateDoctorComboBox();

            // Dashboard
            updateDashboard();

            // Randevu tablosu
            filterAppointments();

            // Doluluk Oranı
            updateOccupancyRate();

            // Atanmış Doktor
            int assignedCount = admin.getAssignedDoctorCount();
            lbl_assignedDoctorCount.setText(String.valueOf(assignedCount));

            // Header'daki max kapasite güncelle 
            int newCap = admin.getMaxCapacity();
            lblMaxCapacity.setText("Maksimum Kapasite: " + newCap);

            // Başarılı mesajı göster
            Helper.showMsg("Tüm veriler yenilendi.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }    // ------------------------------------------------------------------------
    // Çıkış
    // ------------------------------------------------------------------------
    private void logout() {
        LoginGUI login = new LoginGUI();
        login.setVisible(true);
        dispose();
    }

    // ------------------------------------------------------------------------
    // Dashboard Güncelle
    // ------------------------------------------------------------------------
    private void updateDashboard() throws SQLException {
        lbl_doctorCount.setText(String.valueOf(admin.getDoktorList().size()));
        lbl_clinicCount.setText(String.valueOf(clinic.getList().size()));
        lbl_patientCount.setText(String.valueOf(admin.getHastaCount()));

        if (grafikPaneli != null) {
            grafikPaneli.grafigiYenile();
        }
    }

    // ------------------------------------------------------------------------
    // Klinik Modelini Yenile
    // ------------------------------------------------------------------------
    private void updateClinicModel() throws SQLException {
        clinicModel.setRowCount(0);
        List<Clinic> klinikListesi = clinic.getList();
        for (Clinic c : klinikListesi) {
            clinicData[0] = c.getId();
            clinicData[1] = c.getName();
            clinicModel.addRow(clinicData);
        }
    }

    // ------------------------------------------------------------------------
    // Doktor Combobox (Poliklinik atama + Randevu filtre)
    // ------------------------------------------------------------------------
    private void updateDoctorComboBox() {
        if (select_doctor != null) {
            select_doctor.removeAllItems();
            select_doctor.addItem(new Item(0, "Doktor Seçiniz"));
        }
        if (cmb_filterDoktor != null) {
            cmb_filterDoktor.removeAllItems();
            cmb_filterDoktor.addItem(new Item(0, "Tümü"));
        }
        try {
            List<User> doktorListesi = admin.getDoktorList();
            for (User d : doktorListesi) {
                if (select_doctor != null) {
                    select_doctor.addItem(new Item(d.getId(), d.getName()));
                }
                if (cmb_filterDoktor != null) {
                    cmb_filterDoktor.addItem(new Item(d.getId(), d.getName()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------------
    // Randevu Filtre
    // ------------------------------------------------------------------------
    private void filterAppointments() {
        String hastaAd = fld_filterHasta.getText().trim();
        Item drItem = (Item) cmb_filterDoktor.getSelectedItem();
        int doktorID = (drItem != null) ? drItem.getKey() : 0;

        Date dtStart = dateChooserStart.getDate();
        Date dtEnd = dateChooserEnd.getDate();

        try {
            allAppointments = admin.getAppointmentList(hastaAd, doktorID, dtStart, dtEnd);
            currentPage = 1;
            refreshTableByPage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void selectAllPhysically() {
        appointmentModel.setRowCount(0);
        for (Appointment a : allAppointments) {
            appointmentData[0] = a.getId();
            appointmentData[1] = a.getHastaAdi();
            appointmentData[2] = a.getDoktorAdi();
            appointmentData[3] = a.getAppDate();
            appointmentData[4] = a.getStatus();
            appointmentModel.addRow(appointmentData);
        }
        int rowCount = appointmentModel.getRowCount();
        if (rowCount > 0) {
            table_appointment.setRowSelectionInterval(0, rowCount - 1);
        }
        lblPageInfo.setText("Tümü Gösteriliyor");
    }

    private void approveSelectedAppointments() {
        int[] selectedRows = table_appointment.getSelectedRows();
        if (selectedRows.length == 0) {
            Helper.showMsg("Onaylamak için satır(lar) seçiniz!");
            return;
        }
        try {
            for (int row : selectedRows) {
                Appointment a;
                if (appointmentModel.getRowCount() == allAppointments.size()) {
                    a = allAppointments.get(row);
                } else {
                    int realIndex = (currentPage - 1) * pageSize + row;
                    a = allAppointments.get(realIndex);
                }
                boolean success = admin.approveAppointment(a.getId());
                if (success) {
                    a.setStatus("aktif");
                }
            }
            selectAllPhysically();
            table_appointment.clearSelection();
            Helper.showMsg("Tüm Randevular Onaylanmıştır");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void cancelSelectedAppointments() {
        int[] selectedRows = table_appointment.getSelectedRows();
        if (selectedRows.length == 0) {
            Helper.showMsg("İptal etmek için satır(lar) seçiniz!");
            return;
        }
        try {
            for (int row : selectedRows) {
                Appointment a;
                if (appointmentModel.getRowCount() == allAppointments.size()) {
                    a = allAppointments.get(row);
                } else {
                    int realIndex = (currentPage - 1) * pageSize + row;
                    a = allAppointments.get(realIndex);
                }
                boolean success = admin.cancelAppointment(a.getId());
                if (success) {
                    a.setStatus("iptal");
                }
            }
            selectAllPhysically();
            table_appointment.clearSelection();
            Helper.showMsg("Tüm Randevular İptal Edilmiştir");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // Sayfalama (Randevu)
    private void prevPage() {
        if (currentPage > 1) {
            currentPage--;
            refreshTableByPage();
        }
    }

    private void nextPage() {
        int maxPage = (int) Math.ceil((double) allAppointments.size() / pageSize);
        if (currentPage < maxPage) {
            currentPage++;
            refreshTableByPage();
        }
    }

    private void refreshTableByPage() {
        if (appointmentModel.getRowCount() == allAppointments.size()) {
            return;
        }
        appointmentModel.setRowCount(0);
        int startIdx = (currentPage - 1) * pageSize;
        int endIdx   = Math.min(startIdx + pageSize, allAppointments.size());
        for (int i = startIdx; i < endIdx; i++) {
            Appointment a = allAppointments.get(i);
            appointmentData[0] = a.getId();
            appointmentData[1] = a.getHastaAdi();
            appointmentData[2] = a.getDoktorAdi();
            appointmentData[3] = a.getAppDate();
            appointmentData[4] = a.getStatus();
            appointmentModel.addRow(appointmentData);
        }
        int maxPage = (int) Math.ceil((double) allAppointments.size() / pageSize);
        lblPageInfo.setText("Sayfa " + currentPage + " / " + maxPage);
    }

    // Randevu İstatistikleri Filtrele
    private void updateAppointmentStats() {
        Date start = statsStartDate.getDate();
        Date end   = statsEndDate.getDate();

        if (start == null || end == null) {
            Helper.showMsg("Lütfen başlangıç ve bitiş tarihlerini seçiniz!");
            return;
        }

        try {
            var stats = admin.getAppointmentStats(start, end);
            lblWeeklyValue.setText(String.valueOf(stats.get("weekly")));
            lblMonthlyValue.setText(String.valueOf(stats.get("monthly")));
            lblYearlyValue.setText(String.valueOf(stats.get("yearly")));
            lblCancelledValue.setText(String.valueOf(stats.get("cancelled")));

            grafikPaneliStats.grafigiYenile();
        } catch (Exception ex) {
            ex.printStackTrace();
            Helper.showMsg("İstatistikler hesaplanırken bir hata oluştu!");
        }
    }

    // Timer
    private void setupStatsTimer() {
        statsTimer = new Timer(10000, e -> {
            try {
                var stats = admin.getAppointmentStats(null, null);
                SwingUtilities.invokeLater(() -> {
                    lblWeeklyValue.setText(String.valueOf(stats.get("weekly")));
                    lblMonthlyValue.setText(String.valueOf(stats.get("monthly")));
                    lblYearlyValue.setText(String.valueOf(stats.get("yearly")));
                    lblCancelledValue.setText(String.valueOf(stats.get("cancelled")));
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void showAndStartStats() {
        try {
            var stats = admin.getAppointmentStats(null, null);
            lblWeeklyValue.setText(String.valueOf(stats.get("weekly")));
            lblMonthlyValue.setText(String.valueOf(stats.get("monthly")));
            lblYearlyValue.setText(String.valueOf(stats.get("yearly")));
            lblCancelledValue.setText(String.valueOf(stats.get("cancelled")));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (statsTimer == null) {
            setupStatsTimer();
        }
        statsTimer.start();
    }

    // ------------------------------------------------------------------------
    // Yardımcı UI metotları
    // ------------------------------------------------------------------------
    private JButton createHeaderButton(String text, int x) {
        JButton button = new JButton(text);
        button.setBounds(x, 15, 90, 30);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setBackground(Color.WHITE);
        button.setForeground(primaryColor);
        button.setBorder(BorderFactory.createLineBorder(primaryColor, 1, true));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void styleActionButton(JButton button) {
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setBackground(primaryColor);
        button.setForeground(Color.WHITE);
        button.setBorder(new EmptyBorder(5, 15, 5, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(5, 10, 5, 10)
        ));
    }

    private void styleComboBox(JComboBox<?> combo) {
        combo.setFont(new Font("Inter", Font.PLAIN, 14));
        combo.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(borderColor, 1, true),
                new EmptyBorder(0, 5, 0, 5)
        ));
        combo.setBackground(Color.WHITE);
    }

    private void styleTable1(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Inter", Font.PLAIN, 14));
        table.setShowGrid(true);
        table.setGridColor(new Color(229, 231, 235));
        table.setSelectionBackground(new Color(209, 250, 229));
        table.setSelectionForeground(new Color(6, 95, 70));

        // Alternatif satır renkleri
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			/**
			 * 
			 */


			@Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                }

                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                return c;
            }
        });
    }

    private JPanel createStatsCard(String title, String value) {
        JPanel card = new JPanel(null);
        card.setBackground(secondaryColor);
        card.setBorder(BorderFactory.createLineBorder(borderColor));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Inter", Font.BOLD, 16));
        titleLabel.setForeground(textColor);
        titleLabel.setBounds(20, 15, 200, 25);
        card.add(titleLabel);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Inter", Font.BOLD, 24));
        valueLabel.setForeground(primaryColor);
        valueLabel.setBounds(20, 45, 200, 35);
        card.add(valueLabel);

        return card;
    }

    private JPanel createStatsBlock(String title, JLabel valueLabel) {
        JPanel card = new JPanel(null);
        card.setBackground(secondaryColor);
        card.setBorder(BorderFactory.createLineBorder(borderColor));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 14));
        lblTitle.setForeground(textColor);
        lblTitle.setBounds(10, 10, 200, 20);
        card.add(lblTitle);

        valueLabel.setFont(new Font("Inter", Font.BOLD, 20));
        valueLabel.setForeground(primaryColor);
        valueLabel.setBounds(10, 40, 200, 25);
        card.add(valueLabel);

        return card;
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        styleTextField(field);
        return field;
    }

    private JPasswordField createPasswordField() {
        JPasswordField field = new JPasswordField();
        styleTextField(field);
        return field;
    }

    private JButton createActionButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        styleActionButton(button);
        button.addActionListener(action);
        return button;
    }

    private void addFormField(JPanel panel, String labelText, JComponent field, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Inter", Font.BOLD, 14));
        label.setForeground(textColor);
        label.setBounds(0, y, 300, 25);
        panel.add(label);

        field.setBounds(0, y + 25, 300, 35);
        panel.add(field);
    }

    // ------------------------------------------------------------------------
    // Doktor sayfalama
    // ------------------------------------------------------------------------
    private void prevDoctorPage() {
        if (currentDoctorPage > 1) {
            currentDoctorPage--;
            refreshDoctorTableByPage();
        }
    }

    private void nextDoctorPage() {
        int maxPage = (int) Math.ceil((double) allDoctors.size() / doctorPageSize);
        if (currentDoctorPage < maxPage) {
            currentDoctorPage++;
            refreshDoctorTableByPage();
        }
    }

    private void refreshDoctorTableByPage() {
        doktorModel.setRowCount(0);
        int startIdx = (currentDoctorPage - 1) * doctorPageSize;
        int endIdx   = Math.min(startIdx + doctorPageSize, allDoctors.size());
        for (int i = startIdx; i < endIdx; i++) {
            User d = allDoctors.get(i);
            Object[] rowData = {
                    d.getId(),
                    d.getName(),
                    d.getTcno(),
                    d.getPassword()
            };
            doktorModel.addRow(rowData);
        }
        int maxPage = (int) Math.ceil((double) allDoctors.size() / doctorPageSize);
        lblDoctorPageInfo.setText("Sayfa " + currentDoctorPage + " / " + maxPage);
    }

    // ------------------------------------------------------------------------
    // Hasta sayfalama
    // ------------------------------------------------------------------------
    private void prevPatientPage() {
        if (currentPatientPage > 1) {
            currentPatientPage--;
            refreshPatientTableByPage();
        }
    }

    private void nextPatientPage() {
        int maxPage = (int) Math.ceil((double) allPatients.size() / patientPageSize);
        if (currentPatientPage < maxPage) {
            currentPatientPage++;
            refreshPatientTableByPage();
        }
    }

    private void refreshPatientTableByPage() {
        hastaModel.setRowCount(0);
        int startIdx = (currentPatientPage - 1) * patientPageSize;
        int endIdx = Math.min(startIdx + patientPageSize, allPatients.size());

        for (int i = startIdx; i < endIdx; i++) {
            User hasta = allPatients.get(i);
            Object[] row = {
                    hasta.getId(),
                    hasta.getName(),
                    hasta.getTcno(),
                    hasta.getPassword(),
                    (hasta.getEmail() != null ? hasta.getEmail() : ""),
                    (hasta.getPhone() != null ? hasta.getPhone() : ""),
                    (hasta.getGender() != null ? hasta.getGender() : "")
            };
            hastaModel.addRow(row);
        }

        int maxPage = (int) Math.ceil((double) allPatients.size() / patientPageSize);
        lblPatientPageInfo.setText("Sayfa " + currentPatientPage + " / " + maxPage);
    }
}
