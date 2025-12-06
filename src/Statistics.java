import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Statistics {
    private int totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;
    private int entryCount;
    private Set<String> existingPages;
    private Set<String> notExistingPages;
    private Map<String, Integer> osCount;
    private Map <String, Integer> browserCount;
    private Set<LogEntry> logEntries;
    private int errorRequests; // количество ошибочных запросов (4xx и 5xx)
    private int botRequests; // количество запросов от ботов
    private int humanRequests; // количество запросов от реальных пользователей
    private Set<String> humanUserIPs; // уникальные IP реальных пользователей

    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
        this.entryCount = 0;
        this.existingPages = new HashSet<>();
        this.notExistingPages = new HashSet<>();
        this.osCount = new HashMap<>();
        this.browserCount = new HashMap<>();
        this.logEntries = new HashSet<>();
        this.errorRequests = 0;
        this.botRequests = 0;
        this.humanRequests = 0;
        this.humanUserIPs = new HashSet<>();
        this.logEntries = new HashSet<>();
    }

    public void addEntry(LogEntry entry) {
        logEntries.add(entry);
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
        // ошибочные запросы (4xx или 5xx)
        int responseCode = entry.getResponseCode();
        if (responseCode >= 400 && responseCode < 600) {
            errorRequests++;
        }

        // подсчет ботов и реальных пользователей
        if (entry.getUserAgent().isBot()) {
            botRequests++;
        } else {
            humanRequests++;
            // добавляем IP реального пользователя
            humanUserIPs.add(entry.getIpAddress());
        }
        //считаем ОС
        String osType = entry.getUserAgent().getOsType();
        osCount.put(osType, osCount.getOrDefault(osType,0)+1);
        //браузеры
        String browserType = entry.getUserAgent().getBrowserType();
        browserCount.put(browserType, browserCount.getOrDefault(browserType, 0) + 1);
        this.entryCount++;
    }

    // подсчёт среднего количества посещений сайта за час
    public double getAverageVisitsPerHour() {
        if (minTime == null || maxTime == null) {
            return 0.0;
        }
        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);
        if (hoursBetween < 1) {
            hoursBetween = 1;
        }
        // подсчет запросов только от обычных браузеров (не ботов)
        long regularBrowserRequests = logEntries.stream()
                .filter(entry -> entry.getUserAgent().isRegularBrowser())
                .count();
        return (double) regularBrowserRequests / hoursBetween;
    }

    // подсчёт среднего количества ошибочных запросов в час
    public double getAverageErrorRequestsPerHour() {
        if (minTime == null || maxTime == null) {
            return 0.0;
        }
        long hoursBetween = ChronoUnit.HOURS.between(minTime, maxTime);
        if (hoursBetween < 1) {
            hoursBetween = 1;
        }
        // подсчет ошибочных запросов (4xx и 5xx)
        long errorRequestsCount = logEntries.stream()
                .filter(entry -> {
                    int code = entry.getResponseCode();
                    return code >= 400 && code < 600;
                })
                .count();
        return (double) errorRequestsCount / hoursBetween;
    }

    // ср. посещаемость одним пользователем
    public double getAverageVisitsPerUser() {
        // получение уникальных IP обычных браузеров (не ботов)
        Set<String> uniqueRegularUserIPs = logEntries.stream()
                .filter(entry -> entry.getUserAgent().isRegularBrowser())
                .map(LogEntry::getIpAddress)
                .collect(Collectors.toSet());

        // общее количество запросов от обычных браузеров
        long totalRegularBrowserRequests = logEntries.stream()
                .filter(entry -> entry.getUserAgent().isRegularBrowser())
                .count();
        if (uniqueRegularUserIPs.isEmpty() || totalRegularBrowserRequests == 0) {
            return 0.0;
        }
        return (double) totalRegularBrowserRequests / uniqueRegularUserIPs.size();
    }
    // статистика по ботам и обычным браузерам
    public String getBotStatistics() {
        long totalRequests = logEntries.size();

        Map<String, Long> agentTypeStats = logEntries.stream()
                .collect(Collectors.groupingBy(
                        entry -> entry.getUserAgent().getAgentType(),
                        Collectors.counting()
                ));

        long regularBrowserCount = logEntries.stream()
                .filter(entry -> entry.getUserAgent().isRegularBrowser())
                .count();

        long botCount = totalRequests - regularBrowserCount;

        long uniqueRegularUsers = logEntries.stream()
                .filter(entry -> entry.getUserAgent().isRegularBrowser())
                .map(LogEntry::getIpAddress)
                .distinct()
                .count();

        StringBuilder sb = new StringBuilder();
        sb.append("Статистика по типам клиентов:\n");
        sb.append(String.format("  Всего запросов: %d\n", totalRequests));

        agentTypeStats.forEach((type, count) -> {
            double percentage = totalRequests > 0 ? (double) count / totalRequests * 100 : 0;
            sb.append(String.format("  %s: %d (%.2f%%)\n", type, count, percentage));
        });

        sb.append(String.format("  Уникальных IP обычных пользователей: %d\n", uniqueRegularUsers));
        return sb.toString();
    }
    // статистика ошибок
    public String getErrorStatistics() {
        long totalRequests = logEntries.size();
        long errorCount = logEntries.stream()
                .filter(entry -> {
                    int code = entry.getResponseCode();
                    return code >= 400 && code < 600;
                })
                .count();
        double avgErrorsPerHour = getAverageErrorRequestsPerHour();
        StringBuilder sb = new StringBuilder();
        sb.append("Статистика ошибок:\n");
        sb.append(String.format("  Всего ошибочных запросов (4xx, 5xx): %d\n", errorCount));
        sb.append(String.format("  Процент ошибок: %.2f%%\n",
                totalRequests > 0 ? (double) errorCount / totalRequests * 100 : 0));
        sb.append(String.format("  Среднее количество ошибок в час: %.2f\n", avgErrorsPerHour));
        return sb.toString();
    }

    // статистика посещаемости
    public String getVisitStatistics() {
        long humanCount = logEntries.stream()
                .filter(entry -> entry.getUserAgent().isRegularBrowser()) // ИСПРАВЛЕНО: используем isRegularBrowser()
                .count();

        long uniqueHumanIPs = logEntries.stream()
                .filter(entry -> entry.getUserAgent().isRegularBrowser()) // ИСПРАВЛЕНО: используем isRegularBrowser()
                .map(LogEntry::getIpAddress)
                .distinct()
                .count();

        double avgVisitsPerHour = getAverageVisitsPerHour();
        double avgVisitsPerUser = getAverageVisitsPerUser();

//        // топ-5 самых активных пользователей
//        Map<String, Long> topUsers = logEntries.stream()
//                .filter(entry -> entry.getUserAgent().isRegularBrowser()) // ИСПРАВЛЕНО: используем isRegularBrowser()
//                .collect(Collectors.groupingBy(
//                        LogEntry::getIpAddress,
//                        Collectors.counting()
//                ))
//                .entrySet().stream()
//                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
//                .limit(5)
//                .collect(Collectors.toMap(
//                        Map.Entry::getKey,
//                        Map.Entry::getValue,
//                        (e1, e2) -> e1,
//                        HashMap::new
//                ));

        StringBuilder sb = new StringBuilder();
        sb.append("Статистика посещаемости:\n");
        sb.append(String.format("  Всего посещений реальными пользователями: %d\n", humanCount));
        sb.append(String.format("  Уникальных пользователей (IP): %d\n", uniqueHumanIPs));
        sb.append(String.format("  Средняя посещаемость в час: %.2f\n", avgVisitsPerHour));
        sb.append(String.format("  Средняя посещаемость на пользователя: %.2f\n", avgVisitsPerUser));

//        if (!topUsers.isEmpty()) {
//            sb.append("\n  Топ-5 самых активных пользователей:\n");
//            topUsers.forEach((ip, count) -> {
//                double percentage = humanCount > 0 ? (double) count / humanCount * 100 : 0;
//                sb.append(String.format("    %s: %d запросов (%.1f%%)\n", ip, count, percentage));
//            });
//        }

        return sb.toString();
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
    public int getBotRequests() {
        return botRequests;
    }

    public int getHumanRequests() {
        return humanRequests;
    }

    public int getErrorRequests() {
        return errorRequests;
    }

    public int getUniqueHumanUsers() {
        return humanUserIPs.size();
    }

    public Set<LogEntry> getLogEntries() {
        return new HashSet<>(logEntries);
    }
}
