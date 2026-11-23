# Протокол запусків Surefire - Спринт 4

**Проєкт:** "ФінКит" (П2, онлайн-банкінг для фізичних осіб на базі demo ParaBank та модуля `ua.kpi.tzyapz.finkit`)  
**Етап:** Спринт 4 (Автоматизація: JUnit 5 і Selenium WebDriver)  
**Дата:** 09.11.2026 (повторно перевірено після виправлення юніт-тестів за рецензією викладача: 23.11.2026)  
**Автор:** ІТ Бурлачка Артем  
**Рецензент:** КТ Бойко Олег

---

## 1. Середовище виконання та контроль незмінності `finkit/v1`

- **ОС:** Windows 11 (`amd64`, версія `10.0`)
- **JDK:** Zulu OpenJDK `25.0.2` (`C:\Program Files\Zulu\zulu-25`, цільовий рівень компіляції `<maven.compiler.release>17</maven.compiler.release>`)
- **Apache Maven:** `3.9.9` (`mvn` або портативний `%USERPROFILE%\tools\apache-maven-3.9.9\bin\mvn.cmd`)
- **Плагіни та бібліотеки:** `maven-compiler-plugin:3.13.0`, `maven-surefire-plugin:3.2.5`, `junit-jupiter:5.10.2`, `selenium-java:4.21.0`
- **Браузер і драйвер:** Google Chrome `157.0.8103.79` (`--headless=new`, `--window-size=1280,900`), ChromeDriver `157.0.8103.78` (`%USERPROFILE%\.cache\selenium\chromedriver\win64\157.0.8103.78\chromedriver.exe`), стенд `https://parabank.parasoft.com/parabank/`
- **Перевірка побайтової ідентичності `src/main/java/ua/kpi/tzyapz/finkit/*.java` відносно `finkit/v1` (SHA-256):**
  - `AccountLedger.java`: `27FF59BD310B50BFE4D20CF5C65F5CAAF690918CFC32E78A0BF7F3C5952329D8` (ідентично `v1`)
  - `LedgerEntry.java`: `57649423520ACB0E2A602ED1342B856B0674791798149BFACF69758BC0D83894` (ідентично `v1`)
  - `LoanCalculator.java`: `688BBA75A1BF4DD7EE3722262ADE30520D7EDC8A2923E6A2417A77A9372BF9CB` (ідентично `v1`)
  - `TransferError.java`: `5FC580B615688A996BDE1ACD7974B32694F107321DAD237CD2D022ADE7BF71CF` (ідентично `v1`)
  - `TransferValidator.java`: `396A40AC918D0A11B93B5E71A2C0FC4C3A90B1BF46C231E40C0FEBCCCF7C80F8` (ідентично `v1`)

---

## 2. Зведення трьох контрольних запусків Maven Surefire (23.11.2026)

| № | Профіль запуску | Команда | Дата / завершення (локальний час) | Тривалість | Код виходу | Підсумковий рядок Surefire | Результат |
| :-: | --- | --- | --- | --- | :-: | --- | --- |
| 1 | Тільки юніт-тести (за замовчуванням `excludedGroups=ui`) | `mvn clean test` | `23.11.2026` | `7.138 s` | `1` | `Tests run: 69, Failures: 16, Errors: 0, Skipped: 0` | `BUILD FAILURE` (падають лише 10 задокументованих методів `v1`) |
| 2 | Тільки UI-тести (`@Tag("ui")`) | `mvn test -DexcludedGroups=none -Dtest="*UiTest"` | `23.11.2026` (`21:48:18+02:00`) | `01:07 min` | `0` | `Tests run: 10, Failures: 0, Errors: 0, Skipped: 0` | `BUILD SUCCESS` (10/10 UI-сценаріїв проходять) |
| 3 | Повний комбінований набір (Unit + UI) | `mvn test -DexcludedGroups=none` | `23.11.2026` (`21:44:37+02:00`) | `01:13 min` | `1` | `Tests run: 79, Failures: 16, Errors: 0, Skipped: 0` | `BUILD FAILURE` (усі 10 UI-тестів зелені; падіння лише через дефекти `v1`) |

---

## 3. Деталізація по тестових класах і зіставлення методів та викликів JUnit 5

Через використання `@ParameterizedTest` (`@CsvSource`, `@MethodSource`) один тестовий метод JUnit 5 породжує кілька окремих викликів у звіті Surefire. Обидві метрики зафіксовано нижче.

| Тестовий клас | Унікальних методів (усього / passed / failed) | Викликів Surefire: запуск 1 (`clean test`) | Викликів Surefire: запуск 2 (`*UiTest`) | Викликів Surefire: запуск 3 (повний, час) |
| --- | :---: | --- | --- | --- |
| `ua.kpi.tzyapz.finkit.AccountLedgerTest` | `8` (`5` / `3`) | `Tests run: 11, Failures: 3, Errors: 0, Skipped: 0` (`0.199 s`) | Не запускався | `Tests run: 11, Failures: 3, Errors: 0, Skipped: 0` (`0.228 s`) |
| `ua.kpi.tzyapz.finkit.LoanCalculatorTest` | `7` (`3` / `4`) | `Tests run: 29, Failures: 6, Errors: 0, Skipped: 0` (`0.170 s`) | Не запускався | `Tests run: 29, Failures: 6, Errors: 0, Skipped: 0` (`0.188 s`) |
| `ua.kpi.tzyapz.finkit.TransferValidatorTest` | `10` (`7` / `3`) | `Tests run: 29, Failures: 7, Errors: 0, Skipped: 0` (`0.110 s`) | Не запускався | `Tests run: 29, Failures: 7, Errors: 0, Skipped: 0` (`0.115 s`) |
| `ua.kpi.tzyapz.finkit.ui.AccountsAndPaymentsUiTest` | `5` (`5` / `0`) | Виключено (`excludedGroups=ui`) | `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0` (`39.57 s`) | `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0` (`42.85 s`) |
| `ua.kpi.tzyapz.finkit.ui.AuthAndPublicUiTest` | `5` (`5` / `0`) | Виключено (`excludedGroups=ui`) | `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0` (`24.91 s`) | `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0` (`26.51 s`) |
| **Разом** | **`35` (`25` / `10`)** | **`69` викликів (`53` pass / `16` fail)** | **`10` викликів (`10` pass / `0` fail)** | **`79` викликів (`63` pass / `16` fail)** |

Підсумок за методикою підрахунку Спринту 4:
- **Юніт-тести:** `25` унікальні методи (`15` зелених + `10` червоних через дефекти `v1`), `69` викликів Surefire (`53` пройдено, `16` провалено).
- **UI-тести (`@Tag("ui")`):** `10` унікальних методів (`10` пройдено, `0` провалено).
- **Повний комбінований запуск:** `35` унікальних методів / `79` викликів Surefire (`63` пройдено, `16` провалено, `0` помилок ініціалізації, `0` пропущених `@Disabled`).

---

## 4. Трасування всіх падаючих тестів до звітів про дефекти `v1`

Кожен із 10 падаючих юніт-методів (16 падаючих викликів Surefire) безпосередньо прив'язаний до зареєстрованої картки дефекту у `docs/sprint4/bug-reports.md`. Незрозумілих або нестабільних падінь у наборі немає.

| № | Код | Клас і метод тесту | Падаючих викликів | Пов'язаний звіт | Вимога BL | Фактична причина падіння на `finkit/v1` |
| :-: | --- | --- | :-: | --- | --- | --- |
| 1 | `ТА-1` | `LoanCalculatorTest#monthlyPayment_validRatesAndReferenceCases_matchesAnnuityFormula` | `3 / 4` | `BUG-M01` | `BL-10` | Ставка `i` округляється до 4 знаків (`9265.37` замість `9263.45`, `8789.73` замість `8791.59`, `8562.58` замість `8560.75`) |
| 2 | `ТА-2` | `LoanCalculatorTest#totalInterest_referenceAndNonDivisibleRate_usesRoundedMonthlyPayment` | `1 / 1` | `BUG-M01` | `BL-20` | Каскадна похибка `totalInterest` (`11184.44` замість `11161.40`, `5476.76` замість `5499.08`) |
| 3 | `КТ-1` | `LoanCalculatorTest#totalInterest_boundaryRatesAndHalfUpScale_returnsTwoDecimalPlaces` | `1 / 1` | `BUG-M01` | `BL-10`, `BL-20`, `BL-30` | Похибка на межах `5.00 %` (`10.52` vs `10.44`) та `40.00 %` (`1323505.40` vs `1325098.40`) |
| 4 | `ТА-3` | `LoanCalculatorTest#monthlyPayment_minBoundaryMonths3_isAccepted` | `1 / 1` | `BUG-M02` | `BL-30`, `BL-10` | Умова `months <= MIN_MONTHS` відхиляє включну межу `months = 3` з `IllegalArgumentException` (`12.00 %` ізольовано від `BUG-M01`) |
| 5 | `ІТ-2` | `TransferValidatorTest#validate_invalidToIban_returnsInvalidToIbanError` | `5 / 5` | `BUG-M03` | `BL-50`, `BL-40` | У `validate(...)` не перевіряється `toIban`; для всіх 5 невалідних значень повертається `[]` замість `INVALID_TO_IBAN` |
| 6 | `ІТ-3` | `TransferValidatorTest#validate_exactDailyLimit50000_isAllowed` | `1 / 1` | `BUG-M04` | `BL-40` | Умова `>= 0` блокує точну межу `50000.00` кодом `[DAILY_LIMIT_EXCEEDED]` |
| 7 | `СМ-3` | `AccountLedgerTest#withdraw_exactFullBalance_setsBalanceToZero` | `1 / 1` | `BUG-M05` | `BL-70`, `BL-80` | Умова `value.compareTo(balance) >= 0` забороняє зняття повного залишку `250.00` |
| 8 | `СМ-4` | `AccountLedgerTest#history_multipleOperations_returnsChronologicalOrder` | `1 / 1` | `BUG-M06` | `BL-70`, `BL-80` | Виклик `Collections.reverse(copy)` повертає історію у зворотному порядку |
| 9 | `КТ-3` | `TransferValidatorTest#isValidIban_validAndInvalidFormats_matchesBl50` | `1 / 9` | `BUG-M07` | `BL-50` | `isValidIban` не вилучає пробіли перед перевіркою шаблону `^UA[0-9]{27}$` (`"UA21 3223 ..."` дає `false`) |
| 10 | `АО-3` | `AccountLedgerTest#withdraw_amountAboveBalance_throwsAndLeavesBalanceUnchanged` | `1 / 1` | `BUG-M08` | `BL-70`, `BL-80` | При овердрафті `v1` кидає `IllegalStateException` замість `IllegalArgumentException` за `SRS-1.1` |

---

## 5. Додаток - Фрагменти консольного виводу Maven Surefire

### 5.1 Запуск 1 (`mvn clean test`)
```text
[INFO] Tests run: 69, Failures: 16, Errors: 0, Skipped: 0
[INFO] BUILD FAILURE
[INFO] Total time:  7.138 s
```

### 5.2 Запуск 2 (`mvn test -DexcludedGroups=none -Dtest="*UiTest"`)
```text
[INFO] Running ua.kpi.tzyapz.finkit.ui.AccountsAndPaymentsUiTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 39.57 s -- in ua.kpi.tzyapz.finkit.ui.AccountsAndPaymentsUiTest
[INFO] Running ua.kpi.tzyapz.finkit.ui.AuthAndPublicUiTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 24.91 s -- in ua.kpi.tzyapz.finkit.ui.AuthAndPublicUiTest
[INFO] Results:
[INFO] Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time:  01:07 min
[INFO] Finished at: 2026-11-23T21:48:18+02:00
```

### 5.3 Запуск 3 (`mvn test -DexcludedGroups=none`)
```text
[INFO] Running ua.kpi.tzyapz.finkit.AccountLedgerTest
[ERROR] Tests run: 11, Failures: 3, Errors: 0, Skipped: 0, Time elapsed: 0.228 s <<< FAILURE! -- in ua.kpi.tzyapz.finkit.AccountLedgerTest
[INFO] Running ua.kpi.tzyapz.finkit.LoanCalculatorTest
[ERROR] Tests run: 29, Failures: 6, Errors: 0, Skipped: 0, Time elapsed: 0.188 s <<< FAILURE! -- in ua.kpi.tzyapz.finkit.LoanCalculatorTest
[INFO] Running ua.kpi.tzyapz.finkit.TransferValidatorTest
[ERROR] Tests run: 29, Failures: 7, Errors: 0, Skipped: 0, Time elapsed: 0.115 s <<< FAILURE! -- in ua.kpi.tzyapz.finkit.TransferValidatorTest
[INFO] Running ua.kpi.tzyapz.finkit.ui.AccountsAndPaymentsUiTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 42.85 s -- in ua.kpi.tzyapz.finkit.ui.AccountsAndPaymentsUiTest
[INFO] Running ua.kpi.tzyapz.finkit.ui.AuthAndPublicUiTest
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 26.51 s -- in ua.kpi.tzyapz.finkit.ui.AuthAndPublicUiTest
[INFO] Results:
[INFO] Tests run: 79, Failures: 16, Errors: 0, Skipped: 0
[INFO] BUILD FAILURE
[INFO] Total time:  01:13 min
[INFO] Finished at: 2026-11-23T21:44:37+02:00
```
