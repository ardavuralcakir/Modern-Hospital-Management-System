package View;

import Model.Hasta;
import Model.BlogComment;
import dbhelper.Helper;
import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.DefaultTableCellRenderer;

public class CommentsGUI extends JFrame {
    private JPanel w_pane;
    private Color primaryColor = new Color(6, 95, 70);
    private Color borderColor = new Color(209, 213, 219);
    private JTextArea commentTextArea;
    private DefaultTableModel commentModel;
    private JTable table_comments;
    private BlogComment blogComment = new BlogComment();
    private Hasta hasta;
    private int postId;
    public CommentsGUI(Hasta hasta, int postId, String postContent) {
        this.hasta = hasta;
        this.postId = postId;
        // Frame ayarları
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setTitle("Yorumlar");
        setResizable(false);

        // Ana panel
        w_pane = new JPanel();
        w_pane.setBackground(Color.WHITE);
        w_pane.setLayout(null);
        setContentPane(w_pane);

        // Post içeriği
        JTextArea postArea = new JTextArea(postContent);
        postArea.setFont(new Font("Inter", Font.PLAIN, 14));
        postArea.setLineWrap(true);
        postArea.setWrapStyleWord(true);
        postArea.setEditable(false);
        postArea.setBackground(new Color(240, 253, 244));
        JScrollPane postScroll = new JScrollPane(postArea);
        postScroll.setBounds(20, 20, 560, 80);
        w_pane.add(postScroll);

        // Yorum yazma alanı
        commentTextArea = new JTextArea();
        commentTextArea.setFont(new Font("Inter", Font.PLAIN, 14));
        commentTextArea.setLineWrap(true);
        commentTextArea.setWrapStyleWord(true);
        JScrollPane commentScroll = new JScrollPane(commentTextArea);
        commentScroll.setBounds(20, 120, 460, 60);
        w_pane.add(commentScroll);

        // Yorum gönder butonu
        JButton sendButton = createStyledButton("Gönder", 490, 120, 90, 60);
        sendButton.addActionListener(e -> {
            try {
                addComment();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        w_pane.add(sendButton);

        // Yorum listesi
        commentModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        commentModel.setColumnIdentifiers(new Object[]{"Kullanıcı", "Yorum", "Tarih"});
        table_comments = new JTable(commentModel);
        customizeTable(table_comments);

        JScrollPane scrollPane = new JScrollPane(table_comments);
        scrollPane.setBounds(20, 200, 560, 250);
        customizeScrollPane(scrollPane);
        w_pane.add(scrollPane);

        // Yorumları yükle
        try {
            updateComments();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void addComment() throws SQLException {
        String commentContent = commentTextArea.getText().trim();
        if (!commentContent.isEmpty()) {
            if (blogComment.addComment(postId, hasta.getId(), hasta.getName(), commentContent)) {
                Helper.showMsg("success");
                commentTextArea.setText("");
                updateComments();
            }
        }
    }

    private void updateComments() throws SQLException {
        // Tabloyu temizle
        commentModel.setRowCount(0);
        
        // Yorumları yükle
        for (BlogComment comment : blogComment.getComments(postId)) {
            Object[] row = new Object[]{
                comment.getUserName(),
                comment.getContent(),
                comment.getCreatedAt()
            };
            commentModel.addRow(row);
        }
    }

    private void customizeTable(JTable table) {
        table.setFont(new Font("Inter", Font.PLAIN, 14));
        table.setRowHeight(60);
        table.setSelectionBackground(new Color(240, 253, 244));
        table.setSelectionForeground(Color.BLACK);
        table.setShowGrid(true);
        table.setGridColor(borderColor);
        
        // Sütun genişliklerini ayarla
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Kullanıcı
        table.getColumnModel().getColumn(1).setPreferredWidth(360); // Yorum
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Tarih
        
        // Yorum sütunu için hücre düzenleyici
        table.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JTextArea textArea = new JTextArea(value.toString());
                textArea.setWrapStyleWord(true);
                textArea.setLineWrap(true);
                textArea.setFont(table.getFont());
                textArea.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                textArea.setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                return textArea;
            }
        });
        
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Inter", Font.BOLD, 14));
        header.setBackground(new Color(243, 244, 246));
        header.setForeground(primaryColor);
        header.setPreferredSize(new Dimension(0, 35));
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