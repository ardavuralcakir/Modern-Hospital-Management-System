package Model;

import dbhelper.DBConnection;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class Admin extends User {

    private DBConnection conn = new DBConnection();

    public Admin(int id, String tcno, String name, String password, String type) {
        super(id, tcno, name, password, type);
    }

    public Admin() {
    }

    /**
     * Doktor listesi (type='doktor')
     */
    public ArrayList<User> getDoktorList() throws SQLException {
        ArrayList<User> list = new ArrayList<>();
        String sql = "SELECT * FROM user WHERE type='doktor'";

        try (Connection con = conn.connDb();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Yeni doktor ekleme
     */
    public boolean addDoktor(String tcno, String password, String name) throws SQLException {
        String sql = "INSERT INTO user (tcno, password, name, type) VALUES (?,?,?,?)";
        boolean key = false;
        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tcno);
            ps.setString(2, password);
            ps.setString(3, name);
            ps.setString(4, "doktor");
            ps.executeUpdate();
            key = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return key;
    }
    public boolean checkDbAdmin(String username, String password) throws SQLException {
        boolean isAdmin = false;
        String sql = "SELECT * FROM user WHERE tcno=? AND password=? AND type='admin'";
        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Veritabanında bu kullanıcı var ve type=admin
                    isAdmin = true;
                }
            }
        }
        return isAdmin;
    }
    
    
    
    /**
     * Doktor silme
     */
    public boolean deleteDoktor(int id) throws SQLException {
        String sql = "DELETE FROM user WHERE id=?";
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

    /**
     * Doktor bilgisi güncelleme
     */
    public boolean updateDoktor(int id, String tcno, String name, String password) throws SQLException {
        String sql = "UPDATE user SET name=?, tcno=?, password=? WHERE id=?";
        boolean key = false;
        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, tcno);
            ps.setString(3, password);
            ps.setInt(4, id);
            ps.executeUpdate();
            key = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return key;
    }

    /**
     * Doktoru polikliniğe ata
     */
    public boolean addWorker(int user_id, int clinic_id) throws SQLException {
        String selectSql = "SELECT * FROM worker WHERE clinic_id=? AND user_id=?";
        String insertSql = "INSERT INTO worker (user_id, clinic_id) VALUES (?,?)";
        boolean key = false;

        try (Connection con = conn.connDb();
             PreparedStatement psSelect = con.prepareStatement(selectSql)) {

            psSelect.setInt(1, clinic_id);
            psSelect.setInt(2, user_id);
            try (ResultSet rs = psSelect.executeQuery()) {
                if (!rs.next()) {
                    // Bu doktor henüz poliklinikte kayıtlı değil, ekleyelim
                    try (PreparedStatement psInsert = con.prepareStatement(insertSql)) {
                        psInsert.setInt(1, user_id);
                        psInsert.setInt(2, clinic_id);
                        psInsert.executeUpdate();
                    }
                }
            }
            key = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return key;
    }

    
    public boolean removeWorker(int doctor_id, int clinic_id) throws SQLException {
        String query = "DELETE FROM worker WHERE user_id = ? AND clinic_id = ?";
        boolean key = false;
        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, doctor_id);
            ps.setInt(2, clinic_id);
            ps.executeUpdate();
            key = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return key;
    }
    
    
    
    /**
     * Poliklinikte çalışan doktorlar
     */
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

    /**
     * Toplam hasta sayısı
     */
    public int getHastaCount() throws SQLException {
        int count = 0;
        String sql = "SELECT COUNT(*) AS total FROM user WHERE type='hasta'";
        try (Connection con = conn.connDb();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                count = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * (Opsiyonel) Günlük randevu sayısı
     */
    public int getDailyAppointmentCount() throws SQLException {
        int count = 0;
        String sqlAll   = "SELECT app_date FROM appointment";
        String sqlCount = "SELECT COUNT(*) AS total FROM appointment";
        try (Connection con = conn.connDb();
             Statement st = con.createStatement()) {
            // Debug amaçlı tüm tarihler
            try (ResultSet rs = st.executeQuery(sqlAll)) {
                while (rs.next()) {
                    System.out.println("Randevu Tarihi: " + rs.getString("app_date"));
                }
            }
            try (ResultSet rs2 = st.executeQuery(sqlCount)) {
                if (rs2.next()) {
                    count = rs2.getInt("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * (YENİ) Bugünün (mevcut tarihin) randevu sayısı
     */
    public int getTodayAppointmentCount() throws SQLException {
        int count = 0;
        String sql = "SELECT COUNT(*) AS total FROM appointment WHERE DATE(app_date) = CURDATE()";
        try (Connection con = conn.connDb();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                count = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * (Opsiyonel) Aktif randevu sayısı
     */
    public int getActiveRandevuCount() throws SQLException {
        int count = 0;
        String sql = "SELECT COUNT(*) AS cnt FROM appointment WHERE status='aktif'";
        try (Connection con = conn.connDb();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                count = rs.getInt("cnt");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * Randevu listesini (filtreli) getir
     */
    public List<Appointment> getAppointmentList(String hastaAd, int doktorID, Date dtStart, Date dtEnd) {
        List<Appointment> list = new ArrayList<>();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            con = conn.connDb();
            StringBuilder sql = new StringBuilder("SELECT * FROM appointment WHERE 1=1 ");
            if (hastaAd != null && !hastaAd.isEmpty()) {
                sql.append(" AND hasta_name LIKE ? ");
            }
            if (doktorID > 0) {
                sql.append(" AND doctor_id = ? ");
            }
            if (dtStart != null && dtEnd != null) {
                sql.append(" AND DATE(app_date) BETWEEN ? AND ? ");
            }
            ps = con.prepareStatement(sql.toString());
            int paramIndex = 1;
            if (hastaAd != null && !hastaAd.isEmpty()) {
                ps.setString(paramIndex++, "%" + hastaAd + "%");
            }
            if (doktorID > 0) {
                ps.setInt(paramIndex++, doktorID);
            }
            if (dtStart != null && dtEnd != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                ps.setString(paramIndex++, sdf.format(dtStart));
                ps.setString(paramIndex++, sdf.format(dtEnd));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                Appointment app = new Appointment();
                app.setId(rs.getInt("id"));
                app.setDoctorID(rs.getInt("doctor_id"));
                app.setHastaID(rs.getInt("hasta_id"));
                app.setDoctorName(rs.getString("doctor_name"));
                app.setHastaName(rs.getString("hasta_name"));
                app.setAppDate(rs.getString("app_date"));
                try {
                    app.setStatus(rs.getString("status"));
                } catch (SQLException ignore) {}
                list.add(app);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception ignore) {}
            try { if (ps != null) ps.close(); } catch (Exception ignore) {}
            try { if (con != null) con.close(); } catch (Exception ignore) {}
        }
        return list;
    }

    /**
     * Randevu onayı (status='aktif')
     */
    public boolean approveAppointment(int randevuID) throws SQLException {
        boolean key = false;
        String sql = "UPDATE appointment SET status='aktif' WHERE id=?";
        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, randevuID);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                key = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return key;
    }

    /**
     * Genel randevu durumu güncelleme
     */
    public boolean updateAppointmentStatus(int randevuID, String newStatus) throws SQLException {
        boolean key = false;
        String sql = "UPDATE appointment SET status=? WHERE id=?";
        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setInt(2, randevuID);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                key = true;
            }
        }
        return key;
    }

    /**
     * Randevu iptali (status='iptal')
     */
    public boolean cancelAppointment(int randevuID) throws SQLException {
        boolean key = false;
        String sql = "UPDATE appointment SET status='iptal' WHERE id=?";
        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, randevuID);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                key = true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return key;
    }

    /**
     * İstatistikler (Haftalık, Aylık, Yıllık, İptal)
     */
    public Map<String, Integer> getAppointmentStats(Date start, Date end) throws SQLException {
        Map<String, Integer> stats = new HashMap<>();

        // Haftalık
        Calendar weekCal = Calendar.getInstance();
        weekCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        weekCal.set(Calendar.HOUR_OF_DAY, 0);
        weekCal.set(Calendar.MINUTE, 0);
        weekCal.set(Calendar.SECOND, 0);
        Date weekStart = weekCal.getTime();
        weekCal.add(Calendar.DAY_OF_WEEK, 7);
        Date weekEnd = weekCal.getTime();

        // Aylık
        Calendar monthCal = Calendar.getInstance();
        monthCal.set(Calendar.DAY_OF_MONTH, 1);
        monthCal.set(Calendar.HOUR_OF_DAY, 0);
        monthCal.set(Calendar.MINUTE, 0);
        monthCal.set(Calendar.SECOND, 0);
        Date monthStart = monthCal.getTime();
        monthCal.set(Calendar.DAY_OF_MONTH, monthCal.getActualMaximum(Calendar.DAY_OF_MONTH));
        monthCal.set(Calendar.HOUR_OF_DAY, 23);
        monthCal.set(Calendar.MINUTE, 59);
        monthCal.set(Calendar.SECOND, 59);
        Date monthEnd = monthCal.getTime();

        // Yıllık
        Calendar yearCal = Calendar.getInstance();
        yearCal.set(Calendar.DAY_OF_YEAR, 1);
        yearCal.set(Calendar.HOUR_OF_DAY, 0);
        yearCal.set(Calendar.MINUTE, 0);
        yearCal.set(Calendar.SECOND, 0);
        Date yearStart = yearCal.getTime();
        yearCal.set(Calendar.DAY_OF_YEAR, yearCal.getActualMaximum(Calendar.DAY_OF_YEAR));
        yearCal.set(Calendar.HOUR_OF_DAY, 23);
        yearCal.set(Calendar.MINUTE, 59);
        yearCal.set(Calendar.SECOND, 59);
        Date yearEnd = yearCal.getTime();

        String weeklyQuery    = "SELECT COUNT(*) AS count FROM appointment WHERE app_date >= ? AND app_date < ?";
        String monthlyQuery   = "SELECT COUNT(*) AS count FROM appointment WHERE app_date >= ? AND app_date <= ?";
        String yearlyQuery    = "SELECT COUNT(*) AS count FROM appointment WHERE app_date >= ? AND app_date <= ?";
        String cancelledQuery = "SELECT COUNT(*) AS count FROM appointment WHERE app_date BETWEEN ? AND ? AND status = 'iptal'";

        try (Connection con = conn.connDb()) {
            // Haftalık
            try (PreparedStatement ps = con.prepareStatement(weeklyQuery)) {
                ps.setTimestamp(1, new java.sql.Timestamp(weekStart.getTime()));
                ps.setTimestamp(2, new java.sql.Timestamp(weekEnd.getTime()));
                try (ResultSet rs = ps.executeQuery()) {
                    stats.put("weekly", rs.next() ? rs.getInt("count") : 0);
                }
            }
            // Aylık
            try (PreparedStatement ps = con.prepareStatement(monthlyQuery)) {
                ps.setTimestamp(1, new java.sql.Timestamp(monthStart.getTime()));
                ps.setTimestamp(2, new java.sql.Timestamp(monthEnd.getTime()));
                try (ResultSet rs = ps.executeQuery()) {
                    stats.put("monthly", rs.next() ? rs.getInt("count") : 0);
                }
            }
            // Yıllık
            try (PreparedStatement ps = con.prepareStatement(yearlyQuery)) {
                ps.setTimestamp(1, new java.sql.Timestamp(yearStart.getTime()));
                ps.setTimestamp(2, new java.sql.Timestamp(yearEnd.getTime()));
                try (ResultSet rs = ps.executeQuery()) {
                    stats.put("yearly", rs.next() ? rs.getInt("count") : 0);
                }
            }
            // İptal (kullanıcının seçtiği tarih aralığında)
            if (start != null && end != null) {
                try (PreparedStatement ps = con.prepareStatement(cancelledQuery)) {
                    ps.setTimestamp(1, new java.sql.Timestamp(start.getTime()));
                    ps.setTimestamp(2, new java.sql.Timestamp(end.getTime()));
                    try (ResultSet rs = ps.executeQuery()) {
                        stats.put("cancelled", rs.next() ? rs.getInt("count") : 0);
                    }
                }
            } else {
                stats.put("cancelled", 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            stats.put("weekly", 0);
            stats.put("monthly", 0);
            stats.put("yearly", 0);
            stats.put("cancelled", 0);
        }
        return stats;
    }

    /**
     * Hasta silme
     */
    public boolean deleteHasta(int id) throws SQLException {
        String sql = "DELETE FROM user WHERE id=? AND type='hasta'";
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

    /**
     * (YENİ) Hasta listesi (type='hasta')
     */
    public ArrayList<User> getHastaList() throws SQLException {
        ArrayList<User> list = new ArrayList<>();
        Connection con = null;
        Statement st = null;
        ResultSet rs = null;
        try {
            con = conn.connDb();
            String query = "SELECT * FROM user WHERE type = 'hasta'";
            st = con.createStatement();
            rs = st.executeQuery(query);
            while (rs.next()) {
                User obj = new User();
                obj.setId(rs.getInt("id"));
                obj.setName(rs.getString("name"));
                obj.setTcno(rs.getString("tcno"));
                obj.setPassword(rs.getString("password"));
                obj.setType(rs.getString("type"));
                obj.setEmail(rs.getString("email"));
                obj.setPhone(rs.getString("pnumber")); // 'phone' yerine 'pnumber'
                obj.setGender(rs.getString("gender"));
                list.add(obj);
            }
        } catch (SQLException e) {
            System.out.println("SQL Hatası: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (st != null) try { st.close(); } catch (SQLException e) { }
            if (con != null) try { con.close(); } catch (SQLException e) { }
        }
        return list;
    }

    // -------------------------------------------------------------------------
    //  YENİ: Maksimum kapasiteyi veritabanı tablosu üzerinden okuyup/güncelleyen metotlar
    // -------------------------------------------------------------------------
    /**
     * Mevcut maksimum kapasiteyi veritabanından döndürür.
     * Varsayılan tablo: hastane_ayarlari (id=1)
     */
    public int getMaxCapacity() throws SQLException {
        int capacity = 100; // eğer tablo boşsa en azından 100 dönsün
        String sql = "SELECT max_capacity FROM hastane_ayarlari WHERE id = 1";
        try (Connection con = conn.connDb();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                capacity = rs.getInt("max_capacity");
            }
        }
        return capacity;
    }

    /**
     * Yeni kapasite değerini veritabanındaki hastane_ayarlari tablosunda günceller.
     */
    public boolean updateMaxCapacity(int newCap) throws SQLException {
        String sql = "UPDATE hastane_ayarlari SET max_capacity=? WHERE id=1";
        boolean updated = false;
        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, newCap);
            int rows = ps.executeUpdate();
            updated = (rows > 0);
        }
        return updated;
    }

    // ------------------------------------------------------------------------
    // (YENİ) worker tablosunda kaç tane EŞSİZ (distinct) user_id olduğunu sayar
    // ------------------------------------------------------------------------
    public int getAssignedDoctorCount() throws SQLException {
        int count = 0;
        String sql = "SELECT COUNT(DISTINCT user_id) AS total FROM worker";
        try (Connection con = conn.connDb();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                count = rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }
}
