package View;

import Model.Clinic;
import Model.Hasta;
import Model.Whour;
import dbhelper.Helper;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.*;

public class RandevuSisGUI extends JFrame {
    private JPanel w_pane;
    private JTable table_clinic;
    private DefaultTableModel clinicModel;
    private DefaultTableModel mainModel;
    private Object[] clinicData = null;
    private Clinic clinic = new Clinic();
    private Color primaryColor = new Color(6, 95, 70);
    private Color borderColor = new Color(209, 213, 219);
    private Color progressInactiveColor = new Color(229, 231, 235);
    private int tableWidth = 500;
    private int tableX;
    
    // Sayfalama için değişkenler
    private int rowsPerPage = 7;
    private int currentPage = 0;
    private JButton nextButton;
    private JButton prevButton;
    private JLabel pageLabel;
    private int totalPages;

    public RandevuSisGUI(Hasta hasta) throws SQLException {
        // Frame ayarları
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setTitle("Poliklinik Seçimi");
        setResizable(false);

        // Ana panel
        w_pane = new JPanel();
        w_pane.setBackground(Color.WHITE);
        w_pane.setLayout(null);
        setContentPane(w_pane);

        // Üst panel
        JPanel topPanel = new JPanel();
        topPanel.setBackground(primaryColor);
        topPanel.setBounds(0, 0, 900, 80);
        topPanel.setLayout(null);
        w_pane.add(topPanel);

        // Başlık
        JLabel headerTitle = new JLabel("Randevu Sistemi");
        headerTitle.setFont(new Font("Inter", Font.BOLD, 24));
        headerTitle.setForeground(Color.WHITE);
        headerTitle.setBounds(30, 20, 300, 40);
        topPanel.add(headerTitle);

        // Ana Sayfa butonu
        JButton btnHome = createHeaderButton("Ana Sayfa", 750, 25, 100, 35);
        btnHome.addActionListener(e -> {
            dispose();
            HomepageGUI homePage = new HomepageGUI(hasta);
            homePage.setVisible(true);
        });
        topPanel.add(btnHome);

        // Progress bar paneli
        int progressWidth = 740;
        int progressX = 90;
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(null);
        progressPanel.setBounds(progressX, 90, progressWidth, 50);
        progressPanel.setBackground(Color.WHITE);
        w_pane.add(progressPanel);

        // Progress steps
        createProgressStep(progressPanel, "1", "Poliklinik", 20, true);
        createConnectingLine(progressPanel, 20, false);
        createProgressStep(progressPanel, "2", "Doktor", 340, false);
        createConnectingLine(progressPanel, 360, false);
        createProgressStep(progressPanel, "3", "Randevu", 680, false);

        // Bilgi paneli
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(240, 253, 244));
        infoPanel.setBounds(30, 150, 840, 50);
        infoPanel.setLayout(null);
        infoPanel.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)));
        w_pane.add(infoPanel);

        // Bilgi metni
        JLabel infoText = new JLabel("Lütfen randevu almak istediğiniz polikliniği seçiniz.");
        infoText.setFont(new Font("Inter", Font.BOLD, 14));
        infoText.setForeground(primaryColor);
        infoText.setBounds(20, 15, 800, 20);
        infoPanel.add(infoText);

        // Başlık
        JLabel title = new JLabel("Poliklinik Seçimi", SwingConstants.CENTER);
        title.setFont(new Font("Inter", Font.BOLD, 20));
        title.setForeground(primaryColor);
        title.setBounds(30, 210, 800, 30);
        w_pane.add(title);

        // Ana model oluşturma
        mainModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        mainModel.setColumnIdentifiers(new Object[]{"ID", "Poliklinik Adı"});
        
        // Ana modeli doldur
        for (Clinic c : clinic.getList()) {
            clinicData = new Object[]{c.getId(), c.getName()};
            mainModel.addRow(clinicData);
        }

        // Görüntüleme modeli oluştur
        clinicModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        clinicModel.setColumnIdentifiers(new Object[]{"ID", "Poliklinik Adı"});
        table_clinic = new JTable(clinicModel);
        customizeTable(table_clinic);

        tableX = (900 - tableWidth) / 2;
        JScrollPane scrollPane = new JScrollPane(table_clinic);
        scrollPane.setBounds(tableX, 250, tableWidth, 250);
        customizeScrollPane(scrollPane);
        w_pane.add(scrollPane);

        // Sayfalama kontrolleri
        int totalRows = mainModel.getRowCount();
        totalPages = (int) Math.ceil((double) totalRows / rowsPerPage);

        // Sayfa numarası göstergesi
        pageLabel = new JLabel("Sayfa " + (currentPage + 1) + " / " + totalPages, SwingConstants.CENTER);
        pageLabel.setBounds(400, 510, 100, 30);
        pageLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        w_pane.add(pageLabel);

        // Önceki sayfa butonu
        prevButton = createStyledButton("←", 200, 510, 60, 30);
        prevButton.setEnabled(false);
        prevButton.addActionListener(e -> handlePrevPage());
        w_pane.add(prevButton);

        // Sonraki sayfa butonu
        nextButton = createStyledButton("→", 640, 510, 60, 30);
        nextButton.setEnabled(totalPages > 1);
        nextButton.addActionListener(e -> handleNextPage());
        w_pane.add(nextButton);

        // İleri butonu
        JButton btnNext = createStyledButton("İleri", 760, 510, 100, 35);
        btnNext.addActionListener(e -> {
            int row = table_clinic.getSelectedRow();
            if (row != -1) {
                int clinicID = Integer.parseInt(table_clinic.getValueAt(row, 0).toString());
                String clinicName = table_clinic.getValueAt(row, 1).toString();
                
                try {
                    dispose();
                    DoctorSelectionGUI doctorGUI = new DoctorSelectionGUI(hasta, clinicID, clinicName);
                    doctorGUI.setVisible(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                Helper.showMsg("Lütfen bir poliklinik seçiniz!");
            }
        });
        w_pane.add(btnNext);

      

        // İlk sayfa görünümünü ayarla
        updateTableRows(table_clinic, 0, rowsPerPage);
    }

    private void handlePrevPage() {
        if (currentPage > 0) {
            currentPage--;
            updateTableRows(table_clinic, currentPage * rowsPerPage, rowsPerPage);
            updatePageControls();
        }
    }

    private void handleNextPage() {
        if (currentPage < totalPages - 1) {
            currentPage++;
            updateTableRows(table_clinic, currentPage * rowsPerPage, rowsPerPage);
            updatePageControls();
        }
    }

    private void updatePageControls() {
        pageLabel.setText("Sayfa " + (currentPage + 1) + " / " + totalPages);
        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < totalPages - 1);
    }

    private void updateTableRows(JTable table, int start, int count) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Mevcut satırları temizle
        
        int end = Math.min(start + count, mainModel.getRowCount());
        for (int i = start; i < end; i++) {
            Object[] row = new Object[]{
                mainModel.getValueAt(i, 0),
                mainModel.getValueAt(i, 1)
            };
            model.addRow(row);
        }
    }

    private void createProgressStep(JPanel panel, String number, String text, int x, boolean active) {
        JPanel circle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(active ? primaryColor : progressInactiveColor);
                g2d.fillOval(0, 0, 30, 30);
            }
        };
        circle.setBounds(x, 0, 30, 30);
        circle.setOpaque(false);
        
        JLabel numberLabel = new JLabel(number);
        numberLabel.setFont(new Font("Inter", Font.BOLD, 14));
        numberLabel.setForeground(Color.WHITE);
        numberLabel.setBounds(0, 0, 30, 30);
        numberLabel.setHorizontalAlignment(JLabel.CENTER);
        circle.add(numberLabel);
        
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Inter", Font.BOLD, 14));
        textLabel.setForeground(active ? primaryColor : new Color(107, 114, 128));
        textLabel.setBounds(x - 35, 35, 100, 20);
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        
        panel.add(circle);
        panel.add(textLabel);
    }

    private void createConnectingLine(JPanel panel, int x, boolean completed) {
        JPanel lineContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(completed ? primaryColor : progressInactiveColor);
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);
            }
        };
        
        lineContainer.setBounds(x + 40, 13, 260, 4);
        lineContainer.setOpaque(false);
        panel.add(lineContainer);
    }

    private void customizeTable(JTable table) {
        table.setFont(new Font("Inter", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(240, 253, 244));
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(true);
        table.setGridColor(borderColor);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Inter", Font.BOLD, 14));
        header.setBackground(new Color(243, 244, 246));
        header.setForeground(primaryColor);
        header.setPreferredSize(new Dimension(0, 35));
        
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer())
            .setHorizontalAlignment(JLabel.LEFT);
    }

    private void customizeScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(borderColor));
        scrollPane.getViewport().setBackground(Color.WHITE);
    }

    private JButton createStyledButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setBounds(x, y, width, height);
        button.setBackground(primaryColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(4, 120, 87));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(primaryColor);
            }
        });

        return button;
    }

    private JButton createHeaderButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 12));
        button.setBounds(x, y, width, height);
        button.setBackground(Color.WHITE);
        button.setForeground(primaryColor);
        button.setBorder(BorderFactory.createLineBorder(primaryColor, 1));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(240, 253, 244));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });

        return button;
    }
}
class DoctorSelectionGUI extends JFrame {
    private JPanel w_pane;
    private JTable table_doctor;
    private DefaultTableModel doctorModel;
    private Object[] doctorData = null;
    private Clinic clinic = new Clinic();
    private Color primaryColor = new Color(6, 95, 70);
    private Color borderColor = new Color(209, 213, 219);
    private Color progressInactiveColor = new Color(229, 231, 235);
    private static int lastSelectedDoctorID = 0;
    public DoctorSelectionGUI(Hasta hasta, int clinicID, String clinicName) throws SQLException {
        // Frame ayarları
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setTitle("Doktor Seçimi");
        setResizable(false);

        // Ana panel
        w_pane = new JPanel();
        w_pane.setBackground(Color.WHITE);
        w_pane.setLayout(null);
        setContentPane(w_pane);

        // Üst panel
        JPanel topPanel = new JPanel();
        topPanel.setBackground(primaryColor);
        topPanel.setBounds(0, 0, 900, 80);
        topPanel.setLayout(null);
        w_pane.add(topPanel);

        // Başlık
        JLabel headerTitle = new JLabel("Randevu Sistemi");
        headerTitle.setFont(new Font("Inter", Font.BOLD, 24));
        headerTitle.setForeground(Color.WHITE);
        headerTitle.setBounds(30, 20, 300, 40);
        topPanel.add(headerTitle);

        // Ana Sayfa butonu
        JButton btnHome = createHeaderButton("Ana Sayfa", 750, 25, 100, 35);
        btnHome.addActionListener(e -> {
            dispose();
            HomepageGUI homePage = new HomepageGUI(hasta);
            homePage.setVisible(true);
        });
        topPanel.add(btnHome);

        // Progress bar paneli
        int progressWidth = 740;
        int progressX = 90;
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(null);
        progressPanel.setBounds(progressX, 90, progressWidth, 50);
        progressPanel.setBackground(Color.WHITE);
        w_pane.add(progressPanel);

        // Progress steps
        createProgressStep(progressPanel, "✓", "Poliklinik", 20, true);
        createConnectingLine(progressPanel, 20, true);
        createProgressStep(progressPanel, "2", "Doktor", 340, true);
        createConnectingLine(progressPanel, 360, false);
        createProgressStep(progressPanel, "3", "Randevu", 680, false);

        // Bilgi paneli
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(240, 253, 244));
        infoPanel.setBounds(30, 150, 840, 50);
        infoPanel.setLayout(null);
        infoPanel.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)));
        w_pane.add(infoPanel);

        // Bilgi metni
        JLabel infoText = new JLabel("Seçilen Poliklinik: " + clinicName);
        infoText.setFont(new Font("Inter", Font.BOLD, 14));
        infoText.setForeground(primaryColor);
        infoText.setBounds(20, 15, 800, 20);
        infoPanel.add(infoText);

        // Başlık
        JLabel title = new JLabel("Doktor Seçimi", SwingConstants.CENTER);
        title.setFont(new Font("Inter", Font.BOLD, 20));
        title.setForeground(primaryColor);
        title.setBounds(30, 210, 800, 30);
        w_pane.add(title);

        // Doktor tablosu modelini oluştur
        doctorModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        doctorModel.setColumnIdentifiers(new Object[]{"ID", "Ad Soyad"});
        table_doctor = new JTable(doctorModel);
        customizeTable(table_doctor);

        // Seçilen kliniğe ait doktorları listele
        try {
            for (var doctor : clinic.getClinicDoctorList(clinicID)) {
                doctorData = new Object[]{doctor.getId(), doctor.getName()};
                doctorModel.addRow(doctorData);
                
                if (doctor.getId() == lastSelectedDoctorID) {
                    int rowIndex = doctorModel.getRowCount() - 1;
                    if (rowIndex >= 0) {
                        table_doctor.setRowSelectionInterval(rowIndex, rowIndex);
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // Tablo paneli
        int tableWidth = 500;
        int tableX = (900 - tableWidth) / 2;
        JScrollPane scrollPane = new JScrollPane(table_doctor);
        scrollPane.setBounds(tableX, 250, tableWidth, 250);
        customizeScrollPane(scrollPane);
        w_pane.add(scrollPane);

        // Geri butonu
        JButton btnBack = createStyledButton("Geri", 30, 510, 100, 35);
        btnBack.addActionListener(e -> {
            dispose();
            try {
                RandevuSisGUI randevuGUI = new RandevuSisGUI(hasta);
                randevuGUI.setVisible(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        w_pane.add(btnBack);

        // İleri butonu
        JButton btnNext = createStyledButton("İleri", 760, 510, 100, 35);
        btnNext.addActionListener(e -> {
            int row = table_doctor.getSelectedRow();
            if (row != -1) {
                lastSelectedDoctorID = Integer.parseInt(table_doctor.getValueAt(row, 0).toString());
                String selectDoctorName = table_doctor.getValueAt(row, 1).toString();
                
                try {
                    dispose();
                    WhourSelectionGUI whourGUI = new WhourSelectionGUI(hasta, lastSelectedDoctorID, selectDoctorName);
                    whourGUI.setVisible(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            } else {
                Helper.showMsg("Lütfen bir doktor seçiniz!");
            }
        });
        w_pane.add(btnNext);
    }

    // Seçili doktoru sıfırlamak için static metod
    public static void resetSelection() {
        lastSelectedDoctorID = 0;
    }

    private void createProgressStep(JPanel panel, String number, String text, int x, boolean active) {
        // Circle background
        JPanel circle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(active ? primaryColor : progressInactiveColor);
                g2d.fillOval(0, 0, 30, 30);
            }
        };
        circle.setBounds(x, 0, 30, 30);
        circle.setOpaque(false);
        
        // Number
        JLabel numberLabel = new JLabel(number);
        numberLabel.setFont(new Font("Inter", Font.BOLD, 14));
        numberLabel.setForeground(Color.WHITE);
        numberLabel.setBounds(0, 0, 30, 30);
        numberLabel.setHorizontalAlignment(JLabel.CENTER);
        circle.add(numberLabel);
        
        // Text
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Inter", Font.BOLD, 14));
        textLabel.setForeground(active ? primaryColor : new Color(107, 114, 128));
        textLabel.setBounds(x - 35, 35, 100, 20);
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        
        panel.add(circle);
        panel.add(textLabel);
    }

    private void createConnectingLine(JPanel panel, int x, boolean completed) {
        JPanel lineContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(completed ? primaryColor : progressInactiveColor);
                g2d.setStroke(new BasicStroke(2.0f));
                g2d.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);
            }
        };
        
        lineContainer.setBounds(x + 40, 13, 260, 4);
        lineContainer.setOpaque(false);
        panel.add(lineContainer);
    }

    private void customizeTable(JTable table) {
        table.setFont(new Font("Inter", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(240, 253, 244));
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(true);
        table.setGridColor(borderColor);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Inter", Font.BOLD, 14));
        header.setBackground(new Color(243, 244, 246));
        header.setForeground(primaryColor);
        header.setPreferredSize(new Dimension(0, 35));
        
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer())
            .setHorizontalAlignment(JLabel.LEFT);
    }

    private void customizeScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(borderColor));
        scrollPane.getViewport().setBackground(Color.WHITE);
    }

    private JButton createStyledButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setBounds(x, y, width, height);
        button.setBackground(primaryColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(4, 120, 87));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(primaryColor);
            }
        });

        return button;
    }

    private JButton createHeaderButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 12));
        button.setBounds(x, y, width, height);
        button.setBackground(Color.WHITE);
        button.setForeground(primaryColor);
        button.setBorder(BorderFactory.createLineBorder(primaryColor, 1));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(240, 253, 244));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });

        return button;
    }
}
class WhourSelectionGUI extends JFrame {
    private JPanel w_pane;
    private JTable table_whour;
    private DefaultTableModel whourModel;
    private Object[] whourData = null;
    private Whour whour = new Whour();
    private Color primaryColor = new Color(6, 95, 70);
    private Color borderColor = new Color(209, 213, 219);
    private Color progressInactiveColor = new Color(229, 231, 235);

    public WhourSelectionGUI(Hasta hasta, int doctorID, String doctorName) throws SQLException {
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setTitle("Randevu Saati Seçimi");
        setResizable(false);

        w_pane = new JPanel();
        w_pane.setBackground(Color.WHITE);
        w_pane.setLayout(null);
        setContentPane(w_pane);

        // Üst panel
        JPanel topPanel = new JPanel();
        topPanel.setBackground(primaryColor);
        topPanel.setBounds(0, 0, 900, 80);
        topPanel.setLayout(null);
        w_pane.add(topPanel);

        // Başlık
        JLabel headerTitle = new JLabel("Randevu Sistemi");
        headerTitle.setFont(new Font("Inter", Font.BOLD, 24));
        headerTitle.setForeground(Color.WHITE);
        headerTitle.setBounds(30, 20, 300, 40);
        topPanel.add(headerTitle);

        // Ana Sayfa butonu
        JButton btnHome = createHeaderButton("Ana Sayfa", 750, 25, 100, 35);
        btnHome.addActionListener(e -> {
            dispose();
            HomepageGUI homePage = new HomepageGUI(hasta);
            homePage.setVisible(true);
        });
        topPanel.add(btnHome);

        // Progress bar paneli
        int progressWidth = 740;
        int progressX = 90;
        JPanel progressPanel = new JPanel();
        progressPanel.setLayout(null);
        progressPanel.setBounds(progressX, 90, progressWidth, 50);
        progressPanel.setBackground(Color.WHITE);
        w_pane.add(progressPanel);

        // Progress steps
        createProgressStep(progressPanel, "✓", "Poliklinik", 20, true);
        createConnectingLine(progressPanel, 20, true);
        createProgressStep(progressPanel, "✓", "Doktor", 340, true);
        createConnectingLine(progressPanel, 360, true);
        createProgressStep(progressPanel, "3", "Randevu", 680, true);

        // Bilgi paneli
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(new Color(240, 253, 244));
        infoPanel.setBounds(30, 150, 840, 50);
        infoPanel.setLayout(null);
        infoPanel.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)));
        w_pane.add(infoPanel);

        // Bilgi metni
        JLabel infoText = new JLabel("Seçilen Doktor: " + doctorName);
        infoText.setFont(new Font("Inter", Font.BOLD, 14));
        infoText.setForeground(primaryColor);
        infoText.setBounds(20, 15, 800, 20);
        infoPanel.add(infoText);

        // Başlık
        JLabel title = new JLabel("Randevu Saati Seçimi", SwingConstants.CENTER);
        title.setFont(new Font("Inter", Font.BOLD, 20));
        title.setForeground(primaryColor);
        title.setBounds(30, 210, 800, 30);
        w_pane.add(title);

        // Randevu saatleri tablosu
        whourModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        whourModel.setColumnIdentifiers(new Object[]{"ID", "Tarih"});
        table_whour = new JTable(whourModel);
        customizeTable(table_whour);

        // Tabloyu doldur
        for (var w : whour.getWhourList(doctorID)) {
            whourData = new Object[]{w.getId(), w.getWdate()};
            whourModel.addRow(whourData);
        }

        // Tablo paneli
        int tableWidth = 500;
        int tableX = (900 - tableWidth) / 2;
        JScrollPane scrollPane = new JScrollPane(table_whour);
        scrollPane.setBounds(tableX, 250, tableWidth, 250);
        customizeScrollPane(scrollPane);
        w_pane.add(scrollPane);

        // Geri butonu
        JButton btnBack = createStyledButton("Geri", 30, 510, 100, 35);
        btnBack.addActionListener(e -> {
            dispose();
            try {
                RandevuSisGUI randevuGUI = new RandevuSisGUI(hasta);
                randevuGUI.setVisible(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        w_pane.add(btnBack);

        // Randevu al butonu
        JButton btnAppointment = createStyledButton("Randevu Al", 760, 510, 90, 35);
        btnAppointment.addActionListener(e -> {
            int row = table_whour.getSelectedRow();
            if (row != -1) {
                String date = table_whour.getValueAt(row, 1).toString();
                try {
                    if (hasta.addAppointment(doctorID, hasta.getId(), doctorName, hasta.getName(), date)) {
                        Helper.showMsg("success");
                        hasta.updateWhourStatus(doctorID, date);
                        dispose();
                        HomepageGUI homePage = new HomepageGUI(hasta);
                        homePage.setVisible(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                Helper.showMsg("Lütfen bir randevu saati seçiniz!");
            }
        });
        w_pane.add(btnAppointment);
    }

    private JButton createHeaderButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 12));
        button.setBounds(x, y, width, height);
        button.setBackground(Color.WHITE);
        button.setForeground(primaryColor);
        button.setBorder(BorderFactory.createLineBorder(primaryColor, 1));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(240, 253, 244));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });

        return button;
    }

    private void createProgressStep(JPanel panel, String number, String text, int x, boolean active) {
        // Circle background
        JPanel circle = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(active ? primaryColor : progressInactiveColor);
                g2d.fillOval(0, 0, 30, 30);
            }
        };
        circle.setBounds(x, 0, 30, 30);
        circle.setOpaque(false);
        
        // Number
        JLabel numberLabel = new JLabel(number);
        numberLabel.setFont(new Font("Inter", Font.BOLD, 14));
        numberLabel.setForeground(Color.WHITE);
        numberLabel.setBounds(0, 0, 30, 30);
        numberLabel.setHorizontalAlignment(JLabel.CENTER);
        circle.add(numberLabel);
        
        // Text
        JLabel textLabel = new JLabel(text);
        textLabel.setFont(new Font("Inter", Font.BOLD, 14));
        textLabel.setForeground(active ? primaryColor : new Color(107, 114, 128));
        textLabel.setBounds(x - 35, 35, 100, 20);
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        
        panel.add(circle);
        panel.add(textLabel);
    }

    private void createConnectingLine(JPanel panel, int x, boolean completed) {
        // Çizgi paneli
        JPanel lineContainer = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Çizgi rengi: tamamlanmış ise yeşil, değilse gri
                g2d.setColor(completed ? primaryColor : progressInactiveColor);
                // Çizgi kalınlığı
                g2d.setStroke(new BasicStroke(2.0f));
                // Çizgi çizimi - panel yüksekliğinin ortasına
                g2d.drawLine(0, getHeight()/2, getWidth(), getHeight()/2);
            }
        };
        
        // Çizgi container pozisyonu ve boyutu
        lineContainer.setBounds(x + 40, 13, 260, 4);
        lineContainer.setOpaque(false);
        panel.add(lineContainer);
    }
    private void customizeTable(JTable table) {
        table.setFont(new Font("Inter", Font.PLAIN, 14));
        table.setRowHeight(30);
        table.setSelectionBackground(new Color(240, 253, 244));
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(true);
        table.setGridColor(borderColor);
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Inter", Font.BOLD, 14));
        header.setBackground(new Color(243, 244, 246));
        header.setForeground(primaryColor);
        header.setPreferredSize(new Dimension(0, 35));
        
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer())
            .setHorizontalAlignment(JLabel.LEFT);
    }

    private void customizeScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(borderColor));
        scrollPane.getViewport().setBackground(Color.WHITE);
    }

    private JButton createStyledButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setBounds(x, y, width, height);
        button.setBackground(primaryColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(4, 120, 87));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(primaryColor);
            }
        });

        return button;
    }
}