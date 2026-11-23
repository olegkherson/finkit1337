# ФінКит (`finkit`) - автотести Спринту 4 (JUnit 5 + Selenium WebDriver)

Модуль бізнес-логіки `ua.kpi.tzyapz.finkit` версії `v1` (кредитний калькулятор, валідатор переказів, книга обліку рахунку) та набір автоматизованих UI-тестів онлайн-банкінгу "ФінКит" (demo ParaBank) у межах Спринту 4 ("Автоматизація: JUnit 5 і Selenium WebDriver").

Проєкт побудовано на базі стартового шаблону та незміненого модуля `finkit/v1` без зовнішніх залежностей у `src/main/java`. Модульні тести перевіряють вимоги `BL-10...BL-80` (`docs/sprint2/SRS-1.1.md`), а UI-тести (`@Tag("ui")`) відтворюють 10 стабільних сценаріїв Спринту 3 у реальному headless Google Chrome.

---

## Склад команди і ролі у спринтах 4-5

| № | ПІБ | GitHub | Роль у Спринті 4 | Роль у Спринті 5 |
| :-: | --- | --- | --- | --- |
| 1 | Бойко Олег | `olegkherson` | КТ (Керівник тестування) | ТА (Тест-аналітик) |
| 2 | Закусило Дмитрій | `FB43ZakusyloDmytriy` | ТА (Тест-аналітик) | ІТ (Інженер з тестування) |
| 3 | Бурлачка Артем | `theluminal` | ІТ (Інженер з тестування) | СМ (Скрам-майстер) |
| 4 | Топчій Микита | `MykytaTop` | СМ (Скрам-майстер / рецензент) | АО (Аудитор-опонент) |
| 5 | Сушко Ілля | `Illsush` | АО (Аудитор-опонент) | КТ (Керівник тестування) |

---

## Вимоги до середовища

- **Java:** JDK 17 або новіша (цільовий рівень компіляції у `pom.xml`: `<maven.compiler.release>17</maven.compiler.release>`; перевірено на Zulu OpenJDK `25.0.2`).
- **Система збірки:** Apache Maven `3.9.9` (підтримується `3.8+`). Якщо `mvn` не додано до системного `PATH`, використовується повний шлях до портативного виконуваного файлу Maven (`mvn.cmd` / `mvn`).
- **Залежності та плагіни (завантажуються Maven автоматично з Maven Central):**
  - `org.junit.jupiter:junit-jupiter:5.10.2`
  - `org.seleniumhq.selenium:selenium-java:4.21.0` (містить Selenium Manager для автоматичного отримання сумісного `chromedriver`)
  - `maven-compiler-plugin:3.13.0`, `maven-surefire-plugin:3.2.5`
- **Браузер та мережа (для UI-тестів із `@Tag("ui")`):** Google Chrome (`157.0.8103.79` або новіший) та доступ до `https://parabank.parasoft.com/parabank/`.

---

## Як зібрати і запустити

Усі команди виконуються з кореневого каталогу проєкту:

1. **Збірка та запуск лише юніт-тестів JUnit 5** (група `ui` виключена за замовчуванням у `pom.xml` через `<excludedGroups>ui</excludedGroups>`):
   ```powershell
   mvn clean test
   ```
   або для перевірки лише юніт-класів:
   ```powershell
   mvn test -Dtest="*Test"
   ```
   *(На Windows за використання портативного Maven замість `mvn` викликається повний шлях до `mvn.cmd`, наприклад `& "%USERPROFILE%\tools\apache-maven-3.9.9\bin\mvn.cmd" clean test`.)*

   **Очікувана поведінка на модулі `finkit/v1`:** команда завершується з кодом виходу `1` (`BUILD FAILURE`), оскільки `10` із `23` юніт-методів (`16` із `67` викликів Surefire) обгрунтовано падають через зафіксовані дефекти та розбіжності модуля `v1` (`BUG-001...BUG-008`). Жоден падаючий тест не вимкнено через `@Disabled` і не підігнано під баг.

2. **Запуск лише WebDriver UI-тестів у реальному headless Chrome (`@Tag("ui")`):**
   ```powershell
   mvn test -DexcludedGroups=none -Dtest="*UiTest"
   ```
   **Очікувана поведінка:** `Tests run: 10, Failures: 0, Errors: 0, Skipped: 0`, `BUILD SUCCESS` (код виходу `0`). Кожен тест реєструє окремого ізольованого користувача `fins4...`, працює з явними очікуваннями `WebDriverWait` та гарантовано закриває браузер у `@AfterEach`.

3. **Повний комбінований запуск (усі юніт-тести + усі UI-тести):**
   ```powershell
   mvn test -DexcludedGroups=none
   ```
   **Очікувана поведінка:** `Tests run: 77, Failures: 16, Errors: 0, Skipped: 0` (усі `10` UI-тестів проходять, усі `16` падінь належать задокументованим дефектам `v1`).

4. **Перегляд звітів Surefire:**
   Текстові та XML-звіти формуються в каталозі `target/surefire-reports/` (`*.txt`, `TEST-*.xml`), а зведений протокол прогонів зафіксовано в `docs/surefire-summary.md`.

---

## Структура

```text
finkit_sprint4/
├── README.md                                  # опис проєкту, інструкція збірки, покриття та внесок
├── .gitignore                                 # виключення target/, .idea/, *.iml, out/, .DS_Store
├── pom.xml                                    # JDK 17, JUnit 5.10.2, Selenium 4.21.0, Surefire 3.2.5
├── docs/
│   ├── task-board.md                          # таблиця розподілу задач команди (8 задач, 32 підзадачі)
│   ├── coverage-matrix.md                     # матриця покриття BL-10...BL-80 із авторами і рецензентами
│   ├── ui-scenarios.md                        # специфікація 10+3 UI-сценаріїв і 2 сценаріїв Selenium IDE
│   ├── bug-reports.md                         # звіти про дефекти v1 у форматі Спринту 3 (BUG-001...BUG-008)
│   ├── code-review.md                         # протокол перехресного code review (чек-листи 5.3 і 13.2)
│   ├── surefire-summary.md                    # протокол 3 контрольних запусків Surefire та SHA-256 v1
│   ├── quality-dossier-v4.md                  # Досьє якості v4 (розділ "Автоматизація")
│   ├── audit.md                               # протокол відтворення "з нуля" і аудиту АО
│   └── retro.md                               # протокол ретроспективи Start/Stop/Continue
├── selenium-ide/
│   ├── finkit.side                            # проєкт Selenium IDE (JSON 2.0, сценарії TC-004 і TC-012)
│   └── export/
│       ├── Tc004LoginGreetingIdeDraftTest.java # експортована Java-чернетка для TC-004 (довідково)
│       └── Tc012TransferFundsIdeDraftTest.java # експортована Java-чернетка для TC-012 (довідково)
└── src/
    ├── main/java/ua/kpi/tzyapz/finkit/        # незмінений модуль продукту v1
    │   ├── AccountLedger.java
    │   ├── LedgerEntry.java
    │   ├── LoanCalculator.java
    │   ├── TransferError.java
    │   └── TransferValidator.java
    └── test/java/ua/kpi/tzyapz/finkit/
        ├── AccountLedgerTest.java             # 7 юніт-тестів (СМ: 5, АО: 2)
        ├── LoanCalculatorTest.java            # 7 юніт-тестів (ТА: 5, КТ: 2)
        ├── TransferValidatorTest.java         # 9 юніт-тестів (ІТ: 5, КТ: 2, АО: 2)
        └── ui/
            ├── AccountsAndPaymentsUiTest.java # 5 UI-тестів (TC-008, TC-012, TC-018, TC-021, TC-022)
            └── AuthAndPublicUiTest.java       # 5 UI-тестів (TC-033, TC-001, TC-003, TC-004, TC-007)
```

---

## Що покрито

### Юніт-тести JUnit 5 (23 методи, `67` викликів Surefire)

| Тест | Вимога SRS | Тест-кейс / код | Автор |
| --- | --- | --- | --- |
| `LoanCalculatorTest#monthlyPayment_validRatesAndReferenceCases_matchesAnnuityFormula` | `BL-10` | `ТА-1` (`BUG-001`) | ТА Закусило Дмитрій |
| `LoanCalculatorTest#totalInterest_referenceAndNonDivisibleRate_usesRoundedMonthlyPayment` | `BL-20` | `ТА-2` (`BUG-001`) | ТА Закусило Дмитрій |
| `LoanCalculatorTest#monthlyPayment_minBoundaryMonths3_isAccepted` | `BL-30`, `BL-10` | `ТА-3` (`BUG-002`) | ТА Закусило Дмитрій |
| `LoanCalculatorTest#monthlyPayment_inclusivePrincipalRateAndMaxMonthsBoundaries_succeed` | `BL-30` | `ТА-4` | ТА Закусило Дмитрій |
| `LoanCalculatorTest#monthlyPayment_outOfBoundsOrNullParameters_throwsIllegalArgumentException` | `BL-30` | `ТА-5` | ТА Закусило Дмитрій |
| `LoanCalculatorTest#totalInterest_boundaryRatesAndHalfUpScale_returnsTwoDecimalPlaces` | `BL-10`, `BL-20`, `BL-30` | `КТ-1` (`BUG-001`) | КТ Бойко Олег |
| `LoanCalculatorTest#totalInterest_outOfBounds_throwsIllegalArgumentException` | `BL-30`, `BL-20` | `КТ-2` | КТ Бойко Олег |
| `TransferValidatorTest#validate_validTransferAndBelowDailyLimit_returnsEmptyList` | `BL-40`, `BL-50` | `ІТ-1` | ІТ Бурлачка Артем |
| `TransferValidatorTest#validate_invalidToIban_returnsInvalidToIbanError` | `BL-50`, `BL-40` | `ІТ-2` (`BUG-003`) | ІТ Бурлачка Артем |
| `TransferValidatorTest#validate_exactDailyLimit50000_isAllowed` | `BL-40` | `ІТ-3` (`BUG-004`) | ІТ Бурлачка Артем |
| `TransferValidatorTest#validate_aboveDailyLimit50000_returnsDailyLimitExceeded` | `BL-40` | `ІТ-4` | ІТ Бурлачка Артем |
| `TransferValidatorTest#validate_nonPositiveOrNullAmount_returnsAmountNotPositive` | `BL-40` | `ІТ-5` | ІТ Бурлачка Артем |
| `TransferValidatorTest#isValidIban_validAndInvalidFormats_matchesBl50` | `BL-50` | `КТ-3` (`BUG-007`) | КТ Бойко Олег |
| `TransferValidatorTest#validate_sameAccountAndMultipleViolations_collectsAllExpectedErrors` | `BL-40`, `BL-50` | `КТ-4` | КТ Бойко Олег |
| `TransferValidatorTest#validate_invalidFromIban_returnsInvalidFromIbanError` | `BL-50`, `BL-40` | `АО-1` | АО Сушко Ілля |
| `TransferValidatorTest#validate_amountAtAndAboveBalanceBoundary_validatesInsufficientFunds` | `BL-40` | `АО-2` | АО Сушко Ілля |
| `AccountLedgerTest#deposit_validAmountsAndHalfUpRounding_updatesBalance` | `BL-70`, `BL-80` | `СМ-1` | СМ Топчій Микита |
| `AccountLedgerTest#withdraw_partialValidAmount_reducesBalance` | `BL-70`, `BL-80` | `СМ-2` | СМ Топчій Микита |
| `AccountLedgerTest#withdraw_exactFullBalance_setsBalanceToZero` | `BL-70`, `BL-80` | `СМ-3` (`BUG-005`) | СМ Топчій Микита |
| `AccountLedgerTest#history_multipleOperations_returnsChronologicalOrder` | `BL-70`, `BL-80` | `СМ-4` (`BUG-006`) | СМ Топчій Микита |
| `AccountLedgerTest#depositAndWithdraw_zeroOrNegativeOrNullAmount_throwsAndPreservesBalance` | `BL-70`, `BL-80` | `СМ-5` | СМ Топчій Микита |
| `AccountLedgerTest#withdraw_amountAboveBalance_throwsAndLeavesBalanceUnchanged` | `BL-70`, `BL-80` | `АО-3` (`BUG-008`) | АО Сушко Ілля |
| `AccountLedgerTest#deposit_upperBoundary999999999_99_andImmutableHistory_succeeds` | `BL-80`, `BL-70` | `АО-4` | АО Сушко Ілля |

*(Примітка щодо `BL-60`: файл локального реєстру `resources/recipients.csv` та логіка перевірки реєстру одержувачів відсутні у постачанні модуля `finkit/v1`, тому `BL-60` прозоро зафіксовано в `docs/coverage-matrix.md` як нереалізоване в `v1`.)*

### UI-тести WebDriver (`@Tag("ui")`, 10 сценаріїв) і Selenium IDE (2 сценарії)

| Тест | Вимога SRS | Тест-кейс | Автор |
| --- | --- | --- | --- |
| `AuthAndPublicUiTest#customerLoginBlock_isDisplayedOnPublicPages` | `GUI-10` | `TC-033` | ІТ Бурлачка Артем |
| `AuthAndPublicUiTest#register_validElevenFields_logsInAutomatically` | `F-10` | `TC-001` | ІТ Бурлачка Артем |
| `AuthAndPublicUiTest#register_passwordMismatch_showsErrorAndBlocksLogin` | `GUI-30` | `TC-003` | ІТ Бурлачка Артем |
| `AuthAndPublicUiTest#login_afterLogout_opensCabinetWithGreeting` | `F-20` | `TC-004` | ІТ Бурлачка Артем |
| `AuthAndPublicUiTest#lookup_mismatchedSsn_showsNotFoundError` | `F-30` | `TC-007` | ІТ Бурлачка Артем |
| `AccountsAndPaymentsUiTest#overview_displaysAccountsAndConsistentTotal` | `F-40` | `TC-008` | ІТ Бурлачка Артем |
| `AccountsAndPaymentsUiTest#transferFunds_betweenOwnAccounts_updatesBalances` | `F-70` | `TC-012` | ІТ Бурлачка Артем |
| `AccountsAndPaymentsUiTest#billPay_mismatchedAccountNumbers_showsValidationError` | `GUI-60` | `TC-018` | ІТ Бурлачка Артем |
| `AccountsAndPaymentsUiTest#findTransactions_byAmount_returnsMatchingTransfer` | `F-90` | `TC-021` | ІТ Бурлачка Артем |
| `AccountsAndPaymentsUiTest#updateProfile_persistsNewAddressAndPhone` | `F-100` | `TC-022` | ІТ Бурлачка Артем |
| `selenium-ide/finkit.side` (`TC-004`: вхід і привітання) + `export/Tc004LoginGreetingIdeDraftTest.java` | `F-20`, `GUI-40` | `TC-004` | ІТ Бурлачка Артем |
| `selenium-ide/finkit.side` (`TC-012`: переказ коштів) + `export/Tc012TransferFundsIdeDraftTest.java` | `F-70`, `L-10` | `TC-012` | ІТ Бурлачка Артем |

---

## Дефекти

Повні картки за форматом Спринту 3 (кроки відтворення, фактичні значення Surefire, очікування за `SRS-1.1.md`, рецензії) наведено в `docs/bug-reports.md`:

| Bug ID | Короткий опис дефекту модуля `finkit/v1` | Вимога BL | Severity | Priority | Пов'язані тести |
| --- | --- | --- | :---: | :---: | --- |
| `BUG-001` | Проміжне округлення місячної ставки `i` до 4 знаків у `LoanCalculator` спотворює платіж і переплату | `BL-10`, `BL-20` | Major | P2 | `ТА-1`, `ТА-2`, `КТ-1` |
| `BUG-002` | `LoanCalculator` відхиляє включну нижню межу строку 3 місяці (`months <= MIN_MONTHS`) | `BL-30`, `BL-10` | Major | P2 | `ТА-3` |
| `BUG-003` | `TransferValidator` не перевіряє формат `toIban` і не повертає `INVALID_TO_IBAN` | `BL-50`, `BL-40` | Major | P2 | `ІТ-2` |
| `BUG-004` | `TransferValidator` блокує переказ при `todaySpent + amount == 50 000.00` (`>= 0` замість `> 0`) | `BL-40` | Major | P2 | `ІТ-3` |
| `BUG-005` | `AccountLedger.withdraw` забороняє зняття повного балансу `amount == balance` (`>= 0`) | `BL-70`, `BL-80` | Major | P2 | `СМ-3` |
| `BUG-006` | `AccountLedger.history` повертає операції у зворотному порядку (`Collections.reverse`) | `BL-70`, `BL-80` | Major | P2 | `СМ-4` |
| `BUG-007` | `TransferValidator.isValidIban` відхиляє валідний IBAN із пробілами всупереч `BL-50` | `BL-50` | Normal | P3 | `КТ-3` |
| `BUG-008` | `AccountLedger.withdraw` при овердрафті кидає `IllegalStateException` замість `IllegalArgumentException` за `BL-70` | `BL-70`, `BL-80` | Normal | P3 | `АО-3` |

---

## Версії

- **`v1` - Спринт 4 (поточний стан модуля `ua.kpi.tzyapz.finkit`):**
  - `mvn clean test` (юніт-тести): `23` унікальні методи (`13` пройдено, `10` падають через дефекти `BUG-001...BUG-008`), `67` викликів Surefire (`51` пройдено, `16` провалено, `0` помилок, `0` пропущених).
  - `mvn test -DexcludedGroups=none -Dtest="*UiTest"` (UI-тести): `10` із `10` сценаріїв проходять у headless Chrome (`BUILD SUCCESS`).
  - `mvn test -DexcludedGroups=none` (повний набір): `77` викликів (`61` пройдено, `16` провалено, `0` помилок, `0` пропущених).
- **`v2` - Спринт 5 (заплановано):** виправлення дефектів `BUG-001...BUG-008`, реалізація `BL-60` (`resources/recipients.csv`) та повторна регресія на оновленому модулі.

**Персональний внесок учасників у юніт-тести JUnit 5 (при нормі >= 3 на особу та >= 20 на команду):**
- Закусило Дмитрій (`ТА`): **5** юніт-тестів (`ТА-1...ТА-5` у `LoanCalculatorTest`);
- Бурлачка Артем (`ІТ`): **5** юніт-тестів (`ІТ-1...ІТ-5` у `TransferValidatorTest`), **10** WebDriver UI-тестів і `finkit.side` + `export/`;
- Бойко Олег (`КТ`): **4** юніт-тести (`КТ-1`, `КТ-2` у `LoanCalculatorTest`; `КТ-3`, `КТ-4` у `TransferValidatorTest`);
- Топчій Микита (`СМ`): **5** юніт-тестів (`СМ-1...СМ-5` у `AccountLedgerTest`);
- Сушко Ілля (`АО`): **4** юніт-тести (`АО-1`, `АО-2` у `TransferValidatorTest`; `АО-3`, `АО-4` у `AccountLedgerTest`).

---

## Спринт 4 (Автоматизація: JUnit 5 і Selenium WebDriver)

- Докладна інструкція збірки, покриття `BL-10...BL-80` та підсумки прогонів: [`docs/sprint4/README.md`](docs/sprint4/README.md)
- Досьє якості v4, матриця покриття та звіти про дефекти (`BUG-M01...BUG-M08`): [`docs/sprint4/quality-dossier-v4.md`](docs/sprint4/quality-dossier-v4.md), [`docs/sprint4/coverage-matrix.md`](docs/sprint4/coverage-matrix.md), [`docs/sprint4/bug-reports.md`](docs/sprint4/bug-reports.md)
- Команда запуску юніт-тестів JUnit 5: `mvn clean test`
