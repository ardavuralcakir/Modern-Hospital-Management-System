package Model;

import dbhelper.DBConnection;

import java.sql.*;
import java.util.ArrayList;

/**
 * Clinic tablosu için temel CRUD ve bazı ek metodları barındıran sınıf.
 * Tablodaki sütunlar: id (int), name (varchar).
 */
public class Clinic {

    // DBConnection nesnesi
    private DBConnection conn = new DBConnection();

    // Alanlar
    private int id;
    private String name;

    // -- Constructor'lar --
    public Clinic() {
    }

    public Clinic(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // ---------------------------------------------------------------------
    // Tüm Clinic Kayıtlarını Listele
    // ---------------------------------------------------------------------
    public ArrayList<Clinic> getList() throws SQLException {
        ArrayList<Clinic> list = new ArrayList<>();
        String sql = "SELECT * FROM clinic";

        try (Connection con = conn.connDb();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Clinic obj = new Clinic();
                obj.setId(rs.getInt("id"));
                obj.setName(rs.getString("name"));
                list.add(obj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    
    
    // Doktor ID'sine göre klinik ID'sini getir
    public int getFetchClinicByDoctorId(int doctorId) throws SQLException {
        int clinicId = 0;
        String query = "SELECT clinic_id FROM worker WHERE id = ?";
        
        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setInt(1, doctorId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                clinicId = rs.getInt("clinic_id");
            }
            rs.close();
        }
        return clinicId;
    }

    // Klinik ID'sine göre klinik adını getir
    public String getFetchClinicName(int clinicId) throws SQLException {
        String clinicName = "";
        String query = "SELECT name FROM clinic WHERE id = ?";
        
        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setInt(1, clinicId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                clinicName = rs.getString("name");
            }
            rs.close();
        }
        return clinicName;
    }

    
    // ---------------------------------------------------------------------
    // Yeni Klinik Ekle
    // ---------------------------------------------------------------------
    public boolean addClinic(String name) throws SQLException {
        String sql = "INSERT INTO clinic (name) VALUES (?)";
        boolean key = false;

        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.executeUpdate();
            key = true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return key;
    }

    // ---------------------------------------------------------------------
    // Klinik Sil
    // ---------------------------------------------------------------------
    public boolean deleteClinic(int id) throws SQLException {
        String sql = "DELETE FROM clinic WHERE id=?";
        boolean key = false;

        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();
            key = true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return key;
    }

    // ---------------------------------------------------------------------
    // Klinik Güncelle
    // ---------------------------------------------------------------------
    public boolean updateClinic(int id, String name) throws SQLException {
        String sql = "UPDATE clinic SET name=? WHERE id=?";
        boolean key = false;

        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setInt(2, id);
            ps.executeUpdate();
            key = true;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return key;
    }

    // ---------------------------------------------------------------------
    // ID'ye Göre Klinik Getir (fetch)
    // ---------------------------------------------------------------------
    public Clinic getFech(int id) {
        Clinic c = new Clinic();
        String sql = "SELECT * FROM clinic WHERE id=" + id;

        try (Connection con = conn.connDb();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                c.setId(rs.getInt("id"));
                c.setName(rs.getString("name"));
                break; // ilk bulduğu kaydı almak için
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return c;
    }

    // ---------------------------------------------------------------------
    // Belirli klinikteki doktorları listele (JOIN worker + user)
    // ---------------------------------------------------------------------
    public ArrayList<User> getClinicDoctorList(int clinic_id) throws SQLException {
        ArrayList<User> list = new ArrayList<>();
        String sql = "SELECT u.id, u.tcno, u.type, u.name, u.password " +
                     "FROM worker w " +
                     "LEFT JOIN user u ON w.user_id=u.id " +
                     "WHERE w.clinic_id=?";

        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, clinic_id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User obj = new User(
                        rs.getInt("id"),
                        rs.getString("tcno"),
                        rs.getString("name"),
                        rs.getString("password"),
                        rs.getString("type")
                    );
                    list.add(obj);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ---------------------------------------------------------------------
    // Örnek: Toplam Hasta Sayısı (Mevcut tasarımda "whour" tablosuna bakıyor)
    // ---------------------------------------------------------------------
    public int getToplamHastaSayisi() throws SQLException {
        int total = 0;
        String sql = "SELECT COUNT(DISTINCT doctor_id) AS total FROM whour WHERE status='p'";

        try (Connection con = conn.connDb();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                total = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return total;
    }

    // ---------------------------------------------------------------------
    // Belirli klinikteki (clinic_id) hasta sayısı
    // ---------------------------------------------------------------------
    public int getHastaSayisi(int klinikId) throws SQLException {
        int count = 0;
        // Burada whour ve worker tablolarını birleştirerek,
        // status='p' (örn. "pending" ya da "patient") olan kayıtları sayıyor.
        String sql = "SELECT COUNT(DISTINCT w.doctor_id) AS hasta_sayisi " +
                     "FROM whour w " +
                     "INNER JOIN worker wr ON w.doctor_id = wr.user_id " +
                     "WHERE wr.clinic_id = ? AND w.status = 'p'";

        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, klinikId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("hasta_sayisi");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    // ---------------------------------------------------------------------
    // Belirli klinikteki randevu sayısı
    // ---------------------------------------------------------------------
    public int getRandevuSayisi(int klinikId) throws SQLException {
        int count = 0;
        String sql = "SELECT COUNT(*) AS randevu_sayisi " +
                     "FROM whour w " +
                     "INNER JOIN worker wr ON w.doctor_id = wr.user_id " +
                     "WHERE wr.clinic_id = ? AND w.status = 'p'";

        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, klinikId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    count = rs.getInt("randevu_sayisi");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    // ---------------------------------------------------------------------
    // Getter / Setter
    // ---------------------------------------------------------------------
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
