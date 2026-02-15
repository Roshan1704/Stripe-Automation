package com.stripe.automation.drivers;

import com.stripe.automation.config.ConfigManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;

public final class WebDriverFactory {
    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private WebDriverFactory() {}

    public static WebDriver getDriver() {
        if (DRIVER.get() == null) {
            String browser = ConfigManager.get("browser");
            boolean headless = Boolean.parseBoolean(ConfigManager.get("headless"));
            String remoteUrl = System.getProperty("selenium.remote.url", System.getenv("SELENIUM_REMOTE_URL"));

            WebDriver webDriver = "firefox".equalsIgnoreCase(browser)
                    ? createFirefoxDriver(headless, remoteUrl)
                    : createChromeDriver(headless, remoteUrl);

            webDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
            webDriver.manage().window().maximize();
            DRIVER.set(webDriver);
        }
        return DRIVER.get();
    }

    private static WebDriver createFirefoxDriver(boolean headless, String remoteUrl) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        if (remoteUrl != null && !remoteUrl.isBlank()) {
            return buildRemoteDriver(remoteUrl, options);
        }
        return new org.openqa.selenium.firefox.FirefoxDriver(options);
    }

    private static WebDriver createChromeDriver(boolean headless, String remoteUrl) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new", "--disable-gpu", "--window-size=1920,1080");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
        if (remoteUrl != null && !remoteUrl.isBlank()) {
            return buildRemoteDriver(remoteUrl, options);
        }
        return new org.openqa.selenium.chrome.ChromeDriver(options);
    }

    private static WebDriver buildRemoteDriver(String remoteUrl, org.openqa.selenium.Capabilities options) {
        try {
            return new RemoteWebDriver(URI.create(remoteUrl).toURL(), options);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid selenium remote URL: " + remoteUrl, e);
        }
    }

    public static void quitDriver() {
        if (DRIVER.get() != null) {
            DRIVER.get().quit();
            DRIVER.remove();
        }
    }
}
