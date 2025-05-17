package Model;

import dbhelper.DBConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Appointment, "appointment" tablosundaki randevu kayıtlarını tutan model sınıf.
 * Tabloda şu sütunların olduğunu varsayar:
 *   id (int), doctor_id (int), hasta_id (int),
 *   doctor_name (String), hasta_name (String),
 *   app_date (String/Date), status (String) -- isteğe bağlı.
 */
public class Appointment {
    
    // Alanlar
    private int id;         // appointment tablosundaki PK (randevu ID)
    private int doctorID;   // Doktorun user tablosundaki ID'si
    private int hastaID;    // Hastanın user tablosundaki ID'si
    private String doctorName; 
    private String hastaName;
    private String appDate; // Randevu tarihi (String veya Timestamp)
    private String status;  // Randevunun durumu (aktif, iptal, vs.)
    private String cancelReason;
    private String cancelledBy;
    private int doctorId;
    private int hastaId;

    // DB bağlantısı - eğer bu sınıftan veritabanı işlemleri yapacaksanız
    private final DBConnection conn = new DBConnection();

    // ---------------------
    // Constructor'lar
    // ---------------------
    public Appointment() {
        // Boş
    }

    /**
     * Tüm alanlarıyla Appointment nesnesi oluşturma.
     */
    public Appointment(int id, int doctorID, int hastaID,
                       String doctorName, String hastaName,
                       String appDate, String status) {
        this.id = id;
        this.doctorID = doctorID;
        this.hastaID = hastaID;
        this.doctorName = doctorName;
        this.hastaName = hastaName;
        this.appDate = appDate;
        this.status = status;
    }

    // ---------------------
    // Ek Örnek CRUD / Yardımcı Metotlar (Opsiyonel)
    // ---------------------

    /**
     * Randevu silme örneği (app_date ve doctor_name'e göre).
     * Projenize göre düzenleyebilirsiniz.
     */
    public boolean deleteAppoint(String date, String name) throws SQLException {
        boolean key = false;
        String sqlDelete = "DELETE FROM appointment WHERE app_date = ?";
        String sqlUpdate = "UPDATE whour SET status='a' WHERE doctor_name = ? AND wdate = ?";

        try (Connection con = conn.connDb();
             PreparedStatement ps1 = con.prepareStatement(sqlDelete);
             PreparedStatement ps2 = con.prepareStatement(sqlUpdate)) {

            // appointment tablosundan sil
            ps1.setString(1, date);
            int result1 = ps1.executeUpdate();

            // whour tablosunda ilgili saat aralığını tekrar müsait yap
            ps2.setString(1, name);
            ps2.setString(2, date);
            int result2 = ps2.executeUpdate();

            key = (result1 > 0 && result2 > 0);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return key;
    }

    /**
     * (Opsiyonel) Belirli hastaID'ye ait randevuları getirir.
     * Projenize göre düzenleyebilirsiniz.
     */
    public ArrayList<Appointment> getHastaList(int hasta_id) throws SQLException {
        ArrayList<Appointment> list = new ArrayList<>();
        String query = "SELECT * FROM appointment WHERE hasta_id = ?";

        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(query)) {

            ps.setInt(1, hasta_id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appointment obj = mapResultSetToAppointment(rs);
                    list.add(obj);
                }
            }
        }
        return list;
    }

    /**
     * DoktorID'ye ait randevuları getirir (Opsiyonel).
     */
    public ArrayList<Appointment> getRandevuList(int doctor_id) throws SQLException {
        ArrayList<Appointment> list = new ArrayList<>();
        String query = "SELECT * FROM appointment WHERE doctor_id = ?";
        
        try (Connection conn = this.conn.connDb();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, doctor_id);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Appointment obj = new Appointment();
                    obj.setId(rs.getInt("id"));
                    obj.setDoctorId(rs.getInt("doctor_id"));
                    obj.setDoctorName(rs.getString("doctor_name"));
                    obj.setHastaId(rs.getInt("hasta_id"));
                    obj.setHastaName(rs.getString("hasta_name"));
                    obj.setAppDate(rs.getString("app_date"));
                    obj.setStatus(rs.getString("status"));
                    obj.setCancelReason(rs.getString("cancel_reason"));
                    obj.setCancelledBy(rs.getString("cancelled_by"));
                    list.add(obj);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
        return list;
    }

    // ---------------------
    // getDoktorAdi() / getHastaAdi()
    // ---------------------
    /**
     * AdminGUI içinde tabloya doktor adını basarken kullanılıyor.
     */
    public String getDoktorAdi() {
        return this.doctorName;
    }

    /**
     * AdminGUI içinde tabloya hasta adını basarken kullanılıyor.
     */
    public String getHastaAdi() {
        return this.hastaName;
    }

    // ---------------------
    // Getter - Setter
    // ---------------------
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getDoctorID() {
        return doctorID;
    }
    public void setDoctorID(int doctorID) {
        this.doctorID = doctorID;
    }

    public int getHastaID() {
        return hastaID;
    }
    public void setHastaID(int hastaID) {
        this.hastaID = hastaID;
    }

    public String getDoctorName() {
        return doctorName;
    }
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getHastaName() {
        return hastaName;
    }
    public void setHastaName(String hastaName) {
        this.hastaName = hastaName;
    }

    public String getAppDate() {
        return appDate;
    }
    public void setAppDate(String appDate) {
        this.appDate = appDate;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getCancelReason() {
        return cancelReason;
    }
    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public String getCancelledBy() {
        return cancelledBy;
    }
    public void setCancelledBy(String cancelledBy) {
        this.cancelledBy = cancelledBy;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getHastaId() {
        return hastaId;
    }

    public void setHastaId(int hastaId) {
        this.hastaId = hastaId;
    }

    // ---------------------
    // Özel Yardımcı Metot
    // ---------------------
    /**
     * ResultSet'ten Appointment nesnesi oluşturmak için (Opsiyonel).
     */
    private Appointment mapResultSetToAppointment(ResultSet rs) throws SQLException {
        Appointment obj = new Appointment();
        obj.setId(rs.getInt("id"));
        obj.setDoctorID(rs.getInt("doctor_id"));
        obj.setHastaID(rs.getInt("hasta_id"));
        obj.setDoctorName(rs.getString("doctor_name"));
        obj.setHastaName(rs.getString("hasta_name"));
        obj.setAppDate(rs.getString("app_date")); 
        // status varsa
        try {
            obj.setStatus(rs.getString("status"));
        } catch (SQLException ignore) {
            // Yoksa yut
        }
        return obj;
    }
}
