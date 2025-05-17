package Model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import dbhelper.DBConnection;

public class Comment {
    private int id;
    private int blogId;
    private int userId;
    private String content;
    private String date;
    private String userName; // Yorumu yapan kullanıcının adı
    private DBConnection conn = new DBConnection();

    public Comment() {}

    public Comment(int id, int blogId, int userId, String content, String date, String userName) {
        this.id = id;
        this.blogId = blogId;
        this.userId = userId;
        this.content = content;
        this.date = date;
        this.userName = userName;
    }

    public ArrayList<Comment> getCommentsByBlogId(int blogId) throws SQLException {
        ArrayList<Comment> list = new ArrayList<>();
        String query = "SELECT c.*, u.name as user_name FROM blog_comments c " +
                      "JOIN user u ON c.user_id = u.id " +
                      "WHERE c.blog_id = ? ORDER BY c.date DESC";
        
        Connection con = conn.connDb();
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, blogId);
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            Comment obj = new Comment();
            obj.setId(rs.getInt("id"));
            obj.setBlogId(rs.getInt("blog_id"));
            obj.setUserId(rs.getInt("user_id"));
            obj.setContent(rs.getString("content"));
            obj.setDate(rs.getString("date"));
            obj.setUserName(rs.getString("user_name"));
            list.add(obj);
        }
        return list;
    }

    public boolean addComment(int blogId, int userId, String content) throws SQLException {
        // Kullanıcının email doğrulamasını kontrol et
        String checkQuery = "SELECT email_verified FROM user WHERE id = ?";
        Connection con = conn.connDb();
        PreparedStatement checkPs = con.prepareStatement(checkQuery);
        checkPs.setInt(1, userId);
        ResultSet rs = checkPs.executeQuery();
        
        if (rs.next() && rs.getBoolean("email_verified")) {
            String query = "INSERT INTO blog_comments (blog_id, user_id, content, date) VALUES (?, ?, ?, NOW())";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, blogId);
            ps.setInt(2, userId);
            ps.setString(3, content);
            return ps.executeUpdate() > 0;
        }
        return false; // Email doğrulanmamış kullanıcılar yorum yapamaz
    }

    public boolean deleteComment(int commentId, int userId) throws SQLException {
        String query = "DELETE FROM blog_comments WHERE id = ? AND user_id = ?";
        Connection con = conn.connDb();
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, commentId);
        ps.setInt(2, userId);
        return ps.executeUpdate() > 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBlogId() { return blogId; }
    public void setBlogId(int blogId) { this.blogId = blogId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
} 