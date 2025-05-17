package Model;

import dbhelper.DBConnection;

/**
 * User tablosundaki temel kullanıcı bilgilerini tutan sınıf.
 * Örnek alanlar: id, tcno, name, password, type
 * 'type' alanı: "doktor", "hasta", "admin" vb. roller için kullanılabilir.
 */
public class User {
    // Veritabanı bağlantısı (isteğe bağlı, burada sadece referans tutuluyor)
    protected DBConnection conn = new DBConnection();

    // Temel alanlar
    private int id;             // user tablosundaki ID
    private String tcno;        // Kullanıcının TC kimlik numarası
    private String name;        // Ad-Soyad
    private String password;    // Şifre
    private String type;        // Rol: "doktor", "hasta", "admin" vb.
    
    // İletişim ve kişisel bilgiler
    private String email;       // E-posta
    private String phone;       // Telefon numarası
    private String gender;      // Cinsiyet
    private boolean emailVerified; // E-posta doğrulama durumu

    /**
     * Boş constructor
     */
    public User() {}

    /**
     * Parametreli constructor (Eski sürüm, yeni alanlar null kalır)
     *
     * @param id        user tablosundaki ID
     * @param tcno      TC kimlik no
     * @param name      Ad-Soyad
     * @param password  Şifre
     * @param type      Rol (örn. doktor, hasta, admin vb.)
     */
    public User(int id, String tcno, String name, String password, String type) {
        this(id, tcno, name, password, type, null, null, null);
    }

    /**
     * Parametreli constructor (Tüm alanları içeren yeni sürüm)
     *
     * @param id        user tablosundaki ID
     * @param tcno      TC kimlik no
     * @param name      Ad-Soyad
     * @param password  Şifre
     * @param type      Rol (örn. doktor, hasta, admin vb.)
     * @param email     E-posta adresi
     * @param phone     Telefon numarası
     * @param gender    Cinsiyet
     */
    public User(int id, String tcno, String name, String password, String type,
                String email, String phone, String gender) {
        this.id = id;
        this.tcno = tcno;
        this.name = name;
        this.password = password;
        this.type = type;
        this.email = email;
        this.phone = phone;
        this.gender = gender;
        this.emailVerified = false; // Varsayılan olarak doğrulanmamış
    }

    // -- Getter / Setter Metotları --

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTcno() {
        return tcno;
    }

    public void setTcno(String tcno) {
        this.tcno = tcno;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Şifreyi ayarlarken ek kontrol veya şifre politikası eklenebilir.
     * @param password Yeni şifre
     */
    public void setPassword(String password) {
        // Örneğin: if (password.length() < 6) { ... } gibi kontroller yapabilirsiniz.
        this.password = password;
    }

    public String getType() {
        return type;
    }

    /**
     * Kullanıcının rolü ("hasta", "doktor", "admin" vb.)
     * @param type Rol
     */
    public void setType(String type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        // Örneğin e-posta formatını kontrol edebilirsiniz: if (!email.contains("@")) { ... }
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        // Dilerseniz telefon formatı kontrolü ekleyebilirsiniz.
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        // Örneğin "Erkek", "Kadın", "Diğer" vb. validasyon yapabilirsiniz.
        this.gender = gender;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
}
