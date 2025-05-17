package Model;

import dbhelper.DBConnection;
import java.sql.*;
import java.util.ArrayList;

public class SavedCollection {
    private int id;
    private int userId;
    private String collectionName;
    private Timestamp createdDate;
    private DBConnection conn = new DBConnection();

    public SavedCollection() {}

    public SavedCollection(int id, int userId, String collectionName, Timestamp createdDate) {
        this.id = id;
        this.userId = userId;
        this.collectionName = collectionName;
        this.createdDate = createdDate;
    }

    // Yeni koleksiyon oluştur
    public boolean createCollection(int userId, String collectionName) throws SQLException {
        String query = "INSERT INTO saved_collections (user_id, collection_name) VALUES (?, ?)";
        try (Connection con = conn.connDb();
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, userId);
            pst.setString(2, collectionName);
            return pst.executeUpdate() > 0;
        }
    }

    // Kullanıcının koleksiyonlarını getir
    public ArrayList<SavedCollection> getUserCollections(int userId) throws SQLException {
        ArrayList<SavedCollection> collections = new ArrayList<>();
        String query = "SELECT * FROM saved_collections WHERE user_id = ? ORDER BY created_date DESC";
        Connection con = conn.connDb();
        
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                SavedCollection collection = new SavedCollection(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("collection_name"),
                    rs.getTimestamp("created_date")
                );
                collections.add(collection);
            }
        }
        return collections;
    }

    // Blog gönderisini koleksiyona kaydet
    public boolean saveBlogToCollection(int collectionId, int blogId, int userId) throws SQLException {
        String query = "INSERT INTO saved_posts (collection_id, blog_id, user_id) VALUES (?, ?, ?)";
        Connection con = conn.connDb();
        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, collectionId);
            pst.setInt(2, blogId);
            pst.setInt(3, userId);
            return pst.executeUpdate() > 0;
        }
    }

    // Blog gönderisini koleksiyondan kaldır
    public boolean removeBlogFromCollection(int collectionId, int blogId, int userId) throws SQLException {
        String query = "DELETE FROM saved_posts WHERE collection_id = ? AND blog_id = ? AND user_id = ?";
        try (Connection con = conn.connDb();
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, collectionId);
            pst.setInt(2, blogId);
            pst.setInt(3, userId);
            return pst.executeUpdate() > 0;
        }
    }

    // Koleksiyondaki blog gönderilerini getir
    public ArrayList<Blog> getCollectionBlogs(int collectionId) throws SQLException {
        ArrayList<Blog> blogs = new ArrayList<>();
        String query = "SELECT b.*, u.name as doctor_name, " +
                      "(SELECT COUNT(*) FROM blog_likes l WHERE l.post_id = b.id) as like_count, " +
                      "(SELECT COUNT(*) FROM blog_comments c WHERE c.post_id = b.id) as comment_count " +
                      "FROM blog b " +
                      "INNER JOIN saved_posts sp ON b.id = sp.blog_id " +
                      "LEFT JOIN user u ON b.doctor_id = u.id " +
                      "WHERE sp.collection_id = ? " +
                      "ORDER BY sp.saved_date DESC";
        
        try (Connection con = conn.connDb();
             PreparedStatement pst = con.prepareStatement(query)) {
            pst.setInt(1, collectionId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    Blog blog = new Blog();
                    blog.setId(rs.getInt("id"));
                    blog.setTitle(rs.getString("title"));
                    blog.setContent(rs.getString("content"));
                    blog.setAuthor(rs.getString("doctor_name"));
                    blog.setDate(rs.getTimestamp("date"));
                    blog.setLikeCount(rs.getInt("like_count"));
                    blog.setCommentCount(rs.getInt("comment_count"));
                    blogs.add(blog);
                }
            }
        }
        return blogs;
    }

    // Koleksiyonu sil
    public boolean deleteCollection(int collectionId, int userId) throws SQLException {
        try (Connection con = conn.connDb()) {
            con.setAutoCommit(false);
            try {
                // Önce koleksiyondaki kayıtlı gönderileri sil
                String deletePostsQuery = "DELETE FROM saved_posts WHERE collection_id = ? AND user_id = ?";
                try (PreparedStatement pst = con.prepareStatement(deletePostsQuery)) {
                    pst.setInt(1, collectionId);
                    pst.setInt(2, userId);
                    pst.executeUpdate();
                }

                // Sonra koleksiyonu sil
                String deleteCollectionQuery = "DELETE FROM saved_collections WHERE id = ? AND user_id = ?";
                try (PreparedStatement pst = con.prepareStatement(deleteCollectionQuery)) {
                    pst.setInt(1, collectionId);
                    pst.setInt(2, userId);
                    int result = pst.executeUpdate();
                    con.commit();
                    return result > 0;
                }
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getCollectionName() { return collectionName; }
    public void setCollectionName(String collectionName) { this.collectionName = collectionName; }
    
    public Timestamp getCreatedDate() { return createdDate; }
    public void setCreatedDate(Timestamp createdDate) { this.createdDate = createdDate; }
} 