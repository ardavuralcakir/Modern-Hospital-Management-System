package Model;

import dbhelper.DBConnection;
import java.sql.*;
import java.util.*;
import java.util.regex.*;

public class Blog {
    private int id;
    private String title;
    private String content;
    private String author;
    private int authorId;
    private Timestamp date;
    private int likeCount;
    private int commentCount;
    private Set<String> hashtags;
    private DBConnection conn = new DBConnection();

    public Blog() {
        this.hashtags = new HashSet<>();
    }

    public Blog(int id, String title, String content, String author, int authorId, Timestamp date) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.author = author;
        this.authorId = authorId;
        this.date = date;
        this.hashtags = extractHashtags(content);
        this.likeCount = 0;
        this.commentCount = 0;
    }

    private Set<String> extractHashtags(String content) {
        Set<String> tags = new HashSet<>();
        Pattern pattern = Pattern.compile("#(\\w+)");
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            tags.add(matcher.group(1).toLowerCase());
        }
        return tags;
    }

    public ArrayList<Blog> getBlogList() throws SQLException {
        ArrayList<Blog> list = new ArrayList<>();
        Connection con = conn.connDb();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(
            "SELECT b.*, u.name as doctor_name, " +
            "(SELECT COUNT(*) FROM blog_likes l WHERE l.post_id = b.id) as like_count, " +
            "(SELECT COUNT(*) FROM blog_comments c WHERE c.post_id = b.id) as comment_count " +
            "FROM blog b " +
            "LEFT JOIN user u ON b.doctor_id = u.id " +
            "ORDER BY b.date DESC, like_count DESC, comment_count DESC"
        );
        while (rs.next()) {
            Blog obj = new Blog();
            obj.setId(rs.getInt("id"));
            obj.setTitle(rs.getString("title"));
            obj.setContent(rs.getString("content"));
            obj.setAuthor(rs.getString("doctor_name"));
            obj.setAuthorId(rs.getInt("doctor_id"));
            obj.setDate(rs.getTimestamp("date"));
            obj.setLikeCount(rs.getInt("like_count"));
            obj.setCommentCount(rs.getInt("comment_count"));
            obj.setHashtags(getHashtagsForPost(obj.getId()));
            list.add(obj);
        }
        return list;
    }

    private Set<String> getHashtagsForPost(int postId) throws SQLException {
        Set<String> hashtags = new HashSet<>();
        Connection con = conn.connDb();
        PreparedStatement ps = con.prepareStatement(
            "SELECT hashtag FROM blog_hashtags WHERE post_id = ?"
        );
        ps.setInt(1, postId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            hashtags.add(rs.getString("hashtag"));
        }
        return hashtags;
    }

    public boolean addBlog(String title, String content, int doctorId) throws SQLException {
        Connection con = conn.connDb();
        con.setAutoCommit(false);
        try {
            // Blog girdisini ekle
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO blog (title, content, doctor_id, date) VALUES (?, ?, ?, NOW())",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, title);
            ps.setString(2, content);
            ps.setInt(3, doctorId);
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Blog oluşturulamadı.");
            }

            // Yeni blog ID'sini al
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (!generatedKeys.next()) {
                throw new SQLException("Blog ID alınamadı.");
            }
            int blogId = generatedKeys.getInt(1);

            // Hashtag'leri ekle
            Set<String> hashtags = extractHashtags(content);
            if (!hashtags.isEmpty()) {
                PreparedStatement psHashtag = con.prepareStatement(
                    "INSERT INTO blog_hashtags (post_id, hashtag) VALUES (?, ?)"
                );
                for (String hashtag : hashtags) {
                    psHashtag.setInt(1, blogId);
                    psHashtag.setString(2, hashtag);
                    psHashtag.addBatch();
                }
                psHashtag.executeBatch();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    public boolean likeBlog(int blogId, int userId) throws SQLException {
        String query = "INSERT INTO blog_likes (post_id, user_id, created_at) VALUES (?, ?, NOW())";
        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, blogId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean unlikeBlog(int blogId, int userId) throws SQLException {
        String query = "DELETE FROM blog_likes WHERE post_id = ? AND user_id = ?";
        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, blogId);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean deleteBlog(int blogId) throws SQLException {
        Connection con = conn.connDb();
        con.setAutoCommit(false);
        try {
            // Önce blog_likes tablosundan beğenileri sil
            PreparedStatement ps = con.prepareStatement("DELETE FROM blog_likes WHERE post_id = ?");
            ps.setInt(1, blogId);
            ps.executeUpdate();

            // Sonra blog_comments tablosundan yorumları sil
            ps = con.prepareStatement("DELETE FROM blog_comments WHERE post_id = ?");
            ps.setInt(1, blogId);
            ps.executeUpdate();

            // Son olarak blog tablosundan blog girdisini sil
            ps = con.prepareStatement("DELETE FROM blog WHERE id = ?");
            ps.setInt(1, blogId);
            int affectedRows = ps.executeUpdate();

            con.commit();
            return affectedRows > 0;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    public boolean addComment(int blogId, int userId, String userName, String content) throws SQLException {
        String query = "INSERT INTO blog_comments (post_id, user_id, user_name, content) VALUES (?, ?, ?, ?)";
        Connection con = conn.connDb();
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, blogId);
        ps.setInt(2, userId);
        ps.setString(3, userName);
        ps.setString(4, content);
        return ps.executeUpdate() > 0;
    }

    public boolean isLikedByUser(int blogId, int userId) throws SQLException {
        String query = "SELECT COUNT(*) as count FROM blog_likes WHERE post_id = ? AND user_id = ?";
        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, blogId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
            return false;
        }
    }

    public ArrayList<Comment> getComments(int blogId) throws SQLException {
        ArrayList<Comment> comments = new ArrayList<>();
        String query = "SELECT c.*, u.name as user_name FROM blog_comments c " +
                      "JOIN user u ON c.user_id = u.id " +
                      "WHERE c.post_id = ? ORDER BY c.created_at DESC";
        
        try (Connection con = conn.connDb();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, blogId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Comment comment = new Comment();
                comment.setId(rs.getInt("id"));
                comment.setUserId(rs.getInt("user_id"));
                comment.setContent(rs.getString("content"));
                comment.setUserName(rs.getString("user_name"));
                comment.setDate(rs.getString("created_at"));
                comments.add(comment);
            }
        }
        return comments;
    }

    public String getTruncatedContent() {
        if (content == null) return "";
        if (content.length() <= 250) return content;
        return content.substring(0, 250) + "...";
    }

    public Connection getConnection() throws SQLException {
        return conn.connDb();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { 
        return content; 
    }
    public void setContent(String content) { 
        this.content = content;
        this.hashtags = extractHashtags(content);
    }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }
    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
    public int getLikeCount() { return likeCount; }
    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
    public Set<String> getHashtags() { return hashtags; }
    public void setHashtags(Set<String> hashtags) { this.hashtags = hashtags; }
} 