package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Doctor extends User {

	Connection con = conn.connDb();
	Statement st = null;
	ResultSet rs = null;
	PreparedStatement preparedStatement = null;

	public Doctor() {
		super();
		// TODO Auto-generated constructor stub
	}

	public Doctor(int id, String tcno, String name, String password, String type) throws SQLException {
		super(id, tcno, name, password, type);
		// TODO Auto-generated constructor stub
	}

	public ArrayList<Whour> getWhourList(int doctor_id) throws SQLException {
		ArrayList<Whour> list = new ArrayList<>();

		Whour obj;
		try {
			st = con.createStatement();
			rs = st.executeQuery("SELECT * FROM whour WHERE status ='a' AND doctor_id=" + doctor_id);

			while (rs.next()) {
				obj = new Whour();
				obj.setId(rs.getInt("id"));
				obj.setDoctor_id(rs.getInt("doctor_id"));
				obj.setDoctor_name(rs.getString("doctor_name"));
				obj.setStatus(rs.getString("status"));
				obj.setWdate(rs.getString("wdate"));
				list.add(obj);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return list;
	}

	public boolean addWhour(int doctor_id, String doctor_name, String wdate) throws SQLException {
	    String query = "INSERT INTO whour" + "(doctor_id,doctor_name,wdate,status) VALUES" + "(?,?,?,?)";
	    boolean key = false;
	    
	    try {
	        // Önce aynı tarih için kayıt var mı kontrol et
	        st = con.createStatement();
	        rs = st.executeQuery("SELECT * FROM whour WHERE status='a' AND doctor_id=" + doctor_id + 
	                           " AND wdate='" + wdate + "'");
	        
	        if (!rs.next()) { // Eğer aynı kayıt yoksa ekle
	            preparedStatement = con.prepareStatement(query);
	            preparedStatement.setInt(1, doctor_id);
	            preparedStatement.setString(2, doctor_name);
	            preparedStatement.setString(3, wdate);
	            preparedStatement.setString(4, "a"); // aktif durumu
	            
	            int result = preparedStatement.executeUpdate();
	            if(result > 0) {
	                key = true;
	                System.out.println("Çalışma saati başarıyla eklendi: " + wdate);
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    } finally {
	        if (preparedStatement != null) preparedStatement.close();
	        if (rs != null) rs.close();
	        if (st != null) st.close();
	    }
	    
	    return key;
	}

	public boolean deleteWhour(int id) throws SQLException {
		String query = "DELETE FROM whour WHERE id=?";
		boolean key = false;
		try (Connection con = conn.connDb();
             PreparedStatement preparedStatement = con.prepareStatement(query)) {
			preparedStatement.setInt(1, id);
			preparedStatement.executeUpdate();
			key = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return key;
	}

}
