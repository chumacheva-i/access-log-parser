public class UserAgent {
    private final String osType;
    private final String browserType;

    public UserAgent(String userAgentString) {
        this.osType = parseOperatingSystem(userAgentString);
        this.browserType = parseBrowser(userAgentString);
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
    public String getOsType() {
        return osType;
    }
    public String getBrowserType() {
        return browserType;
    }
}