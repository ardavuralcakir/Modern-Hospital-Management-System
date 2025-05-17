package Model;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * "Hasta" sınıfı, User'dan miras alıyor.
 * Hasta'ya özel metotlar (register, addAppointment, vs.) burada tanımlanmıştır.
 */
public class Hasta extends User {

    public Connection con = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet rs = null;

    public Hasta() {
        super();
    }

    public Hasta(int id, String tcno, String name, String password, String type) {
        super(id, tcno, name, password, type);
    }

    /**
     * T.C. kimlik numarası veritabanında kayıtlı mı?
     */
    public boolean isTCNumberRegistered(String tcno) {
        boolean result = false;
        String query = "SELECT * FROM user WHERE tcno = ?";
        try {
            con = conn.connDb();
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, tcno);
            rs = preparedStatement.executeQuery();
            result = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return result;
    }

    /**
     * E-posta adresi veritabanında kayıtlı mı?
     */
    public boolean isEmailRegistered(String email) {
        boolean result = false;
        String query = "SELECT * FROM user WHERE email = ?";
        try {
            con = conn.connDb();
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, email);
            rs = preparedStatement.executeQuery();
            result = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return result;
    }

    /**
     * Telefon numarası veritabanında kayıtlı mı?
     */
    public boolean isPhoneRegistered(String phone) {
        boolean result = false;
        String query = "SELECT * FROM user WHERE pnumber = ?";
        try {
            con = conn.connDb();
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, phone);
            rs = preparedStatement.executeQuery();
            result = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return result;
    }

    /**
     * Veritabanındaki cinsiyet değerini getirir.
     */
    public String getGender() {
        String gender = "";
        String query = "SELECT gender FROM user WHERE id = ?";
        try {
            con = conn.connDb();
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, this.getId());
            rs = preparedStatement.executeQuery();

            if (rs.next()) {
                gender = rs.getString("gender");
                // Ek kontrol: Veritabanında uygunsuz bir değer varsa varsayılan "Erkek"
                if (!"Erkek".equals(gender) && !"Kadın".equals(gender)) {
                    gender = "Erkek";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return gender;
    }

    /**
     * Cinsiyeti veritabanında günceller.
     */
    public void setGender(String gender) {
        // Sadece "Erkek" veya "Kadın" değerlerine izin veriyoruz
        if (!"Erkek".equals(gender) && !"Kadın".equals(gender)) {
            return; // Değişiklik yapmıyoruz
        }
        String query = "UPDATE user SET gender = ? WHERE id = ?";
        try {
            con = conn.connDb();
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, gender);
            preparedStatement.setInt(2, this.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
    }

    /**
     * Telefon numarasını veritabanından okur.
     */
    public String getPhone() {
        String phone = "";
        String query = "SELECT pnumber FROM user WHERE id = ?";
        try {
            con = conn.connDb();
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, this.getId());
            rs = preparedStatement.executeQuery();

            if (rs.next()) {
                phone = rs.getString("pnumber");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return phone;
    }

    /**
     * E-posta adresini veritabanından okur.
     */
    public String getEmail() {
        String email = "";
        String query = "SELECT email FROM user WHERE id = ?";
        try {
            con = conn.connDb();
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, this.getId());
            rs = preparedStatement.executeQuery();

            if (rs.next()) {
                email = rs.getString("email");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return email;
    }

    /**
     * Hastanın şifresini günceller (önce eski şifreyi kontrol edip eşleşirse yeni şifreye geçirir).
     */
    public boolean updatePassword(String oldPassword, String newPassword) {
        boolean success = false;
        String query = "UPDATE user SET password = ? WHERE id = ? AND password = ?";
        try {
            con = conn.connDb();
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, newPassword);
            preparedStatement.setInt(2, this.getId());
            preparedStatement.setString(3, oldPassword);

            success = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return success;
    }

    /**
     * Hasta kaydı (register). Gerekli alanlar: tcno, password, name, email, pnumber, gender, birthDate.
     * 
     * @param tcno T.C. kimlik
     * @param password Şifre
     * @param name Ad-Soyad
     * @param email E-posta
     * @param pnumber Telefon
     * @param gender Cinsiyet (Erkek/Kadın)
     * @param birthDate Doğum Tarihi
     * @return Kayıt başarılıysa true
     */
    public boolean register(String tcno, String password, String name, String email,
                            String pnumber, String gender, Date birthDate) {

        // 1) Geçerli cinsiyet mi?
        if (!"Erkek".equals(gender) && !"Kadın".equals(gender)) {
            return false;
        }

        // 2) TC kimlik kayıtlı mı?
        if (isTCNumberRegistered(tcno)) {
            return false;
        }

        // 3) E-posta kayıtlı mı?
        if (isEmailRegistered(email)) {
            return false;
        }

        // 4) Telefon kayıtlı mı?
        if (isPhoneRegistered(pnumber)) {
            return false;
        }

        boolean success = false;
        String query = "INSERT INTO user (tcno, password, name, email, pnumber, gender, dg, type) "
                     + "VALUES (?, ?, ?, ?, ?, ?, ?, 'hasta')";

        try {
            con = conn.connDb();
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setString(1, tcno);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, name);
            preparedStatement.setString(4, email);
            preparedStatement.setString(5, pnumber);
            preparedStatement.setString(6, gender);
            preparedStatement.setDate(7, birthDate);

            success = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return success;
    }

    /**
     * Randevu ekler (appointment tablosuna kaydeder).
     */
    public boolean addAppointment(int doctor_id, int hasta_id, String doctor_name,
                                  String hasta_name, String appDate) {
        boolean success = false;
        String query = "INSERT INTO appointment (doctor_id, doctor_name, hasta_id, hasta_name, app_date) "
                     + "VALUES (?, ?, ?, ?, ?)";
        try {
            con = conn.connDb();
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, doctor_id);
            preparedStatement.setString(2, doctor_name);
            preparedStatement.setInt(3, hasta_id);
            preparedStatement.setString(4, hasta_name);
            preparedStatement.setString(5, appDate);

            success = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return success;
    }

    /**
     * Doktorun çalışma saatini günceller (Örneğin randevu alındıysa 'p' yap).
     */
    public boolean updateWhourStatus(int doctor_id, String wdate) {
        boolean success = false;
        String query = "UPDATE whour SET status = 'p' WHERE doctor_id = ? AND wdate = ?";
        try {
            con = conn.connDb();
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, doctor_id);
            preparedStatement.setString(2, wdate);

            success = preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources();
        }
        return success;
    }

    /**
     * Kaynakları kapatır (bağlantı, statement, resultset).
     */
    private void closeResources() {
        try {
            if (rs != null) rs.close();
            if (preparedStatement != null) preparedStatement.close();
            if (con != null) con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Bu metot, projede bir yerde Reflection vb. kullanılıyorsa override edilebilir.
     * Şu an için basitçe null döndürüyor.
     */
    public Object getConn() {
        return null;
    }

    public boolean cancelAppointment(String date, String doctorName, String reason, String cancelledBy) throws SQLException {
        String query = "UPDATE appointment SET status = 'İPTAL', cancel_reason = ?, cancelled_by = ? WHERE app_date = ? AND doctor_name = ?";
        
        try {
            con = conn.connDb();
            PreparedStatement pst = con.prepareStatement(query);
            pst.setString(1, reason);
            pst.setString(2, cancelledBy);
            pst.setString(3, date);
            pst.setString(4, doctorName);
            
            return pst.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources();
        }
    }
}
