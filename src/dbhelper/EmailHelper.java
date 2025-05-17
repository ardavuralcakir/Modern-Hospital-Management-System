package dbhelper;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailHelper {
    private static final String FROM_EMAIL = "babakama34@gmail.com"; // Gmail adresiniz
    private static final String PASSWORD = "zsmv lmjr fzqh hqst"; // Gmail uygulama şifreniz

    public static void sendVerificationCode(String toEmail, String code) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, PASSWORD);
            }
        });

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
        message.setSubject("Şifre Sıfırlama Doğrulama Kodu");
        
        String htmlContent = String.format(
            "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;'>" +
            "<h2 style='color: #065F46;'>Şifre Sıfırlama Doğrulama Kodu</h2>" +
            "<p>Merhaba,</p>" +
            "<p>Şifre sıfırlama talebiniz için doğrulama kodunuz:</p>" +
            "<div style='background-color: #F0FDFA; padding: 20px; border-radius: 8px; margin: 20px 0;'>" +
            "<h1 style='color: #065F46; text-align: center; margin: 0;'>%s</h1>" +
            "</div>" +
            "<p style='color: #4B5563; font-size: 14px;'>Bu kod 20 saniye süreyle geçerlidir.</p>" +
            "<p style='color: #4B5563; font-size: 14px;'>Eğer bu işlemi siz yapmadıysanız, lütfen bu e-postayı dikkate almayınız.</p>" +
            "</div>",
            code
        );

        message.setContent(htmlContent, "text/html; charset=UTF-8");
        Transport.send(message);
    }
}