# Звіти про дефекти модуля finkit/v1 - Спринт 4

**Проєкт:** "ФінКит" (П2, онлайн-банкінг для фізичних осіб на базі demo ParaBank та модуля `ua.kpi.tzyapz.finkit`)  
**Етап:** Спринт 4 (Автоматизація: JUnit 5 і Selenium WebDriver)  
**Дата:** 08.11.2026  
**Автор:** Команда "ФінКит" (розпочато ТА Закусилом Дмитрієм)  
**Рецензент:** СМ Топчій Микита, КТ Бойко Олег

---

Очікування: `docs/sprint2/SRS-1.1.md` (розд. 5, `BL-10...BL-80`) та `docs/sprint2/test-plan.md`. Для усунення колізії нумерації з UI-дефектами Спринту 3 (`BUG-001...BUG-025`, GitHub Issues `#13-#35`) модульним дефектам `finkit/v1` присвоєно унікальні ідентифікатори `BUG-M01...BUG-M08` (Issues `#36-#43`).

## Зведення

| Bug ID | Title | Severity | Priority | Status | Review | Linked Test | Вимога |
| --- | --- | --- | --- | --- | --- | --- | --- |
| BUG-M01 | Проміжне округлення місячної ставки i до 4 знаків у LoanCalculator спотворює платіж і переплату | Major | P2 | New | reviewed | `LoanCalculatorTest#monthlyPayment_validRatesAndReferenceCases_matchesAnnuityFormula`, `totalInterest_referenceAndNonDivisibleRate_usesRoundedMonthlyPayment`, `totalInterest_boundaryRatesAndHalfUpScale_returnsTwoDecimalPlaces` | `BL-10`, `BL-20` |
| BUG-M02 | LoanCalculator відхиляє включну нижню межу строку кредиту 3 місяці через умову months <= MIN_MONTHS | Major | P2 | New | reviewed | `LoanCalculatorTest#monthlyPayment_minBoundaryMonths3_isAccepted` | `BL-30`, `BL-10` |
| BUG-M03 | TransferValidator не перевіряє формат IBAN одержувача toIban і не повертає INVALID_TO_IBAN | Major | P2 | New | reviewed | `TransferValidatorTest#validate_invalidToIban_returnsInvalidToIbanError` | `BL-50`, `BL-40` |
| BUG-M04 | TransferValidator блокує переказ при досягненні точної межі денного ліміту 50 000.00 грн через умову >= 0 | Major | P2 | New | reviewed | `TransferValidatorTest#validate_exactDailyLimit50000_isAllowed` | `BL-40` |
| BUG-M05 | AccountLedger забороняє зняття повного залишку рахунку amount == balance через умову >= 0 | Major | P2 | New | reviewed | `AccountLedgerTest#withdraw_exactFullBalance_setsBalanceToZero` | `BL-70`, `BL-80` |
| BUG-M06 | AccountLedger.history повертає історію операцій у зворотному порядку через Collections.reverse | Major | P2 | New | reviewed | `AccountLedgerTest#history_multipleOperations_returnsChronologicalOrder` | `BL-70`, `BL-80` |
| BUG-M07 | TransferValidator.isValidIban відхиляє валідний IBAN із пробілами всупереч правилу BL-50 | Normal | P3 | New | reviewed | `TransferValidatorTest#isValidIban_validAndInvalidFormats_matchesBl50` | `BL-50` |
| BUG-M08 | AccountLedger.withdraw при овердрафті кидає IllegalStateException замість IllegalArgumentException за BL-70 | Normal | P3 | New | reviewed | `AccountLedgerTest#withdraw_amountAboveBalance_throwsAndLeavesBalanceUnchanged` | `BL-70`, `BL-80` |

## Повні звіти

## BUG-M01: Проміжне округлення місячної ставки i до 4 знаків у LoanCalculator спотворює платіж і переплату

**Severity:** Major  
**Priority:** P2  
**Status:** New  
**Review:** reviewed  
**Linked:** `LoanCalculatorTest#monthlyPayment_validRatesAndReferenceCases_matchesAnnuityFormula` (`ТА-1`), `LoanCalculatorTest#totalInterest_referenceAndNonDivisibleRate_usesRoundedMonthlyPayment` (`ТА-2`), `LoanCalculatorTest#totalInterest_boundaryRatesAndHalfUpScale_returnsTwoDecimalPlaces` (`КТ-1`); `BL-10`, `BL-20`  
**Reporter:** Закусило Дмитрій, 03.11.2026  
**Reviewer:** Топчій Микита, 08.11.2026

### Description
Вимога `BL-10` і вступ розділу 5 `SRS-1.1.md` визначають, що щомісячний ануїтетний платіж обчислюється за формулою `A = P * i / (1 - (1 + i)^(-n))`, де `i = r / 12 / 100`, а проміжні обчислення не округлюються; округлюється лише фінальний результат до 2 знаків після коми за `RoundingMode.HALF_UP`. Аналогічно `BL-20` вимагає обчислювати переплату `A * n - P` із коректного `A`. Фактично у `LoanCalculator.java` (рядки 36-38) при обчисленні `i` виконується `annualRatePercent.divide(BigDecimal.valueOf(1200), 4, RoundingMode.HALF_UP)`, що обрізає місячну ставку до 4 десяткових знаків і призводить до похибки в усіх ставках, некратних 12 (наприклад, `5%`, `10%`, `20%`), а також каскадно спотворює `totalInterest`. Severity Major: основна фінансова формула калькулятора повертає неточні суми платежу та переплати без обхідного шляху для ставок, некратних 12. Priority P2: дефект не блокує компіляцію або запуск решти тестів у Спринті 4, але підлягає обов'язковому виправленню у версії `v2` (Спринт 5).

### Steps to reproduce
1. Створити екземпляр `LoanCalculator calculator = new LoanCalculator();` (модуль `finkit/v1`).
2. Викликати `calculator.monthlyPayment(new BigDecimal("100000"), new BigDecimal("20"), 12)`.
3. Викликати `calculator.monthlyPayment(new BigDecimal("100000"), new BigDecimal("10"), 12)` та `calculator.monthlyPayment(new BigDecimal("100000"), new BigDecimal("5"), 12)`.
4. Викликати `calculator.totalInterest(new BigDecimal("100000"), new BigDecimal("20"), 12)` та `calculator.totalInterest(new BigDecimal("100000"), new BigDecimal("10"), 12)`.
5. Викликати `calculator.totalInterest(new BigDecimal("1000.00"), new BigDecimal("5.00"), 4)` та `calculator.totalInterest(new BigDecimal("1000000.00"), new BigDecimal("40.00"), 60)` (граничні ставки `КТ-1`).

### Actual result
Методи повертають викривлені значення внаслідок округлення `i` до 4 знаків:
- `monthlyPayment(100000, 20, 12)` = `9265.37` (замість `9263.45`);
- `monthlyPayment(100000, 10, 12)` = `8789.73` (замість `8791.59`);
- `monthlyPayment(100000, 5, 12)` = `8562.58` (замість `8560.75`);
- `totalInterest(100000, 20, 12)` = `11184.44` (замість `11161.40`);
- `totalInterest(100000, 10, 12)` = `5476.76` (замість `5499.08`);
- `totalInterest(1000.00, 5.00, 4)` = `10.52` (замість `10.44`);
- `totalInterest(1000000.00, 40.00, 60)` = `1323505.40` (замість `1325098.40`).

### Expected result
За `BL-10`, `BL-20` і `test-plan.md` (без проміжного округлення `i`):
- `monthlyPayment(100000, 20, 12)` = `9263.45`, `totalInterest(100000, 20, 12)` = `11161.40`;
- `monthlyPayment(100000, 10, 12)` = `8791.59`, `totalInterest(100000, 10, 12)` = `5499.08`;
- `monthlyPayment(100000, 5, 12)` = `8560.75`;
- `totalInterest(1000.00, 5.00, 4)` = `10.44`;
- `totalInterest(1000000.00, 40.00, 60)` = `1325098.40`.

### Environment
JDK 25.0.2 (цільовий рівень компіляції JDK 17), Apache Maven 3.9.9, JUnit Jupiter 5.10.2, Maven Surefire 3.2.5, модуль `ua.kpi.tzyapz.finkit` v1 (`LoanCalculator.java:36-38`), Windows 11, команда запуску: `mvn clean test`, 03.11.2026.

### Attachments
- `target/surefire-reports/ua.kpi.tzyapz.finkit.LoanCalculatorTest.txt` - зафіксовано падіння `ТА-1` (`9265.37`, `8789.73`, `8562.58`), `ТА-2` (`11184.44`, `5476.76`) та `КТ-1` (`10.52` замість `10.44` на рядку `LoanCalculatorTest.java:173`, `1323505.40` замість `1325098.40` на рядку `LoanCalculatorTest.java:177`); зведення у `docs/sprint4/surefire-summary.md`.

---

## BUG-M02: LoanCalculator відхиляє включну нижню межу строку кредиту 3 місяці через умову months <= MIN_MONTHS

**Severity:** Major  
**Priority:** P2  
**Status:** New  
**Review:** reviewed  
**Linked:** `LoanCalculatorTest#monthlyPayment_minBoundaryMonths3_isAccepted` (`ТА-3`); `BL-30`, `BL-10`  
**Reporter:** Закусило Дмитрій, 03.11.2026 (оновлено вхідну ставку в `ТА-3` на `12.00 %` 23.11.2026)  
**Reviewer:** Топчій Микита, 23.11.2026

### Description
Вимога `BL-30` (`SRS-1.1.md`), Javadoc методу `LoanCalculator.monthlyPayment` і текст повідомлення винятку встановлюють допустимий строк кредиту `3..60` місяців включно (`MIN_MONTHS = 3`). Фактично у приватному методі `LoanCalculator.validate` (`LoanCalculator.java:67`) записано сувору нерівність `if (months <= MIN_MONTHS || months > MAX_MONTHS)`, через що граничне значення `months = 3` помилково відхиляється з `IllegalArgumentException`. Дефект унеможливлює розрахунок кредиту на мінімальний дозволений строк 3 місяці як у `monthlyPayment`, так і в `totalInterest`. Severity Major: валідний граничний клас даних повністю блокується винятком без альтернативного сценарію для 3 місяців. Priority P2: не перешкоджає проходженню решти тестів Спринту 4, але потребує виправлення в `v2` (`months < MIN_MONTHS`).

### Steps to reproduce
1. Створити екземпляр `LoanCalculator calculator = new LoanCalculator();` (модуль `finkit/v1`).
2. Передати мінімально допустимі за `BL-30` параметри: `principal = new BigDecimal("1000.00")`, `annualRatePercent = new BigDecimal("12.00")`, `months = 3` (ставку `12.00 %` обрано кратною 12, щоб ізолювати `BUG-M02` від `BUG-M01`).
3. Викликати `calculator.monthlyPayment(principal, annualRatePercent, 3)`.

### Actual result
Метод кидає `java.lang.IllegalArgumentException: Строк має бути в межах 3..60 місяців` (стектрейс: `LoanCalculator.validate(LoanCalculator.java:68)` -> `LoanCalculator.monthlyPayment(LoanCalculator.java:35)`).

### Expected result
За `BL-30` та `BL-10` строк `months = 3` є включною нижньою межею, тому виняток не кидається, а `monthlyPayment(1000.00, 12.00, 3)` повертає `340.02`.

### Environment
JDK 25.0.2 (цільовий рівень компіляції JDK 17), Apache Maven 3.9.9, JUnit Jupiter 5.10.2, Maven Surefire 3.2.5, модуль `ua.kpi.tzyapz.finkit` v1 (`LoanCalculator.java:67`), Windows 11, команда запуску: `mvn clean test`, 03.11.2026 (перевірено 23.11.2026).

### Attachments
- `target/surefire-reports/ua.kpi.tzyapz.finkit.LoanCalculatorTest.txt` - стектрейс падіння `ТА-3` на `LoanCalculator.validate:68`; зведення у `docs/sprint4/surefire-summary.md`.

---

## BUG-M03: TransferValidator не перевіряє формат IBAN одержувача toIban і не повертає INVALID_TO_IBAN

**Severity:** Major  
**Priority:** P2  
**Status:** New  
**Review:** reviewed  
**Linked:** `TransferValidatorTest#validate_invalidToIban_returnsInvalidToIbanError` (`ІТ-2`); `BL-50`, `BL-40`  
**Reporter:** Бурлачка Артем, 04.11.2026  
**Reviewer:** Топчій Микита, 08.11.2026

### Description
Вимоги `BL-40` та `BL-50` (`SRS-1.1.md`), а також перелік кодів помилок `TransferError` визначають обов'язкову перевірку формату IBAN як для рахунку-джерела (`INVALID_FROM_IBAN`), так і для рахунку-одержувача (`INVALID_TO_IBAN`, шаблон `^UA[0-9]{27}$`). Фактично у методі `TransferValidator.validate` (`TransferValidator.java:32-37`) викликається лише `!isValidIban(fromIban)`, тоді як перевірка `!isValidIban(toIban)` повністю відсутня, через що переказ на недійсний, порожній або `null` IBAN одержувача не супроводжується кодом `TransferError.INVALID_TO_IBAN` і помилково вважається дозволеним (`isValid == true`). Severity Major: валідатор пропускає некоректні реквізити рахунку-одержувача без повернення передбаченої помилки `TransferError.INVALID_TO_IBAN`. Priority P2: не перешкоджає виконанню решти модульних і UI-тестів Спринту 4, підлягає виправленню у `v2` (Спринт 5).

### Steps to reproduce
1. Створити екземпляр `TransferValidator validator = new TransferValidator();` (модуль `finkit/v1`).
2. Задати валідні `fromIban = "UA213223130000026007233566001"`, `amount = new BigDecimal("100.00")`, `balance = new BigDecimal("5000.00")`, `todaySpent = BigDecimal.ZERO`.
3. Викликати `validator.validate(fromIban, toIban, amount, balance, todaySpent)` і `validator.isValid(...)` по черзі для `toIban`: `"UA123"`, `"PL213223130000026007233566001"`, `"ua213223130000026007233566001"`, `""`, `null`.

### Actual result
Для всіх 5 невалідних значень `toIban` метод `validate` повертає порожній список `[]` (не містить `TransferError.INVALID_TO_IBAN`), а `isValid(...)` повертає `true` (усі 5 викликів параметризованого тесту `ІТ-2` падають на `TransferValidatorTest.java:86`, перевірки в рядках `87` і `89`).

### Expected result
Для кожного невалідного значення `toIban` список помилок `validate(...)` містить `TransferError.INVALID_TO_IBAN`, а `isValid(...)` повертає `false`.

### Environment
JDK 25.0.2 (цільовий рівень компіляції JDK 17), Apache Maven 3.9.9, JUnit Jupiter 5.10.2, Maven Surefire 3.2.5, модуль `ua.kpi.tzyapz.finkit` v1 (`TransferValidator.java:32-37`), Windows 11, команда запуску: `mvn clean test`, 04.11.2026.

### Attachments
- `target/surefire-reports/ua.kpi.tzyapz.finkit.TransferValidatorTest.txt` - 5 падінь `validate_invalidToIban_returnsInvalidToIbanError` (`фактично: []`, `isValid == true`); зведення у `docs/sprint4/surefire-summary.md`.

---

## BUG-M04: TransferValidator блокує переказ при досягненні точної межі денного ліміту 50 000.00 грн через умову >= 0

**Severity:** Major  
**Priority:** P2  
**Status:** New  
**Review:** reviewed  
**Linked:** `TransferValidatorTest#validate_exactDailyLimit50000_isAllowed` (`ІТ-3`); `BL-40`  
**Reporter:** Бурлачка Артем, 04.11.2026  
**Reviewer:** Топчій Микита, 08.11.2026

### Description
Вимога `BL-40` (`SRS-1.1.md`) встановлює включний денний ліміт переказів клієнта: переказ дозволяється, якщо `todaySpent + amount <= 50 000` грн. Фактично у методі `TransferValidator.validate` (`TransferValidator.java:45`) записано умову `if (todaySpent.add(amount).compareTo(DAILY_LIMIT) >= 0)`, через що рівність `todaySpent + amount == 50 000.00` помилково класифікується як порушення `TransferError.DAILY_LIMIT_EXCEEDED`. Severity Major: коректні граничні операції, які сумарно дорівнюють точно `50 000.00` грн за добу, хибно відхиляються. Priority P2: не блокує виконання інших тестів у Спринті 4, підлягає виправленню у `v2` (`> 0` замість `>= 0`).

### Steps to reproduce
1. Створити екземпляр `TransferValidator validator = new TransferValidator();` (модуль `finkit/v1`).
2. Передати валідні `fromIban = "UA213223130000026007233566001"`, `toIban = "UA903052990000026004012345678"`.
3. Викликати `validator.validate(...)` для кейсу 1: `todaySpent = 49700.00`, `amount = 300.00`, `balance = 5000.00`; та кейсу 2: `todaySpent = 0.00`, `amount = 50000.00`, `balance = 50000.00`.

### Actual result
Обидва виклики повертають список `[DAILY_LIMIT_EXCEEDED]` замість порожнього списку, а `isValid(...)` повертає `false` (падіння тесту `ІТ-3` на `TransferValidatorTest.java:118`, перевірки в рядках `119`, `121`, `122`, `124`).

### Expected result
Для обох граничних кейсів (`49700.00 + 300.00 == 50000.00` та `0.00 + 50000.00 == 50000.00`) метод `validate(...)` повертає порожній список `[]`, а `isValid(...)` повертає `true`.

### Environment
JDK 25.0.2 (цільовий рівень компіляції JDK 17), Apache Maven 3.9.9, JUnit Jupiter 5.10.2, Maven Surefire 3.2.5, модуль `ua.kpi.tzyapz.finkit` v1 (`TransferValidator.java:45`), Windows 11, команда запуску: `mvn clean test`, 04.11.2026.

### Attachments
- `target/surefire-reports/ua.kpi.tzyapz.finkit.TransferValidatorTest.txt` - падіння `validate_exactDailyLimit50000_isAllowed` (`отримано: [DAILY_LIMIT_EXCEEDED]`); зведення у `docs/sprint4/surefire-summary.md`.

---

## BUG-M05: AccountLedger забороняє зняття повного залишку рахунку amount == balance через умову >= 0

**Severity:** Major  
**Priority:** P2  
**Status:** New  
**Review:** reviewed  
**Linked:** `AccountLedgerTest#withdraw_exactFullBalance_setsBalanceToZero` (`СМ-3`); `BL-70`, `BL-80`  
**Reporter:** Топчій Микита, 05.11.2026  
**Reviewer:** Бойко Олег, 07.11.2026

### Description
Вимоги `BL-70` і `BL-80` (`docs/sprint2/SRS-1.1.md`), а також Javadoc методу `AccountLedger.withdraw` визначають, що овердрафт виникає лише тоді, коли сума списання перевищує поточний баланс (`amount > balance`), а допустима нижня межа залишку рахунку після операції становить `0.00` грн. Фактично у методі `AccountLedger.withdraw` (`AccountLedger.java:39`) записано умову `if (value.compareTo(balance) >= 0)`, через що списання суми, яка точно дорівнює поточному балансу (`amount == balance`), хибно відхиляється винятком `IllegalStateException`. Через це клієнт не може повністю обнулити рахунок однією операцією списання. Severity Major: коректна гранична операція повного зняття коштів (`balance == 0.00`) повністю блокується винятком без можливості вивести залишок у нуль. Priority P2: не блокує виконання решти тестів Спринту 4, але потребує виправлення в `v2` (`> 0` замість `>= 0`).

### Steps to reproduce
1. Створити екземпляр `AccountLedger ledger = new AccountLedger();` (модуль `finkit/v1`).
2. Викликати `ledger.deposit(new BigDecimal("250.00"))`.
3. Викликати `ledger.withdraw(new BigDecimal("250.00"))`.

### Actual result
Метод `withdraw` кидає `java.lang.IllegalStateException: Овердрафт заборонено: недостатньо коштів` (`AccountLedger.withdraw(AccountLedger.java:40)`), баланс залишається `250.00`, а в історії є лише 1 запис (`DEPOSIT 250.00`).

### Expected result
За `BL-70` і `BL-80` зняття суми `250.00` за балансу `250.00` виконується без винятку, баланс стає `0.00` (шкала `2`), а в історії з'являється другий запис `LedgerEntry[type=WITHDRAWAL, amount=250.00, balanceAfter=0.00]`.

### Environment
JDK 25.0.2 (цільовий рівень компіляції JDK 17), Apache Maven 3.9.9, JUnit Jupiter 5.10.2, Maven Surefire 3.2.5, модуль `ua.kpi.tzyapz.finkit` v1 (`AccountLedger.java:39-40`), Windows 11, команда запуску: `mvn clean test`, 05.11.2026.

### Attachments
- `target/surefire-reports/ua.kpi.tzyapz.finkit.AccountLedgerTest.txt` - падіння `withdraw_exactFullBalance_setsBalanceToZero` (`Unexpected exception thrown: java.lang.IllegalStateException: Овердрафт заборонено: недостатньо коштів`); зведення у `docs/sprint4/surefire-summary.md`.

---

## BUG-M06: AccountLedger.history повертає історію операцій у зворотному порядку через Collections.reverse

**Severity:** Major  
**Priority:** P2  
**Status:** New  
**Review:** reviewed  
**Linked:** `AccountLedgerTest#history_multipleOperations_returnsChronologicalOrder` (`СМ-4`); `BL-70`, `BL-80`  
**Reporter:** Топчій Микита, 05.11.2026  
**Reviewer:** Бойко Олег, 07.11.2026

### Description
Вимоги `BL-70` і `BL-80` (`docs/sprint2/SRS-1.1.md`), а також Javadoc методу `AccountLedger.history()` (`AccountLedger.java:58`) гарантують повернення незмінюваного переліку записів "у порядку виконання операцій" (хронологічному: від найпершої операції до найостаннішої). Фактично у методі `AccountLedger.history()` (`AccountLedger.java:61-63`) перед обгортанням у `Collections.unmodifiableList(copy)` виконується зайвий виклик `Collections.reverse(copy)`, через що список операцій повертається у зворотному порядку (найновіша операція стоїть на індексі `0`). Severity Major: контракт методу `history()` порушує порядок фінансових транзакцій у виписці рахунку без обхідного шляху на рівні виклику API модуля. Priority P2: не перешкоджає запуску решти тестів Спринту 4, підлягає виправленню в `v2` (видалити `Collections.reverse(copy)`).

### Steps to reproduce
1. Створити екземпляр `AccountLedger ledger = new AccountLedger();` (модуль `finkit/v1`).
2. Послідовно виконати операції: `ledger.deposit(new BigDecimal("100.00"))`, `ledger.withdraw(new BigDecimal("30.00"))`, `ledger.deposit(new BigDecimal("50.00"))`.
3. Викликати `List<LedgerEntry> history = ledger.history();` і перевірити порядок елементів `history.get(0)`, `history.get(1)`, `history.get(2)`.

### Actual result
Метод повертає список у зворотному порядку: `history.get(0)` дорівнює `LedgerEntry[type=DEPOSIT, amount=50.00, balanceAfter=120.00]`, а `history.get(2)` дорівнює `LedgerEntry[type=DEPOSIT, amount=100.00, balanceAfter=100.00]` (тест `СМ-4` падає з `MultipleFailuresError (2 failures)`).

### Expected result
За `BL-70`, `BL-80` та Javadoc `AccountLedger.history()` перелік містить операції у прямому хронологічному порядку:
1. `LedgerEntry[type=DEPOSIT, amount=100.00, balanceAfter=100.00]`
2. `LedgerEntry[type=WITHDRAWAL, amount=30.00, balanceAfter=70.00]`
3. `LedgerEntry[type=DEPOSIT, amount=50.00, balanceAfter=120.00]`

### Environment
JDK 25.0.2 (цільовий рівень компіляції JDK 17), Apache Maven 3.9.9, JUnit Jupiter 5.10.2, Maven Surefire 3.2.5, модуль `ua.kpi.tzyapz.finkit` v1 (`AccountLedger.java:61-63`), Windows 11, команда запуску: `mvn clean test`, 05.11.2026.

### Attachments
- `target/surefire-reports/ua.kpi.tzyapz.finkit.AccountLedgerTest.txt` - падіння `history_multipleOperations_returnsChronologicalOrder` (`Multiple Failures (2 failures)`); зведення у `docs/sprint4/surefire-summary.md`.

---

## BUG-M07: TransferValidator.isValidIban відхиляє валідний IBAN із пробілами всупереч правилу BL-50

**Severity:** Normal  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** `TransferValidatorTest#isValidIban_validAndInvalidFormats_matchesBl50` (`КТ-3`); `BL-50`  
**Reporter:** Бойко Олег, 06.11.2026  
**Reviewer:** Топчій Микита, 07.11.2026

### Description
Вимога `BL-50` (`docs/sprint2/SRS-1.1.md`, розд. 5) і `docs/sprint2/test-plan.md` (розд. 5.3) визначають, що IBAN є дійсним, якщо після вилучення пробілів рядок має довжину 29 символів і відповідає шаблону `^UA[0-9]{27}$` ("UA" великими літерами та 27 цифр), при цьому в `test-plan.md` наведено еталонний тестовий вхід із пробілами `"UA21 3223 1300 0002 6007 2335 6600 1"`. Фактично у `TransferValidator.java` (рядки 17, 67-69) метод `isValidIban(String iban)` перевіряє `IBAN_UA.matcher(iban).matches()` без попереднього видалення пробілів, що відповідає звуженому Javadoc `v1`, але суперечить `SRS-1.1.md` (`BL-50`). Severity Normal: функція валідації працює для компактного запису без пробілів, але відхиляє коректний IBAN у стандартному групованому форматі з пробілами. Priority P3: існує простий обхідний шлях (передача IBAN без пробілів), проте поведінка потребує узгодження та виправлення у версії `v2` (Спринт 5).

### Steps to reproduce
1. Викликати статичний метод `TransferValidator.isValidIban("UA21 3223 1300 0002 6007 2335 6600 1")` у модулі `finkit/v1`.
2. Порівняти повернуте булеве значення з очікуванням за вимогою `BL-50` (`SRS-1.1.md`).

### Actual result
Метод `TransferValidator.isValidIban("UA21 3223 1300 0002 6007 2335 6600 1")` повертає `false` (падіння параметризованого тесту `КТ-3` [2]: `Невідповідність перевірки IBAN 'UA21 3223 1300 0002 6007 2335 6600 1' правилу BL-50 (UA + 27 цифр, пробіли вилучаються) ==> expected: <true> but was: <false>`).

### Expected result
За `BL-50` (`SRS-1.1.md`) перед перевіркою шаблону `^UA[0-9]{27}$` пробіли вилучаються, тому `TransferValidator.isValidIban("UA21 3223 1300 0002 6007 2335 6600 1")` повертає `true`.

### Environment
JDK 25.0.2 (цільовий рівень компіляції JDK 17), Apache Maven 3.9.9, JUnit Jupiter 5.10.2, Maven Surefire 3.2.5, модуль `ua.kpi.tzyapz.finkit` v1 (`TransferValidator.java:17,67-69`), Windows 11, команда запуску: `mvn clean test`, 06.11.2026.

### Attachments
- `target/surefire-reports/ua.kpi.tzyapz.finkit.TransferValidatorTest.txt` - зафіксовано падіння `КТ-3` (`isValidIban_validAndInvalidFormats_matchesBl50[2]`: `expected: <true> but was: <false>`); зведення у `docs/sprint4/surefire-summary.md`.

---

## BUG-M08: AccountLedger.withdraw при овердрафті кидає IllegalStateException замість IllegalArgumentException за BL-70

**Severity:** Normal  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** `AccountLedgerTest#withdraw_amountAboveBalance_throwsAndLeavesBalanceUnchanged` (`АО-3`); `BL-70`, `BL-80`  
**Reporter:** Сушко Ілля, 06.11.2026  
**Reviewer:** Топчій Микита, 07.11.2026

### Description
Вимога `BL-70` (`docs/sprint2/SRS-1.1.md`, розд. 5) і `docs/sprint2/test-plan.md` встановлюють, що виклик `AccountLedger.withdraw(amount)` при `amount > balance()` не змінює баланс і кидає `IllegalArgumentException` (овердрафт заборонено). Фактично у `AccountLedger.java` (Javadoc рядок 35 та реалізація рядок 40) при овердрафті кидається `IllegalStateException("Овердрафт заборонено: недостатньо коштів")`. При цьому сам захист від овердрафту спрацьовує (баланс і історія не змінюються), але контракт типу винятку розходиться з вимогою `BL-70`, через що споживачі модуля, які очікують `IllegalArgumentException` за `SRS-1.1`, отримують неперехоплений `IllegalStateException`. Severity Normal: заборона овердрафту виконується і дані рахунку не пошкоджуються, однак тип винятку не відповідає специфікації `BL-70`. Priority P3: наявний простий обхідний шлях на рівні викликача (перевірка за Javadoc `v1` або обробка `RuntimeException`), але розбіжність між `SRS-1.1` і реалізацією потребує узгодження у версії `v2` (Спринт 5).

### Steps to reproduce
1. Створити екземпляр `AccountLedger ledger = new AccountLedger();` (модуль `finkit/v1`).
2. Викликати `ledger.deposit(new BigDecimal("100.00"))`.
3. Викликати `ledger.withdraw(new BigDecimal("100.01"))` та перевірити тип кинутого винятку, `ledger.balance()` і `ledger.history().size()`.

### Actual result
Метод `withdraw` кидає `java.lang.IllegalStateException: Овердрафт заборонено: недостатньо коштів` (`AccountLedger.withdraw(AccountLedger.java:40)`), баланс залишається `100.00`, розмір історії дорівнює `1` (тест `АО-3` падає на `AccountLedgerTest.java:198` з повідомленням `Multiple Failures (1 failure): Unexpected exception type thrown, expected: <java.lang.IllegalArgumentException> but was: <java.lang.IllegalStateException>`).

### Expected result
За `BL-70` (`docs/sprint2/SRS-1.1.md`) виклик `withdraw(new BigDecimal("100.01"))` при балансі `100.00` кидає `IllegalArgumentException`, залишаючи `balance() == 100.00` та `history().size() == 1`.

### Environment
JDK 25.0.2 (цільовий рівень компіляції JDK 17), Apache Maven 3.9.9, JUnit Jupiter 5.10.2, Maven Surefire 3.2.5, модуль `ua.kpi.tzyapz.finkit` v1 (`AccountLedger.java:35,40`), Windows 11, команда запуску: `mvn clean test`, 06.11.2026.

### Attachments
- `target/surefire-reports/ua.kpi.tzyapz.finkit.AccountLedgerTest.txt` - зафіксовано падіння `АО-3` (`withdraw_amountAboveBalance_throwsAndLeavesBalanceUnchanged`: `expected: <java.lang.IllegalArgumentException> but was: <java.lang.IllegalStateException>`); зведення у `docs/sprint4/surefire-summary.md`.
