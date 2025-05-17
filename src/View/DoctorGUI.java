package View;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.formdev.flatlaf.FlatLightLaf;
import com.toedter.calendar.JDateChooser;

import Model.Appointment;
import Model.Doctor;
import Model.Whour;
import dbhelper.Helper;

public class DoctorGUI extends JFrame {

    // serialVersionUID uyarısını önlemek için
    private static final long serialVersionUID = 1L;

    private final Doctor doctor;

    private final JPanel w_pane;
    private final JTable table_whour;
    private final DefaultTableModel whourModel;
    private final Object[] whourData;

    private final JTable table_doctorAppoint;
    private final DefaultTableModel d_appointModel;
    private final Object[] d_appointData;

    private final Appointment appoint;
    private final JPopupMenu d_appointMenu;

    // Dashboard bileşenleri
    private final JPanel dashboardPanel;
    private final JTable tableTodayApp;
    private final DefaultTableModel todayAppModel;
    private final JPanel statsPanel = new JPanel();

    // Renkler (LoginGUI / RegisterGUI ile uyumlu)
    private static final Color PRIMARY_COLOR       = new Color(6, 95, 70);      // Koyu yeşil
    private static final Color SECONDARY_COLOR     = new Color(240, 253, 244);  // Açık yeşil
    private static final Color GRAY_TEXT_COLOR     = new Color(75, 85, 99);     // Gri metin
    private static final Color BORDER_COLOR        = new Color(209, 213, 219); // Çerçeve rengi
    private static final Color TABLE_HEADER_COLOR  = new Color(243, 244, 246);
    private static final Color DANGER_COLOR        = new Color(220, 38, 38);    // Kırmızı (Sil butonu)
    private static final Color UPCOMING_COLOR = new Color(34, 197, 94);  // Yeşil
    private static final Color NEXT_COLOR = new Color(234, 179, 8);     // Sarı
    private static final Color CANCELLED_COLOR = new Color(239, 68, 68); // Kırmızı

    public static void main(String[] args) {
        // 1) FlatLaf ile modern görünüm kazandır
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());

            // 2) Varsayılan fontu ve ARC değerlerini ayarla
            UIManager.put("defaultFont",       new Font("Inter", Font.PLAIN, 14));
            UIManager.put("Button.arc",        999);
            UIManager.put("Component.arc",     999);
            UIManager.put("TextComponent.arc", 999);

        } catch (Exception e) {
            e.printStackTrace();
        }

        EventQueue.invokeLater(() -> {
            try {
                Doctor doctor = new Doctor();
                DoctorGUI frame = new DoctorGUI(doctor);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public DoctorGUI(Doctor doctor) {
        this.doctor = doctor;
        this.appoint = new Appointment();

        // Model initialization
        whourModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        Object[] colWhour = {"ID", "Tarih"};
        whourModel.setColumnIdentifiers(colWhour);
        whourData = new Object[2];

        d_appointModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        Object[] colAppoint = {"ID", "Hasta", "Tarih"};
        d_appointModel.setColumnIdentifiers(colAppoint);
        d_appointData = new Object[3];

        todayAppModel = new DefaultTableModel() {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        Object[] colTodayApp = {"Saat", "Hasta Adı", "Durum", "Not"};
        todayAppModel.setColumnIdentifiers(colTodayApp);

        // Frame ayarları
        setTitle("Hastane Yönetim Sistemi");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 1000, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // Ana panel
        w_pane = new JPanel();
        w_pane.setBackground(SECONDARY_COLOR);
        w_pane.setBorder(new EmptyBorder(15, 15, 15, 15));
        w_pane.setLayout(null);
        setContentPane(w_pane);

        // Tablolar
        table_whour         = new JTable(whourModel);
        table_doctorAppoint = new JTable(d_appointModel);

        // “Bugünün randevuları” tablosu (Dashboard)
        tableTodayApp       = new JTable(todayAppModel);

        // Randevu menüsü
        d_appointMenu = new JPopupMenu();

        // Dashboard paneli
        dashboardPanel = new JPanel();
        dashboardPanel.setLayout(null);
        dashboardPanel.setBackground(Color.WHITE);

        // istatistik paneli
        statsPanel.setLayout(new GridLayout(1, 3, 20, 0));
        statsPanel.setBackground(Color.WHITE);

        // UI bileşenlerini oluştur
        createHeaderPanel();
        createTabbedPane();
        setupAppointmentMenu();
        
        // Veri yükle
        loadData();
    }

    /**
     * Header (üst bant)
     */
    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBounds(0, 0, 1000, 60);
        headerPanel.setLayout(null);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        w_pane.add(headerPanel);

        JLabel lblWelcome = new JLabel("Hoşgeldiniz, Dr. " + doctor.getName());
        lblWelcome.setFont(new Font("Inter", Font.BOLD, 16));
        lblWelcome.setForeground(PRIMARY_COLOR);
        lblWelcome.setBounds(20, 15, 400, 30);
        headerPanel.add(lblWelcome);

        // Blog butonu
        JButton btnBlog = createHeaderButton("Blog", 770);
        btnBlog.addActionListener(e -> {
            dispose();
            BlogGUI blogGUI = new BlogGUI(doctor);
            blogGUI.setVisible(true);
        });
        headerPanel.add(btnBlog);

        // Çıkış butonu
        JButton btnLogout = createHeaderButton("Çıkış Yap", 880);
        btnLogout.addActionListener(e -> {
            if (Helper.confirm("Çıkış yapmak istediğinizden emin misiniz?")) {
                LoginGUI login = new LoginGUI();
                login.setVisible(true);
                dispose();
            }
        });
        headerPanel.add(btnLogout);
    }

    /**
     * Sekmeli yapı (Dashboard, Çalışma Saatleri, Randevular)
     */
    private void createTabbedPane() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBounds(15, 75, 955, 470);
        tabbedPane.setFont(new Font("Inter", Font.BOLD, 14));
        tabbedPane.setBackground(SECONDARY_COLOR);

        // Dashboard
        tabbedPane.addTab("Dashboard", dashboardPanel);
        // whour
        tabbedPane.addTab("Çalışma Saatleri", createWhourPanel());
        // Randevular
        tabbedPane.addTab("Randevular", createAppointmentPanel());

        w_pane.add(tabbedPane);

        createDashboardPanel(); // Dashboard panelin içeriği
    }

    /**
     * Dashboard sekmesindeki içerikler
     */
    private void createDashboardPanel() {
        // İstatistik kartlarını tutan panel
        statsPanel.setBounds(15, 15, 925, 100);
        dashboardPanel.add(statsPanel);

        // 3 kart
        statsPanel.add(createStatsCard("Günlük Randevular", "0", new Color(147, 197, 253)));
        statsPanel.add(createStatsCard("Haftalık Randevular", "0", new Color(167, 243, 208)));
        statsPanel.add(createStatsCard("Aylık Randevular", "0", new Color(253, 164, 175)));

        // “Bugünün Randevuları”
        JPanel todayAppPanel = new JPanel();
        todayAppPanel.setBackground(Color.WHITE);
        todayAppPanel.setBounds(15, 130, 925, 300);
        todayAppPanel.setLayout(null);
        todayAppPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Bugünün Randevuları",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Inter", Font.BOLD, 14)
        ));
        dashboardPanel.add(todayAppPanel);

        styleTable(tableTodayApp);

        JScrollPane scrollToday = new JScrollPane(tableTodayApp);
        scrollToday.setBounds(15, 30, 895, 255);
        todayAppPanel.add(scrollToday);
    }

    /**
     * “Çalışma Saatleri” sekmesi paneli
     */
    private JPanel createWhourPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(null);

        // Üstte form
        JPanel formPanel = createWhourFormPanel();
        panel.add(formPanel);

        // Tablonun stili
        styleTable(table_whour);

        JScrollPane scrollPane = new JScrollPane(table_whour);
        scrollPane.setBounds(15, 90, 925, 330);
        panel.add(scrollPane);

        return panel;
    }

    /**
     * “Randevular” sekmesi paneli
     */
    private JPanel createAppointmentPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setLayout(null);

        styleTable(table_doctorAppoint);
        table_doctorAppoint.setComponentPopupMenu(d_appointMenu);

        JScrollPane scrollPane = new JScrollPane(table_doctorAppoint);
        scrollPane.setBounds(15, 15, 925, 405);
        panel.add(scrollPane);

        return panel;
    }

    /**
     * Randevular menüsü
     */
    private void setupAppointmentMenu() {
        JMenuItem deleteMenuItem = new JMenuItem("Randevuyu İptal Et");
        deleteMenuItem.setFont(new Font("Inter", Font.PLAIN, 14));
        deleteMenuItem.addActionListener(e -> cancelAppointment());
        d_appointMenu.add(deleteMenuItem);
    }

    /**
     * “Çalışma Saatleri” form paneli
     */
    private JPanel createWhourFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setBounds(15, 15, 925, 60);
        formPanel.setLayout(null);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(10, 15, 10, 15)
        ));

        // Tarih seçimi
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setBounds(0, 10, 200, 35);
        dateChooser.setFont(new Font("Inter", Font.PLAIN, 14));
        formPanel.add(dateChooser);

        // Saat seçimi
        String[] timeSlots = {
            "10:00", "10:30", "11:00", "11:30", "12:00", "12:30",
            "13:30", "14:00", "14:30", "15:00", "15:30"
        };
        JComboBox<String> timeCombo = new JComboBox<>(timeSlots);
        timeCombo.setBounds(220, 10, 100, 35);
        timeCombo.setFont(new Font("Inter", Font.PLAIN, 14));
        formPanel.add(timeCombo);

        // Ekle butonu
        JButton btnAdd = createActionButton("Ekle", PRIMARY_COLOR);
        btnAdd.setBounds(340, 10, 100, 35);
        btnAdd.addActionListener(e -> addWhour(dateChooser, timeCombo));
        formPanel.add(btnAdd);

        // Sil butonu
        JButton btnDelete = createActionButton("Sil", DANGER_COLOR);
        btnDelete.setBounds(810, 10, 100, 35);
        btnDelete.addActionListener(e -> deleteWhour());
        formPanel.add(btnDelete);

        return formPanel;
    }

    /**
     * Verileri yükle
     */
    private void loadData() {
        try {
            updateWhourModel();
            updateDAppointModel(doctor.getId());
            updateDashboardStats();
        } catch (SQLException e) {
            Helper.showMsg("Veri yüklenirken hata: " + e.getMessage());
        }
    }

    /**
     * Çalışma saati ekle
     */
    private void addWhour(JDateChooser dateChooser, JComboBox<String> timeCombo) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = "";
        try {
            date = sdf.format(dateChooser.getDate());
        } catch (Exception e) {
            Helper.showMsg("Geçersiz tarih formatı");
            return;
        }

        if (date.isEmpty()) {
            Helper.showMsg("Lütfen geçerli bir tarih seçin");
            return;
        }

        String time = " " + timeCombo.getSelectedItem().toString() + ":00";
        String selectDate = date + time;
        try {
            if (doctor.addWhour(doctor.getId(), doctor.getName(), selectDate)) {
                Helper.showMsg("success");
                updateWhourModel();
            } else {
                Helper.showMsg("Çalışma saati eklenirken bir hata oluştu");
            }
        } catch (SQLException e) {
            Helper.showMsg("Veritabanı hatası: " + e.getMessage());
        }
    }

    /**
     * Çalışma saati sil
     */
    private void deleteWhour() {
        int selRow = table_whour.getSelectedRow();
        if (selRow >= 0) {
            if (Helper.confirm("Seçili çalışma saatini silmek istediğinizden emin misiniz?")) {
                String selectRow = table_whour.getModel().getValueAt(selRow, 0).toString();
                int selID = Integer.parseInt(selectRow);
                try {
                    if (doctor.deleteWhour(selID)) {
                        Helper.showMsg("success");
                        updateWhourModel();
                    } else {
                        Helper.showMsg("Çalışma saati silinirken bir hata oluştu");
                    }
                } catch (SQLException e) {
                    Helper.showMsg("Veritabanı hatası: " + e.getMessage());
                }
            }
        } else {
            Helper.showMsg("Lütfen bir çalışma saati seçin");
        }
    }

    /**
     * Randevuyu iptal et (popup menü)
     */
    private void cancelAppointment() {
        int row = table_doctorAppoint.getSelectedRow();
        if (row >= 0) {
            if (Helper.confirm("Randevuyu iptal etmek istediğinizden emin misiniz?")) {
                try {
                    String selDate = table_doctorAppoint.getValueAt(row, 2).toString();
                    String selDoctorName = doctor.getName();
                    if (appoint.deleteAppoint(selDate, selDoctorName)) {
                        updateDAppointModel(doctor.getId());
                        updateDashboardStats();
                        Helper.showMsg("success");
                    } else {
                        Helper.showMsg("Randevu iptal edilirken bir hata oluştu");
                    }
                } catch (SQLException e) {
                    Helper.showMsg("Veritabanı hatası: " + e.getMessage());
                }
            }
        } else {
            Helper.showMsg("Lütfen bir randevu seçin");
        }
    }

    /**
     * Çalışma saatleri tablosu verilerini yenile
     */
    public void updateWhourModel() throws SQLException {
        ((DefaultTableModel) table_whour.getModel()).setRowCount(0);

        for (Whour whour : doctor.getWhourList(doctor.getId())) {
            whourData[0] = whour.getId();
            whourData[1] = whour.getWdate();
            whourModel.addRow(whourData);
        }
    }

    /**
     * Randevu tablosu verilerini yenile
     */
    public void updateDAppointModel(int doctor_id) throws SQLException {
        DefaultTableModel model = (DefaultTableModel) table_doctorAppoint.getModel();
        model.setRowCount(0);

        ArrayList<Appointment> appointments = appoint.getRandevuList(doctor_id);
        
        // Aktif ve iptal edilmiş randevuları ayır
        ArrayList<Object[]> activeAppointments = new ArrayList<>();
        ArrayList<Object[]> cancelledAppointments = new ArrayList<>();

        for (Appointment app : appointments) {
            Object[] row = new Object[]{
                app.getId(),
                app.getHastaName(),
                app.getAppDate(),
                app.getStatus(),
                app.getCancelReason()
            };

            if (app.getStatus() != null && app.getStatus().equals("İPTAL")) {
                cancelledAppointments.add(row);
            } else {
                activeAppointments.add(row);
            }
        }

        // Aktif randevuları tarihe göre sırala
        activeAppointments.sort((a, b) -> {
            String dateA = (String) a[2];
            String dateB = (String) b[2];
            return dateA.compareTo(dateB);
        });

        // Önce aktif randevuları ekle
        for (Object[] row : activeAppointments) {
            model.addRow(row);
        }

        // Sonra iptal edilmiş randevuları ekle
        for (Object[] row : cancelledAppointments) {
            model.addRow(row);
        }

        // Tablo hücrelerini özelleştir
        table_doctorAppoint.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                String status = (String) table.getValueAt(row, 3);
                
                if (status != null && status.equals("İPTAL")) {
                    c.setForeground(CANCELLED_COLOR);
                } else if (row == 0) { // En yakın randevu
                    c.setForeground(UPCOMING_COLOR);
                } else if (row == 1) { // Bir sonraki randevu
                    c.setForeground(NEXT_COLOR);
                } else {
                    c.setForeground(Color.BLACK);
                }

                return c;
            }
        });
    }

    /**
     * Dashboard istatistiklerini güncelle
     */
    private void updateDashboardStats() {
        try {
            ArrayList<Appointment> appointments = appoint.getRandevuList(doctor.getId());
            int todayCount = 0, weekCount = 0, monthCount = 0;

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date now = new Date();
            String today = dateFormat.format(now);

            // Tabloyu temizle
            todayAppModel.setRowCount(0);

            if (appointments != null && !appointments.isEmpty()) {
                for (Appointment app : appointments) {
                    String appDate = app.getAppDate();
                    if (appDate != null) {
                        String appDateOnly = appDate.split(" ")[0];
                        String appTimeOnly = appDate.contains(" ") ? appDate.split(" ")[1] : "";

                        // Bugün
                        if (appDateOnly.equals(today)) {
                            todayCount++;
                            Object[] rowData = new Object[4];
                            rowData[0] = appTimeOnly;
                            rowData[1] = app.getHastaName();
                            rowData[2] = "Bekliyor";
                            rowData[3] = "";
                            todayAppModel.addRow(rowData);
                        }

                        try {
                            Date appointmentDate = dateFormat.parse(appDateOnly);
                            Date weekAgo = new Date(now.getTime() - 7L * 24 * 60 * 60 * 1000);

                            if (appointmentDate.after(weekAgo) || appointmentDate.equals(weekAgo)) {
                                weekCount++;
                            }
                            if (appDateOnly.substring(0, 7).equals(today.substring(0, 7))) {
                                monthCount++;
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            updateStatsCardValue(0, String.valueOf(todayCount));
            updateStatsCardValue(1, String.valueOf(weekCount));
            updateStatsCardValue(2, String.valueOf(monthCount));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Belirli bir istatistik kartının değerini güncelle
     */
    private void updateStatsCardValue(int cardIndex, String value) {
        if (statsPanel != null && statsPanel.getComponentCount() > cardIndex) {
            JPanel card = (JPanel) statsPanel.getComponent(cardIndex);
            if (card != null && card.getComponentCount() > 1) {
                JLabel lblValue = (JLabel) card.getComponent(1);
                if (lblValue != null) {
                    lblValue.setText(value);
                }
            }
        }
    }

    /**
     * Dashboard üzerinde istatistik kartı oluştur
     */
    private JPanel createStatsCard(String title, String value, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 14));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Inter", Font.BOLD, 24));
        lblValue.setForeground(Color.WHITE);

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    /**
     * createActionButton(String, Color) - Renk parametresi alan buton
     */
    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Header butonu (ör. “Çıkış Yap”)
     */
    private JButton createHeaderButton(String text, int x) {
        JButton button = new JButton(text);
        button.setBounds(x, 15, 90, 30);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setBackground(Color.WHITE);
        button.setForeground(PRIMARY_COLOR);
        button.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Tabloların stil ayarları
     */
    private void styleTable(JTable table) {
        table.setRowHeight(35);
        table.setFont(new Font("Inter", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 14));
        table.getTableHeader().setBackground(TABLE_HEADER_COLOR);
        table.getTableHeader().setForeground(GRAY_TEXT_COLOR);
        table.setSelectionBackground(new Color(243, 244, 246));
        table.setSelectionForeground(PRIMARY_COLOR);
        table.setShowGrid(true);
        table.setGridColor(BORDER_COLOR);

        // Sütunları ortalamak için
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }
}
