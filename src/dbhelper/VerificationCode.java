package dbhelper;

import java.sql.*;
import java.util.UUID;
import java.util.Timer;
import java.util.TimerTask;

public class VerificationCode implements AutoCloseable {
    private String code;
    private long expirationTime;
    private static final long EXPIRATION_DURATION = 30000; // 10 dakika (milisaniye cinsinden)
    private DBConnection conn = new DBConnection();
    private Timer expirationTimer;

    public VerificationCode() {
        this.code = generateCode();
        this.expirationTime = System.currentTimeMillis() + EXPIRATION_DURATION;
        scheduleExpiration();
    }

    public VerificationCode(String code, long expirationTime) {
        this.code = code;
        this.expirationTime = expirationTime;
        scheduleExpiration();
    }

    private String generateCode() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private void scheduleExpiration() {
        expirationTimer = new Timer(true);
        expirationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                try (Connection con = conn.connDb();
                     PreparedStatement pst = con.prepareStatement(
                         "DELETE FROM verification_codes WHERE code = ?")) {
                    pst.setString(1, code);
                    pst.executeUpdate();
                    System.out.println("Verification code expired and deleted: " + code);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                expirationTimer.cancel();
            }
        }, EXPIRATION_DURATION);
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expirationTime;
    }

    public boolean insertVerificationCode() {
        try (Connection con = conn.connDb();
             PreparedStatement pst = con.prepareStatement(
                "INSERT INTO verification_codes (code, expiration_time) VALUES (?, ?)")) {
            
            pst.setString(1, code);
            pst.setTimestamp(2, new Timestamp(expirationTime));
            return pst.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean verifyCode(String code) {
        DBConnection conn = new DBConnection();
        try (Connection con = conn.connDb();
             PreparedStatement pst = con.prepareStatement(
                "SELECT * FROM verification_codes WHERE code = ? AND expiration_time > ?")) {
            
            pst.setString(1, code);
            pst.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            
            ResultSet rs = pst.executeQuery();
            boolean isValid = rs.next();
            
            if (isValid) {
                deleteCode(code, con);
            }
            
            return isValid;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void deleteCode(String code, Connection con) {
        try (PreparedStatement pst = con.prepareStatement(
                "DELETE FROM verification_codes WHERE code = ?")) {
            pst.setString(1, code);
            pst.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteExpiredCodes() {
        DBConnection conn = new DBConnection();
        try (Connection con = conn.connDb();
             PreparedStatement pst = con.prepareStatement(
                "DELETE FROM verification_codes WHERE expiration_time <= ?")) {
            
            pst.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            int deletedCount = pst.executeUpdate();
            if (deletedCount > 0) {
                System.out.println(deletedCount + " expired verification code(s) deleted.");
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (expirationTimer != null) {
            expirationTimer.cancel();
        }
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }
}