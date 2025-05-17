package View;

import Model.Hasta;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class HomepageGUI extends JFrame {
    private static final long serialVersionUID = 1L;
    private Hasta hasta;
    private Color primaryColor = new Color(6, 95, 70);
    private Color secondaryColor = new Color(240, 253, 244);
    private Color accentColor = new Color(6, 95, 70);
    private Color backgroundColor = new Color(245, 245, 245);
    private List<Pharmacy> pharmacies = new ArrayList<>();
    /*private JXMapViewer mapViewer;*/
    private JEditorPane listPane;
    private JLabel loadingLabel;
    private JComboBox<String> cityCombo;
    private JComboBox<String> districtCombo;
    private JButton searchButton;
    private JTabbedPane tabbedPane;
    private JScrollPane listScroll;
    private JScrollPane mapScroll;
    private int currentPage = 1;
    private static final int PHARMACIES_PER_PAGE = 1;
    private JButton prevButton;
    private JButton nextButton;
    private JLabel pageLabel;
    
    private class Pharmacy {
        String name;
        String address;
        String phone;
        String dist;
        int x, y; // Harita üzerindeki konumu
        
        public Pharmacy(String name, String address, String phone, String dist) {
            this.name = name;
            this.address = address;
            this.phone = phone;
            this.dist = dist;
            // Rastgele konum ata (sonra gerçek konumlarla değiştirilecek)
            this.x = (int)(Math.random() * 400) + 50;
            this.y = (int)(Math.random() * 200) + 25;
        }
    }

    private void fetchPharmacies(String city, String district) {
        try {
            String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8.toString());
            String encodedDistrict = URLEncoder.encode(district, StandardCharsets.UTF_8.toString());
            String apiUrl = "https://api.collectapi.com/health/dutyPharmacy?il=" + encodedCity + "&ilce=" + encodedDistrict;
            URL url = new URL(apiUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            // API Headers
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "apikey 1EnmErFgTuVz6fc66F9Srf:7p92tJvzHmNQ9r8Goz3ACL");

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("API yanıt vermedi (Hata kodu: " + responseCode + ")");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            String jsonStr = response.toString();
            pharmacies.clear();

            try {
                // Success kontrolü
                if (jsonStr.contains("\"success\":false")) {
                    throw new Exception("API yanıtı başarısız");
                }

                // Result array'ini bul
                int resultStart = jsonStr.indexOf("\"result\":[");
                if (resultStart == -1) {
                    throw new Exception("Eczane verisi bulunamadı");
                }
                resultStart += 9;

                int resultEnd = jsonStr.lastIndexOf("]");
                if (resultEnd == -1 || resultEnd <= resultStart) {
                    throw new Exception("Eczane verisi hatalı");
                }

                String resultArray = jsonStr.substring(resultStart, resultEnd + 1);
                if (resultArray.isEmpty() || resultArray.equals("[]")) {
                    return; // Boş liste, hata değil
                }

                // Her bir eczane objesi için
                String[] pharmacyObjects = resultArray.split("\\},\\{");
                for (String pharmacyStr : pharmacyObjects) {
                    pharmacyStr = pharmacyStr.replace("[{", "").replace("}]", "");
                    
                    try {
                        String name = extractValue(pharmacyStr, "name");
                        String address = extractValue(pharmacyStr, "address");
                        String phone = extractValue(pharmacyStr, "phone");
                        String dist = extractValue(pharmacyStr, "dist");

                        if (name != null && !name.isEmpty()) {
                            Pharmacy newPharmacy = new Pharmacy(
                                name,
                                address != null ? address : "Adres bilgisi yok",
                                phone != null ? phone : "Telefon bilgisi yok",
                                dist != null ? dist : "Konum bilgisi yok"
                            );
                            
                            // Harita üzerinde rastgele konum ata
                            newPharmacy.x = (int)(Math.random() * 400) + 50;
                            newPharmacy.y = (int)(Math.random() * 150) + 25;
                            
                            pharmacies.add(newPharmacy);
                        }
                    } catch (Exception ex) {
                        System.err.println("Eczane verisi işlenirken hata: " + ex.getMessage());
                        continue; // Hatalı eczaneyi atla
                    }
                }
            } catch (Exception e) {
                throw new Exception("Veri işlenirken hata oluştu: " + e.getMessage());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Eczane bilgileri alınırken bir hata oluştu:\n" + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String extractValue(String jsonStr, String key) {
        try {
            String searchKey = "\"" + key + "\":\"";
            int start = jsonStr.indexOf(searchKey);
            if (start == -1) return null;
            
            start += searchKey.length();
            int end = jsonStr.indexOf("\"", start);
            if (end == -1) return null;
            
            String value = jsonStr.substring(start, end);
            return value.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private void getGeocode(String address, Consumer<double[]> callback) {
        new Thread(() -> {
            try {
                // Debug: Gönderilen adresi yazdır
                System.out.println("Geocoding için gönderilen adres: " + address);

                // URL'yi düzgün formatlayalım
                String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8.toString())
                    .replace("+", "%20"); // Boşlukları %20 ile değiştir
                
                String geocodeUrl = String.format(
                    "https://nominatim.openstreetmap.org/search" +
                    "?q=%s" +
                    "&format=json" +
                    "&limit=1" +
                    "&countrycodes=tr" +
                    "&addressdetails=1" +
                    "&accept-language=tr",
                    encodedAddress
                );
                
                // Debug: API URL'sini yazdır
                System.out.println("Geocoding API URL: " + geocodeUrl);
                
                URL url = new URL(geocodeUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setRequestProperty("Accept", "application/json");
                
                // 1 saniye bekle (rate limiting için)
                Thread.sleep(1000);
                
                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    System.err.println("Geocoding hatası: HTTP " + responseCode);
                    // Hata durumunda yanıtı yazdır
                    try (BufferedReader errorReader = new BufferedReader(
                            new InputStreamReader(conn.getErrorStream()))) {
                        String errorLine;
                        StringBuilder errorResponse = new StringBuilder();
                        while ((errorLine = errorReader.readLine()) != null) {
                            errorResponse.append(errorLine);
                        }
                        System.err.println("Hata yanıtı: " + errorResponse.toString());
                    } catch (Exception e) {
                        System.err.println("Hata yanıtı okunamadı: " + e.getMessage());
                    }
                    SwingUtilities.invokeLater(() -> callback.accept(new double[] {41.0082, 28.9784}));
                    return;
                }
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                // Debug: API yanıtını yazdır
                System.out.println("Geocoding API yanıtı: " + response.toString());
                
                String jsonStr = response.toString();
                if (jsonStr.startsWith("[") && !jsonStr.equals("[]")) {
                    jsonStr = jsonStr.substring(1, jsonStr.length() - 1);
                    
                    String lat = extractValue(jsonStr, "lat");
                    String lon = extractValue(jsonStr, "lon");
                    
                    // Debug: Bulunan koordinatları yazdır
                    System.out.println("Bulunan koordinatlar - Lat: " + lat + ", Lon: " + lon);
                    
                    if (lat != null && lon != null) {
                        try {
                            double latitude = Double.parseDouble(lat);
                            double longitude = Double.parseDouble(lon);
                            if (isValidCoordinate(latitude, longitude)) {
                                System.out.println("Geçerli koordinatlar bulundu: " + latitude + ", " + longitude);
                                SwingUtilities.invokeLater(() -> callback.accept(new double[] {latitude, longitude}));
                                return;
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("Koordinat dönüştürme hatası: " + e.getMessage());
                        }
                    }
                }
                
                System.err.println("Geçerli koordinat bulunamadı, varsayılan konum kullanılıyor");
                SwingUtilities.invokeLater(() -> callback.accept(new double[] {41.0082, 28.9784}));
                
            } catch (Exception e) {
                System.err.println("Geocoding hatası: " + e.getMessage());
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> callback.accept(new double[] {41.0082, 28.9784}));
            }
        }).start();
    }

    private boolean isValidCoordinate(double lat, double lon) {
        return lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180;
    }

    public HomepageGUI(Hasta hasta) {
        this.hasta = hasta;
        setTitle("Hastane Yönetim Sistemi - Ana Sayfa");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setResizable(false);

        // Ana panel
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setColor(secondaryColor);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(null);
        setContentPane(mainPanel);

        // Üst panel
        JPanel topPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(primaryColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        topPanel.setOpaque(false);
        topPanel.setBounds(0, 0, 1100, 80);
        topPanel.setLayout(null);
        mainPanel.add(topPanel);

        // Hoşgeldiniz mesajı
        JLabel welcomeLabel = new JLabel(getGreetingMessage(hasta.getName()));
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        welcomeLabel.setForeground(Color.WHITE);
        welcomeLabel.setBounds(50, 20, 500, 40);
        topPanel.add(welcomeLabel);

        // Modern butonlar
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(null);
        buttonPanel.setOpaque(false);
        buttonPanel.setBounds(550, 0, 550, 80);
        topPanel.add(buttonPanel);

        // Butonları aynı renk tonlarında oluştur
        Color buttonColor = new Color(255, 255, 255, 50); // Yarı saydam beyaz
        JButton randevuButton = createModernButton("Randevu Al", e -> makeAppointment(), buttonColor);
        JButton randevularimButton = createModernButton("Randevularım", e -> openAppointments(), buttonColor);
        JButton blogButton = createModernButton("Blog", e -> openBlog(), buttonColor);
        JButton profilButton = createModernButton("Profil", e -> openProfile(), buttonColor);
        JButton cikisButton = createModernButton("Çıkış", e -> logout(), buttonColor);

        // Butonları konumlandır (5 buton için yeni konumlar)
        randevuButton.setBounds(0, 20, 100, 40);
        randevularimButton.setBounds(110, 20, 100, 40);
        blogButton.setBounds(220, 20, 100, 40);
        profilButton.setBounds(330, 20, 100, 40);
        cikisButton.setBounds(440, 20, 100, 40);

        // Butonları ekle
        buttonPanel.add(randevuButton);
        buttonPanel.add(randevularimButton);
        buttonPanel.add(blogButton);
        buttonPanel.add(profilButton);
        buttonPanel.add(cikisButton);

        // İçerik paneli
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new GridLayout(1, 2, 20, 20));
        contentPanel.setOpaque(false);
        contentPanel.setBounds(20, 100, 1060, 540);
        mainPanel.add(contentPanel);

        // Sol taraf - Sağlık kartları
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new GridLayout(2, 1, 20, 20));
        leftPanel.setOpaque(false);

        // Sağlıklı Yaşam kartı
        leftPanel.add(createHealthCard("Sağlıklı Yaşam", 
            "Dengeli beslenme ve düzenli egzersiz sağlıklı bir yaşamın temelidir. " + 
            "Günde en az 30 dakika egzersiz yapmayı hedefleyin."));

        // Stres Yönetimi kartı
        leftPanel.add(createHealthCard("Stres Yönetimi", 
            "Meditasyon ve nefes egzersizleri ile stresi kontrol altında tutun. " + 
            "Düzenli molalar verin ve hobiler edinin."));

        contentPanel.add(leftPanel);

        // Sağ taraf - Nöbetçi Eczane Arama (Tam alan)
        contentPanel.add(createPharmacyMapCard("Nöbetçi Eczaneler"));

        // Harita görünümü
        initializeMap();
       // JScrollPane mapScroll = new JScrollPane(mapViewer);

        tabbedPane.addTab("Liste Görünümü", new ImageIcon(), listScroll);
        tabbedPane.addTab("Harita Görünümü", new ImageIcon(), mapScroll);

        // Arama butonu işlevselliği
        searchButton.addActionListener(e -> {
            String city = cityCombo.getSelectedItem().toString().trim().toLowerCase();
            String district = districtCombo.getSelectedItem().toString().trim().toLowerCase();
            
            listPane.setText("<h2 style='text-align:center'>Nöbetçi eczaneler aranıyor...</h2>");
            loadingLabel.setText("Harita yükleniyor...");
            
            new Thread(() -> {
                fetchPharmacies(city, district);
                
                SwingUtilities.invokeLater(() -> {
                    if (pharmacies.isEmpty()) {
                        listPane.setText("<h2 style='text-align:center'>Bu bölgede nöbetçi eczane bulunamadı.</h2>");
                        loadingLabel.setText("Bu bölgede nöbetçi eczane bulunamadı.");
                    } else {
                        showListView(pharmacies);
                        showSimpleMap(pharmacies);
                    }
                });
            }).start();
        });
    }

    private JPanel createHealthCard(String title, String content) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
                
                // Hafif gölge efekti
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fillRoundRect(5, 5, getWidth()-6, getHeight()-6, 30, 30);
            }
        };
        card.setLayout(new BorderLayout(15, 15));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(primaryColor);

        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        contentArea.setForeground(new Color(75, 85, 99));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setOpaque(false);
        contentArea.setEditable(false);
        contentArea.setBorder(null);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(contentArea, BorderLayout.CENTER);

        return card;
    }

    private JPanel createPharmacyMapCard(String title) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 30, 30);
                
                g2d.setColor(new Color(0, 0, 0, 10));
                g2d.fillRoundRect(5, 5, getWidth()-6, getHeight()-6, 30, 30);
            }
        };
        card.setLayout(new BorderLayout(20, 20));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(25, 25, 25, 25));

        // Başlık paneli
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        titlePanel.setOpaque(false);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(primaryColor);
        titlePanel.add(titleLabel);

        // Arama paneli
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new BoxLayout(searchPanel, BoxLayout.X_AXIS));
        searchPanel.setOpaque(false);
        searchPanel.setBorder(new EmptyBorder(10, 0, 20, 0));
        
        // İl arama komponenti
        JPanel cityPanel = new JPanel(new BorderLayout(5, 5));
        cityPanel.setOpaque(false);
        JLabel cityLabel = new JLabel("İl");
        cityLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cityLabel.setForeground(new Color(75, 85, 99));
        
        cityCombo = new JComboBox<>(new String[]{"İstanbul"});
        cityCombo.setPreferredSize(new Dimension(200, 40));
        cityCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cityCombo.setBackground(Color.WHITE);
        cityPanel.add(cityLabel, BorderLayout.NORTH);
        cityPanel.add(cityCombo, BorderLayout.CENTER);

        // İlçe arama komponenti
        JPanel districtPanel = new JPanel(new BorderLayout(5, 5));
        districtPanel.setOpaque(false);
        JLabel districtLabel = new JLabel("İlçe");
        districtLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        districtLabel.setForeground(new Color(75, 85, 99));
        
        districtCombo = new JComboBox<>(new String[]{"Beylikdüzü", "Avcılar", "Başakşehir", "Esenyurt", "Büyükçekmece"});
        districtCombo.setPreferredSize(new Dimension(200, 40));
        districtCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        districtCombo.setBackground(Color.WHITE);
        districtPanel.add(districtLabel, BorderLayout.NORTH);
        districtPanel.add(districtCombo, BorderLayout.CENTER);

        // Arama butonu
        searchButton = new JButton("Eczane Ara") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gradient = new GradientPaint(
                    0, 0, primaryColor,
                    0, getHeight(), primaryColor.darker()
                );
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(getText(), x, y);
            }
        };
        searchButton.setPreferredSize(new Dimension(150, 40));
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchButton.setBorderPainted(false);
        searchButton.setContentAreaFilled(false);
        searchButton.setFocusPainted(false);
        searchButton.setForeground(Color.WHITE);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Arama bileşenlerini panele ekle
        searchPanel.add(cityPanel);
        searchPanel.add(Box.createHorizontalStrut(15));
        searchPanel.add(districtPanel);
        searchPanel.add(Box.createHorizontalStrut(15));
        searchPanel.add(searchButton);
        searchPanel.add(Box.createHorizontalGlue());

        // Sekme paneli
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Liste görünümü
        listPane = new JEditorPane();
        listPane.setEditable(false);
        listPane.setContentType("text/html");
        listPane.setBorder(null);
        listPane.setPreferredSize(new Dimension(400, 150)); // Boyutu küçült
        
        // Sayfalama paneli
        JPanel paginationPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 3));
        paginationPanel.setBackground(Color.WHITE);
        paginationPanel.setVisible(false);
        
        // Geri butonu
        prevButton = new JButton("Geri") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(secondaryColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(secondaryColor.brighter());
                } else {
                    g2d.setColor(secondaryColor);
                }
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), x, y);
            }
        };
        prevButton.setPreferredSize(new Dimension(70, 25));
        prevButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        prevButton.setBorderPainted(false);
        prevButton.setContentAreaFilled(false);
        prevButton.setFocusPainted(false);
        prevButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        prevButton.addActionListener(e -> previousPage());
        
        // Sayfa numarası etiketi
        pageLabel = new JLabel("0/0", SwingConstants.CENTER);
        pageLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pageLabel.setForeground(new Color(6, 95, 70));
        pageLabel.setPreferredSize(new Dimension(40, 25));
        
        // İleri butonu
        nextButton = new JButton("İleri") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(secondaryColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(secondaryColor.brighter());
                } else {
                    g2d.setColor(secondaryColor);
                }
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 8, 8));
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), x, y);
            }
        };
        nextButton.setPreferredSize(new Dimension(70, 25));
        nextButton.setFont(new Font("Segoe UI", Font.BOLD, 11));
        nextButton.setBorderPainted(false);
        nextButton.setContentAreaFilled(false);
        nextButton.setFocusPainted(false);
        nextButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        nextButton.addActionListener(e -> nextPage());
        
        // Butonları panele ekle
        paginationPanel.add(prevButton);
        paginationPanel.add(pageLabel);
        paginationPanel.add(nextButton);
        
        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.add(listPane, BorderLayout.CENTER);
        mainPanel.add(paginationPanel, BorderLayout.SOUTH);
        
        listScroll = new JScrollPane(mainPanel);
        listScroll.setBorder(null);
        listScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        listScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
       

        // Ana içerik paneli
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setOpaque(false);
        contentPanel.add(searchPanel, BorderLayout.NORTH);
        contentPanel.add(tabbedPane, BorderLayout.CENTER);

        card.add(titlePanel, BorderLayout.NORTH);
        card.add(contentPanel, BorderLayout.CENTER);

        return card;
    }

    private String getGreetingMessage(String username) {
        String firstChar = username.substring(0, 1).toUpperCase();
        String restOfName = username.substring(1).split(" ")[0].toLowerCase();
        String firstName = firstChar + restOfName;
        String honorific = hasta.getGender().equals("Erkek") ? "Bey" : "Hanım";
        return "Hoşgeldiniz, " + firstName + " " + honorific;
    }

    private void openProfile() {
        try {
            dispose(); // Ana pencereyi kapat
            ProfilimGUI profilGUI = new ProfilimGUI(hasta);
            profilGUI.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openAppointments() {
        try {
            dispose(); // Ana pencereyi kapat
            RandevularımGUI randevuGUI = new RandevularımGUI(hasta);
            randevuGUI.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void makeAppointment() {
        try {
            dispose(); // Ana pencereyi kapat
            RandevuSisGUI randevuSisGUI = new RandevuSisGUI(hasta);
            randevuSisGUI.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void logout() {
        // Özel tasarımlı onay dialog'u oluştur
        JDialog dialog = new JDialog(this, "Çıkış Onayı", true);
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());
        dialog.setUndecorated(true); // Pencere dekorasyonlarını kaldır

        // Ana panel
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                // Kenarlık
                g2d.setColor(primaryColor);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 20, 20);
            }
        };
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Mesaj
        JLabel messageLabel = new JLabel("Çıkış yapmak istediğinizden emin misiniz?");
        messageLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        messageLabel.setHorizontalAlignment(JLabel.CENTER);
        messageLabel.setForeground(primaryColor);

        // Butonlar paneli
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        // Evet butonu
        JButton yesButton = new JButton("Evet") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(213, 0, 0).darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(213, 0, 0).brighter());
                } else {
                    g2d.setColor(new Color(213, 0, 0));
                }
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), x, y);
            }
        };
        yesButton.setPreferredSize(new Dimension(100, 35));
        yesButton.setBorderPainted(false);
        yesButton.setContentAreaFilled(false);
        yesButton.setFocusPainted(false);
        yesButton.setForeground(Color.WHITE);
        yesButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        yesButton.addActionListener(e -> {
            dialog.dispose();
            this.dispose();
            LoginGUI loginGUI = new LoginGUI();
            loginGUI.setVisible(true);
        });

        // Hayır butonu
        JButton noButton = new JButton("Hayır") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isPressed()) {
                    g2d.setColor(primaryColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(primaryColor.brighter());
                } else {
                    g2d.setColor(primaryColor);
                }
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 10, 10));
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                g2d.setColor(Color.WHITE);
                g2d.drawString(getText(), x, y);
            }
        };
        noButton.setPreferredSize(new Dimension(100, 35));
        noButton.setBorderPainted(false);
        noButton.setContentAreaFilled(false);
        noButton.setFocusPainted(false);
        noButton.setForeground(Color.WHITE);
        noButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        noButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);

        // Bileşenleri ana panele ekle
        mainPanel.add(messageLabel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Dialog'a ekle
        dialog.add(mainPanel);
        
        // ESC tuşu ile kapatma
        dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);

        // Dialog'u göster
        dialog.setVisible(true);
    }

    private JButton createModernButton(String text, ActionListener action, Color bgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(new Color(255, 255, 255, 80));
                } else if (getModel().isRollover()) {
                    g2d.setColor(new Color(255, 255, 255, 100));
                } else {
                    g2d.setColor(bgColor);
                }
                
                g2d.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
                
                FontMetrics fm = g2d.getFontMetrics();
                Rectangle2D r = fm.getStringBounds(getText(), g2d);
                int x = (getWidth() - (int) r.getWidth()) / 2;
                int y = (getHeight() - (int) r.getHeight()) / 2 + fm.getAscent();
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 14));
                g2d.drawString(getText(), x, y);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(130, 40));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(action);
        
        return button;
    }

    private void initializeMap() {
        /*mapViewer = new JXMapViewer();
        
        // OSM tile factory oluştur
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mapViewer.setTileFactory(tileFactory);

        // İstanbul'u merkez al
        GeoPosition istanbul = new GeoPosition(41.0082, 28.9784);
        mapViewer.setZoom(7);
        mapViewer.setAddressLocation(istanbul);

        // Mouse ile zoom ve pan kontrollerini ekle
        MouseInputListener mia = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mia);
        mapViewer.addMouseMotionListener(mia);
        mapViewer.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.getWheelRotation() < 0) {
                    mapViewer.setZoom(Math.max(1, mapViewer.getZoom() - 1));
                } else {
                    mapViewer.setZoom(Math.min(15, mapViewer.getZoom() + 1));
                }
            }
        });*/

        // Yükleniyor etiketi
        loadingLabel = new JLabel("Yakında gelecek", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        loadingLabel.setForeground(primaryColor);

        // Harita yerine mesaj paneli oluştur
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(Color.WHITE);
        messagePanel.add(loadingLabel, BorderLayout.CENTER);
        mapScroll = new JScrollPane(messagePanel);
    }

    private void showSimpleMap(List<Pharmacy> pharmacies) {
        /*if (pharmacies.isEmpty()) {
            // İstanbul merkezi göster
            GeoPosition istanbul = new GeoPosition(41.0082, 28.9784);
            mapViewer.setAddressLocation(istanbul);
            mapViewer.setZoom(7);
            return;
        }

        // Waypoint'leri ve painter'ları temizle
        Set<Waypoint> waypoints = new HashSet<>();
        List<Painter<JXMapViewer>> painters = new ArrayList<>();

        // Beylikdüzü koordinatları
        double beylikduzuLat = 41.0022;
        double beylikduzuLon = 28.6413;

        // Eczaneleri haritaya ekle
        for (Pharmacy pharmacy : pharmacies) {
            // Eczane konumunu Beylikdüzü merkezi etrafında rastgele dağıt
            double randomLat = beylikduzuLat + (Math.random() - 0.5) * 0.01;
            double randomLon = beylikduzuLon + (Math.random() - 0.5) * 0.01;
            
            try {
                // Waypoint oluştur
                PharmacyWaypoint waypoint = new PharmacyWaypoint(
                    new GeoPosition(randomLat, randomLon),
                    pharmacy
                );
                waypoints.add(waypoint);
            } catch (Exception ex) {
                ex.printStackTrace();
                continue;
            }
        }

        // Waypoint painter'ı güncelle
        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);
        
        // Özel waypoint renderer ekle
        waypointPainter.setRenderer(new WaypointRenderer<Waypoint>() {
            @Override
            public void paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint wp) {
                Point2D point = map.getTileFactory().geoToPixel(
                    wp.getPosition(), map.getZoom());
                
                int x = (int) point.getX() - 8;
                int y = (int) point.getY() - 8;
                
                // Kırmızı nokta çiz
                g.setColor(new Color(255, 0, 0, 180));
                g.fillOval(x, y, 16, 16);
                
                // Beyaz kenarlık ekle
                g.setColor(Color.WHITE);
                g.setStroke(new BasicStroke(2));
                g.drawOval(x, y, 16, 16);
            }
        });

        painters.add(waypointPainter);

        // Compound painter oluştur ve set et
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(painter);

        // Haritayı Beylikdüzü'ne odakla
        mapViewer.setAddressLocation(new GeoPosition(beylikduzuLat, beylikduzuLon));
        mapViewer.setZoom(14);

        // Mouse tıklama olayını ekle
        mapViewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point2D pt = e.getPoint();
                
                // En yakın eczaneyi bul ve bilgilerini göster
                waypoints.forEach(w -> {
                    Point2D wp = mapViewer.getTileFactory().geoToPixel(
                        w.getPosition(), mapViewer.getZoom());
                    
                    if (pt.distance(wp) < 15) {
                        PharmacyWaypoint pw = (PharmacyWaypoint) w;
                        Pharmacy p = pw.getPharmacy();
                        
                        // Özel tasarımlı bilgi penceresi
                        JPanel infoPanel = new JPanel();
                        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
                        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                        infoPanel.setBackground(Color.WHITE);

                        JLabel nameLabel = new JLabel("<html><b>" + p.name + "</b></html>");
                        nameLabel.setForeground(primaryColor);
                        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
                        
                        JLabel addressLabel = new JLabel("<html>Adres: " + p.address + "</html>");
                        addressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        
                        JLabel phoneLabel = new JLabel("Tel: " + p.phone);
                        phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                        
                        infoPanel.add(nameLabel);
                        infoPanel.add(Box.createVerticalStrut(5));
                        infoPanel.add(addressLabel);
                        infoPanel.add(Box.createVerticalStrut(3));
                        infoPanel.add(phoneLabel);

                        JOptionPane.showMessageDialog(
                            mapViewer,
                            infoPanel,
                            "Nöbetçi Eczane Bilgileri",
                            JOptionPane.PLAIN_MESSAGE
                        );
                    }
                });
            }
        });*/

        // "Yakında gelecek" mesajını göster
        JPanel comingSoonPanel = new JPanel(new BorderLayout());
        comingSoonPanel.setBackground(Color.WHITE);
        JLabel comingSoonLabel = new JLabel("Yakında gelecek", SwingConstants.CENTER);
        comingSoonLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        comingSoonLabel.setForeground(primaryColor);
        comingSoonPanel.add(comingSoonLabel, BorderLayout.CENTER);
        
        if (mapScroll != null) {
            mapScroll.setViewportView(comingSoonPanel);
        }
    }

    private void showLoadingMessage(String message) {
        loadingLabel.setText(message);
        
        // Yeni panel oluştur ve mesajı göster
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(Color.WHITE);
        messagePanel.add(loadingLabel, BorderLayout.CENTER);
        
        if (mapScroll != null) {
            mapScroll.setViewportView(messagePanel);
        }
    }

    // JavaScript arayüzü için iç sınıf
    public class JavascriptInterface {
        public void previousPage() {
            SwingUtilities.invokeLater(() -> HomepageGUI.this.previousPage());
        }
        
        public void nextPage() {
            SwingUtilities.invokeLater(() -> HomepageGUI.this.nextPage());
        }
    }

    private void openBlog() {
        try {
            dispose();
            BlogGUI blogGUI = new BlogGUI(hasta);
            blogGUI.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showListView(List<Pharmacy> pharmacies) {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><style>");
        html.append("body { margin: 0; padding: 5px; font-family: Arial; background: #f5f5f5; }");
        html.append(".map { background: white; border-radius: 8px; padding: 10px; }");
        html.append(".pharmacy { background: #fff; margin: 3px 0; padding: 8px; border-radius: 5px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }");
        html.append(".pharmacy h3 { margin: 0 0 3px 0; color: #065F46; font-size: 14px; }");
        html.append(".pharmacy p { margin: 2px 0; color: #333; font-size: 12px; }");
        html.append("</style></head><body>");
        html.append("<div class='map'>");

        if (pharmacies.isEmpty()) {
            html.append("<h2 style='color: #006E54; text-align: center; font-size: 13px;'>Lütfen il ve ilçe seçin</h2>");
            html.append("<p style='text-align: center; color: #666; font-size: 11px;'>Nöbetçi eczaneler burada listelenecek</p>");
            prevButton.setEnabled(false);
            nextButton.setEnabled(false);
            pageLabel.setText("0/0");
            prevButton.getParent().setVisible(false);
        } else {
            html.append("<h2 style='color: #065F46; margin-bottom: 8px; font-size: 14px;'>Nöbetçi Eczaneler</h2>");
            
            int totalPages = pharmacies.size();
            int index = currentPage - 1;
            
            Pharmacy pharmacy = pharmacies.get(index);
            html.append("<div class='pharmacy'>");
            html.append("<h3>").append(pharmacy.name).append("</h3>");
            html.append("<p><strong>Adres:</strong> ").append(pharmacy.address).append("</p>");
            html.append("<p><strong>Telefon:</strong> ").append(pharmacy.phone).append("</p>");
            html.append("<p><strong>Bölge:</strong> ").append(pharmacy.dist).append("</p>");
            html.append("</div>");
            
            prevButton.setEnabled(currentPage > 1);
            nextButton.setEnabled(currentPage < totalPages);
            pageLabel.setText(currentPage + "/" + totalPages);
            prevButton.getParent().setVisible(true);
        }

        html.append("</div></body></html>");
        listPane.setText(html.toString());
    }

    private void previousPage() {
        if (currentPage > 1) {
            currentPage--;
            showListView(pharmacies);
        }
    }

    private void nextPage() {
        int totalPages = (int) Math.ceil((double) pharmacies.size() / PHARMACIES_PER_PAGE);
        if (currentPage < totalPages) {
            currentPage++;
            showListView(pharmacies);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                Hasta hasta = new Hasta();
                HomepageGUI frame = new HomepageGUI(hasta);
                frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}