package View;

import Model.Admin;
import Model.Clinic;
import dbhelper.DBConnection;
import dbhelper.Helper;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import javax.swing.JPanel;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

/**
 * GrafikPaneli:
 * - JavaFX tabanlı LineChart
 * - Haftalık / Aylık / Yıllık metotlarda y-ekseni ve x-ekseni ayarları
 * - Doktor / Klinik / Hasta grafik metodları (veritabanı sorguları)
 */
public class GrafikPaneli {
    private DBConnection dbConnection = new DBConnection();

    private JFXPanel fxPanel;
    private LineChart<String, Number> lineChart;
    private CategoryAxis xAxis;
    private NumberAxis yAxis;
    private Scene scene;
    private boolean isInitialized = false;
    private StackPane root;

    // Ana renk (yeşil)
    private static final String GREEN_HEX = "#065F46";

    // --------------------------------------------------------------------
    // Constructor
    // --------------------------------------------------------------------
    public GrafikPaneli(Admin admin, Clinic clinic) {
        initFXPanel();
    }

    // --------------------------------------------------------------------
    // JavaFX panelini başlat
    // --------------------------------------------------------------------
    private void initFXPanel() {
        fxPanel = new JFXPanel();
        fxPanel.setPreferredSize(new Dimension(920, 260));

        CountDownLatch initLatch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                createChart();
            } finally {
                initLatch.countDown();
            }
        });

        try {
            initLatch.await();
            isInitialized = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // --------------------------------------------------------------------
    // Chart oluştur
    // --------------------------------------------------------------------
    private void createChart() {
        xAxis = new CategoryAxis();
        yAxis = new NumberAxis();

        xAxis.setLabel("Tarih");
        yAxis.setLabel("Randevu Sayısı");

        // X ekseni boşluk ve rota ayarları
        xAxis.setStartMargin(0);
        xAxis.setEndMargin(0);
        xAxis.setGapStartAndEnd(false);
        xAxis.setTickLabelRotation(0);

        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Randevu İstatistikleri");
        lineChart.setCreateSymbols(true);
        lineChart.setAnimated(false);
        lineChart.setLegendVisible(false);

        root = new StackPane(lineChart);
        root.setStyle("-fx-background-color: white;");
        scene = new Scene(root, 920, 260);
        fxPanel.setScene(scene);
    }

    // --------------------------------------------------------------------
    // 1) HAFTALIK GRAFİK
    //    Y ekseni 1'er 1'er (değiştirildi)
    // --------------------------------------------------------------------
    public void gosterHaftalikGrafik(Date start, Date end) {
        if (!isInitialized) return;

        List<DataPoint> dataPoints = getRandevuCountByDateRange(start, end, "DAY");
        if (dataPoints.isEmpty() || allZeroValues(dataPoints)) {
            showNoDataMessage();
            return;
        }

        Platform.runLater(() -> {
            showChart();
            lineChart.getData().clear();

            lineChart.setTitle("Haftalık Randevu Dağılımı: "
                + Helper.formatDateTr(start) + " - " + Helper.formatDateTr(end));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Randevular");

            // Eski kodda => yAxis.setTickUnit(Math.max(1, maxValue / 10));
            // Şimdi => daima 1'er 1'er
            int maxValue = dataPoints.stream()
                    .mapToInt(dp -> dp.value)
                    .max()
                    .orElse(0);

            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(maxValue + 1); 
            yAxis.setTickUnit(1); // 1'er 1'er artış
            setupIntegerYLabels();

            for (DataPoint dp : dataPoints) {
                series.getData().add(new XYChart.Data<>(formatDateLabel(dp.label), dp.value));
            }
            lineChart.getData().add(series);
            applyGreenColor(series);
        });
    }

    // --------------------------------------------------------------------
    // 2) AYLIK GRAFİK
    //    X ekseni etiketleri yatay kalsın (rotation=0)
    // --------------------------------------------------------------------
    public void gosterAylikGrafik(Date start, Date end) {
        if (!isInitialized) return;

        List<DataPoint> dataPoints = getRandevuCountByDateRange(start, end, "DAY");
        if (dataPoints.isEmpty() || allZeroValues(dataPoints)) {
            showNoDataMessage();
            return;
        }

        Platform.runLater(() -> {
            showChart();
            lineChart.getData().clear();

            lineChart.setTitle("Aylık Randevu Dağılımı: "
                + Helper.formatDateTr(start) + " - " + Helper.formatDateTr(end));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Randevular");

            int maxValue = dataPoints.stream().mapToInt(dp -> dp.value).max().orElse(0);
            int upperBound = ((maxValue + 9) / 10) * 10 + 10;

            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(upperBound);
            yAxis.setTickUnit(10); 
            setupIntegerYLabels();

            // X ekseni: yatay => rotation=0
            xAxis.setTickLabelRotation(0);

            for (DataPoint dp : dataPoints) {
                series.getData().add(new XYChart.Data<>(dp.label, dp.value));
            }

            lineChart.getData().add(series);
            applyGreenColor(series);
        });
    }

    // --------------------------------------------------------------------
    // 3) YILLIK GRAFİK
    //    Y ekseni 50'şer 50'şer (değiştirildi)
    //    X ekseni Ocak-Aralık (12 ay)
    // --------------------------------------------------------------------
    public void gosterYillikGrafik(Date start, Date end) {
        if (!isInitialized) return;

        List<DataPoint> dataPoints = getRandevuCountByDateRange(start, end, "MONTH");
        if (dataPoints.isEmpty() || allZeroValues(dataPoints)) {
            showNoDataMessage();
            return;
        }

        Platform.runLater(() -> {
            showChart();
            lineChart.getData().clear();

            lineChart.setTitle("Yıllık Randevu Dağılımı: "
                + Helper.formatDateTr(start) + " - " + Helper.formatDateTr(end));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Randevular");

            // Y ekseni ayarları (50'şer artış)
            int maxValue = dataPoints.stream().mapToInt(dp -> dp.value).max().orElse(0);
            int upperBound = ((maxValue + 49) / 50) * 50 + 50;

            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(upperBound);
            yAxis.setTickUnit(50);
            setupIntegerYLabels();

            // Tüm ayları sırayla ekle (Ocak'tan Aralık'a)
            LocalDate currentDate = toLocalDate(start);
            int startYear = currentDate.getYear();
            
            // Kesinlikle 12 ay olacak şekilde döngü
            String[] aylar = new String[12];
            for (int month = 1; month <= 12; month++) {
                String monthLabel = Helper.getMonthNameTr(month) + " " + startYear;
                
                // Veri varsa onu kullan, yoksa 0 göster
                int value = dataPoints.stream()
                    .filter(dp -> dp.label.equals(monthLabel))
                    .findFirst()
                    .map(dp -> dp.value)
                    .orElse(0);
                    
                aylar[month-1] = monthLabel;
                series.getData().add(new XYChart.Data<>(monthLabel, value));
            }

            lineChart.getData().add(series);
            applyGreenColor(series);

            // X ekseni ayarları
            xAxis.setTickLabelRotation(0);
            
            // X ekseni için kesin olarak tüm ayları göster
            xAxis.setCategories(FXCollections.observableArrayList(aylar));
        });
    }

    // --------------------------------------------------------------------
    // Doktor kayıtları
    // --------------------------------------------------------------------
    public void gosterDoktorKayitlari(Date startDate, Date endDate) {
        if (!isInitialized) return;

        List<DataPoint> dataPoints = getDoctorRegisterCountByDate(startDate, endDate);
        if (dataPoints.isEmpty() || allZeroValues(dataPoints)) {
            showNoDataMessage();
            return;
        }

        Platform.runLater(() -> {
            showChart();
            lineChart.getData().clear();
            lineChart.setTitle("Doktorlar: "
                + Helper.formatDateTr(startDate) + " - " + Helper.formatDateTr(endDate));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Doktorlar");

            int maxValue = dataPoints.stream().mapToInt(dp -> dp.value).max().orElse(0);

            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(maxValue + (maxValue * 0.2));
            yAxis.setTickUnit(Math.max(1, maxValue / 10));
            setupIntegerYLabels();

            for (DataPoint dp : dataPoints) {
                series.getData().add(new XYChart.Data<>(formatDateLabel(dp.label), dp.value));
            }
            lineChart.getData().add(series);
            applyGreenColor(series);
        });
    }

    public void gosterKlinikKayitlari(Date startDate, Date endDate) {
        if (!isInitialized) return;

        List<DataPoint> dataPoints = getClinicRegisterCountByDate(startDate, endDate);
        if (dataPoints.isEmpty() || allZeroValues(dataPoints)) {
            showNoDataMessage();
            return;
        }

        Platform.runLater(() -> {
            showChart();
            lineChart.getData().clear();
            lineChart.setTitle("Poliklinikler: "
                + Helper.formatDateTr(startDate) + " - " + Helper.formatDateTr(endDate));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Poliklinikler");

            int maxValue = dataPoints.stream().mapToInt(dp -> dp.value).max().orElse(0);

            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(maxValue + (maxValue * 0.2));
            yAxis.setTickUnit(Math.max(1, maxValue / 10));
            setupIntegerYLabels();

            for (DataPoint dp : dataPoints) {
                series.getData().add(new XYChart.Data<>(formatDateLabel(dp.label), dp.value));
            }
            lineChart.getData().add(series);
            applyGreenColor(series);
        });
    }

    public void gosterHastaKayitlari(Date startDate, Date endDate) {
        if (!isInitialized) return;

        List<DataPoint> dataPoints = getHastaRegisterCountByDate(startDate, endDate);
        if (dataPoints.isEmpty() || allZeroValues(dataPoints)) {
            showNoDataMessage();
            return;
        }

        Platform.runLater(() -> {
            showChart();
            lineChart.getData().clear();
            lineChart.setTitle("Hastalar: "
                + Helper.formatDateTr(startDate) + " - " + Helper.formatDateTr(endDate));

            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Hastalar");

            int maxValue = dataPoints.stream()
                    .mapToInt(dp -> dp.value)
                    .max()
                    .orElse(0);

            yAxis.setAutoRanging(false);
            yAxis.setLowerBound(0);
            yAxis.setUpperBound(maxValue + (maxValue * 0.2));
            yAxis.setTickUnit(Math.max(1, maxValue / 10));
            setupIntegerYLabels();

            for (DataPoint dp : dataPoints) {
                series.getData().add(new XYChart.Data<>(formatDateLabel(dp.label), dp.value));
            }
            lineChart.getData().add(series);
            applyGreenColor(series);
        });
    }

    // --------------------------------------------------------------------
    // Veritabanından "DAY" veya "MONTH" bazında randevu sayısı
    // --------------------------------------------------------------------
    private List<DataPoint> getRandevuCountByDateRange(Date startDate, Date endDate, String groupBy) {
        List<DataPoint> result = new ArrayList<>();
        if (startDate == null || endDate == null) return result;

        LocalDate start = toLocalDate(startDate);
        LocalDate finish = toLocalDate(endDate);

        String sql;
        if ("MONTH".equals(groupBy)) {
            sql = "SELECT DATE_FORMAT(app_date, '%Y-%m') AS period, " +
                  "COUNT(*) AS count FROM appointment " +
                  "WHERE app_date BETWEEN ? AND ? " +
                  "GROUP BY DATE_FORMAT(app_date, '%Y-%m') " +
                  "ORDER BY period ASC";
            } else {
            // groupBy = "DAY"
            sql = "SELECT DATE(app_date) AS period, COUNT(*) AS count " +
                  "FROM appointment " +
                  "WHERE app_date BETWEEN ? AND ? " +
                  "GROUP BY DATE(app_date) " +
                  "ORDER BY period ASC";
        }

        try (Connection con = dbConnection.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(start));
            ps.setDate(2, java.sql.Date.valueOf(finish));

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String period = rs.getString("period");
                int count = rs.getInt("count");

                // Ay bazlı vs. Gün bazlı label
                String label;
                if ("MONTH".equals(groupBy)) {
                    // period = "YYYY-MM"
                    String[] parts = period.split("-");
                    String year = parts[0];
                    int mm = Integer.parseInt(parts[1]);
                    label = Helper.getMonthNameTr(mm) + " " + year;
                } else {
                    // period = "YYYY-MM-DD"
                    LocalDate date = LocalDate.parse(period);
                    label = date.getDayOfMonth() + " " + Helper.getMonthNameTr(date.getMonthValue());
                }
                result.add(new DataPoint(label, count));
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Gün bazlı veride aradaki boş günler 0 ile doldurulabilir
        if (!"MONTH".equals(groupBy)) {
            List<DataPoint> filledData = new ArrayList<>();
            LocalDate current = start;
            while (!current.isAfter(finish)) {
                String currentLabel = current.getDayOfMonth() + " "
                        + Helper.getMonthNameTr(current.getMonthValue());

                boolean found = false;
                for (DataPoint dp : result) {
                    if (dp.label.equals(currentLabel)) {
                        filledData.add(dp);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    filledData.add(new DataPoint(currentLabel, 0));
                }
                current = current.plusDays(1);
            }
            return filledData;
        }

        return result;
    }

    // --------------------------------------------------------------------
    // Doktor kayıtları (user.type='doktor')
    // --------------------------------------------------------------------
    private List<DataPoint> getDoctorRegisterCountByDate(Date startDate, Date endDate) {
        return getUserRegisterCountByType(startDate, endDate, "doktor");
    }

    // --------------------------------------------------------------------
    // Poliklinik kayıtları (clinic tablosu)
    // --------------------------------------------------------------------
    private List<DataPoint> getClinicRegisterCountByDate(Date startDate, Date endDate) {
        List<DataPoint> result = new ArrayList<>();
        if (startDate == null || endDate == null) return result;

        LocalDate start = toLocalDate(startDate);
        LocalDate finish = toLocalDate(endDate);

        String sql =
            "SELECT DATE(created_at) AS gun, COUNT(*) AS adet " +
            "FROM clinic " +
            "WHERE created_at BETWEEN ? AND ? " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY DATE(created_at) ASC";

        try (Connection con = dbConnection.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(start));
            ps.setDate(2, java.sql.Date.valueOf(finish));
            ResultSet rs = ps.executeQuery();

            List<DataRow> tempData = new ArrayList<>();
            while (rs.next()) {
                java.sql.Date sqld = rs.getDate("gun");
                LocalDate localD = (sqld != null) ? sqld.toLocalDate() : null;
                int adet = rs.getInt("adet");
                tempData.add(new DataRow(localD, adet));
            }
            rs.close();

            LocalDate loopDay = start;
            while (!loopDay.isAfter(finish)) {
                int count = 0;
                for (DataRow dr : tempData) {
                    if (dr.dateValue != null && dr.dateValue.isEqual(loopDay)) {
                        count = dr.count;
                        break;
                    }
                }
                String label = String.format("%d %s",
                        loopDay.getDayOfMonth(),
                        Helper.getMonthNameTr(loopDay.getMonthValue()));
                result.add(new DataPoint(label, count));

                loopDay = loopDay.plusDays(1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // --------------------------------------------------------------------
    // Hasta kayıtları (user.type='hasta')
    // --------------------------------------------------------------------
    private List<DataPoint> getHastaRegisterCountByDate(Date startDate, Date endDate) {
        return getUserRegisterCountByType(startDate, endDate, "hasta");
    }

    // --------------------------------------------------------------------
    // USER tablo: Doktor/Hasta kayıtları
    // --------------------------------------------------------------------
    private List<DataPoint> getUserRegisterCountByType(Date startDate, Date endDate, String userType) {
        List<DataPoint> result = new ArrayList<>();
        if (startDate == null || endDate == null) return result;

        LocalDate start = toLocalDate(startDate);
        LocalDate finish = toLocalDate(endDate);

        String sql =
            "SELECT DATE(created_at) AS gun, COUNT(*) AS adet " +
            "FROM user " +
            "WHERE type=? AND created_at BETWEEN ? AND ? " +
            "GROUP BY DATE(created_at) " +
            "ORDER BY DATE(created_at) ASC";

        try (Connection con = dbConnection.connDb();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, userType);
            ps.setDate(2, java.sql.Date.valueOf(start));
            ps.setDate(3, java.sql.Date.valueOf(finish));

            ResultSet rs = ps.executeQuery();
            List<DataRow> tempData = new ArrayList<>();
            while (rs.next()) {
                java.sql.Date sqld = rs.getDate("gun");
                LocalDate localD = (sqld != null) ? sqld.toLocalDate() : null;
                int adet = rs.getInt("adet");
                tempData.add(new DataRow(localD, adet));
            }
            rs.close();

            // Gün gün doldur
            LocalDate loopDay = start;
            while (!loopDay.isAfter(finish)) {
                int count = 0;
                for (DataRow dr : tempData) {
                    if (dr.dateValue != null && dr.dateValue.isEqual(loopDay)) {
                        count = dr.count;
                        break;
                    }
                }
                String label = String.format("%d %s",
                        loopDay.getDayOfMonth(),
                        Helper.getMonthNameTr(loopDay.getMonthValue()));
                result.add(new DataPoint(label, count));

                loopDay = loopDay.plusDays(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // --------------------------------------------------------------------
    // Y eksenini tam sayı formatında göster
    // --------------------------------------------------------------------
    private void setupIntegerYLabels() {
        yAxis.setMinorTickCount(0);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number object) {
                if (object != null) {
                    return String.valueOf(object.intValue());
                }
                return "";
            }
        });
    }

    // --------------------------------------------------------------------
    // "Veri yok" mesajı
    // --------------------------------------------------------------------
    private void showNoDataMessage() {
        Platform.runLater(() -> {
            Text noDataText = new Text("Bu bölümde henüz veri yok");
            noDataText.setStyle("-fx-font-size: 20px; -fx-fill: #666;");
            root.getChildren().clear();
            root.getChildren().add(noDataText);
        });
    }

    // --------------------------------------------------------------------
    // Grafiği tekrar göster
    // --------------------------------------------------------------------
    private void showChart() {
        Platform.runLater(() -> {
            root.getChildren().clear();
            root.getChildren().add(lineChart);
        });
    }

    // --------------------------------------------------------------------
    // getGrafikPaneli
    // --------------------------------------------------------------------
    public JPanel getGrafikPaneli() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(fxPanel, BorderLayout.CENTER);
        return panel;
    }

    // --------------------------------------------------------------------
    // grafigiYenile
    // --------------------------------------------------------------------
    public void grafigiYenile() {
        if (!isInitialized) return;
        Platform.runLater(() -> {
            lineChart.getData().clear();
            lineChart.setTitle("Randevu İstatistikleri");
            showChart();
        });
    }

    // --------------------------------------------------------------------
    // formatDateLabel ("21 Aralık" gibi ifadelere "0 padding" ekler)
    // --------------------------------------------------------------------
    private String formatDateLabel(String originalLabel) {
        // "21 Aralık" => eğer 1 haneliyse "01 Aralık"
        String[] parts = originalLabel.split(" ");
        if (parts.length != 2) return originalLabel;

        String day = parts[0];
        if (day.length() == 1) {
            day = "0" + day;
        }
        return day + " " + parts[1];
    }

    // --------------------------------------------------------------------
    // applyGreenColor (Çizgi ve noktaları yeşil yap)
    // --------------------------------------------------------------------
    private void applyGreenColor(XYChart.Series<String, Number> series) {
        if (series.getNode() != null) {
            series.getNode().setStyle("-fx-stroke: " + GREEN_HEX + "; -fx-stroke-width: 2px;");
        }
        for (XYChart.Data<String, Number> data : series.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-background-color: " + GREEN_HEX + ", white;");
            }
        }
    }

    // --------------------------------------------------------------------
    // Tüm veri 0 mı?
    // --------------------------------------------------------------------
    private boolean allZeroValues(List<DataPoint> dataPoints) {
        return dataPoints.stream().allMatch(dp -> dp.value == 0);
    }

    // --------------------------------------------------------------------
    // toLocalDate
    // --------------------------------------------------------------------
    private LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // --------------------------------------------------------------------
    // İç sınıflar (veri tutucu)
    // --------------------------------------------------------------------
    private static class DataRow {
        LocalDate dateValue;
        int count;
        DataRow(LocalDate d, int c) {
            this.dateValue = d;
            this.count = c;
        }
    }

    private static class DataPoint {
        String label;
        int value;
        DataPoint(String label, int value) {
            this.label = label;
            this.value = value;
        }
    }
}
