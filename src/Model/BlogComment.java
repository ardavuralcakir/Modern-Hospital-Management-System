package Model;

import java.sql.*;
import java.util.ArrayList;
import dbhelper.DBConnection;

public class BlogComment {
    private int id;
    private int postId;
    private int userId;
    private String userName;
    private String content;
    private Timestamp createdAt;
    
    private DBConnection conn = new DBConnection();
    private Connection con = conn.connDb();
    private ResultSet rs = null;
    private PreparedStatement preparedStatement = null;

    public BlogComment() {}

    public BlogComment(int id, int postId, int userId, String userName, String content, Timestamp createdAt) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public ArrayList<BlogComment> getComments(int postId) throws SQLException {
        ArrayList<BlogComment> list = new ArrayList<>();
        
        try {
            String query = "SELECT * FROM blog_comments WHERE post_id = ? ORDER BY created_at DESC";
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, postId);
            rs = preparedStatement.executeQuery();
            
            while (rs.next()) {
                BlogComment obj = new BlogComment();
                obj.setId(rs.getInt("id"));
                obj.setPostId(rs.getInt("post_id"));
                obj.setUserId(rs.getInt("user_id"));
                obj.setUserName(rs.getString("user_name"));
                obj.setContent(rs.getString("content"));
                obj.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(obj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }

    public boolean addComment(int postId, int userId, String userName, String content) throws SQLException {
        String insertQuery = "INSERT INTO blog_comments (post_id, user_id, user_name, content, created_at) VALUES (?, ?, ?, ?, NOW())";
        String updateQuery = "UPDATE blog_posts SET comments = comments + 1 WHERE id = ?";
        boolean key = false;
        
        try {
            // Yorum ekle
            preparedStatement = con.prepareStatement(insertQuery);
            preparedStatement.setInt(1, postId);
            preparedStatement.setInt(2, userId);
            preparedStatement.setString(3, userName);
            preparedStatement.setString(4, content);
            preparedStatement.executeUpdate();

            // Blog postunun yorum sayısını güncelle
            preparedStatement = con.prepareStatement(updateQuery);
            preparedStatement.setInt(1, postId);
            preparedStatement.executeUpdate();
            
            key = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return key;
    }

    // Getter ve Setter metodları
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
} 