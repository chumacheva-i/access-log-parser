import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;
class LineTooLongException extends RuntimeException {
    public LineTooLongException(String message) {
        super(message);
    }
}
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
        int yandexBotCount = 0;
        int googleBotCount = 0;
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
                BotCounters counters = parseLogLine(line, totalLines);
                yandexBotCount += counters.yandexBot;
                googleBotCount += counters.googleBot;
            }
            if (totalLines > 0) {
                double yandexBotPercentage = (double) yandexBotCount / totalLines * 100;
                double googleBotPercentage = (double) googleBotCount / totalLines * 100;
                System.out.println("Результаты анализа файла:");
                System.out.println("Общее количество запросов: " + totalLines);
                System.out.println("Запросы от YandexBot: " + yandexBotCount + " (" + String.format("%.2f", yandexBotPercentage) + "%)");
                System.out.println("Запросы от Googlebot: " + googleBotCount + " (" + String.format("%.2f", googleBotPercentage) + "%)");
            } else {
                System.out.println("Файл пуст.");
            }
            System.out.println();

        } catch (LineTooLongException e) {
            System.err.println("Ошибка: " + e.getMessage());
            throw e;
        } catch (Exception ex) {
            System.err.println("Ошибка при чтении файла:");
            ex.printStackTrace();
        }
    }
    private static class BotCounters {
        int yandexBot;
        int googleBot;
        BotCounters(int yandexBot, int googleBot) {
            this.yandexBot = yandexBot;
            this.googleBot = googleBot;
        }
    }
    private static BotCounters parseLogLine(String line, int lineNumber) {
        int yandexBot = 0;
        int googleBot = 0;
        try {
            String[] partsByQuotes = line.split("\"");
            if (partsByQuotes.length >= 6) {
                String userAgent = partsByQuotes[5];
                if (isYandexBot(userAgent)) {
                    yandexBot = 1;
                } else if (isGoogleBot(userAgent)) {
                    googleBot = 1;
                }
            }
        } catch (Exception e) {
            System.err.println("Ошибка при разборе строки " + lineNumber + ": " + e.getMessage());
        }
        return new BotCounters(yandexBot, googleBot);
    }
    private static boolean isYandexBot(String userAgent) {
        return processUserAgent(userAgent, "YandexBot");
    }
    private static boolean isGoogleBot(String userAgent) {
        return processUserAgent(userAgent, "Googlebot");
    }
    private static boolean processUserAgent(String userAgent, String botName) {
        int startBrackets = userAgent.indexOf('(');
        int endBrackets = userAgent.indexOf(')', startBrackets);

        if (startBrackets != -1 && endBrackets != -1) {
            String firstBrackets = userAgent.substring(startBrackets + 1, endBrackets);
            String[] parts = firstBrackets.split(";");
            if (parts.length >= 2) {
                String fragment = parts[1].trim(); // очищаем от пробелов
                String programName = fragment;
                int slashIndex = fragment.indexOf('/');
                if (slashIndex != -1) {
                    programName = fragment.substring(0, slashIndex).trim();
                }
                return botName.equals(programName);
            }
        }
        return false;
    }
}