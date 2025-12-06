import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.stream;

public class UserAgent {
    private final String osType;
    private final String browserType;
    private final String originalUserAgentString;
    private final boolean isBot;

    public UserAgent(String userAgentString) {
        this.originalUserAgentString = userAgentString;
        this.osType = parseOperatingSystem(userAgentString);
        this.browserType = parseBrowser(userAgentString);
        this.isBot = isBot(userAgentString);
    }
    private String parseOperatingSystem(String userAgentString) {
        if (userAgentString == null || userAgentString.isEmpty()) {
            return "Unknown";
        }
        String ua = userAgentString.toLowerCase();
        if (ua.contains("windows")) {
            return "Windows";
        } else if (ua.contains("mac os") || ua.contains("macos")) {
            return "macOS";
        } else if (ua.contains("linux")) {
            return "Linux";
        } else if (ua.contains("android")) {
            return "Android";
        } else if (ua.contains("ios") || ua.contains("iphone")) {
            return "iOS";
        } else {
            return "Other";
        }
    }
    private String parseBrowser(String userAgentString) {
        if (userAgentString == null || userAgentString.isEmpty()) {
            return "Unknown";
        }
        String ua = userAgentString.toLowerCase();
        if (ua.contains("edg/") || ua.contains("edge/")) {
            return "Edge";
        } else if (ua.contains("firefox") || ua.contains("fxios")) {
            return "Firefox";
        } else if (ua.contains("chrome") && !ua.contains("chromium")) {
            return "Chrome";
        } else if (ua.contains("safari") && !ua.contains("chrome")) {
            return "Safari";
        } else if (ua.contains("opera") || ua.contains("opr/")) {
            return "Opera";
        } else {
            return "Other";
        }
    }
    private boolean isBot(String userAgentString) {
        if (userAgentString == null || userAgentString.isEmpty()) {
            return false;
        }

        String ua = userAgentString.toLowerCase();

        // Набор ключевых слов, характерных для ботов
        Set<String> botKeywords = Stream.of(
                "bot", "crawler", "spider", "scraper", "fetcher",
                "googlebot", "bingbot", "yandexbot", "duckduckbot",
                "baiduspider", "sogou", "exabot", "facebot",
                "ia_archiver", "semrushbot", "ahrefsbot", "mj12bot",
                "dotbot", "mail.ru", "discordbot", "twitterbot",
                "facebookexternalhit", "linkedinbot", "slackbot",
                "telegrambot", "whatsapp", "python-requests",
                "java", "curl", "wget", "apache-httpclient",
                "okhttp", "go-http-client", "node-fetch",
                "libwww-perl", "http", "https", "feedfetcher",
                "monitor", "checker", "validator", "analyzer",
                "indexer", "search", "archive", "poller",
                "collector", "harvester", "gatherer", "extractor"
        ).collect(Collectors.toSet());

        // Проверяем наличие любого из ключевых слов в User-Agent
        return botKeywords.stream().anyMatch(ua::contains);
    }
    public boolean isRegularBrowser() {
        return !isBot;
    }
    public String getAgentType() {
        if (isBot) {
            return "Bot";
        } else if (browserType.equals("Unknown") || browserType.equals("Other")) {
            return "Unknown/Other Client";
        } else {
            return "Regular Browser";
        }
    }
    public String getOsType() {
        return osType;
    }
    public String getBrowserType() {
        return browserType;
    }
    public boolean isBot() {
        return isBot;
    }

    public String getOriginalUserAgentString() {
        return originalUserAgentString;
    }

    @Override
    public String toString() {
        return originalUserAgentString;
    }

}