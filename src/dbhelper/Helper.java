package dbhelper;

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Helper {

    public static void optionPaneChangeButtonText() {
        UIManager.put("OptionPane.cancelButtonText", "iptal");
        UIManager.put("OptionPane.noButtonText", "Hayir");
        UIManager.put("OptionPane.okButtonText", "Tamam");
        UIManager.put("OptionPane.yesButtonText", "Evet");
    }

    public static void showMsg(String str) {
        String msg;
        optionPaneChangeButtonText();
        switch (str) {
            case "fill":
                msg = "Lutfen tum alanlari doldurunuz!";
                break;
            case "success":
                msg = "Islem basarili";
                break;
            case "error":
                msg = "Bir hata ile karsilasildi!";
                break;
            default:
                msg = str;
        }
        JOptionPane.showMessageDialog(null, msg, "Mesaj", JOptionPane.INFORMATION_MESSAGE);
    }

    public static boolean confirm(String str) {
        optionPaneChangeButtonText();
        String msg;
        switch (str) {
            case "sure":
                msg = "bu islemi gerceklestirmek istiyor musun?";
                break;
            default:
                msg = str;
                break;
        }
        int rs = JOptionPane.showConfirmDialog(null, msg, "Dikkat", JOptionPane.YES_NO_OPTION);
        return rs == 0;
    }

    public static String formatDateTr(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
        return sdf.format(date);
    }

    public static String getMonthNameTr(int month) {
        String[] monthNames = {
            "Ocak", "Şubat", "Mart", "Nisan",
            "Mayıs", "Haziran", "Temmuz", "Ağustos",
            "Eylül", "Ekim", "Kasım", "Aralık"
        };
        if (month >= 1 && month <= 12) {
            return monthNames[month - 1];
        }
        return "";
    }

    public static boolean isFieldEmpty(JTextField field) {
        return field.getText().trim().isEmpty();
    }

    public static boolean isFieldEmpty(JTextArea area) {
        return area.getText().trim().isEmpty();
    }

    public static int screenCenterPoint(String axis, Dimension size) {
        int point = 0;
        switch (axis) {
            case "x":
                point = (Toolkit.getDefaultToolkit().getScreenSize().width - size.width) / 2;
                break;
            case "y":
                point = (Toolkit.getDefaultToolkit().getScreenSize().height - size.height) / 2;
                break;
            default:
                point = 0;
        }
        return point;
    }
}