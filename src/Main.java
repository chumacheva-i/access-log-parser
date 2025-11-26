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
        int maxLength = 0;
        int minLength = Integer.MAX_VALUE;
        try (FileReader fileReader = new FileReader(path);
             BufferedReader reader = new BufferedReader(fileReader)) {
            String line;
            while ((line = reader.readLine()) != null) {
                int length = line.length();
                totalLines++;
                if (length > maxLength) {
                    maxLength = length;
                }
                if (length < minLength) {
                    minLength = length;
                }
                if (length > 1024) {
                    throw new LineTooLongException("Обнаружена строка длиннее 1024 символов. Длина: " + length +
                            " символов. Строка: " + (line.length() > 100 ? line.substring(0, 100) + "..." : line));
                }
            }
            if (totalLines == 0) {
                minLength = 0;
            }
            System.out.println("Результаты анализа файла:");
            System.out.println("Общее количество строк: " + totalLines);
            System.out.println("Длина самой длинной строки: " + maxLength);
            System.out.println("Длина самой короткой строки: " + minLength);
            System.out.println();
        } catch (LineTooLongException e) {
            System.err.println("Ошибка: " + e.getMessage());
            throw e;
        } catch (Exception ex) {
            System.err.println("Ошибка при чтении файла:");
            ex.printStackTrace();
        }
    }
}