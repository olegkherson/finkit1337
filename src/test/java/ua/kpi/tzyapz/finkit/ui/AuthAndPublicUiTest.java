package ua.kpi.tzyapz.finkit.ui;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Незалежні WebDriver UI-тести публічних сторінок, реєстрації, входу та відновлення доступу ParaBank.
 */
@Tag("ui")
@DisplayName("UI-тести авторизації та публічних сторінок ParaBank (TC-033, TC-001, TC-003, TC-004, TC-007)")
class AuthAndPublicUiTest {

    private static final String BASE_URL = "https://parabank.parasoft.com/parabank/";
    private static final String PASSWORD = "FinKit#2026";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(15);

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--window-size=1280,900", "--remote-allow-origins=*");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, WAIT_TIMEOUT);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // Автор: ІТ Бурлачка Артем | Рецензент: СМ Топчій Микита | Вимога: TC-033
    @Test
    @DisplayName("TC-033 (GUI-10): блок Customer Login відображається на публічних сторінках")
    void customerLoginBlock_isDisplayedOnPublicPages() {
        List<String> publicPages = List.of("index.htm", "register.htm", "lookup.htm");

        for (String page : publicPages) {
            driver.get(BASE_URL + page);
            WebElement loginPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPanel")));
            WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
            WebElement passwordInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("password")));
            WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("#loginPanel input[type='submit'][value='Log In']")));
            WebElement forgotLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Forgot login info?")));
            WebElement registerLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Register")));

            assertAll("Перевірка елементів Customer Login на " + page,
                () -> assertTrue(loginPanel.isDisplayed(), "Панель loginPanel має бути видимою на " + page),
                () -> assertTrue(usernameInput.isDisplayed() && usernameInput.isEnabled(), "Поле username має бути активним на " + page),
                () -> assertTrue(passwordInput.isDisplayed() && passwordInput.isEnabled(), "Поле password має бути активним на " + page),
                () -> assertTrue(loginBtn.isDisplayed() && loginBtn.isEnabled(), "Кнопка Log In має бути активною на " + page),
                () -> assertTrue(forgotLink.isDisplayed(), "Посилання Forgot login info? має бути видимим на " + page),
                () -> assertTrue(registerLink.isDisplayed(), "Посилання Register має бути видимим на " + page)
            );
        }
    }

    // Автор: ІТ Бурлачка Артем | Рецензент: СМ Топчій Микита | Вимога: TC-001
    @Test
    @DisplayName("TC-001 (F-10): реєстрація нового клієнта з 11 полями виконує автовхід")
    void register_validElevenFields_logsInAutomatically() {
        String username = uniqueUsername("tc001");
        fillAndSubmitRegistration(username, PASSWORD, PASSWORD);

        WebElement rightPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rightPanel")));
        WebElement greeting = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#leftPanel p.smallText")));
        WebElement logoutLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Log Out")));

        assertAll(
            () -> assertTrue(rightPanel.getText().contains("Your account was created successfully. You are now logged in."),
                "Очікували підтвердження створення акаунта, отримано: " + rightPanel.getText()),
            () -> assertTrue(greeting.getText().contains("Welcome Ivan Franchuk"),
                "Очікували привітання клієнта, отримано: " + greeting.getText()),
            () -> assertTrue(logoutLink.isDisplayed(), "Після автовходу має відображатися посилання Log Out")
        );

        logoutLink.click();
        WebElement loginUsername = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        assertTrue(loginUsername.isDisplayed(), "Після виходу має з'явитися форма входу");
    }

    // Автор: ІТ Бурлачка Артем | Рецензент: СМ Топчій Микита | Вимога: TC-003
    @Test
    @DisplayName("TC-003 (GUI-30): розбіжність пароля і підтвердження блокує реєстрацію та вхід")
    void register_passwordMismatch_showsErrorAndBlocksLogin() {
        String username = uniqueUsername("tc003");
        fillAndSubmitRegistration(username, PASSWORD, "FinKit#2027");

        WebElement mismatchError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("repeatedPassword.errors")));
        assertAll(
            () -> assertTrue(mismatchError.getText().contains("Passwords did not match."),
                "Очікували повідомлення про невідповідність паролів, отримано: " + mismatchError.getText()),
            () -> assertTrue(driver.findElements(By.linkText("Log Out")).isEmpty(),
                "Невалідна реєстрація не повинна виконувати вхід у кабінет")
        );

        driver.get(BASE_URL + "index.htm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("#loginPanel input[type='submit'][value='Log In']")).click();

        WebElement loginError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel p.error")));
        assertTrue(loginError.isDisplayed() && !loginError.getText().isBlank(),
            "Вхід із незареєстрованим логіном має відхилятися з помилкою");
        assertTrue(driver.findElements(By.linkText("Log Out")).isEmpty(),
            "Після невдалого входу посилання Log Out має бути відсутнім");
    }

    // Автор: ІТ Бурлачка Артем | Рецензент: СМ Топчій Микита | Вимога: TC-004
    @Test
    @DisplayName("TC-004 (F-20): повторний вхід після виходу відкриває кабінет із привітанням")
    void login_afterLogout_opensCabinetWithGreeting() {
        String username = uniqueUsername("tc004");
        registerFreshUserAndLogout(username);

        login(username, PASSWORD);

        WebElement greeting = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#leftPanel p.smallText")));
        WebElement overviewLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Accounts Overview")));
        WebElement transferLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Transfer Funds")));
        WebElement logoutLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Log Out")));

        assertAll(
            () -> assertTrue(greeting.getText().contains("Welcome Ivan Franchuk"),
                "Очікували привітання у кабінеті, отримано: " + greeting.getText()),
            () -> assertTrue(overviewLink.isDisplayed(), "Пункт меню Accounts Overview має бути видимим"),
            () -> assertTrue(transferLink.isDisplayed(), "Пункт меню Transfer Funds має бути видимим"),
            () -> assertTrue(logoutLink.isDisplayed(), "Пункт меню Log Out має бути видимим")
        );

        logoutLink.click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));

        login(username, PASSWORD);
        WebElement logoutAfterRelogin = wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Log Out")));
        assertTrue(logoutAfterRelogin.isDisplayed(), "Повторний вхід має успішно відкривати кабінет");
    }

    // Автор: ІТ Бурлачка Артем | Рецензент: СМ Топчій Микита | Вимога: TC-007
    @Test
    @DisplayName("TC-007 (F-30): відновлення доступу з незбіжним SSN повертає помилку і не розкриває дані")
    void lookup_mismatchedSsn_showsNotFoundError() {
        String username = uniqueUsername("tc007");
        registerFreshUserAndLogout(username);

        driver.get(BASE_URL + "lookup.htm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("firstName"))).sendKeys("Ivan");
        driver.findElement(By.id("lastName")).sendKeys("Franchuk");
        driver.findElement(By.id("address.street")).sendKeys("10 Khreshchatyk St");
        driver.findElement(By.id("address.city")).sendKeys("Kyiv");
        driver.findElement(By.id("address.state")).sendKeys("Kyivska");
        driver.findElement(By.id("address.zipCode")).sendKeys("01001");
        driver.findElement(By.id("ssn")).sendKeys("9999999999");
        driver.findElement(By.cssSelector("input[type='submit'][value='Find My Login Info']")).click();

        WebElement lookupError = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#rightPanel p.error")));
        String rightPanelText = driver.findElement(By.id("rightPanel")).getText();
        assertAll(
            () -> assertTrue(lookupError.getText().contains("The customer information provided could not be found."),
                "Очікували повідомлення про незнайденого клієнта, отримано: " + lookupError.getText()),
            () -> assertFalse(rightPanelText.contains(username), "Логін клієнта не повинен розкриватися"),
            () -> assertFalse(rightPanelText.contains(PASSWORD), "Пароль клієнта не повинен розкриватися")
        );

        login(username, PASSWORD);
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Log Out"))).isDisplayed(),
            "Обліковий запис має залишатися чинним після невдалого відновлення");
    }

    private void registerFreshUserAndLogout(String username) {
        fillAndSubmitRegistration(username, PASSWORD, PASSWORD);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Log Out"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
    }

    private void fillAndSubmitRegistration(String username, String password, String confirmPassword) {
        driver.get(BASE_URL + "register.htm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("customer.firstName"))).sendKeys("Ivan");
        driver.findElement(By.id("customer.lastName")).sendKeys("Franchuk");
        driver.findElement(By.id("customer.address.street")).sendKeys("10 Khreshchatyk St");
        driver.findElement(By.id("customer.address.city")).sendKeys("Kyiv");
        driver.findElement(By.id("customer.address.state")).sendKeys("Kyivska");
        driver.findElement(By.id("customer.address.zipCode")).sendKeys("01001");
        driver.findElement(By.id("customer.phoneNumber")).sendKeys("380501112233");
        driver.findElement(By.id("customer.ssn")).sendKeys("1234567890");
        driver.findElement(By.id("customer.username")).sendKeys(username);
        driver.findElement(By.id("customer.password")).sendKeys(password);
        driver.findElement(By.id("repeatedPassword")).sendKeys(confirmPassword);
        driver.findElement(By.cssSelector("input[type='submit'][value='Register']")).click();
    }

    private void login(String username, String password) {
        driver.get(BASE_URL + "index.htm");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username"))).sendKeys(username);
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.cssSelector("#loginPanel input[type='submit'][value='Log In']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("accountTable")));
    }

    private static String uniqueUsername(String tcTag) {
        return "fins4" + tcTag + "_" + ThreadLocalRandom.current().nextInt(100000, 1000000);
    }
}
