package Model;

import dbhelper.DBConnection;
import java.sql.*;
import java.util.ArrayList;

public class HashtagTrend {
    private String hashtag;
    private int useCount;
    private int totalLikes;
    private int totalComments;
    private Timestamp createdAt;
    private DBConnection conn = new DBConnection();

    public HashtagTrend() {}

    public HashtagTrend(String hashtag, int useCount, int totalLikes, int totalComments, Timestamp createdAt) {
        this.hashtag = hashtag;
        this.useCount = useCount;
        this.totalLikes = totalLikes;
        this.totalComments = totalComments;
        this.createdAt = createdAt;
    }

    public ArrayList<HashtagTrend> getTrendingHashtags() throws SQLException {
        ArrayList<HashtagTrend> trends = new ArrayList<>();
        
        String query = 
            "SELECT h.hashtag, " +
            "       COUNT(*) as use_count, " +
            "       (SELECT COUNT(*) FROM blog_likes l WHERE l.post_id IN " +
            "           (SELECT post_id FROM blog_hashtags WHERE hashtag = h.hashtag)) as total_likes, " +
            "       (SELECT COUNT(*) FROM blog_comments c WHERE c.post_id IN " +
            "           (SELECT post_id FROM blog_hashtags WHERE hashtag = h.hashtag)) as total_comments, " +
            "       MAX(b.date) as last_used " +
            "FROM blog_hashtags h " +
            "JOIN blog b ON h.post_id = b.id " +
            "WHERE b.date >= NOW() - INTERVAL 24 HOUR " +
            "GROUP BY h.hashtag " +
            "ORDER BY total_likes DESC, total_comments DESC, use_count DESC " +
            "LIMIT 10";

        Connection con = conn.connDb();
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            HashtagTrend trend = new HashtagTrend(
                rs.getString("hashtag"),
                rs.getInt("use_count"),
                rs.getInt("total_likes"),
                rs.getInt("total_comments"),
                rs.getTimestamp("last_used")
            );
            trends.add(trend);
        }
        return trends;
    }

    // Getters and Setters
    public String getHashtag() { return hashtag; }
    public void setHashtag(String hashtag) { this.hashtag = hashtag; }
    public int getUseCount() { return useCount; }
    public void setUseCount(int useCount) { this.useCount = useCount; }
    public int getTotalLikes() { return totalLikes; }
    public void setTotalLikes(int totalLikes) { this.totalLikes = totalLikes; }
    public int getTotalComments() { return totalComments; }
    public void setTotalComments(int totalComments) { this.totalComments = totalComments; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
} 