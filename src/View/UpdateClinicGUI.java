package View;

import java.awt.EventQueue;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import dbhelper.Helper;
import Model.Clinic;

import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.awt.event.ActionEvent;

public class UpdateClinicGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private JPanel contentPane;
    private JTextField fld_clinicName;

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    // Örnek bir Clinic nesnesi
                    Clinic exampleClinic = new Clinic(1, "Ortopedi");
                    UpdateClinicGUI frame = new UpdateClinicGUI(exampleClinic);
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public UpdateClinicGUI(Clinic clinic) {
        // Clinic null ise hata
        if (clinic == null) {
            throw new IllegalArgumentException("Clinic nesnesi null olamaz.");
        }
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 225, 150);
        setTitle("Klinik Güncelle");
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JLabel label = new JLabel("Poliklinik Adı");
        label.setFont(new Font("Tahoma", Font.BOLD, 14));
        label.setBounds(10, 11, 129, 14);
        contentPane.add(label);

        fld_clinicName = new JTextField();
        fld_clinicName.setText(clinic.getName()); // Mevcut adı
        fld_clinicName.setColumns(10);
        fld_clinicName.setBounds(10, 36, 189, 23);
        contentPane.add(fld_clinicName);

        JButton btn_updateClinic = new JButton("Düzenle");
        btn_updateClinic.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (Helper.confirm("sure")) {
                    try {
                        boolean success = clinic.updateClinic(
                            clinic.getId(),
                            fld_clinicName.getText()
                        );
                        if (success) {
                            Helper.showMsg("success");
                            dispose();
                        } else {
                            Helper.showMsg("error");
                        }
                    } catch (SQLException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        btn_updateClinic.setFont(new Font("Tahoma", Font.BOLD, 14));
        btn_updateClinic.setBounds(10, 66, 189, 25);
        contentPane.add(btn_updateClinic);
    }
}
