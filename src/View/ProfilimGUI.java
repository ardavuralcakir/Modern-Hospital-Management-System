package View;

import Model.Hasta;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class ProfilimGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private Hasta hasta;
    
    // Renkler
    private static final Color PRIMARY_COLOR = new Color(6, 95, 70);      // Koyu yeşil
    private static final Color SECONDARY_COLOR = new Color(240, 253, 244); // Açık yeşil
    private static final Color GRAY_TEXT_COLOR = new Color(75, 85, 99);    // Gri metin
    private static final Color BORDER_COLOR = new Color(209, 213, 219);   // Çerçeve rengi
    private static final Color HOVER_COLOR = new Color(4, 120, 87);       // Hover rengi
    
    private RoundedPhotoLabel profileImageLabel;
    private JPanel rightPanel;
    private JLabel rightPanelTitle;

    private List<JButton> menuButtons;

    // Yuvarlak profil fotoğrafı için özel JLabel sınıfı
    private class RoundedPhotoLabel extends JLabel {
        private static final long serialVersionUID = 1L;
        private Shape shape;
        private Image image;

        public RoundedPhotoLabel() {
            setPreferredSize(new Dimension(180, 180));
            setMinimumSize(new Dimension(180, 180));
            setMaximumSize(new Dimension(180, 180));
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    JFileChooser fileChooser = new JFileChooser();
                    FileNameExtensionFilter imageFilter = new FileNameExtensionFilter(
                        "Resim Dosyaları (*.jpg, *.jpeg, *.png, *.gif)",
                        "jpg", "jpeg", "png", "gif"
                    );
                    fileChooser.setFileFilter(imageFilter);
                    
                    if (fileChooser.showOpenDialog(ProfilimGUI.this) == JFileChooser.APPROVE_OPTION) {
                        saveProfilePhoto(fileChooser.getSelectedFile());
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (shape == null || !shape.getBounds().equals(getBounds())) {
                shape = new Ellipse2D.Float(0, 0, getWidth() - 1, getHeight() - 1);
            }

            g2.setColor(Color.WHITE);
            g2.fill(shape);

            if (image != null) {
                g2.setClip(shape);
                g2.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }

            g2.setClip(null);
            g2.setColor(PRIMARY_COLOR);
            g2.draw(shape);
            g2.dispose();
        }

        public void setImage(Image image) {
            this.image = image;
            repaint();
        }
    }

    public ProfilimGUI(Hasta hasta) {
        this.hasta = hasta;
        
        setTitle("Profilim");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        // Ana panel
        contentPane = new JPanel();
        contentPane.setLayout(null);
        contentPane.setBackground(SECONDARY_COLOR);
        setContentPane(contentPane);

        // Üst panel
        createHeaderPanel();
        
        // Ana içerik paneli
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBounds(50, 100, 900, 530);
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        mainPanel.setOpaque(true);
        contentPane.add(mainPanel);

        // Sol panel (profil fotoğrafı ve menü)
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(null);
        leftPanel.setBounds(20, 20, 400, 490);
        leftPanel.setBackground(Color.WHITE);
        leftPanel.setOpaque(true);
        mainPanel.add(leftPanel);

        // Profil fotoğrafı
        createProfilePhotoSection(leftPanel);
        
        // Kişisel bilgiler ve menü
        createPersonalInfoSection(leftPanel);

        // Varsayılan profil fotoğrafını yükle
        setDefaultProfileImage();

        // İçeriği hemen göster
        revalidate();
        repaint();
    }

    private void createHeaderPanel() {
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBounds(0, 0, 1000, 70);
        headerPanel.setLayout(null);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        contentPane.add(headerPanel);

        JLabel lblTitle = new JLabel("Profil Bilgileri");
        lblTitle.setFont(new Font("Inter", Font.BOLD, 24));
        lblTitle.setForeground(PRIMARY_COLOR);
        lblTitle.setBounds(50, 15, 400, 40);
        headerPanel.add(lblTitle);

        JButton btnBack = createStyledButton("← Geri", 880, 20, 80, 30);
        btnBack.addActionListener(e -> {
            HomepageGUI homePage = new HomepageGUI(hasta);
            homePage.setVisible(true);
            dispose();
        });
        headerPanel.add(btnBack);
    }

    private void createProfilePhotoSection(JPanel panel) {
        JPanel photoPanel = new JPanel();
        photoPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
        photoPanel.setBounds(110, 20, 180, 200);
        photoPanel.setBackground(Color.WHITE);

        profileImageLabel = new RoundedPhotoLabel();
        setDefaultProfileImage(); // Profil fotoğrafını yükle

        JLabel lblClickToChange = new JLabel("Değiştirmek için tıklayın");
        lblClickToChange.setFont(new Font("Inter", Font.PLAIN, 12));
        lblClickToChange.setForeground(GRAY_TEXT_COLOR);
        lblClickToChange.setHorizontalAlignment(SwingConstants.CENTER);
        lblClickToChange.setPreferredSize(new Dimension(180, 20));

        photoPanel.add(profileImageLabel);
        photoPanel.add(lblClickToChange);
        panel.add(photoPanel);
    }

    private void createPersonalInfoSection(JPanel panel) {
        // Profil menü butonları
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new GridLayout(5, 1, 0, 10));
        menuPanel.setBounds(20, 230, 360, 200);
        menuPanel.setBackground(Color.WHITE);
        menuPanel.setOpaque(true);

        // Sağ panel başlık ve içerik
        rightPanel = new JPanel();
        rightPanel.setLayout(null);
        rightPanel.setBounds(440, 20, 440, 490);
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        rightPanel.setOpaque(true);
        panel.getParent().add(rightPanel);

        rightPanelTitle = new JLabel("Profil Bilgileri");
        rightPanelTitle.setFont(new Font("Inter", Font.BOLD, 20));
        rightPanelTitle.setForeground(PRIMARY_COLOR);
        rightPanelTitle.setBounds(20, 20, 440, 40);
        rightPanel.add(rightPanelTitle);

        // Menü butonları listesi
        menuButtons = new ArrayList<>();

        // Profil Bilgileri Butonu
        JButton btnProfile = createMenuButton("👤  Profil Bilgileri", true);
        btnProfile.addActionListener(e -> {
            updateMenuButtons(btnProfile);
            rightPanelTitle.setText("Profil Bilgileri");
            showProfileInfo();
        });
        menuButtons.add(btnProfile);
        menuPanel.add(btnProfile);

        // Randevu Geçmişi Butonu
        JButton btnHistory = createMenuButton("📅  Randevu Geçmişi", false);
        btnHistory.addActionListener(e -> {
            updateMenuButtons(btnHistory);
            rightPanelTitle.setText("Randevu Geçmişi");
            showAppointmentHistory();
        });
        menuButtons.add(btnHistory);
        menuPanel.add(btnHistory);

        // Bildirim Ayarları Butonu
        JButton btnNotifications = createMenuButton("🔔  Bildirim Ayarları", false);
        btnNotifications.addActionListener(e -> {
            updateMenuButtons(btnNotifications);
            rightPanelTitle.setText("Bildirim Ayarları");
            showNotificationSettings();
        });
        menuButtons.add(btnNotifications);
        menuPanel.add(btnNotifications);

        // Yardım ve Destek Butonu
        JButton btnHelp = createMenuButton("❓  Yardım ve Destek", false);
        btnHelp.addActionListener(e -> {
            updateMenuButtons(btnHelp);
            rightPanelTitle.setText("Yardım ve Destek");
            showHelpAndSupport();
        });
        menuButtons.add(btnHelp);
        menuPanel.add(btnHelp);

        // Şifre Değiştir Butonu
        JButton btnChangePass = createMenuButton("🔒  Şifre Değiştir", false);
        btnChangePass.addActionListener(e -> {
            updateMenuButtons(btnChangePass);
            rightPanelTitle.setText("Şifre Değiştir");
            showPasswordChangeDialog();
        });
        menuButtons.add(btnChangePass);
        menuPanel.add(btnChangePass);

        panel.add(menuPanel);

        // Varsayılan olarak profil bilgilerini göster
        showProfileInfo();
    }

    private void updateMenuButtons(JButton activeButton) {
        for (JButton button : menuButtons) {
            if (button == activeButton) {
                button.setBackground(PRIMARY_COLOR);
                button.setForeground(Color.WHITE);
                button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            } else {
                button.setBackground(Color.WHITE);
                button.setForeground(GRAY_TEXT_COLOR);
                button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
            }
        }
    }

    private void showProfileInfo() {
        rightPanel.removeAll();

        // Başlık
        rightPanelTitle = new JLabel("Profil Bilgileri", SwingConstants.CENTER);
        rightPanelTitle.setFont(new Font("Inter", Font.BOLD, 24));
        rightPanelTitle.setForeground(PRIMARY_COLOR);
        rightPanelTitle.setBounds(0, 10, 440, 40);
        rightPanel.add(rightPanelTitle);

        // Bilgi paneli
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridLayout(5, 1, 0, 25)); // Dikey boşluğu artırdım
        infoPanel.setBounds(30, 60, 380, 400);
        infoPanel.setBackground(Color.WHITE);
        
        // Her bir bilgi bloğu için özel panel
        addStyledInfoField(infoPanel, "TC Kimlik No", hasta.getTcno());
        addStyledInfoField(infoPanel, "Ad Soyad", hasta.getName());
        addStyledInfoField(infoPanel, "Telefon", hasta.getPhone());
        addStyledInfoField(infoPanel, "E-posta", hasta.getEmail());
        
        // Son randevu bilgisi - özel tasarım
        JPanel lastAppointmentPanel = new JPanel();
        lastAppointmentPanel.setLayout(null);
        lastAppointmentPanel.setBackground(new Color(243, 244, 246)); // Açık gri arka plan
        lastAppointmentPanel.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1, true));
        
        JLabel lblLastAppointment = new JLabel("Son Randevu");
        lblLastAppointment.setFont(new Font("Inter", Font.BOLD, 13));
        lblLastAppointment.setForeground(GRAY_TEXT_COLOR);
        lblLastAppointment.setBounds(15, 10, 350, 20);
        
        JLabel lblLastAppointmentValue = new JLabel("");
        lblLastAppointmentValue.setFont(new Font("Inter", Font.BOLD, 14));
        lblLastAppointmentValue.setForeground(Color.BLACK);
        lblLastAppointmentValue.setBounds(15, 30, 350, 25);
        
        lastAppointmentPanel.add(lblLastAppointment);
        lastAppointmentPanel.add(lblLastAppointmentValue);
        lastAppointmentPanel.setPreferredSize(new Dimension(380, 65));
        
        infoPanel.add(lastAppointmentPanel);
        rightPanel.add(infoPanel);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void addStyledInfoField(JPanel panel, String label, String value) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(null);
        fieldPanel.setBackground(Color.WHITE);
        fieldPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235))); // Alt çizgi
        
        JLabel lblField = new JLabel(label);
        lblField.setFont(new Font("Inter", Font.BOLD, 13));
        lblField.setForeground(GRAY_TEXT_COLOR);
        lblField.setBounds(0, 0, 380, 20);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Inter", Font.BOLD, 15));
        lblValue.setForeground(new Color(17, 24, 39)); // Koyu gri
        lblValue.setBounds(0, 25, 380, 25);
        
        fieldPanel.add(lblField);
        fieldPanel.add(lblValue);
        panel.add(fieldPanel);
    }

    private void showAppointmentHistory() {
        rightPanel.removeAll();

        // Başlık
        rightPanelTitle = new JLabel("Randevu Geçmişi", SwingConstants.CENTER);
        rightPanelTitle.setFont(new Font("Inter", Font.BOLD, 24));
        rightPanelTitle.setForeground(PRIMARY_COLOR);
        rightPanelTitle.setBounds(0, 10, 440, 40);
        rightPanel.add(rightPanelTitle);

        // Tablo paneli
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(null);
        tablePanel.setBounds(30, 60, 380, 400);
        tablePanel.setBackground(Color.WHITE);

        // Randevu geçmişi tablosu
        String[] columnNames = {"Tarih", "Doktor", "Klinik", "Durum"};
        Object[][] data = {
            {"15.01.2024 14:30", "Dr. Ahmet Yılmaz", "Dahiliye", "Bekliyor"},
            {"10.01.2024 10:00", "Dr. Ayşe Demir", "Göz", "İptal Edildi"},
            {"05.01.2024 15:45", "Dr. Mehmet Kaya", "KBB", "Tamamlandı"},
            {"01.01.2024 09:15", "Dr. Ali Yıldız", "Dahiliye", "Tamamlandı"},
            {"28.12.2023 11:30", "Dr. Zeynep Kaya", "Göz", "İptal Edildi"},
            {"25.12.2023 13:45", "Dr. Ahmet Yılmaz", "Dahiliye", "Tamamlandı"},
            {"20.12.2023 10:15", "Dr. Ayşe Demir", "Göz", "Tamamlandı"},
            {"15.12.2023 16:00", "Dr. Mehmet Kaya", "KBB", "İptal Edildi"},
            {"10.12.2023 14:30", "Dr. Ali Yıldız", "Dahiliye", "Tamamlandı"},
            {"05.12.2023 11:00", "Dr. Zeynep Kaya", "Göz", "Tamamlandı"},
            {"01.12.2023 09:30", "Dr. Ahmet Yılmaz", "Dahiliye", "Tamamlandı"},
            {"28.11.2023 15:45", "Dr. Ayşe Demir", "Göz", "İptal Edildi"}
        };

        // Sayfalama için değişkenler
        final int ROWS_PER_PAGE = 9;
        final int TOTAL_PAGES = (int) Math.ceil((double) data.length / ROWS_PER_PAGE);
        final int[] currentPage = {0};

        // Sıralama durumu için değişken
        final int[] sortState = {0}; // 0: default, 1: bekliyor, 2: tamamlandı, 3: iptal edildi

        // Tablo oluştur
        final JTable table = new JTable(getCurrentPageData(data, currentPage[0], ROWS_PER_PAGE), columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table.setFont(new Font("Inter", Font.PLAIN, 13));
        table.setRowHeight(35);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setTableHeader(new JTableHeader(table.getColumnModel()) {
            @Override
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.height = 35;
                return d;
            }
        });
        
        // Tablo başlığı tasarımı
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        table.getTableHeader().setBackground(new Color(243, 244, 246));
        table.getTableHeader().setForeground(GRAY_TEXT_COLOR);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR));
        ((DefaultTableCellRenderer)table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);

        // Durum sütununa göre renklendirme
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                
                if (column == 3) {
                    switch ((String) value) {
                        case "Tamamlandı":
                            c.setForeground(new Color(34, 197, 94));
                            break;
                        case "İptal Edildi":
                            c.setForeground(new Color(239, 68, 68));
                            break;
                        case "Bekliyor":
                            c.setForeground(new Color(234, 179, 8));
                            break;
                    }
                } else {
                    c.setForeground(new Color(17, 24, 39));
                }
                
                ((JLabel) c).setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
                return c;
            }
        };
        table.setDefaultRenderer(Object.class, renderer);

        // Tablo başlığına tıklama olayı
        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int columnIndex = table.columnAtPoint(e.getPoint());
                if (columnIndex == 3) { // Durum sütunu
                    sortState[0] = (sortState[0] + 1) % 4; // 4 durumlu sıralama (0,1,2,3)
                    
                    // Sıralama fonksiyonu
                    java.util.Arrays.sort(data, (a, b) -> {
                        String statusA = (String) a[3];
                        String statusB = (String) b[3];
                        
                        switch (sortState[0]) {
                            case 1: // Bekleyen -> Tamamlanan -> İptal edilen
                                if (statusA.equals("Bekliyor")) return -1;
                                if (statusB.equals("Bekliyor")) return 1;
                                if (statusA.equals("Tamamlandı") && statusB.equals("İptal Edildi")) return -1;
                                if (statusA.equals("İptal Edildi") && statusB.equals("Tamamlandı")) return 1;
                                break;

                            case 2: // Tamamlanan -> İptal edilen -> Bekleyen
                                if (statusA.equals("Tamamlandı")) return -1;
                                if (statusB.equals("Tamamlandı")) return 1;
                                if (statusA.equals("İptal Edildi") && statusB.equals("Bekliyor")) return -1;
                                if (statusA.equals("Bekliyor") && statusB.equals("İptal Edildi")) return 1;
                                break;

                            case 3: // İptal edilen -> Bekleyen -> Tamamlanan
                                if (statusA.equals("İptal Edildi")) return -1;
                                if (statusB.equals("İptal Edildi")) return 1;
                                if (statusA.equals("Bekliyor") && statusB.equals("Tamamlandı")) return -1;
                                if (statusA.equals("Tamamlandı") && statusB.equals("Bekliyor")) return 1;
                                break;

                            default: // Tarihe göre (bekleyenler her zaman üstte)
                                if (statusA.equals("Bekliyor")) return -1;
                                if (statusB.equals("Bekliyor")) return 1;
                                return ((String) b[0]).compareTo((String) a[0]);
                        }
                        
                        // Aynı durumdaki randevuları tarihe göre sırala
                        return ((String) b[0]).compareTo((String) a[0]);
                    });
                    
                    // Tabloyu güncelle
                    table.setModel(new DefaultTableModel(
                        getCurrentPageData(data, currentPage[0], ROWS_PER_PAGE),
                        columnNames
                    ));

                    // Durum sütunu renklerini tekrar ayarla
                    table.getColumnModel().getColumn(3).setCellRenderer(renderer);
                }
            }
        });

        // Tablo başlığının tıklanabilir olduğunu göstermek için cursor değiştir
        table.getTableHeader().addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int columnIndex = table.columnAtPoint(e.getPoint());
                if (columnIndex == 3) { // Durum sütunu
                    table.getTableHeader().setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    table.getTableHeader().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });
        
        // Tablo paneli
        JPanel mainTablePanel = new JPanel(null);
        mainTablePanel.setBounds(0, 0, 380, 350);
        mainTablePanel.setBackground(Color.WHITE);
        mainTablePanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));

        // Tablo başlığını ekle
        table.getTableHeader().setBounds(0, 0, 380, 35);
        mainTablePanel.add(table.getTableHeader());

        // Tabloyu ekle
        table.setBounds(0, 35, 380, 315);
        mainTablePanel.add(table);

        // Sayfalama paneli
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        paginationPanel.setBounds(0, 360, 380, 30);
        paginationPanel.setBackground(Color.WHITE);

        // Önceki sayfa butonu
        JButton btnPrev = new JButton("←");
        btnPrev.setEnabled(false);
        stylePageButton(btnPrev);

        // Sayfa numaraları
        JLabel lblPageInfo = new JLabel("Sayfa " + (currentPage[0] + 1) + "/" + TOTAL_PAGES);
        lblPageInfo.setFont(new Font("Inter", Font.BOLD, 14));
        lblPageInfo.setForeground(GRAY_TEXT_COLOR);

        // Sonraki sayfa butonu
        JButton btnNext = new JButton("→");
        btnNext.setEnabled(TOTAL_PAGES > 1);
        stylePageButton(btnNext);

        // Sayfalama butonlarının işlevleri
        btnPrev.addActionListener(e -> {
            if (currentPage[0] > 0) {
                currentPage[0]--;
                table.setModel(new DefaultTableModel(
                    getCurrentPageData(data, currentPage[0], ROWS_PER_PAGE), 
                    columnNames
                ));
                updatePagination(table, getCurrentPageData(data, currentPage[0], ROWS_PER_PAGE), 
                               currentPage[0], TOTAL_PAGES, btnPrev, btnNext, lblPageInfo);
            }
        });

        btnNext.addActionListener(e -> {
            if (currentPage[0] < TOTAL_PAGES - 1) {
                currentPage[0]++;
                table.setModel(new DefaultTableModel(
                    getCurrentPageData(data, currentPage[0], ROWS_PER_PAGE), 
                    columnNames
                ));
                updatePagination(table, getCurrentPageData(data, currentPage[0], ROWS_PER_PAGE), 
                               currentPage[0], TOTAL_PAGES, btnPrev, btnNext, lblPageInfo);
            }
        });

        paginationPanel.add(btnPrev);
        paginationPanel.add(lblPageInfo);
        paginationPanel.add(btnNext);

        tablePanel.add(mainTablePanel);
        tablePanel.add(paginationPanel);
        rightPanel.add(tablePanel);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void stylePageButton(JButton button) {
        button.setFont(new Font("Inter", Font.BOLD, 16));
        button.setForeground(PRIMARY_COLOR);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true));
        button.setPreferredSize(new Dimension(40, 30));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(PRIMARY_COLOR);
                    button.setForeground(Color.WHITE);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) {
                    button.setBackground(Color.WHITE);
                    button.setForeground(PRIMARY_COLOR);
                }
            }
        });
    }

    private void updatePagination(JTable table, Object[][] newData, int currentPage, int totalPages,
                                JButton btnPrev, JButton btnNext, JLabel lblPageInfo) {
        // Tablo verilerini güncelle
        table.setModel(new DefaultTableModel(newData, new String[]{"Tarih", "Doktor", "Klinik", "Durum"}));
        
        // Sayfa bilgisini güncelle
        lblPageInfo.setText("Sayfa " + (currentPage + 1) + "/" + totalPages);
        
        // Butonların durumunu güncelle
        btnPrev.setEnabled(currentPage > 0);
        btnNext.setEnabled(currentPage < totalPages - 1);
    }

    private void showNotificationSettings() {
        rightPanel.removeAll();

        // Başlık
        rightPanelTitle = new JLabel("Bildirim Ayarları", SwingConstants.CENTER);
        rightPanelTitle.setFont(new Font("Inter", Font.BOLD, 24));
        rightPanelTitle.setForeground(PRIMARY_COLOR);
        rightPanelTitle.setBounds(0, 10, 440, 40);
        rightPanel.add(rightPanelTitle);

        // Ayarlar paneli
        JPanel settingsPanel = new JPanel(null);
        settingsPanel.setBounds(30, 60, 380, 400);
        settingsPanel.setBackground(Color.WHITE);
        settingsPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));

        int y = 30;
        
        // E-posta bildirimleri
        addNotificationOption(settingsPanel, "E-posta Bildirimleri", 
            "Randevu onayları ve hatırlatıcılar için e-posta al", y);
        
        // SMS bildirimleri
        addNotificationOption(settingsPanel, "SMS Bildirimleri", 
            "Randevu onayları ve hatırlatıcılar için SMS al", y + 80);

        // Hatırlatıcı zamanı
        JLabel lblReminder = new JLabel("Randevu Hatırlatıcı Zamanı");
        lblReminder.setFont(new Font("Inter", Font.BOLD, 14));
        lblReminder.setBounds(30, y + 160, 320, 30);
        settingsPanel.add(lblReminder);

        String[] times = {"1 saat önce", "3 saat önce", "1 gün önce", "2 gün önce"};
        JComboBox<String> reminderTime = new JComboBox<>(times);
        reminderTime.setFont(new Font("Inter", Font.PLAIN, 14));
        reminderTime.setBounds(30, y + 195, 320, 35);
        reminderTime.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        reminderTime.setBackground(Color.WHITE);
        settingsPanel.add(reminderTime);

        // Kaydet butonu
        JButton btnSave = new JButton("Değişiklikleri Kaydet");
        btnSave.setBounds(30, y + 260, 320, 40);
        btnSave.setFont(new Font("Inter", Font.BOLD, 14));
        btnSave.setBackground(PRIMARY_COLOR);
        btnSave.setForeground(Color.WHITE);
        btnSave.setBorder(new EmptyBorder(5, 15, 5, 15));
        btnSave.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnSave.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnSave.setBackground(HOVER_COLOR);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btnSave.setBackground(PRIMARY_COLOR);
            }
        });
        
        settingsPanel.add(btnSave);
        rightPanel.add(settingsPanel);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void addNotificationOption(JPanel panel, String title, String description, int y) {
        JCheckBox checkBox = new JCheckBox(title);
        checkBox.setFont(new Font("Inter", Font.BOLD, 14));
        checkBox.setBounds(30, y, 320, 30);
        checkBox.setBackground(Color.WHITE);
        panel.add(checkBox);

        JLabel lblDesc = new JLabel(description);
        lblDesc.setFont(new Font("Inter", Font.PLAIN, 13));
        lblDesc.setForeground(GRAY_TEXT_COLOR);
        lblDesc.setBounds(50, y + 25, 300, 20);
        panel.add(lblDesc);
    }

    private void showHelpAndSupport() {
        rightPanel.removeAll();

        // Başlık
        rightPanelTitle = new JLabel("Yardım ve Destek", SwingConstants.CENTER);
        rightPanelTitle.setFont(new Font("Inter", Font.BOLD, 24));
        rightPanelTitle.setForeground(PRIMARY_COLOR);
        rightPanelTitle.setBounds(0, 10, 440, 40);
        rightPanel.add(rightPanelTitle);

        // Yardım paneli
        JPanel helpPanel = new JPanel(null);
        helpPanel.setBounds(30, 60, 380, 400);
        helpPanel.setBackground(Color.WHITE);
        helpPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));

        // SSS Başlığı
        JLabel lblFaq = new JLabel("Sık Sorulan Sorular");
        lblFaq.setFont(new Font("Inter", Font.BOLD, 16));
        lblFaq.setBounds(30, 20, 320, 30);
        helpPanel.add(lblFaq);

        // SSS Listesi ve Cevapları
        String[][] questionsAndAnswers = {
            {"✦  Randevu nasıl alabilirim?", 
             "Randevu almak için:\n\n" +
             "1. Ana sayfada 'Randevu Al' butonuna tıklayın\n" +
             "2. İstediğiniz kliniği seçin\n" +
             "3. Uygun doktor ve saati belirleyin\n" +
             "4. Randevunuzu onaylayın\n\n" +
             "Randevunuz başarıyla oluşturulacaktır."},
            
            {"✦  Randevumu nasıl iptal edebilirim?", 
             "Randevu iptali için:\n\n" +
             "1. 'Randevularım' bölümüne gidin\n" +
             "2. İptal etmek istediğiniz randevuyu seçin\n" +
             "3. 'İptal Et' butonuna tıklayın\n\n" +
             "Not: Randevunuzu en geç 24 saat öncesinde iptal edebilirsiniz."},
            
            {"✦  Şifremi nasıl değiştirebilirim?", 
             "Şifre değiştirmek için:\n\n" +
             "1. Profil menüsünden 'Şifre Değiştir'e tıklayın\n" +
             "2. Mevcut şifrenizi girin\n" +
             "3. Yeni şifrenizi belirleyin ve tekrar girin\n" +
             "4. 'Şifreyi Güncelle' butonuna tıklayın"},
            
            {"✦  Bildirim ayarlarını nasıl güncelleyebilirim?", 
             "Bildirim ayarlarını güncellemek için:\n\n" +
             "1. Profil menüsünden 'Bildirim Ayarları'na gidin\n" +
             "2. E-posta ve SMS bildirimlerini açıp kapatabilirsiniz\n" +
             "3. Randevu hatırlatıcı süresini ayarlayabilirsiniz\n" +
             "4. 'Değişiklikleri Kaydet' butonuna tıklayın"}
        };

        int y = 60;
        for (String[] qa : questionsAndAnswers) {
            JButton btnQuestion = new JButton(qa[0]);
            btnQuestion.setBounds(30, y, 320, 40);
            btnQuestion.setFont(new Font("Inter", Font.PLAIN, 14));
            btnQuestion.setHorizontalAlignment(SwingConstants.LEFT);
            btnQuestion.setBackground(Color.WHITE);
            btnQuestion.setForeground(GRAY_TEXT_COLOR);
            btnQuestion.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
            btnQuestion.setFocusPainted(false);
            btnQuestion.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            btnQuestion.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    btnQuestion.setBackground(SECONDARY_COLOR);
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    btnQuestion.setBackground(Color.WHITE);
                }
            });
            
            btnQuestion.addActionListener(e -> showQuestionDetail(qa[0], qa[1]));
            
            helpPanel.add(btnQuestion);
            y += 50;
        }

        // İletişim Başlığı
        JLabel lblContact = new JLabel("İletişim");
        lblContact.setFont(new Font("Inter", Font.BOLD, 16));
        lblContact.setBounds(30, 280, 320, 30);
        helpPanel.add(lblContact);

        // İletişim Bilgileri
        String[] contacts = {
            "📞  Telefon: 0850 123 45 67",
            "✉️  E-posta: destek@hastane.com",
            "🌐  Web: www.hastane.com"
        };

        y = 320;
        for (String contact : contacts) {
            JLabel lblInfo = new JLabel(contact);
            lblInfo.setFont(new Font("Inter", Font.PLAIN, 14));
            lblInfo.setForeground(GRAY_TEXT_COLOR);
            lblInfo.setBounds(30, y, 320, 25);
            helpPanel.add(lblInfo);
            y += 30;
        }

        rightPanel.add(helpPanel);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void showQuestionDetail(String question, String answer) {
        // Mevcut paneli sakla
        Component[] components = rightPanel.getComponents();
        for (Component c : components) {
            c.setVisible(false);
        }

        // Detay paneli
        JPanel detailPanel = new JPanel(null);
        detailPanel.setBounds(30, 60, 380, 400);
        detailPanel.setBackground(Color.WHITE);
        detailPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));

        // Geri butonu
        JButton btnBack = new JButton("← Geri");
        btnBack.setBounds(30, 20, 80, 30);
        btnBack.setFont(new Font("Inter", Font.BOLD, 14));
        btnBack.setBackground(Color.WHITE);
        btnBack.setForeground(PRIMARY_COLOR);
        btnBack.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true));
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnBack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnBack.setBackground(PRIMARY_COLOR);
                btnBack.setForeground(Color.WHITE);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btnBack.setBackground(Color.WHITE);
                btnBack.setForeground(PRIMARY_COLOR);
            }
        });
        
        btnBack.addActionListener(e -> {
            rightPanel.remove(detailPanel);
            rightPanel.remove(btnBack);
            for (Component c : components) {
                c.setVisible(true);
            }
            rightPanel.revalidate();
            rightPanel.repaint();
        });

        // Soru başlığı
        JLabel lblQuestion = new JLabel(question.substring(3)); // "✦  " prefix'ini kaldır
        lblQuestion.setFont(new Font("Inter", Font.BOLD, 16));
        lblQuestion.setBounds(30, 20, 320, 30);
        detailPanel.add(lblQuestion);

        // Cevap metni
        JTextArea txtAnswer = new JTextArea(answer);
        txtAnswer.setFont(new Font("Inter", Font.PLAIN, 14));
        txtAnswer.setForeground(GRAY_TEXT_COLOR);
        txtAnswer.setLineWrap(true);
        txtAnswer.setWrapStyleWord(true);
        txtAnswer.setEditable(false);
        txtAnswer.setBackground(Color.WHITE);
        txtAnswer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(txtAnswer);
        scrollPane.setBounds(30, 60, 320, 310);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        detailPanel.add(scrollPane);

        rightPanel.add(btnBack);
        rightPanel.add(detailPanel);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void showPasswordChangeDialog() {
        rightPanel.removeAll();

        // Başlık
        rightPanelTitle = new JLabel("Şifre Değiştir", SwingConstants.CENTER);
        rightPanelTitle.setFont(new Font("Inter", Font.BOLD, 24));
        rightPanelTitle.setForeground(PRIMARY_COLOR);
        rightPanelTitle.setBounds(0, 10, 440, 40);
        rightPanel.add(rightPanelTitle);

        // Şifre değiştirme paneli
        JPanel passwordPanel = new JPanel(null);
        passwordPanel.setBounds(30, 60, 380, 400);
        passwordPanel.setBackground(Color.WHITE);
        passwordPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));

        int yOffset = 30;
        int spacing = 80;

        // Mevcut şifre
        JLabel lblCurrentPass = new JLabel("Mevcut Şifre");
        lblCurrentPass.setFont(new Font("Inter", Font.BOLD, 14));
        lblCurrentPass.setBounds(30, yOffset, 320, 25);
        passwordPanel.add(lblCurrentPass);

        JPasswordField currentPass = new JPasswordField();
        currentPass.setBounds(30, yOffset + 30, 320, 35);
        currentPass.setFont(new Font("Inter", Font.PLAIN, 14));
        currentPass.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        passwordPanel.add(currentPass);

        // Yeni şifre
        JLabel lblNewPass = new JLabel("Yeni Şifre");
        lblNewPass.setFont(new Font("Inter", Font.BOLD, 14));
        lblNewPass.setBounds(30, yOffset + spacing, 320, 25);
        passwordPanel.add(lblNewPass);

        JPasswordField newPass = new JPasswordField();
        newPass.setBounds(30, yOffset + spacing + 30, 320, 35);
        newPass.setFont(new Font("Inter", Font.PLAIN, 14));
        newPass.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        passwordPanel.add(newPass);

        // Yeni şifre tekrar
        JLabel lblConfirmPass = new JLabel("Yeni Şifre (Tekrar)");
        lblConfirmPass.setFont(new Font("Inter", Font.BOLD, 14));
        lblConfirmPass.setBounds(30, yOffset + spacing * 2, 320, 25);
        passwordPanel.add(lblConfirmPass);

        JPasswordField confirmPass = new JPasswordField();
        confirmPass.setBounds(30, yOffset + spacing * 2 + 30, 320, 35);
        confirmPass.setFont(new Font("Inter", Font.PLAIN, 14));
        confirmPass.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        passwordPanel.add(confirmPass);

        // Şifre değiştir butonu
        JButton btnChangePassword = new JButton("Şifreyi Güncelle");
        btnChangePassword.setBounds(30, yOffset + spacing * 3, 320, 40);
        btnChangePassword.setFont(new Font("Inter", Font.BOLD, 14));
        btnChangePassword.setBackground(PRIMARY_COLOR);
        btnChangePassword.setForeground(Color.WHITE);
        btnChangePassword.setBorder(new EmptyBorder(5, 15, 5, 15));
        btnChangePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnChangePassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnChangePassword.setBackground(HOVER_COLOR);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btnChangePassword.setBackground(PRIMARY_COLOR);
            }
        });
        
        btnChangePassword.addActionListener(e -> {
            String currentPassword = new String(currentPass.getPassword());
            String newPassword = new String(newPass.getPassword());
            String confirmPassword = new String(confirmPass.getPassword());
            
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showWarning("Lütfen tüm alanları doldurunuz!");
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                showWarning("Yeni şifreler eşleşmiyor!");
                return;
            }

            // Şifre uzunluğu kontrolü
            if (newPassword.length() < 6) {
                showWarning("Yeni şifre en az 6 karakter olmalıdır!");
                return;
            }
            
            // Şifre değiştirme işlemi
            if (hasta.updatePassword(currentPassword, newPassword)) {
                showSuccess("Şifreniz başarıyla güncellendi!");
                currentPass.setText("");
                newPass.setText("");
                confirmPass.setText("");
            } else {
                showError("Mevcut şifreniz yanlış veya bir hata oluştu!");
            }
        });
        
        passwordPanel.add(btnChangePassword);
        rightPanel.add(passwordPanel);
        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private void addPasswordField(JPanel panel, String label, int y) {
        JLabel lblField = new JLabel(label);
        lblField.setFont(new Font("Inter", Font.BOLD, 14));
        lblField.setBounds(30, y, 320, 25);
        panel.add(lblField);

        JPasswordField field = new JPasswordField();
        field.setBounds(30, y + 30, 320, 35);
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        panel.add(field);
    }

    private JPanel createStatsCard(String title, String value, Color bgColor) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(0, 2));
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Inter", Font.BOLD, 12));
        lblTitle.setForeground(Color.WHITE);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Inter", Font.BOLD, 20));
        lblValue.setForeground(Color.WHITE);
        lblValue.setHorizontalAlignment(SwingConstants.CENTER);
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        
        return card;
    }

    private void createPasswordChangeSection(JPanel panel) {
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            "Şifre Değiştir",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Inter", Font.BOLD, 16),
            PRIMARY_COLOR
        ));

        int yOffset = 50;
        int spacing = 80;

        // Mevcut şifre
        JLabel lblCurrentPass = new JLabel("Mevcut Şifre");
        lblCurrentPass.setFont(new Font("Inter", Font.BOLD, 14));
        lblCurrentPass.setBounds(30, yOffset, 150, 25);
        panel.add(lblCurrentPass);

        JPasswordField currentPass = createStyledPasswordField(30, yOffset + 30, 380, 35);
        panel.add(currentPass);

        // Yeni şifre
        JLabel lblNewPass = new JLabel("Yeni Şifre");
        lblNewPass.setFont(new Font("Inter", Font.BOLD, 14));
        lblNewPass.setBounds(30, yOffset + spacing, 150, 25);
        panel.add(lblNewPass);

        JPasswordField newPass = createStyledPasswordField(30, yOffset + spacing + 30, 380, 35);
        panel.add(newPass);

        // Yeni şifre tekrar
        JLabel lblConfirmPass = new JLabel("Yeni Şifre (Tekrar)");
        lblConfirmPass.setFont(new Font("Inter", Font.BOLD, 14));
        lblConfirmPass.setBounds(30, yOffset + spacing * 2, 150, 25);
        panel.add(lblConfirmPass);

        JPasswordField confirmPass = createStyledPasswordField(30, yOffset + spacing * 2 + 30, 380, 35);
        panel.add(confirmPass);

        // Şifre değiştir butonu
        JButton btnChangePassword = new JButton("Şifreyi Güncelle");
        btnChangePassword.setBounds(30, yOffset + spacing * 3, 380, 40);
        btnChangePassword.setFont(new Font("Inter", Font.BOLD, 14));
        btnChangePassword.setBackground(PRIMARY_COLOR);
        btnChangePassword.setForeground(Color.WHITE);
        btnChangePassword.setFocusPainted(false);
        btnChangePassword.setBorder(new EmptyBorder(5, 15, 5, 15));
        btnChangePassword.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btnChangePassword.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnChangePassword.setBackground(HOVER_COLOR);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btnChangePassword.setBackground(PRIMARY_COLOR);
            }
        });
        
        btnChangePassword.addActionListener(e -> {
            String currentPassword = new String(currentPass.getPassword());
            String newPassword = new String(newPass.getPassword());
            String confirmPassword = new String(confirmPass.getPassword());
            
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                showWarning("Lütfen tüm alanları doldurunuz!");
                return;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                showWarning("Yeni şifreler eşleşmiyor!");
                return;
            }

            // Şifre uzunluğu kontrolü
            if (newPassword.length() < 6) {
                showWarning("Yeni şifre en az 6 karakter olmalıdır!");
                return;
            }
            
            // Şifre değiştirme işlemi
            if (hasta.updatePassword(currentPassword, newPassword)) {
                showSuccess("Şifreniz başarıyla güncellendi!");
                currentPass.setText("");
                newPass.setText("");
                confirmPass.setText("");
            } else {
                showError("Mevcut şifreniz yanlış veya bir hata oluştu!");
            }
        });
        
        panel.add(btnChangePassword);
    }

    private void addInfoField(JPanel panel, String label, String value) {
        JPanel fieldPanel = new JPanel();
        fieldPanel.setLayout(null);
        fieldPanel.setBackground(Color.WHITE);
        fieldPanel.setPreferredSize(new Dimension(360, 35));
        
        JLabel lblField = new JLabel(label);
        lblField.setFont(new Font("Inter", Font.BOLD, 13));
        lblField.setForeground(GRAY_TEXT_COLOR);
        lblField.setBounds(0, 0, 360, 15);
        
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Inter", Font.BOLD, 14));
        lblValue.setForeground(Color.BLACK);
        lblValue.setBounds(0, 15, 360, 20);
        
        fieldPanel.add(lblField);
        fieldPanel.add(lblValue);
        panel.add(fieldPanel);
    }

    private JButton createStyledButton(String text, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setBounds(x, y, width, height);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setBackground(Color.WHITE);
        button.setForeground(PRIMARY_COLOR);
        button.setBorder(BorderFactory.createLineBorder(PRIMARY_COLOR, 1, true));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(PRIMARY_COLOR);
                button.setForeground(Color.WHITE);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(Color.WHITE);
                button.setForeground(PRIMARY_COLOR);
            }
        });
        
        return button;
    }

    private JPasswordField createStyledPasswordField(int x, int y, int width, int height) {
        JPasswordField field = new JPasswordField();
        field.setBounds(x, y, width, height);
        field.setFont(new Font("Inter", Font.PLAIN, 14));
        field.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(5, 10, 5, 10)
        ));
        return field;
    }

    private void setDefaultProfileImage() {
        try {
            // Önce kullanıcının profil fotoğrafını kontrol et
            String projectRoot = System.getProperty("user.dir");
            File profilesDir = new File(projectRoot + "/src/Images/profiles");
            
            System.out.println("Profil fotoğrafı aranıyor: " + profilesDir.getAbsolutePath());
            
            if (profilesDir.exists()) {
                File[] userPhotos = profilesDir.listFiles((dir, name) -> 
                    name.startsWith("profile_" + hasta.getTcno() + "."));
                if (userPhotos != null && userPhotos.length > 0) {
                    BufferedImage userImage = ImageIO.read(userPhotos[0]);
                    if (userImage != null) {
                        profileImageLabel.setImage(userImage);
                        System.out.println("Kullanıcı profil fotoğrafı yüklendi: " + userPhotos[0].getAbsolutePath());
                        return;
                    }
                }
            }

            // Kullanıcı fotoğrafı yoksa varsayılan avatarı kullan
            File defaultAvatarFile = new File(projectRoot + "/src/Images/default_avatar.png");
            System.out.println("Varsayılan avatar aranıyor: " + defaultAvatarFile.getAbsolutePath());
            
            if (defaultAvatarFile.exists()) {
                BufferedImage originalImage = ImageIO.read(defaultAvatarFile);
                if (originalImage != null) {
                    Image scaledImage = originalImage.getScaledInstance(180, 180, Image.SCALE_AREA_AVERAGING);
                    profileImageLabel.setImage(scaledImage);
                    System.out.println("Varsayılan avatar yüklendi");
                    return;
                }
            }

            // Varsayılan avatar bulunamazsa, baş harfli avatar oluştur
            System.out.println("Baş harfli avatar oluşturuluyor...");
            BufferedImage defaultImg = new BufferedImage(180, 180, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = defaultImg.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(200, 200, 200));
            g2d.fillOval(0, 0, 180, 180);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Inter", Font.BOLD, 60));
            String initial = hasta.getName().substring(0, 1).toUpperCase();
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(initial, (180 - fm.stringWidth(initial)) / 2, 
                (180 - fm.getHeight()) / 2 + fm.getAscent());
            g2d.dispose();
            profileImageLabel.setImage(defaultImg);

        } catch (Exception e) {
            System.err.println("Avatar yüklenirken hata: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveProfilePhoto(File selectedFile) {
        try {
            // Profil fotoğrafları için dizin kontrolü
            String projectRoot = System.getProperty("user.dir");
            File imagesDir = new File(projectRoot, "src/Images");
            File profilesDir = new File(imagesDir, "profiles");

            System.out.println("Images dizini: " + imagesDir.getAbsolutePath());
            System.out.println("Profiles dizini: " + profilesDir.getAbsolutePath());

            // Dizinlerin varlığını kontrol et ve oluştur
            if (!imagesDir.exists()) {
                boolean created = imagesDir.mkdirs();
                System.out.println("Images dizini oluşturuldu: " + created);
                if (!created) {
                    showError("Images dizini oluşturulamadı!");
                    return;
                }
            }
            if (!profilesDir.exists()) {
                boolean created = profilesDir.mkdirs();
                System.out.println("Profiles dizini oluşturuldu: " + created);
                if (!created) {
                    showError("Profiles dizini oluşturulamadı!");
                    return;
                }
            }

            // TC'ye göre benzersiz dosya adı oluştur
            String tcno = hasta.getTcno();
            String fileExtension = "jpg"; // Sabit format kullan
            String profileImageName = "profile_" + tcno + "." + fileExtension;
            File destinationFile = new File(profilesDir, profileImageName);

            System.out.println("Hedef dosya: " + destinationFile.getAbsolutePath());

            // Eski profil fotoğrafını sil (varsa)
            deleteOldProfilePhoto(tcno, profilesDir);

            BufferedImage originalImage = ImageIO.read(selectedFile);
            if (originalImage != null) {
                // Görüntüyü kare şeklinde kırp
                int size = Math.min(originalImage.getWidth(), originalImage.getHeight());
                BufferedImage croppedImage = originalImage.getSubimage(
                    (originalImage.getWidth() - size) / 2,
                    (originalImage.getHeight() - size) / 2,
                    size, size
                );
                
                // Kırpılmış görüntüyü 180x180 boyutuna ölçekle ve RGB formatına dönüştür
                BufferedImage scaledImage = new BufferedImage(180, 180, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = scaledImage.createGraphics();
                g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, 180, 180);
                g2d.drawImage(croppedImage, 0, 0, 180, 180, null);
                g2d.dispose();

                try {
                    // Yeni profil fotoğrafını kaydet
                    destinationFile.getParentFile().mkdirs(); // Emin olmak için dizini tekrar oluştur
                    ImageIO.write(scaledImage, "jpg", destinationFile);
                    
                    if (!destinationFile.exists()) {
                        showError("Profil fotoğrafı kaydedilemedi!");
                        return;
                    }

                    // GUI'yi güncelle
                    profileImageLabel.setImage(scaledImage);
                    showSuccess("Profil fotoğrafı başarıyla güncellendi!");
                    System.out.println("Profil fotoğrafı kaydedildi: " + destinationFile.getAbsolutePath());
                } catch (IOException e) {
                    showError("Dosya kaydedilirken hata oluştu: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                showError("Geçersiz resim dosyası!");
            }
        } catch (Exception ex) {
            showError("Resim yüklenirken hata oluştu: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1).toLowerCase();
        }
        return "png"; // Varsayılan format
    }

    private void deleteOldProfilePhoto(String tcno, File profilesDir) {
        File[] files = profilesDir.listFiles((dir, name) -> 
            name.startsWith("profile_" + tcno + "."));
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Uyarı", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Hata", JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Başarılı", JOptionPane.INFORMATION_MESSAGE);
    }

    private JButton createMenuButton(String text, boolean isActive) {
        JButton button = new JButton(text);
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        if (isActive) {
            button.setBackground(PRIMARY_COLOR);
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(GRAY_TEXT_COLOR);
            button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
            
            button.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (button.getBackground().equals(Color.WHITE)) {
                        button.setBackground(SECONDARY_COLOR);
                    }
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    if (button.getBackground().equals(SECONDARY_COLOR)) {
                        button.setBackground(Color.WHITE);
                    }
                }
            });
        }
        
        return button;
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Bilgi", JOptionPane.INFORMATION_MESSAGE);
    }

    private Object[][] getCurrentPageData(Object[][] data, int currentPage, int rowsPerPage) {
        int start = currentPage * rowsPerPage;
        int end = Math.min(start + rowsPerPage, data.length);
        return java.util.Arrays.copyOfRange(data, start, end);
    }
}