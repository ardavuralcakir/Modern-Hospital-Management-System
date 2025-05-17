package View;

import Model.Appointment;
import Model.Hasta;
import dbhelper.Helper;
import java.awt.*;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class RandevularımGUI extends JFrame {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel w_pane;
    private JTable table_appoint;
    private DefaultTableModel appointModel;
    private Object[] appointData = null;
    private Appointment appoint = new Appointment();
    private Hasta hasta;
    private JPopupMenu appointmentMenu;

    // Tasarım renkleri ve yazı fontları
    private final Color primaryColor = new Color(6, 95, 70);
    private final Color secondaryColor = new Color(240, 253, 244);
    private final Color accentColor = new Color(4, 120, 87);
    private final Color lightGray = new Color(243, 244, 246);
    private final Font titleFont = new Font("Inter", Font.BOLD, 24);
    private final Font subtitleFont = new Font("Inter", Font.BOLD, 16);
    private final Font tableFont = new Font("Inter", Font.PLAIN, 14);

    public RandevularımGUI(Hasta hasta) throws SQLException {
        this.hasta = hasta;

        // Frame ayarları
        setTitle("Randevularım");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setResizable(false);

        // Ana panel
        w_pane = new JPanel();
        w_pane.setLayout(null);
        w_pane.setBackground(Color.WHITE);
        setContentPane(w_pane);

        // Üst panel
        JPanel topPanel = new JPanel();
        topPanel.setBackground(primaryColor);
        topPanel.setBounds(0, 0, 900, 80);
        topPanel.setLayout(null);
        w_pane.add(topPanel);

        // Başlık
        JLabel title = new JLabel("Randevularım");
        title.setFont(titleFont);
        title.setForeground(Color.WHITE);
        title.setBounds(30, 20, 300, 40);
        topPanel.add(title);

        // Geri butonu
        JButton btnBack = createStyledButton("Ana Sayfa", 750, 25, 100, 35);
        btnBack.addActionListener(e -> {
            dispose();
            HomepageGUI homePage = new HomepageGUI(hasta);
            homePage.setVisible(true);
        });
        topPanel.add(btnBack);

        // Bilgi paneli
        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(secondaryColor);
        infoPanel.setBounds(30, 100, 840, 60);
        infoPanel.setLayout(null);
        infoPanel.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)));
        w_pane.add(infoPanel);

        // Bilgi metni
        JLabel infoText = new JLabel("Randevularınızı görüntüleyebilir ve iptal edebilirsiniz.");
        infoText.setFont(subtitleFont);
        infoText.setForeground(primaryColor);
        infoText.setBounds(20, 20, 800, 20);
        infoPanel.add(infoText);

        // Model initialization
        appointModel = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appointModel.setColumnIdentifiers(new Object[]{"ID", "Doktor", "Tarih"});

        // Randevu Tablosu
        table_appoint = new JTable(appointModel);
        customizeTable(table_appoint);
        
        JScrollPane scrollPane = new JScrollPane(table_appoint);
        scrollPane.setBounds(30, 180, 840, 300);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(209, 213, 219)));
        w_pane.add(scrollPane);

        // İptal butonu
        JButton btnCancel = new JButton("Seçili Randevuyu İptal Et");
        styleButton(btnCancel);
        btnCancel.setBounds(30, 500, 300, 45);
        btnCancel.setFont(new Font("Inter", Font.BOLD, 16));
        btnCancel.addActionListener(e -> deleteSelectedAppointment());
        w_pane.add(btnCancel);

        // Popup menü oluştur
        appointmentMenu = new JPopupMenu();
        JMenuItem cancelItem = new JMenuItem("Randevu İptal Et");
        cancelItem.setFont(new Font("Inter", Font.PLAIN, 14));
        cancelItem.addActionListener(e -> showCancelDialog(hasta));
        appointmentMenu.add(cancelItem);

        // Tabloyu popup menüye bağla
        table_appoint.setComponentPopupMenu(appointmentMenu);

        // Başlangıçta randevu yükleme
        updateAppointmentModel(hasta);
    }

    private void customizeTable(JTable table) {
        table.setFont(tableFont);
        table.setRowHeight(40);
        table.setShowGrid(true);
        table.setGridColor(new Color(229, 231, 235));
        table.setSelectionBackground(secondaryColor);
        table.setSelectionForeground(primaryColor);
        
        // Başlık özelleştirme
        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Inter", Font.BOLD, 14));
        header.setBackground(lightGray);
        header.setForeground(primaryColor);
        header.setPreferredSize(new Dimension(0, 40));

        // Hücre hizalama
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
    }

    private JButton createStyledButton(String text, int x, int y, int width, int height) {
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
                button.setBackground(secondaryColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });

        return button;
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Inter", Font.BOLD, 14));
        button.setBackground(primaryColor);
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(accentColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(primaryColor);
            }
        });
    }

    private void deleteSelectedAppointment() {
        if (table_appoint.getSelectedRow() != -1) {
            try {
                String date = table_appoint.getValueAt(table_appoint.getSelectedRow(), 2).toString();
                String doctorName = table_appoint.getValueAt(table_appoint.getSelectedRow(), 1).toString();
                if (Helper.confirm("Randevuyu iptal etmek istediğinizden emin misiniz?")) {
                    appoint.deleteAppoint(date, doctorName);
                    Helper.showMsg("success");
                    updateAppointModel();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            Helper.showMsg("Lütfen iptal edilecek bir randevu seçin!");
        }
    }

    private void updateAppointModel() throws SQLException {
        appointModel.setRowCount(0);
        for (var appointment : appoint.getHastaList(hasta.getId())) {
            appointData = new Object[]{
                appointment.getId(),
                appointment.getDoctorName(),
                appointment.getAppDate()
            };
            appointModel.addRow(appointData);
        }
    }

    private void showCancelDialog(Hasta hasta) {
        int row = table_appoint.getSelectedRow();
        if (row >= 0) {
            // İptal sebebi için dialog
            String[] options = {
                "Zamanım Uygun Değil",
                "Başka Bir Doktora Gideceğim",
                "Sağlık Durumum İyileşti",
                "Diğer"
            };
            
            JComboBox<String> reasonCombo = new JComboBox<>(options);
            JTextField customReason = new JTextField(20);
            customReason.setVisible(false);
            
            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(3, 1, 5, 5));
            panel.add(new JLabel("İptal Sebebi:"));
            panel.add(reasonCombo);
            panel.add(customReason);
            
            reasonCombo.addActionListener(e -> {
                customReason.setVisible(reasonCombo.getSelectedItem().equals("Diğer"));
                panel.revalidate();
                panel.repaint();
            });

            int result = JOptionPane.showConfirmDialog(
                this, panel, "Randevu İptal", 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.QUESTION_MESSAGE
            );

            if (result == JOptionPane.OK_OPTION) {
                String reason = reasonCombo.getSelectedItem().equals("Diğer") 
                    ? customReason.getText() 
                    : reasonCombo.getSelectedItem().toString();

                if (reason != null && !reason.trim().isEmpty()) {
                    try {
                        String date = table_appoint.getValueAt(row, 2).toString();
                        String doctorName = table_appoint.getValueAt(row, 1).toString();
                        
                        // Randevuyu iptal et ve sebebi kaydet
                        if (hasta.cancelAppointment(date, doctorName, reason, "HASTA")) {
                            Helper.showMsg("success");
                            updateAppointmentModel(hasta);
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    Helper.showMsg("Lütfen iptal sebebi belirtin!");
                }
            }
        }
    }

    private void updateAppointmentModel(Hasta hasta) throws SQLException {
        DefaultTableModel model = (DefaultTableModel) table_appoint.getModel();
        model.setRowCount(0);
        
        for (Appointment appointment : appoint.getHastaList(hasta.getId())) {
            Object[] row = new Object[]{
                appointment.getId(),
                appointment.getDoctorName(),
                appointment.getAppDate(),
                appointment.getStatus()
            };
            
            model.addRow(row);
            
            // İptal edilmiş randevuları kırmızı yap
            if (appointment.getStatus() != null && appointment.getStatus().equals("İPTAL")) {
                int rowIndex = model.getRowCount() - 1;
                table_appoint.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value,
                            boolean isSelected, boolean hasFocus, int row, int column) {
                        Component c = super.getTableCellRendererComponent(table, value,
                                isSelected, hasFocus, row, column);
                        
                        if (row == rowIndex) {
                            c.setForeground(new Color(239, 68, 68)); // Kırmızı
                        } else {
                            c.setForeground(Color.BLACK);
                        }
                        
                        return c;
                    }
                });
            }
        }
    }
}
