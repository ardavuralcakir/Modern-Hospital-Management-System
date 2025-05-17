package View;

import Model.*;
import dbhelper.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class BlogGUI extends JFrame {
    private JPanel wrapper;
    private JPanel pnl_left;
    private JPanel pnl_center;
    private JPanel pnl_right;
    private JPanel pnl_new_tweet;
    private JScrollPane scrl_tweets;
    private JPanel pnl_tweets;
    private JTextArea txt_tweet_content;
    private JButton btn_tweet;
    private JPanel pnl_trending;
    private JList<String> list_trending;
    private ArrayList<JPanel> tweetPanels;
    private Blog blog;
    private HashtagTrend hashtagTrend;
    private User user;
    private String userType;
    private Color primaryColor = new Color(6, 95, 70);  // Koyu yeşil
    private Color secondaryColor = new Color(240, 253, 244);  // Çok açık yeşil
   
    private Color backgroundColor = new Color(245, 248, 250);  // Yumuşak gri-mavi
    private Color cardColor = new Color(252, 252, 252);  // Yumuşak beyaz
    private Color textColor = new Color(51, 51, 51);  // Koyu gri
    private RoundedPhotoLabel profileImageLabel;
    private static final int PROFILE_IMAGE_SIZE = 100;

    // Yorum önbelleği için HashMap ekle
    private Map<Integer, ArrayList<Comment>> commentCache = new HashMap<>();

    // Yuvarlak profil fotoğrafı için özel JLabel sınıfı
    private class RoundedPhotoLabel extends JLabel {
        private java.awt.Shape shape;
        private java.awt.Image image;

        public RoundedPhotoLabel() {
            setPreferredSize(new Dimension(PROFILE_IMAGE_SIZE, PROFILE_IMAGE_SIZE));
            setMinimumSize(new Dimension(PROFILE_IMAGE_SIZE, PROFILE_IMAGE_SIZE));
            setMaximumSize(new Dimension(PROFILE_IMAGE_SIZE, PROFILE_IMAGE_SIZE));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                              java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

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
            g2.setColor(primaryColor);
            g2.draw(shape);
            g2.dispose();
        }

        public void setImage(java.awt.Image image) {
            this.image = image;
            repaint();
        }
    }

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private static final String BLOG_PANEL = "BLOG_PANEL";
    private static final String COLLECTIONS_PANEL = "COLLECTIONS_PANEL";

    public BlogGUI(Doctor doctor) {
        this.user = doctor;
        this.userType = "DOCTOR";
        initGUI();
    }

    public BlogGUI(Hasta hasta) {
        this.user = hasta;
        this.userType = "HASTA";
        initGUI();
    }

    private void initGUI() {
        try {
            UIManager.put("defaultFont", new Font("Inter", Font.PLAIN, 14));
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.blog = new Blog();
        this.hashtagTrend = new HashtagTrend();
        this.tweetPanels = new ArrayList<>();
        
        setTitle("Sağlık Blog");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        getContentPane().setBackground(backgroundColor);
        
        // Ana panel
        wrapper = new JPanel(new BorderLayout(20, 0));
        wrapper.setBorder(new EmptyBorder(20, 20, 20, 20));
        wrapper.setBackground(backgroundColor);
        
        // CardLayout için panel
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(backgroundColor);
        
        createLeftPanel();
        createBlogPanel();
        createRightPanel();
        
        // Panelleri ana panele ekleme
        wrapper.add(pnl_left, BorderLayout.WEST);
        wrapper.add(contentPanel, BorderLayout.CENTER);
        wrapper.add(pnl_right, BorderLayout.EAST);
        
        add(wrapper);
        
        // Event listeners
        setupEventListeners();
        
        // Blog panelini göster
        cardLayout.show(contentPanel, BLOG_PANEL);
        
        setVisible(true);
    }

    private void createBlogPanel() {
        pnl_center = new JPanel(new BorderLayout(0, 20));
        pnl_center.setBackground(backgroundColor);
        
        createCenterPanel();
        loadBlogList();
        
        contentPanel.add(pnl_center, BLOG_PANEL);
    }

    private void createLeftPanel() {
        pnl_left = new JPanel();
        pnl_left.setPreferredSize(new Dimension(250, 0));
        pnl_left.setLayout(new BoxLayout(pnl_left, BoxLayout.Y_AXIS));
        pnl_left.setBackground(primaryColor);
        pnl_left.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Logo Panel
        JPanel logoPanel = new JPanel();
        logoPanel.setLayout(new BoxLayout(logoPanel, BoxLayout.X_AXIS));
        logoPanel.setBackground(primaryColor);
        logoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel logoIcon = new JLabel("🏥");
        logoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        logoIcon.setForeground(Color.WHITE);
        
        JLabel logoText = new JLabel(" Sağlık Blog");
        logoText.setFont(new Font("Inter", Font.BOLD, 24));
        logoText.setForeground(Color.WHITE);
        
        logoPanel.add(logoIcon);
        logoPanel.add(logoText);

        // Profil Panel
        JPanel profilePanel = new JPanel();
        profilePanel.setLayout(new BoxLayout(profilePanel, BoxLayout.Y_AXIS));
        profilePanel.setBackground(primaryColor);
        profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        profilePanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        // Profil fotoğrafı
        profileImageLabel = new RoundedPhotoLabel();
        profileImageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadProfilePhoto();
        
        // Kullanıcı adı
        JLabel nameLabel = new JLabel(user.getName());
        nameLabel.setFont(new Font("Inter", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        // Kullanıcı tipi
        JLabel typeLabel = new JLabel(userType.equals("DOCTOR") ? "Doktor" : "Hasta");
        typeLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        typeLabel.setForeground(new Color(255, 255, 255, 200));
        typeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        profilePanel.add(profileImageLabel);
        profilePanel.add(Box.createVerticalStrut(10));
        profilePanel.add(nameLabel);
        profilePanel.add(Box.createVerticalStrut(5));
        profilePanel.add(typeLabel);

        // Menü Panel
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(primaryColor);
        menuPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        String[] menuItems = {"Ana Sayfa", "Doktorlar", "Makaleler", "Rehber", "İletişim", "Kaydedilenler"};
        String[] menuIcons = {"🏠", "👨‍⚕️", "📚", "📋", "✉️", "📑"};

        for (int i = 0; i < menuItems.length; i++) {
            JButton menuButton = new JButton(menuIcons[i] + "  " + menuItems[i]);
            menuButton.setFont(new Font("Inter", Font.BOLD, 14));
            menuButton.setForeground(Color.WHITE);
            menuButton.setBackground(primaryColor);
            menuButton.setBorderPainted(false);
            menuButton.setFocusPainted(false);
            menuButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            menuButton.setAlignmentX(Component.LEFT_ALIGNMENT);
            menuButton.setMaximumSize(new Dimension(250, 35));
            menuButton.setHorizontalAlignment(SwingConstants.LEFT);
            
            if (menuItems[i].equals("Ana Sayfa")) {
                menuButton.addActionListener(e -> {
                    dispose();
                    if (userType.equals("DOCTOR")) {
                        DoctorGUI doctorGUI = new DoctorGUI((Doctor) user);
                        doctorGUI.setVisible(true);
                    } else {
                        HomepageGUI homepageGUI = new HomepageGUI((Hasta) user);
                        homepageGUI.setVisible(true);
                    }
                });
            } else if (menuItems[i].equals("Kaydedilenler")) {
                menuButton.addActionListener(e -> {
                    showSavedCollections();
                });
            }

            menuPanel.add(menuButton);
            menuPanel.add(Box.createVerticalStrut(5));
        }

        pnl_left.add(logoPanel);
        pnl_left.add(profilePanel);
        pnl_left.add(new JSeparator(JSeparator.HORIZONTAL) {{
            setForeground(new Color(255, 255, 255, 50));
            setMaximumSize(new Dimension(220, 1));
        }});
        pnl_left.add(Box.createVerticalStrut(20));
        pnl_left.add(menuPanel);
    }

    private void createNewBlogPanel() {
        if (userType.equals("DOCTOR")) {
            pnl_new_tweet = new JPanel(new BorderLayout(0, 15));
            pnl_new_tweet.setBackground(cardColor);
            pnl_new_tweet.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 236, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)
            ));

            // Başlık
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(cardColor);
            
            JLabel titleLabel = new JLabel("Yeni Blog Paylaşımı");
            titleLabel.setFont(new Font("Inter", Font.BOLD, 18));
            titleLabel.setForeground(textColor);
            
            headerPanel.add(titleLabel, BorderLayout.WEST);

            // İçerik alanı
            JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
            contentPanel.setBackground(cardColor);

            txt_tweet_content = new JTextArea(4, 20);
            txt_tweet_content.setFont(new Font("Inter", Font.PLAIN, 14));
            txt_tweet_content.setLineWrap(true);
            txt_tweet_content.setWrapStyleWord(true);
            txt_tweet_content.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 236, 240), 1, true),
                new EmptyBorder(10, 10, 10, 10)
            ));
            txt_tweet_content.setText("Düşüncelerinizi paylaşın... (#hashtag kullanabilirsiniz)");
            txt_tweet_content.setForeground(new Color(101, 119, 134));
            txt_tweet_content.setBackground(cardColor);
            
            txt_tweet_content.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent evt) {
                    if (txt_tweet_content.getText().equals("Düşüncelerinizi paylaşın... (#hashtag kullanabilirsiniz)")) {
                        txt_tweet_content.setText("");
                        txt_tweet_content.setForeground(textColor);
                    }
                }
                public void focusLost(java.awt.event.FocusEvent evt) {
                    if (txt_tweet_content.getText().isEmpty()) {
                        txt_tweet_content.setText("Düşüncelerinizi paylaşın... (#hashtag kullanabilirsiniz)");
                        txt_tweet_content.setForeground(new Color(101, 119, 134));
                    }
                }
            });

            // Paylaş butonu
            btn_tweet = new JButton("Paylaş");
            btn_tweet.setFont(new Font("Inter", Font.BOLD, 14));
            btn_tweet.setBackground(primaryColor);
            btn_tweet.setForeground(Color.WHITE);
            btn_tweet.setBorderPainted(false);
            btn_tweet.setFocusPainted(false);
            btn_tweet.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn_tweet.setPreferredSize(new Dimension(100, 35));

            // Buton paneli
            JPanel buttonPanel = new JPanel(new BorderLayout());
            buttonPanel.setBackground(cardColor);

            // Karakter sayacı
            JLabel charCountLabel = new JLabel("0/500");
            charCountLabel.setFont(new Font("Inter", Font.PLAIN, 12));
            charCountLabel.setForeground(new Color(101, 119, 134));
            
            txt_tweet_content.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCount(); }
                public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCount(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCount(); }
                
                private void updateCount() {
                    String text = txt_tweet_content.getText();
                    if (!text.equals("Düşüncelerinizi paylaşın... (#hashtag kullanabilirsiniz)")) {
                        charCountLabel.setText(text.length() + "/500");
                        if (text.length() > 450) {
                            charCountLabel.setForeground(new Color(255, 82, 82));
                        } else {
                            charCountLabel.setForeground(new Color(101, 119, 134));
                        }
                    }
                }
            });

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            rightPanel.setBackground(cardColor);
            rightPanel.add(btn_tweet);

            buttonPanel.add(charCountLabel, BorderLayout.WEST);
            buttonPanel.add(rightPanel, BorderLayout.EAST);

            contentPanel.add(txt_tweet_content, BorderLayout.CENTER);
            contentPanel.add(buttonPanel, BorderLayout.SOUTH);

            pnl_new_tweet.add(headerPanel, BorderLayout.NORTH);
            pnl_new_tweet.add(contentPanel, BorderLayout.CENTER);
        }
    }

    private void createRightPanel() {
        pnl_right = new JPanel(new BorderLayout());
        pnl_right.setBackground(backgroundColor);
        pnl_right.setPreferredSize(new Dimension(300, 0));
        
        // Arama paneli
        JPanel searchPanel = new JPanel(new BorderLayout(0, 10));
        searchPanel.setBackground(new Color(6, 95, 70));
        searchPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 236, 230), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));

        // Arama başlığı
        JLabel searchTitle = new JLabel("Blog Ara");
        searchTitle.setFont(new Font("Inter", Font.BOLD, 18));
        searchTitle.setForeground(Color.WHITE);

        // Arama alanı ve buton için panel
        JPanel searchInputWrapper = new JPanel(new BorderLayout());
        searchInputWrapper.setBackground(Color.WHITE);
        searchInputWrapper.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(Color.WHITE, 1, true),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        // Arama ikonu
        JLabel searchIcon = new JLabel("🔍");
        searchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        searchIcon.setForeground(new Color(128, 128, 128));
        
        // Arama alanı
        JTextField searchField = new JTextField();
        searchField.setFont(new Font("Inter", Font.PLAIN, 14));
        searchField.setBorder(null);
        searchField.setBackground(Color.WHITE);
        searchField.setForeground(new Color(51, 51, 51));
        
        // Placeholder text
        searchField.setText("Blog yazılarında ara...");
        searchField.setForeground(new Color(169, 169, 169));
        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Blog yazılarında ara...")) {
                    searchField.setText("");
                    searchField.setForeground(new Color(51, 51, 51));
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Blog yazılarında ara...");
                    searchField.setForeground(new Color(169, 169, 169));
                }
            }
        });

        // Temizleme butonu
        JButton clearButton = new JButton("✕");
        clearButton.setFont(new Font("Inter", Font.PLAIN, 14));
        clearButton.setForeground(new Color(169, 169, 169));
        clearButton.setBorder(null);
        clearButton.setContentAreaFilled(false);
        clearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clearButton.setVisible(false);

        // Arama alanı değişiklik dinleyicisi
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { updateClearButton(); }
            public void removeUpdate(DocumentEvent e) { updateClearButton(); }
            public void insertUpdate(DocumentEvent e) { updateClearButton(); }

            private void updateClearButton() {
                clearButton.setVisible(!searchField.getText().isEmpty() && 
                    !searchField.getText().equals("Blog yazılarında ara..."));
            }
        });

        // Temizleme butonu işlevi
        clearButton.addActionListener(e -> {
            searchField.setText("");
            searchField.requestFocus();
            loadBlogList(); // Tüm blog yazılarını göster
        });

        // Arama işlevselliği
        searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String searchText = searchField.getText();
                if (!searchText.equals("Blog yazılarında ara...")) {
                    searchAndUpdateMainFeed(searchText);
                }
            }
        });

        // Bileşenleri panellere ekle
        JPanel searchFieldPanel = new JPanel(new BorderLayout(5, 0));
        searchFieldPanel.setBackground(Color.WHITE);
        searchFieldPanel.add(searchIcon, BorderLayout.WEST);
        searchFieldPanel.add(searchField, BorderLayout.CENTER);
        searchFieldPanel.add(clearButton, BorderLayout.EAST);

        searchInputWrapper.add(searchFieldPanel);

        searchPanel.add(searchTitle, BorderLayout.NORTH);
        searchPanel.add(searchInputWrapper, BorderLayout.CENTER);

        // Gündemdeki Konular paneli
        pnl_trending = new JPanel(new BorderLayout());
        pnl_trending.setBackground(new Color(6, 95, 70));
        pnl_trending.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 236, 230), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        // Başlık paneli
        JPanel titlePanel = new JPanel(new BorderLayout(10, 0));
        titlePanel.setBackground(new Color(6, 95, 70));
        
        JLabel trendingTitle = new JLabel("Gündemdeki Konular");
        trendingTitle.setFont(new Font("Inter", Font.BOLD, 18));
        trendingTitle.setForeground(Color.WHITE);
        
        JLabel trendingIcon = new JLabel("🔥");
        trendingIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        trendingIcon.setForeground(Color.WHITE);
        
        titlePanel.add(trendingTitle, BorderLayout.WEST);
        titlePanel.add(trendingIcon, BorderLayout.EAST);

        pnl_trending.add(titlePanel, BorderLayout.NORTH);
        
        // Hashtag listesi
        list_trending = new JList<>();
        list_trending.setFont(new Font("Inter", Font.PLAIN, 14));
        list_trending.setBorder(null);
        list_trending.setBackground(new Color(6, 95, 70));
        list_trending.setForeground(Color.WHITE);
        list_trending.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list_trending.setCursor(new Cursor(Cursor.HAND_CURSOR));
        list_trending.setFixedCellHeight(35);
        
        // Hashtag verilerini al ve modele ekle
        DefaultListModel<String> listModel = new DefaultListModel<>();
        try {
            ArrayList<HashtagTrend> trends = hashtagTrend.getTrendingHashtags();
            for (HashtagTrend trend : trends) {
                listModel.addElement("🔥 " + trend.getHashtag() + " (" + trend.getUseCount() + ")");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        list_trending.setModel(listModel);
        
        DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, 
                    int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                label.setBackground(new Color(6, 95, 70));
                label.setForeground(Color.WHITE);
                if (isSelected) {
                    label.setBackground(new Color(0, 80, 60));
                }
                return label;
            }
        };
        list_trending.setCellRenderer(renderer);
        
        // Hashtag listesi için tıklama olayı
        list_trending.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = list_trending.getSelectedValue();
                if (selected != null) {
                    // "🔥 #hashtag (5)" formatından hashtag'i çıkar
                    String hashtag = selected.split(" ")[1].replace("#", "").split("\\(")[0].trim();
                    filterBlogsByHashtag(hashtag);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(list_trending);
        scrollPane.setBorder(null);
        scrollPane.setBackground(new Color(0, 100, 80));
        
        // Filtreyi temizle butonu
        JButton btnClearFilter = new JButton("Tüm Paylaşımları Göster");
        btnClearFilter.setFont(new Font("Inter", Font.BOLD, 14));
        btnClearFilter.setBackground(primaryColor);
        btnClearFilter.setForeground(Color.white);
        btnClearFilter.setBorder(new LineBorder(primaryColor, 1, true));
        btnClearFilter.setFocusPainted(false);
        btnClearFilter.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClearFilter.addActionListener(e -> {
            list_trending.clearSelection();
            loadBlogList();
        });
        
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        filterPanel.setBackground(primaryColor);
        filterPanel.add(btnClearFilter);
        
        pnl_trending.add(scrollPane, BorderLayout.CENTER);
        pnl_trending.add(filterPanel, BorderLayout.SOUTH);

        // Panel yerleşimini güncelle
        JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
        centerPanel.setBackground(backgroundColor);
        centerPanel.add(searchPanel, BorderLayout.NORTH);
        centerPanel.add(pnl_trending, BorderLayout.CENTER);
        
        pnl_right.add(centerPanel, BorderLayout.CENTER);
    }

    private void searchAndUpdateMainFeed(String searchText) {
        try {
            pnl_tweets.removeAll();
            tweetPanels.clear();
            
            ArrayList<Blog> blogs = blog.getBlogList();
            boolean found = false;
            
            for (Blog b : blogs) {
                if (searchText.trim().isEmpty() || 
                    b.getContent().toLowerCase().contains(searchText.toLowerCase())) {
                    found = true;
                    JPanel tweetPanel = createTweetPanel(b);
                    tweetPanels.add(tweetPanel);
                    pnl_tweets.add(tweetPanel);
                    pnl_tweets.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
            
            if (!found) {
                JLabel noResultLabel = new JLabel("Sonuç bulunamadı.");
                noResultLabel.setFont(new Font("Inter", Font.PLAIN, 14));
                noResultLabel.setForeground(Color.GRAY);
                noResultLabel.setHorizontalAlignment(SwingConstants.CENTER);
                pnl_tweets.add(noResultLabel);
            }
            
            pnl_tweets.revalidate();
            pnl_tweets.repaint();
            
            // Arama sonuçlarında da scroll bar'ı en üste getir
            SwingUtilities.invokeLater(() -> {
                JScrollBar verticalBar = scrl_tweets.getVerticalScrollBar();
                verticalBar.setValue(verticalBar.getMinimum());
            });
        } catch (SQLException e) {
            Helper.showMsg("error");
        }
    }

    private void setupEventListeners() {
        // Blog paylaşım butonu event listener'ı sadece doktor için
        if (userType.equals("DOCTOR") && btn_tweet != null) {
            btn_tweet.addActionListener(e -> {
                try {
                    String content = txt_tweet_content.getText();
                    if (!content.trim().isEmpty()) {
                        blog.addBlog("", content, user.getId());
                        txt_tweet_content.setText("");
                        loadBlogList();
                        loadTrendingHashtags();
                    }
                } catch (SQLException ex) {
                    Helper.showMsg("error");
                }
            });
        }
    }

    private void filterBlogsByHashtag(String hashtag) {
        try {
            pnl_tweets.removeAll();
            tweetPanels.clear();
            
            ArrayList<Blog> blogs = blog.getBlogList();
            for (Blog b : blogs) {
                if (b.getHashtags().contains(hashtag.toLowerCase())) {
                    JPanel tweetPanel = createTweetPanel(b);
                    tweetPanels.add(tweetPanel);
                    pnl_tweets.add(tweetPanel);
                    pnl_tweets.add(Box.createRigidArea(new Dimension(0, 10)));
                }
            }
            
            if (tweetPanels.isEmpty()) {
                JLabel noResultLabel = new JLabel("Bu hashtag ile ilgili paylaşım bulunamadı.");
                noResultLabel.setFont(new Font("Inter", Font.PLAIN, 14));
                noResultLabel.setForeground(Color.GRAY);
                noResultLabel.setHorizontalAlignment(SwingConstants.CENTER);
                pnl_tweets.add(noResultLabel);
            }
            
            pnl_tweets.revalidate();
            pnl_tweets.repaint();
        } catch (SQLException e) {
            Helper.showMsg("error");
        }
    }

    private JPanel createTweetPanel(Blog blog) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 236, 240), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Sabit boyut ayarı
        panel.setPreferredSize(new Dimension(440, 200));
        panel.setMinimumSize(new Dimension(440, 200));
        panel.setMaximumSize(new Dimension(440, 200));
        
        // Üst kısım (yazar ve tarih)
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(cardColor);
        
        // Yazar bilgisi
        JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        authorPanel.setBackground(cardColor);
        
        // Yazar profil fotoğrafı
        RoundedPhotoLabel authorPhoto = new RoundedPhotoLabel();
        authorPhoto.setPreferredSize(new Dimension(40, 40));
        loadAuthorPhoto(authorPhoto, blog.getAuthor());
        
        JLabel authorLabel = new JLabel(blog.getAuthor());
        authorLabel.setFont(new Font("Inter", Font.BOLD, 14));
        authorLabel.setForeground(textColor);
        
        // Tarih
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        JLabel dateLabel = new JLabel(sdf.format(blog.getDate()));
        dateLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(101, 119, 134));
        
        // Sağ üst panel
        JPanel topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        topRightPanel.setBackground(cardColor);

        // Eğer kullanıcı doktor ve kendi paylaşımıysa silme butonu ekle
        if (userType.equals("DOCTOR") && blog.getAuthorId() == user.getId()) {
            JButton deleteButton = new JButton("🗑");
            deleteButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
            deleteButton.setBorderPainted(false);
            deleteButton.setContentAreaFilled(false);
            deleteButton.setFocusPainted(false);
            deleteButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            deleteButton.setToolTipText("Paylaşımı Sil");
            
            deleteButton.addActionListener(e -> {
                int choice = JOptionPane.showConfirmDialog(this,
                    "Bu paylaşımı silmek istediğinizden emin misiniz?",
                    "Paylaşımı Sil",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                
                if (choice == JOptionPane.YES_OPTION) {
                    try {
                        if (blog.deleteBlog(blog.getId())) {
                            loadBlogList(); // Blog listesini yenile
                            loadTrendingHashtags(); // Trend listesini güncelle
                            Helper.showMsg("success");
                        }
                    } catch (SQLException ex) {
                        Helper.showMsg("error");
                    }
                }
            });
            
            topRightPanel.add(deleteButton);
        }

        // Kaydetme butonu
        JButton saveButton = new JButton("🔖");
        saveButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        saveButton.setBorderPainted(false);
        saveButton.setContentAreaFilled(false);
        saveButton.setFocusPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.setToolTipText("Koleksiyona Kaydet");
        saveButton.addActionListener(e -> showSaveToCollectionDialog(blog));
        
        topRightPanel.add(saveButton);
        topRightPanel.add(dateLabel);
        
        authorPanel.add(authorPhoto);
        authorPanel.add(authorLabel);
        headerPanel.add(authorPanel, BorderLayout.WEST);
        headerPanel.add(topRightPanel, BorderLayout.EAST);
        
        // İçerik
        JTextArea contentArea = new JTextArea(blog.getTruncatedContent());
        contentArea.setFont(new Font("Inter", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(cardColor);
        contentArea.setForeground(textColor);
        contentArea.setBorder(null);
        
        // Alt panel (beğeni, yorum ve hashtag'ler)
        JPanel footerPanel = new JPanel(new BorderLayout(10, 10));
        footerPanel.setBackground(cardColor);
        
        // Beğeni ve yorum butonları
        JPanel interactionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        interactionPanel.setBackground(cardColor);
        
        // Beğeni butonu
        JButton likeButton = new JButton();
        JLabel likeCountLabel = new JLabel(String.valueOf(blog.getLikeCount()));
        
        try {
            boolean isLiked = blog.isLikedByUser(blog.getId(), user.getId());
            updateLikeButton(likeButton, likeCountLabel, isLiked, blog.getLikeCount());
        } catch (SQLException e) {
            updateLikeButton(likeButton, likeCountLabel, false, blog.getLikeCount());
        }

        likeButton.addActionListener(e -> {
            try {
                boolean currentlyLiked = blog.isLikedByUser(blog.getId(), user.getId());
                boolean success;
                int newCount;
                
                if (!currentlyLiked) {
                    success = blog.likeBlog(blog.getId(), user.getId());
                    newCount = blog.getLikeCount() + 1;
                } else {
                    success = blog.unlikeBlog(blog.getId(), user.getId());
                    newCount = Math.max(0, blog.getLikeCount() - 1);
                }
                
                if (success) {
                    updateLikeButton(likeButton, likeCountLabel, !currentlyLiked, newCount);
                    blog.setLikeCount(newCount);
                }
            } catch (SQLException ex) {
                Helper.showMsg("error");
            }
        });

        // Yorum butonu
        JButton commentButton = new JButton("💬");
        commentButton.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        commentButton.setBorderPainted(false);
        commentButton.setContentAreaFilled(false);
        commentButton.setFocusPainted(false);
        commentButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        commentButton.setMargin(new Insets(3, 0, 0, 0));  // Yorum ikonunu 3 piksel aşağı kaydır
        
        JLabel commentCountLabel = new JLabel(String.valueOf(blog.getCommentCount()));
        commentCountLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        commentCountLabel.setForeground(new Color(101, 119, 134));
        
        // Yorum butonuna tıklama olayı
        commentButton.addActionListener(e -> {
            showBlogDetail(blog);
        });
        
        // Panel'e tıklama olayı
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showBlogDetail(blog);
            }
        });
        
        JPanel likePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        likePanel.setBackground(cardColor);
        likePanel.add(likeButton);
        likePanel.add(likeCountLabel);
        
        JPanel commentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        commentPanel.setBackground(cardColor);
        commentPanel.add(commentButton);
        commentPanel.add(commentCountLabel);
        
        interactionPanel.add(likePanel);
        interactionPanel.add(commentPanel);
        
        // Hashtag'ler
        JPanel hashtagPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        hashtagPanel.setBackground(cardColor);
        
        for (String hashtag : blog.getHashtags()) {
            JLabel hashtagLabel = new JLabel("#" + hashtag);
            hashtagLabel.setFont(new Font("Inter", Font.BOLD, 12));
            hashtagLabel.setForeground(primaryColor);
            hashtagLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            hashtagPanel.add(hashtagLabel);
        }
        
        footerPanel.add(interactionPanel, BorderLayout.WEST);
        footerPanel.add(hashtagPanel, BorderLayout.EAST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentArea, BorderLayout.CENTER);
        panel.add(footerPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void showBlogDetail(Blog blog) {
        JDialog dialog = new JDialog(this, "Blog Detayı", true);
        dialog.setSize(800, 800);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(0, 0));
        
        // Dialog kapanınca önbelleği temizle
        dialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                commentCache.remove(blog.getId());
            }
        });
        
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // Ana panel
        JPanel mainPanel = new JPanel(new BorderLayout(0, 20));
        mainPanel.setBackground(backgroundColor);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Blog içeriği
        JPanel contentPanel = new JPanel(new BorderLayout(0, 15));
        contentPanel.setBackground(cardColor);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 236, 240), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Blog içeriği scroll panel
        JScrollPane contentScroll = new JScrollPane(contentPanel);
        contentScroll.setBorder(null);
        contentScroll.setBackground(cardColor);
        contentScroll.setPreferredSize(new Dimension(0, 300));

        // Başlık paneli
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(cardColor);
        
        // Yazar bilgisi
        JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        authorPanel.setBackground(cardColor);
        
        RoundedPhotoLabel authorPhoto = new RoundedPhotoLabel();
        authorPhoto.setPreferredSize(new Dimension(40, 40));
        loadAuthorPhoto(authorPhoto, blog.getAuthor());
        
        JLabel authorLabel = new JLabel(blog.getAuthor());
        authorLabel.setFont(new Font("Inter", Font.BOLD, 14));
        authorLabel.setForeground(textColor);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        JLabel dateLabel = new JLabel(sdf.format(blog.getDate()));
        dateLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(101, 119, 134));
        
        authorPanel.add(authorPhoto);
        authorPanel.add(authorLabel);
        headerPanel.add(authorPanel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        // Blog içeriği
        JTextArea contentArea = new JTextArea(blog.getContent());
        contentArea.setFont(new Font("Inter", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(cardColor);
        contentArea.setForeground(textColor);
        contentArea.setBorder(null);

        contentPanel.add(headerPanel, BorderLayout.NORTH);
        contentPanel.add(contentArea, BorderLayout.CENTER);

        // Yorumlar paneli - sabit yükseklik
        JPanel commentsPanel = new JPanel();
        commentsPanel.setLayout(new BoxLayout(commentsPanel, BoxLayout.Y_AXIS));
        commentsPanel.setBackground(cardColor);
        
        // Yorum scroll paneli - sabit yükseklik
        JScrollPane commentsScroll = new JScrollPane(commentsPanel);
        commentsScroll.setBorder(null);
        commentsScroll.setBackground(cardColor);
        commentsScroll.setPreferredSize(new Dimension(0, 300));

        // Özel scroll bar tasarımı
        JScrollBar verticalBar = commentsScroll.getVerticalScrollBar();
        verticalBar.setPreferredSize(new Dimension(8, 0));
        verticalBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(6, 95, 70, 100);
                this.trackColor = backgroundColor;
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });

        // Yorum ekleme paneli
        JPanel addCommentPanel = new JPanel(new BorderLayout(10, 0));
        addCommentPanel.setBackground(cardColor);
        addCommentPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        JTextField commentField = new JTextField();
        commentField.setFont(new Font("Inter", Font.PLAIN, 14));
        commentField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 236, 240), 1, true),
            new EmptyBorder(8, 10, 8, 10)
        ));

        JButton addCommentButton = new JButton("Yorum Ekle");
        addCommentButton.setFont(new Font("Inter", Font.BOLD, 14));
        addCommentButton.setBackground(primaryColor);
        addCommentButton.setForeground(Color.WHITE);
        addCommentButton.setBorderPainted(false);
        addCommentButton.setFocusPainted(false);
        addCommentButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Yorumları yükle
        loadCommentsFromCache(blog.getId(), commentsPanel);

        // Yorum ekleme işlevi
        addCommentButton.addActionListener(e -> {
            String commentText = commentField.getText().trim();
            if (!commentText.isEmpty()) {
                try {
                    if (blog.addComment(blog.getId(), user.getId(), user.getName(), commentText)) {
                        commentField.setText("");
                        commentCache.remove(blog.getId());
                        loadCommentsFromCache(blog.getId(), commentsPanel);
                        Helper.showMsg("success");
                    }
                } catch (SQLException ex) {
                    Helper.showMsg("error");
                }
            }
        });

        addCommentPanel.add(commentField, BorderLayout.CENTER);
        addCommentPanel.add(addCommentButton, BorderLayout.EAST);

        // Alt panel (yorumlar) - sabit yükseklik
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(cardColor);
        bottomPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 236, 240), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        bottomPanel.setPreferredSize(new Dimension(0, 400));

        JLabel commentsLabel = new JLabel("Yorumlar");
        commentsLabel.setFont(new Font("Inter", Font.BOLD, 16));
        commentsLabel.setForeground(textColor);

        bottomPanel.add(commentsLabel, BorderLayout.NORTH);
        bottomPanel.add(commentsScroll, BorderLayout.CENTER);
        bottomPanel.add(addCommentPanel, BorderLayout.SOUTH);

        mainPanel.add(contentScroll, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    private void loadCommentsFromCache(int blogId, JPanel commentsPanel) {
        commentsPanel.removeAll();
        
        try {
            // Önbellekte yoksa veritabanından al
            if (!commentCache.containsKey(blogId)) {
                ArrayList<Comment> comments = blog.getComments(blogId);
                commentCache.put(blogId, comments);
            }
            
            // Önbellekten yorumları göster
            ArrayList<Comment> comments = commentCache.get(blogId);
            for (Comment comment : comments) {
                JPanel commentPanel = createCommentPanel(comment);
                commentsPanel.add(commentPanel);
                commentsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        commentsPanel.revalidate();
        commentsPanel.repaint();
    }

    private void updateLikeButton(JButton likeButton, JLabel countLabel, boolean isLiked, int count) {
        likeButton.setText(isLiked ? "\u2764" : "\u2661");  // Unicode kalp sembolleri
        likeButton.setFont(new Font("Segoe UI Symbol", Font.BOLD, 20));
        likeButton.setForeground(isLiked ? new Color(6, 95, 70) : new Color(101, 119, 134));  // Yeşil renk
        countLabel.setText(String.valueOf(count));
        likeButton.setBorderPainted(false);
        likeButton.setContentAreaFilled(false);
        likeButton.setFocusPainted(false);
        likeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Beğeni butonunun boyutunu sabitle
        likeButton.setPreferredSize(new Dimension(35, 30));
        likeButton.setMinimumSize(new Dimension(35, 30));
        likeButton.setMaximumSize(new Dimension(35, 30));
        
        // Sayaç etiketinin boyutunu sabitle
        countLabel.setPreferredSize(new Dimension(30, 30));
        countLabel.setHorizontalAlignment(SwingConstants.LEFT);
    }

    private void loadAuthorPhoto(RoundedPhotoLabel photoLabel, String authorName) {
        try {
            String projectRoot = System.getProperty("user.dir");
            File profilesDir = new File(projectRoot + "/src/Images/profiles");
            
            if (profilesDir.exists()) {
                File[] authorPhotos = profilesDir.listFiles((dir, name) -> 
                    name.startsWith("profile_") && name.contains(authorName.replaceAll("\\s+", "_")));
                    
                if (authorPhotos != null && authorPhotos.length > 0) {
                    BufferedImage authorImage = ImageIO.read(authorPhotos[0]);
                    if (authorImage != null) {
                        Image scaledImage = authorImage.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                        photoLabel.setImage(scaledImage);
                        return;
                    }
                }
            }

            // Varsayılan avatar
            File defaultAvatarFile = new File(projectRoot + "/src/Images/default_avatar.png");
            if (defaultAvatarFile.exists()) {
                BufferedImage defaultAvatar = ImageIO.read(defaultAvatarFile);
                if (defaultAvatar != null) {
                    Image scaledImage = defaultAvatar.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
                    photoLabel.setImage(scaledImage);
                    return;
                }
            }

            // Varsayılan avatar da yoksa baş harfli avatar
            BufferedImage defaultImg = new BufferedImage(40, 40, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = defaultImg.createGraphics();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                               java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(200, 200, 200));
            g2d.fillOval(0, 0, 40, 40);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Inter", Font.BOLD, 20));
            String initial = authorName.substring(0, 1).toUpperCase();
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(initial, 
                (40 - fm.stringWidth(initial)) / 2,
                (40 - fm.getHeight()) / 2 + fm.getAscent());
            g2d.dispose();
            photoLabel.setImage(defaultImg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadBlogList() {
        try {
            pnl_tweets.removeAll();
            tweetPanels.clear();
            
            ArrayList<Blog> blogs = blog.getBlogList();
            for (Blog b : blogs) {
                JPanel tweetPanel = createTweetPanel(b);
                tweetPanels.add(tweetPanel);
                pnl_tweets.add(tweetPanel);
                pnl_tweets.add(Box.createRigidArea(new Dimension(0, 10)));
            }
            
            pnl_tweets.revalidate();
            pnl_tweets.repaint();
            
            // Scroll bar'ı en üste getir
            SwingUtilities.invokeLater(() -> {
                JScrollBar verticalBar = scrl_tweets.getVerticalScrollBar();
                verticalBar.setValue(verticalBar.getMinimum());
            });
        } catch (SQLException e) {
            Helper.showMsg("error");
        }
    }

    private void loadTrendingHashtags() {
        try {
            DefaultListModel<String> model = new DefaultListModel<>();
            ArrayList<HashtagTrend> trends = hashtagTrend.getTrendingHashtags();
            
            if (trends.isEmpty()) {
                model.addElement("Son 24 saatte paylaşım yok");
            } else {
                for (HashtagTrend trend : trends) {
                    model.addElement("🔥 " + trend.getHashtag() + " (" + trend.getUseCount() + ")");
                }
            }
            
            list_trending.setModel(model);
        } catch (SQLException e) {
            Helper.showMsg("error");
        }
    }

    private void createCenterPanel() {
        pnl_center = new JPanel(new BorderLayout(0, 20));
        pnl_center.setBackground(backgroundColor);
        
        // Yeni blog paylaşım paneli - sadece doktorlar için
        if (userType.equals("DOCTOR")) {
            JPanel newPostPanel = new JPanel(new BorderLayout(0, 15));
            newPostPanel.setBackground(cardColor);
            newPostPanel.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 236, 240), 1, true),
                new EmptyBorder(20, 20, 20, 20)
            ));

            // Başlık
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(cardColor);
            
            JLabel titleLabel = new JLabel("Yeni Blog Paylaşımı");
            titleLabel.setFont(new Font("Inter", Font.BOLD, 18));
            titleLabel.setForeground(textColor);
            
            headerPanel.add(titleLabel, BorderLayout.WEST);

            // İçerik alanı
            JPanel contentPanel = new JPanel(new BorderLayout(0, 10));
            contentPanel.setBackground(cardColor);

            txt_tweet_content = new JTextArea(4, 20);
            txt_tweet_content.setFont(new Font("Inter", Font.PLAIN, 14));
            txt_tweet_content.setLineWrap(true);
            txt_tweet_content.setWrapStyleWord(true);
            txt_tweet_content.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(230, 236, 240), 1, true),
                new EmptyBorder(10, 10, 10, 10)
            ));
            txt_tweet_content.setText("Düşüncelerinizi paylaşın... (#hashtag kullanabilirsiniz)");
            txt_tweet_content.setForeground(new Color(101, 119, 134));
            txt_tweet_content.setBackground(cardColor);
            
            txt_tweet_content.addFocusListener(new java.awt.event.FocusAdapter() {
                public void focusGained(java.awt.event.FocusEvent evt) {
                    if (txt_tweet_content.getText().equals("Düşüncelerinizi paylaşın... (#hashtag kullanabilirsiniz)")) {
                        txt_tweet_content.setText("");
                        txt_tweet_content.setForeground(textColor);
                    }
                }
                public void focusLost(java.awt.event.FocusEvent evt) {
                    if (txt_tweet_content.getText().isEmpty()) {
                        txt_tweet_content.setText("Düşüncelerinizi paylaşın... (#hashtag kullanabilirsiniz)");
                        txt_tweet_content.setForeground(new Color(101, 119, 134));
                    }
                }
            });

            // Paylaş butonu
            btn_tweet = new JButton("Paylaş");
            btn_tweet.setFont(new Font("Inter", Font.BOLD, 14));
            btn_tweet.setBackground(primaryColor);
            btn_tweet.setForeground(Color.WHITE);
            btn_tweet.setBorderPainted(false);
            btn_tweet.setFocusPainted(false);
            btn_tweet.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn_tweet.setPreferredSize(new Dimension(100, 35));

            // Buton paneli
            JPanel buttonPanel = new JPanel(new BorderLayout());
            buttonPanel.setBackground(cardColor);

            // Karakter sayacı
            JLabel charCountLabel = new JLabel("0/450");
            charCountLabel.setFont(new Font("Inter", Font.PLAIN, 12));
            charCountLabel.setForeground(new Color(101, 119, 134));
            
            txt_tweet_content.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void changedUpdate(javax.swing.event.DocumentEvent e) { updateCount(); }
                public void insertUpdate(javax.swing.event.DocumentEvent e) { updateCount(); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { updateCount(); }
                
                private void updateCount() {
                    String text = txt_tweet_content.getText();
                    if (!text.equals("Düşüncelerinizi paylaşın... (#hashtag kullanabilirsiniz)")) {
                        charCountLabel.setText(text.length() + "/450");
                        if (text.length() >450 ) {
                            charCountLabel.setForeground(new Color(255, 82, 82));
                        } else {
                            charCountLabel.setForeground(new Color(101, 119, 134));
                        }
                    }
                }
            });

            JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            rightPanel.setBackground(cardColor);
            rightPanel.add(btn_tweet);

            buttonPanel.add(charCountLabel, BorderLayout.WEST);
            buttonPanel.add(rightPanel, BorderLayout.EAST);

            contentPanel.add(txt_tweet_content, BorderLayout.CENTER);
            contentPanel.add(buttonPanel, BorderLayout.SOUTH);

            newPostPanel.add(headerPanel, BorderLayout.NORTH);
            newPostPanel.add(contentPanel, BorderLayout.CENTER);
            
            pnl_center.add(newPostPanel, BorderLayout.NORTH);
        }
        
        // Blog listesi
        pnl_tweets = new JPanel();
        pnl_tweets.setLayout(new BoxLayout(pnl_tweets, BoxLayout.Y_AXIS));
        pnl_tweets.setBackground(backgroundColor);
        
        // Kaydırma çubuğu
        scrl_tweets = new JScrollPane(pnl_tweets);
        scrl_tweets.setBorder(null);
        scrl_tweets.getVerticalScrollBar().setUnitIncrement(16);
        scrl_tweets.setBackground(backgroundColor);
        
        // Özel kaydırma çubuğu tasarımı
        JScrollBar verticalBar = scrl_tweets.getVerticalScrollBar();
        verticalBar.setPreferredSize(new Dimension(8, 0));
        verticalBar.setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(6, 95, 70, 100);
                this.trackColor = backgroundColor;
            }
            
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
        
        // Başlık paneli
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(primaryColor);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 236, 230), 1, true),
            new EmptyBorder(15, 20, 15, 20)
        ));
        
        JLabel titleLabel = new JLabel("Blog Paylaşımları");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel feedIcon = new JLabel("📰");
        feedIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        feedIcon.setForeground(Color.WHITE);
        
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(feedIcon, BorderLayout.EAST);

        // Blog listesi paneli
        JPanel listPanel = new JPanel(new BorderLayout(0, 15));
        listPanel.setBackground(backgroundColor);
        listPanel.setBorder(new EmptyBorder(0, 0, 20, 0));
        
        listPanel.add(headerPanel, BorderLayout.NORTH);
        listPanel.add(scrl_tweets, BorderLayout.CENTER);
        
        pnl_center.add(listPanel, BorderLayout.CENTER);
    }

    private JPanel createTweetPanel(String author, String content, String date, int likeCount, int commentCount) {
        JPanel tweetPanel = new JPanel();
        tweetPanel.setLayout(new BorderLayout(10, 5));
        tweetPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(5, 5, 5, 5),
            new LineBorder(new Color(200, 200, 200), 1, true)
        ));
        tweetPanel.setBackground(Color.WHITE);

        // Üst panel (yazar ve tarih)
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        // Yazar bilgisi
        JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        authorPanel.setBackground(Color.WHITE);
        
        JLabel doctorIcon = new JLabel(new ImageIcon("src/Images/doctor_icon.png"));
        doctorIcon.setPreferredSize(new Dimension(24, 24));
        
        JLabel authorLabel = new JLabel("Dr. " + author);
        authorLabel.setFont(new Font("Inter", Font.BOLD, 14));
        
        authorPanel.add(doctorIcon);
        authorPanel.add(authorLabel);
        
        // Tarih
        JLabel dateLabel = new JLabel(date);
        dateLabel.setForeground(Color.GRAY);
        dateLabel.setBorder(new EmptyBorder(0, 0, 0, 10));
        
        headerPanel.add(authorPanel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        // İçerik
        JTextArea contentArea = new JTextArea(content);
        contentArea.setFont(new Font("Inter", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(Color.WHITE);
        contentArea.setForeground(textColor);
        contentArea.setBorder(null);
        
        // Alt panel (beğeni ve yorum)
        JPanel footerPanel = new JPanel(new BorderLayout(10, 10));
        footerPanel.setBackground(cardColor);
        
        // Beğeni ve yorum butonları
        JPanel interactionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        interactionPanel.setBackground(cardColor);
        
        // Beğeni sayısı
        JPanel likePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        likePanel.setBackground(cardColor);
        JLabel likeIcon = new JLabel("❤");
        likeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        JLabel likeCountLabel = new JLabel(String.valueOf(likeCount));
        likeCountLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        likeCountLabel.setForeground(new Color(101, 119, 134));
        likePanel.add(likeIcon);
        likePanel.add(likeCountLabel);
        
        // Yorum sayısı
        JPanel commentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        commentPanel.setBackground(cardColor);
        JLabel commentIcon = new JLabel("💬");
        commentIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        JLabel commentCountLabel = new JLabel(String.valueOf(commentCount));
        commentCountLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        commentCountLabel.setForeground(new Color(101, 119, 134));
        commentPanel.add(commentIcon);
        commentPanel.add(commentCountLabel);
        
        interactionPanel.add(likePanel);
        interactionPanel.add(commentPanel);
        
        footerPanel.add(interactionPanel, BorderLayout.WEST);
        
        tweetPanel.add(headerPanel, BorderLayout.NORTH);
        tweetPanel.add(contentArea, BorderLayout.CENTER);
        tweetPanel.add(footerPanel, BorderLayout.SOUTH);
        
        return tweetPanel;
    }

    private void loadProfilePhoto() {
        try {
            String projectRoot = System.getProperty("user.dir");
            File profilesDir = new File(projectRoot + "/src/Images/profiles");
            String tcno = user.getTcno();
            
            // Önce kullanıcının profil fotoğrafını kontrol et
            if (profilesDir.exists()) {
                File[] userPhotos = profilesDir.listFiles((dir, name) -> 
                    name.startsWith("profile_" + tcno + "."));
                    
                if (userPhotos != null && userPhotos.length > 0) {
                    BufferedImage userImage = ImageIO.read(userPhotos[0]);
                    if (userImage != null) {
                        Image scaledImage = userImage.getScaledInstance(
                            PROFILE_IMAGE_SIZE, PROFILE_IMAGE_SIZE, Image.SCALE_SMOOTH);
                        profileImageLabel.setImage(scaledImage);
                        return;
                    }
                }
            }

            // Kullanıcı fotoğrafı yoksa varsayılan avatarı kullan
            File defaultAvatarFile = new File(projectRoot + "/src/Images/default_avatar.png");
            if (defaultAvatarFile.exists()) {
                BufferedImage defaultAvatar = ImageIO.read(defaultAvatarFile);
                if (defaultAvatar != null) {
                    Image scaledImage = defaultAvatar.getScaledInstance(
                        PROFILE_IMAGE_SIZE, PROFILE_IMAGE_SIZE, Image.SCALE_SMOOTH);
                    profileImageLabel.setImage(scaledImage);
                    return;
                }
            }

            // Varsayılan avatar da bulunamazsa, baş harfli avatar oluştur
            BufferedImage defaultImg = new BufferedImage(40, 40, 
                BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = defaultImg.createGraphics();
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, 
                               java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(new Color(200, 200, 200));
            g2d.fillOval(0, 0, 40, 40);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Inter", Font.BOLD, 20));
            String initial = user.getName().substring(0, 1).toUpperCase();
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(initial, 
                (40 - fm.stringWidth(initial)) / 2,
                (40 - fm.getHeight()) / 2 + fm.getAscent());
            g2d.dispose();
            profileImageLabel.setImage(defaultImg);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JPanel createCommentPanel(Comment comment) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(cardColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 236, 240), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));

        // Üst kısım (kullanıcı adı ve tarih)
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(cardColor);

        JLabel userLabel = new JLabel(comment.getUserName());
        userLabel.setFont(new Font("Inter", Font.BOLD, 14));
        userLabel.setForeground(textColor);

        JLabel dateLabel = new JLabel(comment.getDate());
        dateLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(101, 119, 134));

        headerPanel.add(userLabel, BorderLayout.WEST);
        headerPanel.add(dateLabel, BorderLayout.EAST);

        // Yorum içeriği
        JTextArea contentArea = new JTextArea(comment.getContent());
        contentArea.setFont(new Font("Inter", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(cardColor);
        contentArea.setForeground(textColor);
        contentArea.setBorder(null);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentArea, BorderLayout.CENTER);

        return panel;
    }

    private void showSavedCollections() {
        JPanel collectionsPanel = new JPanel(new BorderLayout());
        collectionsPanel.setBackground(backgroundColor);

        // Üst panel - Başlık ve Yeni Koleksiyon Butonu
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(backgroundColor);
        topPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("Koleksiyonlarım");
        titleLabel.setFont(new Font("Inter", Font.BOLD, 20));
        titleLabel.setForeground(textColor);

        JButton addCollectionBtn = new JButton("+ Koleksiyon");
        addCollectionBtn.setFont(new Font("Inter", Font.BOLD, 14));
        addCollectionBtn.setBackground(primaryColor);
        addCollectionBtn.setForeground(Color.WHITE);
        addCollectionBtn.setBorderPainted(false);
        addCollectionBtn.setFocusPainted(false);
        addCollectionBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Geri dönüş butonu
        JButton backButton = new JButton("← Blog Akışı");
        backButton.setFont(new Font("Inter", Font.BOLD, 14));
        backButton.setForeground(primaryColor);
        backButton.setBackground(backgroundColor);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Buton işlevini ekle
        backButton.addActionListener(e -> {
            cardLayout.show(contentPanel, BLOG_PANEL);
        });

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(backgroundColor);
        leftPanel.add(backButton);
        leftPanel.add(titleLabel);

        topPanel.add(leftPanel, BorderLayout.WEST);
        topPanel.add(addCollectionBtn, BorderLayout.EAST);

        // Koleksiyonlar Listesi
        JPanel collectionsListPanel = new JPanel();
        collectionsListPanel.setLayout(new BoxLayout(collectionsListPanel, BoxLayout.Y_AXIS));
        collectionsListPanel.setBackground(backgroundColor);

        JScrollPane scrollPane = new JScrollPane(collectionsListPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Koleksiyonları yükle
        SavedCollection savedCollection = new SavedCollection();
        try {
            ArrayList<SavedCollection> collections = savedCollection.getUserCollections(user.getId());
            for (SavedCollection collection : collections) {
                JPanel collectionPanel = createCollectionPanel(collection);
                collectionsListPanel.add(collectionPanel);
                collectionsListPanel.add(Box.createVerticalStrut(10));
            }
        } catch (SQLException e) {
            Helper.showMsg("error");
        }

        // Yeni Koleksiyon Ekleme
        addCollectionBtn.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, 
                "Koleksiyon adını giriniz:", 
                "Yeni Koleksiyon", 
                JOptionPane.PLAIN_MESSAGE);
            
            if (name != null && !name.trim().isEmpty()) {
                try {
                    if (savedCollection.createCollection(user.getId(), name.trim())) {
                        // Koleksiyonlar listesini yenile
                        collectionsListPanel.removeAll();
                        ArrayList<SavedCollection> collections = savedCollection.getUserCollections(user.getId());
                        for (SavedCollection collection : collections) {
                            JPanel collectionPanel = createCollectionPanel(collection);
                            collectionsListPanel.add(collectionPanel);
                            collectionsListPanel.add(Box.createVerticalStrut(10));
                        }
                        collectionsListPanel.revalidate();
                        collectionsListPanel.repaint();
                        Helper.showMsg("success");
                    }
                } catch (SQLException ex) {
                    Helper.showMsg("error");
                }
            }
        });

        collectionsPanel.add(topPanel, BorderLayout.NORTH);
        collectionsPanel.add(scrollPane, BorderLayout.CENTER);

        // Koleksiyonlar panelini ekle
        contentPanel.add(collectionsPanel, COLLECTIONS_PANEL);
        cardLayout.show(contentPanel, COLLECTIONS_PANEL);
    }

    private void showCollectionContent(SavedCollection collection) {
        // Yeni panel oluştur
        JPanel collectionContentPanel = new JPanel(new BorderLayout());
        collectionContentPanel.setBackground(backgroundColor);

        // Üst panel
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBackground(backgroundColor);
        topPanel.setBorder(new EmptyBorder(10, 15, 10, 15));

        // Geri dönüş butonu ve başlık
        JButton backButton = new JButton("← Koleksiyonlar");
        backButton.setFont(new Font("Inter", Font.BOLD, 14));
        backButton.setForeground(primaryColor);
        backButton.setBackground(backgroundColor);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel titleLabel = new JLabel(collection.getCollectionName());
        titleLabel.setFont(new Font("Inter", Font.BOLD, 20));
        titleLabel.setForeground(textColor);

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(backgroundColor);
        leftPanel.add(backButton);
        leftPanel.add(titleLabel);

        topPanel.add(leftPanel, BorderLayout.WEST);

        // Blog listesi
        JPanel blogsPanel = new JPanel();
        blogsPanel.setLayout(new BoxLayout(blogsPanel, BoxLayout.Y_AXIS));
        blogsPanel.setBackground(backgroundColor);
        blogsPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JScrollPane scrollPane = new JScrollPane(blogsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        try {
            ArrayList<Blog> blogs = collection.getCollectionBlogs(collection.getId());
            if (blogs.isEmpty()) {
                JLabel emptyLabel = new JLabel("Bu koleksiyonda henüz blog yazısı yok.");
                emptyLabel.setFont(new Font("Inter", Font.PLAIN, 14));
                emptyLabel.setForeground(Color.GRAY);
                emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                blogsPanel.add(Box.createVerticalGlue());
                blogsPanel.add(emptyLabel);
                blogsPanel.add(Box.createVerticalGlue());
            } else {
                for (Blog blog : blogs) {
                    JPanel blogPanel = createCollectionBlogPanel(blog, collection);
                    blogsPanel.add(blogPanel);
                    blogsPanel.add(Box.createVerticalStrut(10));
                }
            }
        } catch (SQLException e) {
            Helper.showMsg("error");
        }

        // Geri dönüş butonu işlevi
        backButton.addActionListener(e -> {
            cardLayout.show(contentPanel, COLLECTIONS_PANEL);
        });

        collectionContentPanel.add(topPanel, BorderLayout.NORTH);
        collectionContentPanel.add(scrollPane, BorderLayout.CENTER);

        // Koleksiyon içeriği panelini ekle
        contentPanel.add(collectionContentPanel, "COLLECTION_CONTENT");
        cardLayout.show(contentPanel, "COLLECTION_CONTENT");
    }

    private JPanel createCollectionBlogPanel(Blog blog, SavedCollection collection) {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 236, 240), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));
        
        // Sabit boyut ayarı
        panel.setPreferredSize(new Dimension(440, 200));
        panel.setMinimumSize(new Dimension(440, 200));
        panel.setMaximumSize(new Dimension(440, 200));
        
        // Üst kısım (yazar ve tarih)
        JPanel headerPanel = new JPanel(new BorderLayout(10, 0));
        headerPanel.setBackground(cardColor);
        
        // Yazar bilgisi
        JPanel authorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        authorPanel.setBackground(cardColor);
        
        RoundedPhotoLabel authorPhoto = new RoundedPhotoLabel();
        authorPhoto.setPreferredSize(new Dimension(40, 40));
        loadAuthorPhoto(authorPhoto, blog.getAuthor());
        
        JLabel authorLabel = new JLabel(blog.getAuthor());
        authorLabel.setFont(new Font("Inter", Font.BOLD, 14));
        authorLabel.setForeground(textColor);
        
        // Tarih
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        JLabel dateLabel = new JLabel(sdf.format(blog.getDate()));
        dateLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        dateLabel.setForeground(new Color(101, 119, 134));

        // Koleksiyondan Kaldır butonu
        JButton removeButton = new JButton("−");  // Unicode minus sign
        removeButton.setFont(new Font("Inter", Font.BOLD, 24));
        removeButton.setForeground(new Color(255, 59, 48));  // Kırmızı renk
        removeButton.setBackground(Color.WHITE);
        removeButton.setBorderPainted(false);
        removeButton.setFocusPainted(false);
        removeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        removeButton.setToolTipText("Koleksiyondan Kaldır");
        removeButton.addActionListener(e -> {
            try {
                if (collection.removeBlogFromCollection(collection.getId(), blog.getId(), user.getId())) {
                    Container parent = panel.getParent();
                    if (parent != null) {
                        parent.remove(panel);
                        parent.revalidate();
                        parent.repaint();
                    }
                    Helper.showMsg("Blog koleksiyondan kaldırıldı.");
                }
            } catch (SQLException ex) {
                Helper.showMsg("error");
            }
        });
        
        authorPanel.add(authorPhoto);
        authorPanel.add(authorLabel);
        headerPanel.add(authorPanel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(cardColor);
        rightPanel.add(removeButton);
        rightPanel.add(dateLabel);
        headerPanel.add(rightPanel, BorderLayout.EAST);
        
        // İçerik
        JTextArea contentArea = new JTextArea(blog.getTruncatedContent());
        contentArea.setFont(new Font("Inter", Font.PLAIN, 14));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);
        contentArea.setEditable(false);
        contentArea.setBackground(cardColor);
        contentArea.setForeground(textColor);
        contentArea.setBorder(null);
        
        // Alt panel (beğeni ve yorum)
        JPanel footerPanel = new JPanel(new BorderLayout(10, 10));
        footerPanel.setBackground(cardColor);
        
        // Beğeni ve yorum butonları
        JPanel interactionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        interactionPanel.setBackground(cardColor);
        
        // Beğeni sayısı
        JPanel likePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        likePanel.setBackground(cardColor);
        JLabel likeIcon = new JLabel("❤");
        likeIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        JLabel likeCountLabel = new JLabel(String.valueOf(blog.getLikeCount()));
        likeCountLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        likeCountLabel.setForeground(new Color(101, 119, 134));
        likePanel.add(likeIcon);
        likePanel.add(likeCountLabel);
        
        // Yorum sayısı
        JPanel commentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        commentPanel.setBackground(cardColor);
        JLabel commentIcon = new JLabel("💬");
        commentIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        JLabel commentCountLabel = new JLabel(String.valueOf(blog.getCommentCount()));
        commentCountLabel.setFont(new Font("Inter", Font.PLAIN, 14));
        commentCountLabel.setForeground(new Color(101, 119, 134));
        commentPanel.add(commentIcon);
        commentPanel.add(commentCountLabel);
        
        interactionPanel.add(likePanel);
        interactionPanel.add(commentPanel);
        
        footerPanel.add(interactionPanel, BorderLayout.WEST);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentArea, BorderLayout.CENTER);
        panel.add(footerPanel, BorderLayout.SOUTH);
        
        return panel;
    }

    private void showSaveToCollectionDialog(Blog blog) {
        SavedCollection savedCollection = new SavedCollection();
        try {
            ArrayList<SavedCollection> collections = savedCollection.getUserCollections(user.getId());
            
            if (collections.isEmpty()) {
                int choice = JOptionPane.showConfirmDialog(this,
                    "Henüz bir koleksiyonunuz yok. Yeni bir koleksiyon oluşturmak ister misiniz?",
                    "Koleksiyon Oluştur",
                    JOptionPane.YES_NO_OPTION);
                
                if (choice == JOptionPane.YES_OPTION) {
                    String name = JOptionPane.showInputDialog(this,
                        "Koleksiyon adını giriniz:",
                        "Yeni Koleksiyon",
                        JOptionPane.PLAIN_MESSAGE);
                    
                    if (name != null && !name.trim().isEmpty()) {
                        if (savedCollection.createCollection(user.getId(), name.trim())) {
                            collections = savedCollection.getUserCollections(user.getId());
                        }
                    }
                }
            }
            
            if (!collections.isEmpty()) {
                String[] collectionNames = collections.stream()
                    .map(SavedCollection::getCollectionName)
                    .toArray(String[]::new);

                String selected = (String) JOptionPane.showInputDialog(this,
                    "Blog yazısını kaydetmek istediğiniz koleksiyonu seçin:",
                    "Koleksiyona Kaydet",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    collectionNames,
                    collectionNames[0]);

                if (selected != null) {
                    SavedCollection selectedCollection = collections.stream()
                        .filter(c -> c.getCollectionName().equals(selected))
                        .findFirst()
                        .orElse(null);

                    if (selectedCollection != null) {
                        if (savedCollection.saveBlogToCollection(selectedCollection.getId(), blog.getId(), user.getId())) {
                            JOptionPane.showMessageDialog(this,
                                "Blog yazısı koleksiyona kaydedildi.",
                                "Başarılı",
                                JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Bir hata oluştu.",
                "Hata",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createCollectionPanel(SavedCollection collection) {
        JPanel panel = new JPanel(new BorderLayout(10, 5));
        panel.setBackground(cardColor);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 236, 240), 1, true),
            new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Sol taraf - İkon ve İsim
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 1));
        leftPanel.setBackground(cardColor);

        JLabel folderIcon = new JLabel("📁");
        folderIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        folderIcon.setBorder(new EmptyBorder(1, 0, 0, 0));

        JLabel nameLabel = new JLabel(collection.getCollectionName());
        nameLabel.setFont(new Font("Inter", Font.BOLD, 16));
        nameLabel.setForeground(textColor);

        leftPanel.add(folderIcon);
        leftPanel.add(nameLabel);

        // Sağ taraf - Silme Butonu
        JButton deleteBtn = new JButton("🗑");
        deleteBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        deleteBtn.setBorderPainted(false);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.setFocusPainted(false);
        deleteBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        deleteBtn.setToolTipText("Koleksiyonu Sil");

        // Silme butonu için MouseListener ekle
        deleteBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                e.consume(); // Tıklama olayının panele geçmesini engelle
                int choice = JOptionPane.showConfirmDialog(panel,
                    "Bu koleksiyonu silmek istediğinizden emin misiniz?",
                    "Koleksiyon Sil",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

                if (choice == JOptionPane.YES_OPTION) {
                    try {
                        if (collection.deleteCollection(collection.getId(), user.getId())) {
                            Container parent = panel.getParent();
                            if (parent != null) {
                                parent.remove(panel);
                                parent.revalidate();
                                parent.repaint();
                            }
                            Helper.showMsg("success");
                        }
                    } catch (SQLException ex) {
                        Helper.showMsg("error");
                    }
                }
            }
        });

        // Panel için MouseListener ekle
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showCollectionContent(collection);
            }
        });

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(deleteBtn, BorderLayout.EAST);

        return panel;
    }
} 