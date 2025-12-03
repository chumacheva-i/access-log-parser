import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int fileCount = 0; // Счётчик верно указанных файлов
        boolean running = true;
        while (running) {
            System.out.print("Введите путь к файлу (или 'exit' для выхода): ");
            String path = scanner.nextLine();
            if ("exit".equalsIgnoreCase(path)) {
                System.out.println("Программа завершена");
                running = false;
                break;
            }
            File file = new File(path);
            boolean fileExists = file.exists();
            boolean isDirectory = file.isDirectory();
            if (!fileExists) {
                System.out.println("Файл не существует.");
                continue;
            }
            if (isDirectory) {
                System.out.println("Указанный путь ведёт к папке, а не к файлу.");
                continue;
            }
            System.out.println("Путь указан верно");
            fileCount++;
            System.out.println("Это файл номер " + fileCount);
            analyzeLogFile(path);
        }
        scanner.close();
    }

    public static void analyzeLogFile(String path) {
        int totalLines = 0;
        Statistics statistics = new Statistics();
        try (FileReader fileReader = new FileReader(path);
             BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                int length = line.length();
                totalLines++;
                if (length > 1024) {
                    throw new LineTooLongException("Обнаружена строка длиннее 1024 символов. Длина: " + length +
                            " символов. Строка: " + (line.length() > 100 ? line.substring(0, 100) + "..." : line));
                }
                try {
                    LogEntry entry = new LogEntry(line);
                    statistics.addEntry(entry);
                } catch (Exception e) {
                    System.err.println("Ошибка при разборе строки " + totalLines + ": " + e.getMessage());
                }
            }

            System.out.println("Результаты анализа файла:");
            System.out.println("Общее количество строк: " + totalLines);
            System.out.println("Обработано записей: " + statistics.getEntryCount());
            System.out.println("Общий трафик: " + statistics.getTotalTraffic() + " байт");
            System.out.println("Средний трафик в час: " + String.format("%.2f", statistics.getTrafficRate()) + " байт/час");
            System.out.println("Временной диапазон: " + statistics.getMinTime() + " - " + statistics.getMaxTime());
            System.out.println();

            System.out.println("Статистика существующих страниц (200):");
            System.out.println("Количество уникальных страниц: " + statistics.getExistingPagesCount());
            System.out.println("Список страниц:");
            for (String page : statistics.getExistingPages()) {
                System.out.println("  - " + page);
            }

            System.out.println();
            System.out.println(statistics.getOsStatisticsAsString());

            System.out.println("Статистика несуществующих страниц (400):");
            System.out.println(statistics.getNonExistingPagesAsString());
            System.out.println("Всего несуществующих страниц (400):");
            System.out.println(statistics.getNonExistingPagesCount());
            //System.out.println(statistics.getBrowserStatisticsAsString()); //браузеры проценты не нужны в задании
            System.out.println(statistics.getBrowserStatisticsDetailedAsString());
        } catch (LineTooLongException e) {
            System.err.println("Ошибка: " + e.getMessage());
            throw e;
        } catch (Exception ex) {
            System.err.println("Ошибка при чтении файла:");
            ex.printStackTrace();
        }
    }
}