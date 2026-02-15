package com.stripe.automation.drivers;

import com.stripe.automation.config.ConfigManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

public final class WebDriverFactory {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private WebDriverFactory() {}

    public static WebDriver getDriver() {
        if (DRIVER.get() == null) {
            String browser = ConfigManager.get("browser");
            boolean headless = Boolean.parseBoolean(ConfigManager.get("headless"));
            WebDriver webDriver;
            if ("firefox".equalsIgnoreCase(browser)) {
                FirefoxOptions options = new FirefoxOptions();
                if (headless) {
                    options.addArguments("-headless");
                }
                webDriver = new org.openqa.selenium.firefox.FirefoxDriver(options);
            } else {
                ChromeOptions options = new ChromeOptions();
                if (headless) {
                    options.addArguments("--headless=new", "--disable-gpu", "--window-size=1920,1080");
                }
                options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
                webDriver = new org.openqa.selenium.chrome.ChromeDriver(options);
            }
            webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            webDriver.manage().window().maximize();
            DRIVER.set(webDriver);
        }
        return DRIVER.get();
    }

    public static void quitDriver() {
        if (DRIVER.get() != null) {
            DRIVER.get().quit();
            DRIVER.remove();
        }
    }
}
