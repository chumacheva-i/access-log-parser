import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Statistics {
    private int totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;
    private int entryCount;
    private Set<String> existingPages;
    private Set<String> notExistingPages;
    private Map<String, Integer> osCount;
    private Map <String, Integer> browserCount;
    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
        this.entryCount = 0;
        this.existingPages= new HashSet<>();
        this.notExistingPages = new HashSet<>();
        this.osCount= new HashMap<>();
        this.browserCount = new HashMap<>();
    }
    public void addEntry(LogEntry entry) {
        //общий трафик
        this.totalTraffic += entry.getDataSize();
        //минимальное и максимальное время
        LocalDateTime entryTime = entry.getDateTime();
        if (this.minTime == null || entryTime.isBefore(this.minTime)) {
            this.minTime = entryTime;
        }
        if (this.maxTime == null || entryTime.isAfter(this.maxTime)) {
            this.maxTime = entryTime;
        }
        // страницы с кодом 200
        if (entry.getResponseCode() == 200){
            existingPages.add(entry.getPath());
        }
        //не сущ. стр
        if (entry.getResponseCode() == 404) {
            notExistingPages.add(entry.getPath());
        }
        //считаем ОС
        String osType = entry.getUserAgent().getOsType();
        osCount.put(osType, osCount.getOrDefault(osType,0)+1);
        //бразеры
        String browserType = entry.getUserAgent().getBrowserType();
        browserCount.put(browserType, browserCount.getOrDefault(browserType, 0) + 1);
        this.entryCount++;
    }
    public Set<String> getExistingPages() {
        return new HashSet<>(existingPages); // возвращаем копию для защиты от изменений
    }
    public Map<String, Double> getOsStatistics() {
        Map<String, Double> osStatistics = new HashMap<>();

        if (entryCount == 0) {
            return osStatistics;
        }

        double sum = 0.0;
        for (Map.Entry<String, Integer> entry : osCount.entrySet()) {
            double percentage = (double) entry.getValue() / entryCount;
            osStatistics.put(entry.getKey(), percentage);
            sum += percentage;
        }

        // нормализуем, чтобы сумма была равна 1
        if (Math.abs(sum - 1.0) > 0.0001) {
            Map<String, Double> normalizedStats = new HashMap<>();
            for (Map.Entry<String, Double> entry : osStatistics.entrySet()) {
                normalizedStats.put(entry.getKey(), entry.getValue() / sum);
            }
            return normalizedStats;
        }

        return osStatistics;
    }

    public int getExistingPagesCount() {
        return existingPages.size();
    }
    public Set<String> getNonExistingPages() {
        return new HashSet<>(notExistingPages);
    }

    public int getNonExistingPagesCount() {
        return notExistingPages.size();
    }
    public String getNonExistingPagesAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Список несуществующих страниц (404):\n");
        if (notExistingPages.isEmpty()) {
            sb.append("  Нет несуществующих страниц\n");
        } else {
            for (String page : notExistingPages) {
                sb.append("  - ").append(page).append("\n");
            }
        }
        return sb.toString();
    }

    public String getOsStatisticsAsString() {
        Map<String, Double> stats = getOsStatistics(); // Теперь метод виден
        StringBuilder sb = new StringBuilder();
        sb.append("Статистика операционных систем в долях:");

        double sum = 0.0;
        for (Map.Entry<String, Double> entry : stats.entrySet()) {
            double fraction = entry.getValue();
            int countForOs = osCount.get(entry.getKey());
            sb.append(String.format("  %s: %.4f (%d запросов)\n",
                    entry.getKey(), fraction, countForOs));
            sum += entry.getValue();
        }

        sb.append(String.format("Сумма долей: %.6f\n", sum));
        return sb.toString();
    }
    public Map<String, Integer> getBrowserStatistics() {
        return new HashMap<>(browserCount);
    }
    public Map<String, Double> getBrowserStatisticsAsFractions() {
        Map<String, Double> browserFractions = new HashMap<>();
        if (entryCount == 0) {
            return browserFractions;
        }
        int totalBrowserRequests = getTotalBrowserCount();
        for (Map.Entry<String, Integer> entry : browserCount.entrySet()) {
            double fraction = (double) entry.getValue() / totalBrowserRequests;
            browserFractions.put(entry.getKey(), fraction);
        }
        return browserFractions;
    }

    public String getBrowserStatisticsAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Статистика браузеров:");

        int total = getTotalBrowserCount();

        if (total == 0) {
            sb.append("  Нет данных о браузерах");
        } else {
            sb.append(String.format("  Всего запросов: %d", total));

            for (Map.Entry<String, Integer> entry : browserCount.entrySet()) {
                String browser = entry.getKey();
                int count = entry.getValue();
                double percentage = (double) count / total * 100;
                sb.append(String.format("  %s: %d (%.2f%%)\n",
                        browser, count, percentage));
            }
        }

        return sb.toString();
    }
    public String getBrowserStatisticsDetailedAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Статистика браузеров (в долях):\n");

        Map<String, Double> fractions = getBrowserStatisticsAsFractions();
        double sum = 0.0;

        for (Map.Entry<String, Double> entry : fractions.entrySet()) {
            String browser = entry.getKey();
            double fraction = entry.getValue();
            int count = browserCount.get(browser);

            sb.append(String.format("  %s: %.4f (%d запросов)\n",
                    browser, fraction, count));
            sum += fraction;
        }

        sb.append(String.format("Сумма долей: %.6f\n", sum));
        return sb.toString();
    }
    public int getTotalBrowserCount() {
        int total = 0;
        for (Integer count : browserCount.values()) {
            total += count;
        }
        return total;
    }
    public int getTotalOsCount() {
        int total = 0;
        for (Integer count : osCount.values()) {
            total += count;
        }
        return total;
    }
    public double getTrafficRate() {
        if (minTime == null || maxTime == null || totalTraffic == 0) {
            return 0.0;
        }
        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);
        //считаем как 1 час чтобы избежать деления на 0
        if (hoursBetween < 1) {
            hoursBetween = 1;
        }
        //средний трафик в час
        return (double) totalTraffic / hoursBetween;
    }
    public int getTotalTraffic() {
        return totalTraffic;
    }
    public LocalDateTime getMinTime() {
        return minTime;
    }
    public LocalDateTime getMaxTime() {
        return maxTime;
    }
    public int getEntryCount() {
        return entryCount;
    }
}