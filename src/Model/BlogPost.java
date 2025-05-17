package Model;

import java.sql.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import dbhelper.DBConnection;

public class BlogPost {
    private int id;
    private int userId;
    private String userName;
    private String content;
    private int likes;
    private int comments;
    private Timestamp createdAt;
    
    private DBConnection conn = new DBConnection();
    private Connection con = conn.connDb();
    private Statement st = null;
    private ResultSet rs = null;
    private PreparedStatement preparedStatement = null;

    public BlogPost() {}

    public BlogPost(int id, int userId, String userName, String content, int likes, int comments, Timestamp createdAt) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.content = content;
        this.likes = likes;
        this.comments = comments;
        this.createdAt = createdAt;
    }

    public ArrayList<BlogPost> getList() throws SQLException {
        ArrayList<BlogPost> list = new ArrayList<>();
        
        try {
            st = con.createStatement();
            rs = st.executeQuery("SELECT * FROM blog_posts ORDER BY created_at DESC");
            
            while (rs.next()) {
                BlogPost obj = new BlogPost();
                obj.setId(rs.getInt("id"));
                obj.setUserId(rs.getInt("user_id"));
                obj.setUserName(rs.getString("user_name"));
                obj.setContent(rs.getString("content"));
                obj.setLikes(rs.getInt("likes"));
                obj.setComments(rs.getInt("comments"));
                obj.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(obj);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return list;
    }

    public int addPost(int userId, String userName, String content) throws SQLException {
        String query = "INSERT INTO blog_posts (user_id, user_name, content, likes, comments, created_at) VALUES (?, ?, ?, 0, 0, NOW())";
        int postId = -1;
        
        try {
            preparedStatement = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, userName);
            preparedStatement.setString(3, content);
            preparedStatement.executeUpdate();
            
            // Oluşturulan post ID'sini al
            ResultSet rs = preparedStatement.getGeneratedKeys();
            if (rs.next()) {
                postId = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return postId;
    }

    public boolean addLike(int postId, int userId) throws SQLException {
        // Önce kullanıcının daha önce beğenip beğenmediğini kontrol et
        if (hasUserLiked(userId, postId)) {
            return false;
        }

        // Beğeni sayısını artır
        String updateQuery = "UPDATE blog_posts SET likes = likes + 1 WHERE id = ?";
        String insertQuery = "INSERT INTO blog_likes (post_id, user_id, created_at) VALUES (?, ?, NOW())";
        boolean key = false;
        
        try {
            // Beğeni sayısını güncelle
            preparedStatement = con.prepareStatement(updateQuery);
            preparedStatement.setInt(1, postId);
            preparedStatement.executeUpdate();

            // Beğeni kaydını ekle
            preparedStatement = con.prepareStatement(insertQuery);
            preparedStatement.setInt(1, postId);
            preparedStatement.setInt(2, userId);
            preparedStatement.executeUpdate();
            key = true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return key;
    }

    public boolean hasUserLiked(int userId, int postId) throws SQLException {
        String query = "SELECT * FROM blog_likes WHERE user_id = ? AND post_id = ?";
        boolean hasLiked = false;
        
        try {
            preparedStatement = con.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, postId);
            rs = preparedStatement.executeQuery();
            
            hasLiked = rs.next(); // Eğer kayıt varsa true döner
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return hasLiked;
    }

    // Hashtag'leri kaydet
    public void saveHashtags(int postId, String content) throws SQLException {
        // İçerikten hashtag'leri bul
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            String hashtag = matcher.group(1);
            String query = "INSERT INTO blog_hashtags (post_id, hashtag) VALUES (?, ?)";
            PreparedStatement pst = con.prepareStatement(query);
            pst.setInt(1, postId);
            pst.setString(2, hashtag);
            pst.executeUpdate();
        }
    }

    // Popüler hashtag'leri getir
    public ArrayList<String[]> getPopularHashtags() throws SQLException {
        ArrayList<String[]> hashtags = new ArrayList<>();
        String query = "SELECT hashtag, COUNT(*) as count FROM blog_hashtags " +
                      "GROUP BY hashtag ORDER BY count DESC LIMIT 5";
        
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(query);
        
        while (rs.next()) {
            String[] hashtag = new String[2];
            hashtag[0] = "#" + rs.getString("hashtag");
            hashtag[1] = rs.getInt("count") + " gönderi";
            hashtags.add(hashtag);
        }
        
        return hashtags;
    }

    // Getter ve Setter metodları
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
} 