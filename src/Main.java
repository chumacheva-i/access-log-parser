import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int fileCount = 0; // Счётчик верно указанных файлов

        while (true) {
            System.out.print("Введите путь к файлу: ");
            String path = scanner.nextLine();

            File file = new File(path);
            boolean fileExists = file.exists();
            boolean isDirectory = file.isDirectory();

            // Проверяем условия
            if (!fileExists || isDirectory) {
                if (!fileExists) {
                    System.out.println("Файл не существует.");
                } else if (isDirectory) {
                    System.out.println("Указанный путь ведёт к папке, а не к файлу.");
                }
                continue; // Продолжаем цикл
            }

            // Если файл существует и это не папка
            System.out.println("Путь указан верно");
            fileCount++;
            System.out.println("Это файл номер " + fileCount);
        }
    }
}