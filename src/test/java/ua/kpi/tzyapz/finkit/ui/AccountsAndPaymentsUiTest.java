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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Незалежні WebDriver UI-тести рахунків, переказів, оплати рахунків, пошуку транзакцій та профілю ParaBank.
 */
@Tag("ui")
@DisplayName("UI-тести кабінету, рахунків та платежів ParaBank (TC-008, TC-012, TC-018, TC-021, TC-022)")
class AccountsAndPaymentsUiTest {

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

    // Автор: ІТ Бурлачка Артем | Рецензент: СМ Топчій Микита | Вимога: TC-008
    @Test
    @DisplayName("TC-008 (F-40): огляд рахунків відображає баланси й узгоджений рядок Total")
    void overview_displaysAccountsAndConsistentTotal() {
        registerFreshUser(uniqueUsername("tc008"));
        openSecondSavingsAccount();

        OverviewSnapshot snapshot = readOverviewSnapshot();

        assertAll(
            () -> assertTrue(snapshot.balancesByAccount().size() >= 2,
                "Очікували щонайменше 2 рахунки в таблиці #accountTable, знайдено: " + snapshot.balancesByAccount().size()),
            () -> assertTrue(snapshot.availableValid(), "Кожен рядок рахунку повинен мати доступну суму у форматі $X.XX"),
            () -> assertEquals(0, snapshot.sumOfAccountBalances().compareTo(snapshot.total()),
                "Сума балансів рахунків (" + snapshot.sumOfAccountBalances() + ") має дорівнювати Total (" + snapshot.total() + ")")
        );
    }

    // Автор: ІТ Бурлачка Артем | Рецензент: СМ Топчій Микита | Вимога: TC-012
    @Test
    @DisplayName("TC-012 (F-70): переказ коштів між власними рахунками оновлює баланси при незмінному Total")
    void transferFunds_betweenOwnAccounts_updatesBalances() {
        registerFreshUser(uniqueUsername("tc012"));
        String acc2 = openSecondSavingsAccount();

        OverviewSnapshot before = readOverviewSnapshot();
        String acc1 = before.balancesByAccount().keySet().stream()
            .filter(id -> !id.equals(acc2))
            .findFirst()
            .orElseThrow();
        BigDecimal b1Before = before.balancesByAccount().get(acc1);
        BigDecimal b2Before = before.balancesByAccount().get(acc2);
        BigDecimal transferAmount = new BigDecimal("100.00");

        executeTransfer(acc1, acc2, "100.00");

        OverviewSnapshot after = readOverviewSnapshot();
        BigDecimal b1After = after.balancesByAccount().get(acc1);
        BigDecimal b2After = after.balancesByAccount().get(acc2);

        assertAll(
            () -> assertEquals(0, b1Before.subtract(transferAmount).compareTo(b1After),
                "Баланс рахунку-джерела мав зменшитися на 100.00: було " + b1Before + ", стало " + b1After),
            () -> assertEquals(0, b2Before.add(transferAmount).compareTo(b2After),
                "Баланс рахунку-одержувача мав збільшитися на 100.00: було " + b2Before + ", стало " + b2After),
            () -> assertEquals(0, before.total().compareTo(after.total()),
                "Сумарний баланс Total не повинен змінитися при внутрішньому переказі")
        );
    }

    // Автор: ІТ Бурлачка Артем | Рецензент: СМ Топчій Микита | Вимога: TC-018
    @Test
    @DisplayName("TC-018 (GUI-60): оплата рахунку при розбіжності рахунків показує помилку і не списує кошти")
    void billPay_mismatchedAccountNumbers_showsValidationError() {
        registerFreshUser(uniqueUsername("tc018"));
        OverviewSnapshot before = readOverviewSnapshot();

        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Bill Pay"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("payee.name"))).sendKeys("Ivan Petrenko");
        driver.findElement(By.name("payee.address.street")).sendKeys("10 Main St");
        driver.findElement(By.name("payee.address.city")).sendKeys("Kyiv");
        driver.findElement(By.name("payee.address.state")).sendKeys("Kyivska");
        driver.findElement(By.name("payee.address.zipCode")).sendKeys("01001");
        driver.findElement(By.name("payee.phoneNumber")).sendKeys("380501112233");
        driver.findElement(By.name("payee.accountNumber")).sendKeys("21322313");
        driver.findElement(By.name("verifyAccount")).sendKeys("21322314");
        driver.findElement(By.name("amount")).sendKeys("1.00");
        driver.findElement(By.cssSelector("input[type='button'][value='Send Payment']")).click();

        WebElement mismatchError = wait.until(
            ExpectedConditions.visibilityOfElementLocated(By.id("validationModel-verifyAccount-mismatch")));
        WebElement billpayResult = driver.findElement(By.id("billpayResult"));

        assertAll(
            () -> assertTrue(mismatchError.getText().contains("The account numbers do not match."),
                "Очікували помилку невідповідності рахунків, отримано: " + mismatchError.getText()),
            () -> assertFalse(billpayResult.isDisplayed(), "Блок успішної оплати #billpayResult має залишатися прихованим")
        );

        OverviewSnapshot after = readOverviewSnapshot();
        assertEquals(0, before.total().compareTo(after.total()),
            "Баланс клієнта не повинен змінюватися при помилці валідації Bill Pay");
    }

    // Автор: ІТ Бурлачка Артем | Рецензент: СМ Топчій Микита | Вимога: TC-021
    @Test
    @DisplayName("TC-021 (F-90): пошук транзакцій за сумою повертає виконаний переказ")
    void findTransactions_byAmount_returnsMatchingTransfer() {
        registerFreshUser(uniqueUsername("tc021"));
        String acc2 = openSecondSavingsAccount();

        OverviewSnapshot snapshot = readOverviewSnapshot();
        String acc1 = snapshot.balancesByAccount().keySet().stream()
            .filter(id -> !id.equals(acc2))
            .findFirst()
            .orElseThrow();

        executeTransfer(acc1, acc2, "100.00");

        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Find Transactions"))).click();
        wait.until(d -> {
            List<WebElement> opts = new Select(d.findElement(By.id("accountId"))).getOptions();
            return opts.size() >= 2;
        });
        new Select(driver.findElement(By.id("accountId"))).selectByVisibleText(acc1);

        WebElement amountInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        amountInput.clear();
        amountInput.sendKeys("100.00");
        driver.findElement(By.id("findByAmount")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("transactionTable")));
        wait.until(d -> !d.findElements(By.cssSelector("#transactionTable tbody tr")).isEmpty());

        List<WebElement> rows = driver.findElements(By.cssSelector("#transactionTable tbody tr"));
        boolean foundTransfer = rows.stream().anyMatch(row -> {
            String rowText = row.getText();
            return rowText.contains("Funds Transfer Sent") && rowText.contains("$100.00");
        });

        assertTrue(foundTransfer, "У #transactionTable не знайдено рядок 'Funds Transfer Sent' на '$100.00'");
    }

    // Автор: ІТ Бурлачка Артем | Рецензент: СМ Топчій Микита | Вимога: TC-022
    @Test
    @DisplayName("TC-022 (F-100): оновлення контактних даних зберігає нову адресу і телефон")
    void updateProfile_persistsNewAddressAndPhone() {
        registerFreshUser(uniqueUsername("tc022"));

        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Update Contact Info"))).click();
        waitForProfileFieldsLoaded();

        WebElement streetInput = driver.findElement(By.id("customer.address.street"));
        WebElement phoneInput = driver.findElement(By.id("customer.phoneNumber"));
        streetInput.clear();
        streetInput.sendKeys("15 Nova St");
        phoneInput.clear();
        phoneInput.sendKeys("380671112233");

        driver.findElement(By.cssSelector("input[type='button'][value='Update Profile']")).click();
        WebElement resultPanel = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateProfileResult")));
        assertTrue(resultPanel.getText().contains("Profile Updated"),
            "Очікували підтвердження оновлення профілю, отримано: " + resultPanel.getText());

        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Update Contact Info"))).click();
        waitForProfileFieldsLoaded();

        String savedStreet = driver.findElement(By.id("customer.address.street")).getAttribute("value");
        String savedPhone = driver.findElement(By.id("customer.phoneNumber")).getAttribute("value");

        assertAll(
            () -> assertEquals("15 Nova St", savedStreet, "Оновлена адреса має зберігатися після повторного відкриття сторінки"),
            () -> assertEquals("380671112233", savedPhone, "Оновлений телефон має зберігатися після повторного відкриття сторінки")
        );
    }

    private void registerFreshUser(String username) {
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
        driver.findElement(By.id("customer.password")).sendKeys(PASSWORD);
        driver.findElement(By.id("repeatedPassword")).sendKeys(PASSWORD);
        driver.findElement(By.cssSelector("input[type='submit'][value='Register']")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.linkText("Log Out")));
    }

    private String openSecondSavingsAccount() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Open New Account"))).click();
        wait.until(d -> {
            List<WebElement> opts = new Select(d.findElement(By.id("fromAccountId"))).getOptions();
            return !opts.isEmpty() && !opts.get(0).getText().isBlank();
        });
        new Select(driver.findElement(By.id("type"))).selectByVisibleText("SAVINGS");
        driver.findElement(By.cssSelector("input[type='button'][value='Open New Account']")).click();
        WebElement newAccountLink = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("newAccountId")));
        wait.until(d -> !newAccountLink.getText().isBlank());
        return newAccountLink.getText().trim();
    }

    private void executeTransfer(String fromAcc, String toAcc, String amountStr) {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Transfer Funds"))).click();
        wait.until(d -> new Select(d.findElement(By.id("fromAccountId"))).getOptions().size() >= 2
            && new Select(d.findElement(By.id("toAccountId"))).getOptions().size() >= 2);
        new Select(driver.findElement(By.id("fromAccountId"))).selectByVisibleText(fromAcc);
        new Select(driver.findElement(By.id("toAccountId"))).selectByVisibleText(toAcc);
        WebElement amountInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("amount")));
        amountInput.clear();
        amountInput.sendKeys(amountStr);
        driver.findElement(By.cssSelector("input[type='submit'][value='Transfer']")).click();

        WebElement resultBlock = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("showResult")));
        String title = resultBlock.findElement(By.cssSelector("h1.title")).getText().trim();
        String shownAmount = driver.findElement(By.id("amountResult")).getText().trim();
        String shownFrom = driver.findElement(By.id("fromAccountIdResult")).getText().trim();
        String shownTo = driver.findElement(By.id("toAccountIdResult")).getText().trim();

        assertAll(
            () -> assertEquals("Transfer Complete!", title, "Очікували підтвердження переказу"),
            () -> assertEquals("$" + amountStr, shownAmount, "Відображена сума переказу має збігатися"),
            () -> assertEquals(fromAcc, shownFrom, "Рахунок-джерело має збігатися"),
            () -> assertEquals(toAcc, shownTo, "Рахунок-одержувач має збігатися")
        );
    }

    private OverviewSnapshot readOverviewSnapshot() {
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Accounts Overview"))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#accountTable tbody tr td a")));
        wait.until(d -> {
            List<WebElement> totalCells = d.findElements(By.cssSelector("#accountTable tbody tr:last-child td:nth-child(2)"));
            return !totalCells.isEmpty() && totalCells.get(0).getText().trim().startsWith("$");
        });

        List<WebElement> rows = driver.findElements(By.cssSelector("#accountTable tbody tr"));
        Map<String, BigDecimal> balances = new LinkedHashMap<>();
        BigDecimal total = BigDecimal.ZERO;
        boolean availableValid = true;

        for (WebElement row : rows) {
            List<WebElement> cells = row.findElements(By.tagName("td"));
            if (cells.size() < 2) {
                continue;
            }
            List<WebElement> links = cells.get(0).findElements(By.tagName("a"));
            if (!links.isEmpty()) {
                String accountId = links.get(0).getText().trim();
                BigDecimal balance = parseMoney(cells.get(1).getText());
                String availableText = cells.size() >= 3 ? cells.get(2).getText().trim() : "";
                if (!availableText.startsWith("$")) {
                    availableValid = false;
                }
                balances.put(accountId, balance);
            } else if ("Total".equalsIgnoreCase(cells.get(0).getText().trim())) {
                total = parseMoney(cells.get(1).getText());
            }
        }
        return new OverviewSnapshot(balances, total, availableValid);
    }

    private void waitForProfileFieldsLoaded() {
        wait.until(d -> {
            String street = d.findElement(By.id("customer.address.street")).getAttribute("value");
            String phone = d.findElement(By.id("customer.phoneNumber")).getAttribute("value");
            return street != null && !street.isBlank() && phone != null && !phone.isBlank();
        });
    }

    private static BigDecimal parseMoney(String raw) {
        String normalized = raw.replace("$", "").replace(",", "").trim();
        return new BigDecimal(normalized);
    }

    private static String uniqueUsername(String tcTag) {
        return "fins4" + tcTag + "_" + ThreadLocalRandom.current().nextInt(100000, 1000000);
    }

    private record OverviewSnapshot(
        Map<String, BigDecimal> balancesByAccount,
        BigDecimal total,
        boolean availableValid
    ) {
        BigDecimal sumOfAccountBalances() {
            return balancesByAccount.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }
}
