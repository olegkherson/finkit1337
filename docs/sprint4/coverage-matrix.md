# Матриця покриття вимог BL - Спринт 4

**Проєкт:** "ФінКит" (П2, онлайн-банкінг для фізичних осіб на базі demo ParaBank та модуля `ua.kpi.tzyapz.finkit`)  
**Етап:** Спринт 4 (Автоматизація: JUnit 5 і Selenium WebDriver)  
**Дата:** 10.11.2026  
**Автор:** ТА Закусило Дмитрій (фіналізовано КТ Бойком Олегом за результатами Surefire)  
**Рецензент:** КТ Бойко Олег, СМ Топчій Микита

---

## 1. Мета та принципи трасування вимог

Цей документ фіксує повне трасування вимог до бізнес-логіки модуля `ua.kpi.tzyapz.finkit` (`BL-10...BL-80`) із затвердженої специфікації `docs/sprint2/SRS-1.1.md` та плану тестування `docs/sprint2/test-plan.md` до 23 модульних тестів JUnit 5 версії `finkit/v1`.

Основні правила верифікації та інтерпретації оракулів:
1. Вихідні файли `finkit/v1` (`LoanCalculator.java`, `TransferValidator.java`, `TransferError.java`, `AccountLedger.java`, `LedgerEntry.java`) у Спринті 4 не модифікуються.
2. Очікувані значення у всіх 23 юніт-тестах визначаються виключно за правилами, формулами й межами `SRS-1.1.md` (`BL-10...BL-80`) і `test-plan.md`, без підганяння під фактичну поведінку `v1`.
3. Тести, які падають ("червоні") через виявлені дефекти реалізації `v1` або розбіжності між `SRS-1.1.md` та контрактом `v1`, не вимикаються (`@Disabled` не використовується) і мають прямий зв'язок зі звітами у `docs/bug-reports.md` (`BUG-001...BUG-008`). У наборі відсутні незрозумілі падіння.
4. За нормативом команди з 5 осіб кожен учасник має не менше 3 юніт-тестів із явним авторством та перехресним рецензентом (`Автор != Рецензент`).

---

## 2. Зведена таблиця покриття вимог BL-10...BL-80

| ID вимоги | Пріоритет | Клас / метод `v1` | Правило специфікації `SRS-1.1.md` | К-сть тестів | Пов'язані тести (`Class#id`) | Статус покриття на `v1` |
| --- | :---: | --- | --- | :---: | --- | --- |
| `BL-10` | High | `LoanCalculator.monthlyPayment` | Щомісячний ануїтетний платіж `A = P * i / (1 - (1 + i)^(-n))`, `i = r / 12 / 100`, проміжні обчислення без округлення, результат `BigDecimal` зі шкалою 2 (`HALF_UP`). | 3 | `LoanCalculatorTest` (`ТА-1`, `ТА-3`, `КТ-1`) | Покрито; виявлено дефект проміжного округлення `i` (`BUG-001`). |
| `BL-20` | Medium | `LoanCalculator.totalInterest` | Загальна переплата `A * n - P`, де `A` - округлений результат `BL-10`, округлення `HALF_UP` до 2 знаків. | 3 | `LoanCalculatorTest` (`ТА-2`, `КТ-1`, `КТ-2`) | Покрито; каскадно падає через похибку `A` у `BL-10` (`BUG-001`). |
| `BL-30` | High | `LoanCalculator.monthlyPayment`, `totalInterest` | Допустимі межі: `P` у `1 000..1 000 000` грн, `n` у `3..60` міс., `r` у `5..40` % (усі включно); поза межами або `null` -> `IllegalArgumentException`. | 5 | `LoanCalculatorTest` (`ТА-3`, `ТА-4`, `ТА-5`, `КТ-1`, `КТ-2`) | Покрито; виявлено відхилення нижньої межі `n = 3` (`BUG-002`). |
| `BL-40` | High | `TransferValidator.validate`, `isValid` | Дозволено лише за `amount > 0`, `amount <= balance`, `todaySpent + amount <= 50 000`, `fromIban != toIban`; результат - список `TransferError`. | 8 | `TransferValidatorTest` (`ІТ-1`, `ІТ-2`, `ІТ-3`, `ІТ-4`, `ІТ-5`, `КТ-4`, `АО-1`, `АО-2`) | Покрито; виявлено відхилення рівного денного ліміту `50 000` (`BUG-004`) і відсутність `INVALID_TO_IBAN` (`BUG-003`). |
| `BL-50` | High | `TransferValidator.validate`, `isValidIban` | Дійсний IBAN: `"UA"` великими літерами + 27 цифр (`^UA[0-9]{27}$`), пробіли вилучаються перед перевіркою; `null` і решта -> недійсні (`INVALID_FROM_IBAN`, `INVALID_TO_IBAN`). | 5 | `TransferValidatorTest` (`ІТ-1`, `ІТ-2`, `КТ-3`, `КТ-4`, `АО-1`) | Покрито; виявлено пропуск валідації `toIban` (`BUG-003`) і невилучення пробілів у `isValidIban` (`BUG-007`). |
| `BL-60` | Medium | `TransferValidator` (відсутній у `v1`) | Перевірка існування рахунку-одержувача в локальному реєстрі `resources/recipients.csv` перед `BL-50`. | 0 | Немає (функціональність і файл відсутні у `v1`) | Не реалізовано у модулі `v1` (див. примітку нижче). |
| `BL-70` | High | `AccountLedger.deposit`, `withdraw`, `balance`, `history` | Поповнення/зняття додатної суми; при `<= 0` або `null` - `IllegalArgumentException` без зміни балансу; при `amount > balance()` - заборона овердрафту (`IllegalArgumentException` за `SRS-1.1`), історія операцій. | 7 | `AccountLedgerTest` (`СМ-1`, `СМ-2`, `СМ-3`, `СМ-4`, `СМ-5`, `АО-3`, `АО-4`) | Покрито; виявлено заборону `amount == balance` (`BUG-005`), зворотну `history()` (`BUG-006`) і розбіжність типу винятку овердрафту (`BUG-008`). |
| `BL-80` | Medium | `AccountLedger`, `LedgerEntry` | Межі операцій `0.01..999 999 999.99` грн, округлення `HALF_UP` для >2 знаків після коми, межі балансу `0` і максимум, незмінність записів історії. | 7 | `AccountLedgerTest` (`СМ-1`, `СМ-2`, `СМ-3`, `СМ-4`, `СМ-5`, `АО-3`, `АО-4`) | Покрито повністю (включно з `999999999.99` та `HALF_UP`). |

### 2.1 Прозорий опис `BL-60`, 6 закладених дефектів `v1` та розбіжностей `SRS-1.1` vs Javadoc `v1`

1. **Статус вимоги `BL-60` (`resources/recipients.csv`):**
   - У `docs/sprint2/SRS-1.1.md` (розд. 5, рядок 189) вимога `BL-60` передбачає перевірку існування рахунку-одержувача за локальним реєстром `resources/recipients.csv` перед перевіркою формату IBAN за `BL-50`.
   - У постачанні модуля `finkit/v1` (`code/modules/finkit/v1/README.md` і `TransferValidator.java`) файл `resources/recipients.csv` та логіка зчитування реєстру повністю відсутні, а документація `v1` прямо обмежує `TransferValidator` вимогами `BL-40` та `BL-50`.
   - Команда не створює фіктивних ресурсів у `src/main/resources` і не змінює `v1`. У Спринті 4 статус `BL-60` прозоро фіксується як **"Не реалізовано у модулі v1"** та виноситься до обмежень інкременту, Досьє якості v4 і рекомендацій на Спринт 5 (`v2`).

2. **Шість закладених дефектів модуля `finkit/v1` (`BUG-001...BUG-006`):**
   - **`BUG-001` (`LoanCalculator.java:36-38`, вимоги `BL-10`, `BL-20`):** при обчисленні місячної ставки `annualRatePercent.divide(BigDecimal.valueOf(1200), 4, RoundingMode.HALF_UP)` ставка `i` передчасно округлюється до 4 знаків після коми. Для ставок, кратних 12 (`12%` -> `0.01`), похибка не виникає, але для некратних ставок (`5%`, `10%`, `20%`, `40%`) вона спотворює `monthlyPayment` і каскадно `totalInterest`. Падаючі тести: `ТА-1`, `ТА-2`, `КТ-1`.
   - **`BUG-002` (`LoanCalculator.java:67`, вимога `BL-30`):** перевірка `if (months <= MIN_MONTHS || months > MAX_MONTHS)` відхиляє граничне значення `months = 3` винятком `IllegalArgumentException`, хоча за `BL-30`, Javadoc і текстом самого винятку строк `3..60` місяців є включним. Падаючий тест: `ТА-3`.
   - **`BUG-003` (`TransferValidator.java:32-37`, вимоги `BL-40`, `BL-50`):** метод `validate` перевіряє лише `!isValidIban(fromIban)`, але не викликає `!isValidIban(toIban)`, через що код `TransferError.INVALID_TO_IBAN` ніколи не повертається. Падаючий тест: `ІТ-2`.
   - **`BUG-004` (`TransferValidator.java:45`, вимога `BL-40`):** умова `if (todaySpent.add(amount).compareTo(DAILY_LIMIT) >= 0)` помилково блокує переказ, коли сума `todaySpent + amount` точно дорівнює `50 000.00` грн, тоді як `BL-40` дозволяє `todaySpent + amount <= 50 000`. Падаючий тест: `ІТ-3`.
   - **`BUG-005` (`AccountLedger.java:39`, вимоги `BL-70`, `BL-80`):** умова `if (value.compareTo(balance) >= 0)` забороняє зняття повного залишку (`amount == balance`) і залишати нульовий баланс `0.00`, хоча овердрафт виникає лише при `amount > balance`. Падаючий тест: `СМ-3`.
   - **`BUG-006` (`AccountLedger.java:62`, вимоги `BL-70`, `BL-80`):** у методі `history()` виклик `Collections.reverse(copy)` повертає записи у зворотному порядку всупереч контракту Javadoc `AccountLedger` ("у порядку виконання операцій"). Падаючий тест: `СМ-4`.

3. **Формалізовані розбіжності `SRS-1.1.md` vs Javadoc/код `v1` (`BUG-007`, `BUG-008`):**
   - **`BUG-007` (`TransferValidator.java:67-69`, вимога `BL-50`, тест `КТ-3`):** за `SRS-1.1.md` (`BL-50`) та `test-plan.md` (розд. 5.3) перед перевіркою шаблону `^UA[0-9]{27}$` пробіли мають вилучатися, тому `"UA21 3223 1300 0002 6007 2335 6600 1"` є валідним IBAN. Натомість у `v1` Javadoc `isValidIban` і код рядка 68 не видаляють пробіли перед `IBAN_UA.matcher(iban).matches()`. За принципом пріоритету `SRS-1.1.md` як оракула тест `КТ-3` очікує `true` для IBAN із пробілами, тому падає в `mvn clean test` і фіксується під `BUG-007`.
   - **`BUG-008` (`AccountLedger.java:35,40`, вимога `BL-70`, тест `АО-3`):** за `SRS-1.1.md` (`BL-70`) та `test-plan.md` метод `withdraw(amount)` при `amount > balance()` кидає `IllegalArgumentException`, тоді як у Javadoc (рядок 35) і реалізації `v1` (рядок 40) використовується `IllegalStateException`. За принципом пріоритету `SRS-1.1.md` як оракула тест `АО-3` очікує `IllegalArgumentException` (одночасно фіксуючи, що баланс `100.00` не змінюється), тому падає в `mvn clean test` і фіксується під `BUG-008`.
   - Таким чином, із **23 юніт-тестів** очікувано **13 проходять (зелені)** і **10 падають (червоні)**; кожен із 10 червоних тестів відповідає задокументованому звіту (`BUG-001...BUG-008`).

---

## 3. Повна матриця трасування юніт-тестів (`BL-вимога -> правило -> тест -> межі -> автор -> рецензент -> статус`)

| № / Код | BL-вимога | Правило специфікації | Тест (`Клас.метод`) | Граничні значення, класи еквівалентності та оракул | Автор | Рецензент | Фактичний статус на `v1` (Surefire) |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 (`ТА-1`) | `BL-10` | Ануїтетний платіж `A = P*i/(1-(1+i)^(-n))`, без проміжного округлення `i`, `HALF_UP` до 2 знаків | `LoanCalculatorTest.monthlyPayment_validRatesAndReferenceCases_matchesAnnuityFormula` (`@ParameterizedTest`) | `(100000, 12, 12) -> 8884.88`; `(100000, 20, 12) -> 9263.45` (еталон `test-plan.md`); `(100000, 10, 12) -> 8791.59`; `(100000, 5, 12) -> 8560.75` (`v1` дає `9265.37`, `8789.73`, `8562.58`) | ТА Закусило Дмитрій | СМ Топчій Микита (Схвалено) | **Failed -> `BUG-001`** (`1 pass / 3 fail` викликів) |
| 2 (`ТА-2`) | `BL-20` | Переплата `A * n - P` із використанням округленого `A`, `HALF_UP` до 2 знаків | `LoanCalculatorTest.totalInterest_referenceAndNonDivisibleRate_usesRoundedMonthlyPayment` (`@Test`) | `(100000, 20, 12) -> 11161.40` (`9263.45 * 12 - 100000`, еталон `test-plan.md`); `(100000, 10, 12) -> 5499.08` (`8791.59 * 12 - 100000`) | ТА Закусило Дмитрій | СМ Топчій Микита (Схвалено) | **Failed -> `BUG-001`** (`11184.44`, `5476.76`) |
| 3 (`ТА-3`) | `BL-30`, `BL-10` | Включна нижня межа строку `MIN_MONTHS = 3` для `monthlyPayment` | `LoanCalculatorTest.monthlyPayment_minBoundaryMonths3_isAccepted` (`@Test`) | `P = 1000.00`, `r = 5.00`, `n = 3` -> без винятку, результат `336.11` (`v1` кидає `IllegalArgumentException`) | ТА Закусило Дмитрій | СМ Топчій Микита (Схвалено) | **Failed -> `BUG-002`** (`IllegalArgumentException`) |
| 4 (`ТА-4`) | `BL-30` | Включні нижні, верхні та перехідні межі `P` (`1000..1000000`), `r` (`5..40`), `n` (`4..60`) приймаються без помилки | `LoanCalculatorTest.monthlyPayment_inclusivePrincipalRateAndMaxMonthsBoundaries_succeed` (`@ParameterizedTest`) | `(1000.00, 5.00, 4)`, `(1000000.00, 40.00, 60)`, `(1000.01, 5.01, 59)`, `(999999.99, 39.99, 59)`, `(1000.00, 5.00, 60)`, `(1000000.00, 40.00, 4)` -> без винятку, `scale() == 2`, `> 0` | ТА Закусило Дмитрій | СМ Топчій Микита (Схвалено) | **Passed** (`6 / 6` викликів) |
| 5 (`ТА-5`) | `BL-30` | Значення `P`, `r`, `n` за обома межами або `null` у `monthlyPayment` кидають `IllegalArgumentException` | `LoanCalculatorTest.monthlyPayment_outOfBoundsOrNullParameters_throwsIllegalArgumentException` (`@ParameterizedTest`) | `P = 999.99`, `P = 1000000.01`, `n = 2`, `n = 61`, `r = 4.99`, `r = 40.01`, `principal = null`, `rate = null` -> `IllegalArgumentException` | ТА Закусило Дмитрій | СМ Топчій Микита (Схвалено) | **Passed** (`8 / 8` викликів) |
| 6 (`КТ-1`) | `BL-10`, `BL-20`, `BL-30` | Точність `totalInterest` на граничних ставках `5.00%` і `40.00%` (без проміжного округлення `i`) та шкала `2` | `LoanCalculatorTest.totalInterest_boundaryRatesAndHalfUpScale_returnsTwoDecimalPlaces` (`@Test`) | `(1000.00, 5.00, 4) -> 10.44` (`252.61 * 4 - 1000`); `(1000000.00, 40.00, 60) -> 1325098.40` (`38751.64 * 60 - 1000000`), `scale() == 2` | КТ Бойко Олег | СМ Топчій Микита (Схвалено) | **Failed -> `BUG-001`** (`10.52`, `1323505.40`) |
| 7 (`КТ-2`) | `BL-30`, `BL-20` | Делегування валідації меж у `totalInterest` із киданням `IllegalArgumentException` | `LoanCalculatorTest.totalInterest_outOfBounds_throwsIllegalArgumentException` (`@ParameterizedTest`) | `P = 999.99`, `P = 1000000.01`, `n = 2`, `n = 61`, `r = 4.99`, `r = 40.01`, `null` -> `IllegalArgumentException` | КТ Бойко Олег | СМ Топчій Микита (Схвалено) | **Passed** (`8 / 8` викликів) |
| 8 (`ІТ-1`) | `BL-40`, `BL-50` | Валідні різні IBAN, додатна сума `<= balance`, `todaySpent + amount < 50000` -> переказ дозволено | `TransferValidatorTest.validate_validTransferAndBelowDailyLimit_returnsEmptyList` (`@Test`) | Кейс 1: `amount = 1000.00`, `balance = 5000.00`, `todaySpent = 0.00`; Кейс 2 (межа -0.01): `todaySpent = 49700.00`, `amount = 299.99` -> `[]`, `isValid == true` | ІТ Бурлачка Артем | СМ Топчій Микита (Схвалено) | **Passed** (`1 / 1` виклик) |
| 9 (`ІТ-2`) | `BL-50`, `BL-40` | Недійсний формат `toIban` повертає `TransferError.INVALID_TO_IBAN` | `TransferValidatorTest.validate_invalidToIban_returnsInvalidToIbanError` (`@ParameterizedTest`) | `toIban` in `{"UA123", "PL213223130000026007233566001", "ua213223130000026007233566001", "", null}` при валідних `fromIban`, `amount = 100.00`, `balance = 5000.00`, `todaySpent = 0.00` -> містить `INVALID_TO_IBAN` | ІТ Бурлачка Артем | СМ Топчій Микита (Схвалено) | **Failed -> `BUG-003`** (`0 pass / 5 fail` викликів) |
| 10 (`ІТ-3`) | `BL-40` | Включна верхня межа денного ліміту `todaySpent + amount == 50000.00` дозволена | `TransferValidatorTest.validate_exactDailyLimit50000_isAllowed` (`@Test`) | Кейс 1: `todaySpent = 49700.00`, `amount = 300.00`, `balance = 5000.00`; Кейс 2: `todaySpent = 0.00`, `amount = 50000.00`, `balance = 50000.00` -> `[]`, `isValid == true` | ІТ Бурлачка Артем | СМ Топчій Микита (Схвалено) | **Failed -> `BUG-004`** (`[DAILY_LIMIT_EXCEEDED]`) |
| 11 (`ІТ-4`) | `BL-40` | Перевищення денного ліміту (`todaySpent + amount > 50000.00`) повертає `DAILY_LIMIT_EXCEEDED` | `TransferValidatorTest.validate_aboveDailyLimit50000_returnsDailyLimitExceeded` (`@Test`) | `(todaySpent = 49700.00, amount = 300.01, balance = 5000.00)` і `(todaySpent = 50000.00, amount = 0.01, balance = 5000.00)` -> містить `DAILY_LIMIT_EXCEEDED` | ІТ Бурлачка Артем | СМ Топчій Микита (Схвалено) | **Passed** (`1 / 1` виклик) |
| 12 (`ІТ-5`) | `BL-40` | Нульова, від'ємна або `null` сума повертає `AMOUNT_NOT_POSITIVE`; `0.01` дозволено | `TransferValidatorTest.validate_nonPositiveOrNullAmount_returnsAmountNotPositive` (`@ParameterizedTest`) | `amount` in `{0.00, -0.01, -10.00, null}` -> містить `AMOUNT_NOT_POSITIVE`; окремо `0.01` -> `[]` | ІТ Бурлачка Артем | СМ Топчій Микита (Схвалено) | **Passed** (`4 / 4` викликів) |
| 13 (`КТ-3`) | `BL-50` | Формат `"UA"` + 27 цифр (`^UA[0-9]{27}$`); за `SRS-1.1` пробіли вилучаються перед перевіркою | `TransferValidatorTest.isValidIban_validAndInvalidFormats_matchesBl50` (`@ParameterizedTest`) | Валідні: `"UA213223130000026007233566001"`, `"UA21 3223 1300 0002 6007 2335 6600 1"` -> `true`; невалідні: `"ua21..."`, `"PL21..."`, 26 цифр, 28 цифр, `"UA...A"`, `""`, `null` -> `false` | КТ Бойко Олег | СМ Топчій Микита (Схвалено) | **Failed -> `BUG-007`** (`8 pass / 1 fail` викликів) |
| 14 (`КТ-4`) | `BL-40`, `BL-50` | Виявлення `SAME_ACCOUNT`, `INSUFFICIENT_FUNDS` та агрегація декількох порушень за один виклик | `TransferValidatorTest.validate_sameAccountAndMultipleViolations_collectsAllExpectedErrors` (`@Test`) | `fromIban == toIban` -> `SAME_ACCOUNT`; `amount = 5000.01` при `balance = 5000.00` -> `INSUFFICIENT_FUNDS`; комбінований кейс (`INVALID_FROM_IBAN`, `SAME_ACCOUNT`, `INSUFFICIENT_FUNDS`, `DAILY_LIMIT_EXCEEDED`) | КТ Бойко Олег | СМ Топчій Микита (Схвалено) | **Passed** (`1 / 1` виклик) |
| 15 (`АО-1`) | `BL-50`, `BL-40` | Недійсний `fromIban` повертає `INVALID_FROM_IBAN` і `isValid == false` | `TransferValidatorTest.validate_invalidFromIban_returnsInvalidFromIbanError` (`@ParameterizedTest`) | `fromIban` in `{"UA123", "ua213223130000026007233566001", "PL213223130000026007233566001", "", null}` -> містить `INVALID_FROM_IBAN` | АО Сушко Ілля | СМ Топчій Микита (Схвалено) | **Passed** (`5 / 5` викликів) |
| 16 (`АО-2`) | `BL-40` | Межа достатності коштів: `amount == balance` дозволено, `balance + 0.01` -> `INSUFFICIENT_FUNDS` | `TransferValidatorTest.validate_amountAtAndAboveBalanceBoundary_validatesInsufficientFunds` (`@Test`) | `balance = 5000.00`, `todaySpent = 0.00`: `amount = 5000.00` -> `[]`; `amount = 5000.01` -> `[INSUFFICIENT_FUNDS]` | АО Сушко Ілля | СМ Топчій Микита (Схвалено) | **Passed** (`1 / 1` виклик) |
| 17 (`СМ-1`) | `BL-70`, `BL-80` | `deposit` збільшує баланс із `HALF_UP` округленням дробових сум понад 2 знаки | `AccountLedgerTest.deposit_validAmountsAndHalfUpRounding_updatesBalance` (`@Test`) | Початково `0.00`; `deposit(0.01) -> 0.01`; `deposit(100.005) -> 100.02`; `deposit(10.004) -> 110.02` | СМ Топчій Микита | КТ Бойко Олег (Схвалено) | **Passed** (`1 / 1` виклик) |
| 18 (`СМ-2`) | `BL-70`, `BL-80` | `withdraw` зменшує баланс із `HALF_UP` округленням до копійок | `AccountLedgerTest.withdraw_partialValidAmount_reducesBalance` (`@Test`) | `deposit(500.00)`; `withdraw(120.50) -> 379.50`; `withdraw(0.01) -> 379.49`; `withdraw(0.005) -> 379.48` | СМ Топчій Микита | КТ Бойко Олег (Схвалено) | **Passed** (`1 / 1` виклик) |
| 19 (`СМ-3`) | `BL-70`, `BL-80` | Включна нижня межа залишку `0.00` при знятті всього балансу (`amount == balance`) | `AccountLedgerTest.withdraw_exactFullBalance_setsBalanceToZero` (`@Test`) | `deposit(250.00)`, `withdraw(250.00)` -> `balance() == 0.00`, запис `WITHDRAWAL 250.00 / 0.00` | СМ Топчій Микита | КТ Бойко Олег (Схвалено) | **Failed -> `BUG-005`** (`IllegalStateException`) |
| 20 (`СМ-4`) | `BL-70`, `BL-80` | Хронологічний порядок записів `history()` (від найпершої до останньої операції) | `AccountLedgerTest.history_multipleOperations_returnsChronologicalOrder` (`@Test`) | `deposit(100.00)`, `withdraw(30.00)`, `deposit(50.00)` -> `[DEPOSIT 100.00 / 100.00, WITHDRAWAL 30.00 / 70.00, DEPOSIT 50.00 / 120.00]` | СМ Топчій Микита | КТ Бойко Олег (Схвалено) | **Failed -> `BUG-006`** (зворотний порядок) |
| 21 (`СМ-5`) | `BL-70`, `BL-80` | Нульова, від'ємна або `null` сума для `deposit`/`withdraw` кидає `IllegalArgumentException` і зберігає стан | `AccountLedgerTest.depositAndWithdraw_zeroOrNegativeOrNullAmount_throwsAndPreservesBalance` (`@ParameterizedTest`) | Після `deposit(100.00)` для `0.00`, `-0.01`, `-100.00`, `null`: `IllegalArgumentException`, `balance() == 100.00`, `history().size() == 1` | СМ Топчій Микита | КТ Бойко Олег (Схвалено) | **Passed** (`4 / 4` викликів) |
| 22 (`АО-3`) | `BL-70`, `BL-80` | Овердрафт `amount > balance()`: за `SRS-1.1` `BL-70` кидає `IllegalArgumentException` без зміни балансу | `AccountLedgerTest.withdraw_amountAboveBalance_throwsAndLeavesBalanceUnchanged` (`@Test`) | `deposit(100.00)`, `withdraw(100.01)` -> `IllegalArgumentException` за `SRS-1.1` (`v1` кидає `IllegalStateException` за власним Javadoc), `balance() == 100.00` | АО Сушко Ілля | СМ Топчій Микита (Схвалено) | **Failed -> `BUG-008`** (`IllegalStateException` vs `IllegalArgumentException`) |
| 23 (`АО-4`) | `BL-80`, `BL-70` | Верхня межа операції `999 999 999.99` грн та незмінність колекції `history()` | `AccountLedgerTest.deposit_upperBoundary999999999_99_andImmutableHistory_succeeds` (`@Test`) | `deposit(999999999.99)` -> `balance() == 999999999.99`; спроба `history().add(...)` -> `UnsupportedOperationException` | АО Сушко Ілля | СМ Топчій Микита (Схвалено) | **Passed** (`1 / 1` виклик) |

---

## 4. Квоти авторства та перехресного рецензування (23 тести, 5 учасників)

| Учасник | Роль Спринту 4 | Тест-класи | Кількість тестів | Перелік кодів тестів | Рецензент |
| --- | :---: | --- | :---: | --- | --- |
| Закусило Дмитрій | ТА | `LoanCalculatorTest` | 5 | `ТА-1`, `ТА-2`, `ТА-3`, `ТА-4`, `ТА-5` | СМ Топчій Микита |
| Бурлачка Артем | ІТ | `TransferValidatorTest` | 5 | `ІТ-1`, `ІТ-2`, `ІТ-3`, `ІТ-4`, `ІТ-5` | СМ Топчій Микита |
| Бойко Олег | КТ | `LoanCalculatorTest` (2), `TransferValidatorTest` (2) | 4 | `КТ-1`, `КТ-2`, `КТ-3`, `КТ-4` | СМ Топчій Микита |
| Топчій Микита | СМ | `AccountLedgerTest` | 5 | `СМ-1`, `СМ-2`, `СМ-3`, `СМ-4`, `СМ-5` | КТ Бойко Олег |
| Сушко Ілля | АО | `TransferValidatorTest` (2), `AccountLedgerTest` (2) | 4 | `АО-1`, `АО-2`, `АО-3`, `АО-4` | СМ Топчій Микита |
| **Разом** | **5 осіб** | **3 тест-класи** | **23** | **23 юніт-тести (`13` зелених + `10` червоних за `BUG-001...BUG-008`)** | **100 % перехресна перевірка (Схвалено 10.11.2026)** |

---

## 5. Фінальний статус покриття після прогонів Surefire та рецензування (10.11.2026)

1. **Підсумки запуску юніт-тестів (`mvn clean test`, `excludedGroups=ui`):**
   - Унікальних юніт-методів: `23` (`13` Passed, `10` Failed -> `BUG-001...BUG-008`).
   - Викликів JUnit 5 у звіті Surefire: `67` (`51` Passed, `16` Failed, `0` Errors, `0` Skipped).
   - У розрізі класів: `LoanCalculatorTest` (`29` викликів / `6` падінь), `TransferValidatorTest` (`28` викликів / `7` падінь), `AccountLedgerTest` (`10` викликів / `3` падіння).
2. **Підсумки запуску WebDriver UI-тестів (`mvn test -DexcludedGroups=none -Dtest="*UiTest"`):**
   - `10` незалежних сценаріїв `@Tag("ui")` зі Спринту 3 (`AuthAndPublicUiTest`: `5/5`, `AccountsAndPaymentsUiTest`: `5/5`) -> `Tests run: 10, Failures: 0, Errors: 0, Skipped: 0` (`BUILD SUCCESS`).
3. **Повний комбінований прогін (`mvn test -DexcludedGroups=none`):**
   - `33` унікальні методи / `77` викликів Surefire (`61` Passed, `16` Failed, `0` Errors, `0` Skipped).
4. **Підтвердження перехресного рецензування (`docs/code-review.md`):**
   - `100 %` юніт-тестів (`23/23`), `100 %` UI-тестів (`10/10`), сценарії Selenium IDE (`2/2`) і звіти про дефекти (`8/8`) перевірені перехресним рецензентом (`СМ Топчій Микита` для ТА/ІТ/КТ/АО; `КТ Бойко Олег` для СМ і документів ТА/ІТ) та мають статус **Схвалено** (`10.11.2026`).
