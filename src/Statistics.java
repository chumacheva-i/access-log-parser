import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Statistics {
    private int totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;
    private int entryCount;
    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
        this.entryCount = 0;
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

        this.entryCount++;
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