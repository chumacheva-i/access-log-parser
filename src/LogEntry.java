import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class LogEntry {
    private final String ipAddress;
    private final LocalDateTime dateTime;
    private final HttpMethod method;
    private final String path;
    private final int responseCode;
    private final int dataSize;
    private final String referer;
    private final UserAgent userAgent;

    public LogEntry(String logLine) {
        // разбор строки лога
        String[] parts = parseLogLine(logLine);

        this.ipAddress = parts[0];
        this.dateTime = parseDateTime(parts[1]);
        this.method = parseHttpMethod(parts[2]);
        this.path = parts[3];
        this.responseCode = Integer.parseInt(parts[4]);
        this.dataSize = Integer.parseInt(parts[5]);
        this.referer = parts[6].equals("-") ? null : parts[6];
        this.userAgent = new UserAgent(parts[7]);
    }

    private String[] parseLogLine(String logLine) {
        // Формат: IP - - [дата] "метод путь протокол" код размер "referer" "user-agent"
        String[] result = new String[8];
        // Разделяем по пробелам, но учитываем строки в кавычках и скобках
        String[] tokens = logLine.split(" ");
        // IP адрес
        result[0] = tokens[0];
        // Дата и время (объединяем токены в скобках)
        StringBuilder dateBuilder = new StringBuilder();
        for (int i = 3; i < tokens.length; i++) {
            if (tokens[i].startsWith("[")) {
                dateBuilder.append(tokens[i].substring(1));
            } else if (tokens[i].endsWith("]")) {
                dateBuilder.append(" ").append(tokens[i].substring(0, tokens[i].length() - 1));
                break;
            } else {
                dateBuilder.append(" ").append(tokens[i]);
            }
        }
        result[1] = dateBuilder.toString();
        // Находим начало запроса
        int requestStart = -1;
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].startsWith("\"") && !tokens[i].equals("\"-\"")) {
                requestStart = i;
                break;
            }
        }
        if (requestStart != -1) {
            // Метод и путь
            String request = tokens[requestStart] + " " + tokens[requestStart + 1] + " " + tokens[requestStart + 2];
            request = request.replace("\"", "");
            String[] requestParts = request.split(" ");
            result[2] = requestParts[0]; // метод
            result[3] = requestParts[1]; // путь
            // Код ответа и размер данных
            result[4] = tokens[requestStart + 3]; // код
            result[5] = tokens[requestStart + 4]; // размер
            // Referer
            result[6] = tokens[requestStart + 5].replace("\"", "");
            if (result[6].equals("-")) {
                result[6] = "-";
            }

            // UserAgent (объединяем все оставшиеся токены)
            StringBuilder userAgentBuilder = new StringBuilder();
            for (int i = requestStart + 6; i < tokens.length; i++) {
                userAgentBuilder.append(tokens[i]).append(" ");
            }
            String userAgent = userAgentBuilder.toString().trim();
            result[7] = userAgent.replace("\"", "");
        }

        return result;
    }
    private LocalDateTime parseDateTime(String dateTimeString) {
        // Формат: 13/Dec/2024:10:15:32 +0300
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z", Locale.ENGLISH);
        return LocalDateTime.parse(dateTimeString, formatter);
    }
    private HttpMethod parseHttpMethod(String methodString) {
        try {
            return HttpMethod.valueOf(methodString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return HttpMethod.GET; // значение по умолчанию
        }
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public HttpMethod getMethod() {
        return method;
    }
    public String getPath() {
        return path;
    }
    public int getResponseCode() {
        return responseCode;
    }
    public int getDataSize() {
        return dataSize;
    }
    public String getReferer() {
        return referer;
    }
    public UserAgent getUserAgent() {
        return userAgent;
    }
}