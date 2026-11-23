# Перелік та описи UI-сценаріїв - Спринт 4

**Проєкт:** "ФінКит" (П2, онлайн-банкінг для фізичних осіб на базі demo ParaBank)  
**Етап:** Спринт 4 (Автоматизація: Selenium WebDriver і Selenium IDE)  
**Дата:** 01.11.2026  
**Автор:** ТА Закусило Дмитрій  
**Рецензент:** КТ Бойко Олег

---

## 1. Підхід до відбору, локаторів, очікувань і тестових даних

Для UI-автоматизації Спринту 4 відібрано **10 основних стабільних сценаріїв** (із пулу 14 тест-кейсів Спринту 3 зі статусом `passed`, за винятком `TC-040`, який потребує DevTools-емуляції мережі 3G/2G) та **3 резервні сценарії** (`TC-029`, `TC-034`, `TC-036`). Усі локатори та фактичні тексти інтерфейсу ParaBank (`https://parabank.parasoft.com/parabank/`) верифіковано на живому стенді 01.11.2026 у headless Google Chrome (`157.0.8103.79`).

### 1.1 Правила тестових даних та адаптації до demo (`L-10`, `L-20`, `L-30`, `L-40`)
- **Ізольованість тестів (`L-20`):** кожен UI-тест самостійний і не покладається на дані інших тестів або попередніх запусків. Для реєстрації генерується унікальний логін із префіксом команди `fins4<tc>_<6_цифр>` (наприклад, `fins4tc001_482910`) і паролем `FinKit#2026`.
- **Синтетичні дані:** `Ivan Franchuk`, `10 Khreshchatyk St`, `Kyiv`, `Kyivska` (`State`), `01001` (`Zip Code`), `380501112233`, `1234567890` (`SSN` за `L-40`), пошта `ivan.franchuk@example.com` (лише `example.com`).
- **Адаптація мови та валюти (`L-10`, `L-30`):** інтерфейс ParaBank є англомовним; суми у `$` трактуються як грн у співвідношенні 1:1. Стартовий баланс нового облікового запису на живому demo ParaBank (станом на 01.11.2026 становить `$1200.00`, може змінюватися при скиданні стенду) зчитується тестами динамічно з таблиці `#accountTable` для перевірки відносних інваріантів (`Total == сума балансів`, списання `-100.00`, зарахування `+100.00`).

### 1.2 Критичні технічні особливості живого DOM ParaBank
Під час інспектування живого стенду встановлено такі особливості, обов'язкові для врахування в `@Tag("ui")` тестах і `.side`:
1. **Локатори:** використовуються лише `By.id`, `By.name`, `By.cssSelector`, `By.linkText`; абсолютний XPath заборонений.
2. **Очікування:** застосовується лише `WebDriverWait` (рекомендовано `Duration.ofSeconds(15)`) із `ExpectedConditions` та перевірками заповнення AJAX-елементів (`Select` і таблиця рахунків завантажуються асинхронно після відкриття сторінки). `Thread.sleep` та `implicitlyWait` заборонені.
3. **Кнопки форм із `type="button"`:** на формах `openaccount.htm` (`Open New Account`), `billpay.htm` (`Send Payment`) та `updateprofile.htm` (`Update Profile`) кнопки мають `type="button"`, а не `type="submit"`.
4. **Динамічний `id` поля телефону в `billpay.htm`:** поле `payee.phoneNumber` на живому стенді генерує випадковий UUID у `id`, тому всі поля `billpay.htm` локалізуються за `By.name(...)`.
5. **Поле суми в `transfer.htm`:** поле має `id="amount"`, але `name="input"`, тому локатор повинен використовувати `By.id("amount")`.
6. **Клієнтська валідація `billpay.htm` для `TC-018`:** при введенні нецифрового IBAN у поля рахунку форма ParaBank показує `validationModel-account-invalid` ("Please enter a valid number.") раніше за перевірку збігу. Для відтворення саме сценарію розбіжності (`GUI-60` / `TC-018`) у `billpay.htm` передаються незбіжні числові рахунки (`"21322313"` і `"21322314"`), що викликає `#validationModel-verifyAccount-mismatch` ("The account numbers do not match."), тоді як перевірка українського IBAN за `BL-50` виконується модульними тестами `TransferValidatorTest`.
7. **Асинхронне підвантаження `updateprofile.htm`:** значення полів профілю підставляються JS-запитом після відкриття сторінки, тому перед `clear()`/`sendKeys()` обов'язкове очікування `WebDriverWait`, поки `getAttribute("value")` поля `customer.address.street` не стане непорожнім.

---

## 2. Зведена таблиця відібраних UI-сценаріїв

| № | ID Спринту 3 | Вимога SRS | Пріоритет | Цільовий Java-тест (`ua.kpi.tzyapz.finkit.ui`) | Сторінка ParaBank | Статус верифікації на живому стенді |
| --- | --- | --- | :---: | --- | --- | --- |
| 1 | `TC-033` | `GUI-10` | High | `AuthAndPublicUiTest#customerLoginBlock_isDisplayedOnPublicPages` | `index.htm`, `register.htm`, `lookup.htm` | Підтверджено (passed) |
| 2 | `TC-001` | `F-10` | High | `AuthAndPublicUiTest#register_validElevenFields_logsInAutomatically` | `register.htm` | Підтверджено (passed) |
| 3 | `TC-003` | `GUI-30` | High | `AuthAndPublicUiTest#register_passwordMismatch_showsErrorAndBlocksLogin` | `register.htm`, `index.htm` | Підтверджено (passed) |
| 4 | `TC-004` | `F-20` | High | `AuthAndPublicUiTest#login_afterLogout_opensCabinetWithGreeting` | `index.htm`, `overview.htm` | Підтверджено (passed); також у `.side` |
| 5 | `TC-007` | `F-30` | Medium | `AuthAndPublicUiTest#lookup_mismatchedSsn_showsNotFoundError` | `lookup.htm` | Підтверджено (passed) |
| 6 | `TC-008` | `F-40` | High | `AccountsAndPaymentsUiTest#overview_displaysAccountsAndConsistentTotal` | `overview.htm` | Підтверджено (passed) |
| 7 | `TC-012` | `F-70` | High | `AccountsAndPaymentsUiTest#transferFunds_betweenOwnAccounts_updatesBalances` | `openaccount.htm`, `transfer.htm`, `overview.htm` | Підтверджено (passed); також у `.side` |
| 8 | `TC-018` | `GUI-60` | High | `AccountsAndPaymentsUiTest#billPay_mismatchedAccountNumbers_showsValidationError` | `billpay.htm`, `overview.htm` | Підтверджено (passed) |
| 9 | `TC-021` | `F-90` | Medium | `AccountsAndPaymentsUiTest#findTransactions_byAmount_returnsMatchingTransfer` | `findtrans.htm` | Підтверджено (passed) |
| 10 | `TC-022` | `F-100` | Medium | `AccountsAndPaymentsUiTest#updateProfile_persistsNewAddressAndPhone` | `updateprofile.htm` | Підтверджено (passed) |
| Р1 | `TC-029` | `F-130` | Medium | Резервний (`Contact Us` у кабінеті) | `contact.htm` | Підтверджено (passed) |
| Р2 | `TC-034` | `GUI-20` | High | Резервний (7 полів форми `lookup.htm`) | `lookup.htm` | Підтверджено (passed) |
| Р3 | `TC-036` | `GUI-45` | Medium | Резервний (елементи форми `openaccount.htm`) | `openaccount.htm` | Підтверджено (passed) |

---

## 3. Детальні специфікації 10 основних UI-сценаріїв для WebDriver (`@Tag("ui")`)

### 3.1 `TC-033` (`GUI-10`): Наявність блоку `Customer Login` на публічних сторінках
- **Цільовий метод:** `AuthAndPublicUiTest#customerLoginBlock_isDisplayedOnPublicPages`
- **Передумови:** Неавторизований браузерний сеанс (чисті cookies).
- **Тестові дані:** Публічні адреси `index.htm`, `register.htm`, `lookup.htm`.

| Крок | Дія | Локатор | Очікування (`WebDriverWait`) | Очікуваний результат |
| --- | --- | --- | --- | --- |
| 1 | Відкрити `index.htm`, `register.htm`, `lookup.htm` по черзі. | - | `visibilityOfElementLocated(By.id("loginPanel"))` | Сторінка завантажена без входу. |
| 2 | Перевірити поля введення логіна і пароля в лівій колонці. | `By.name("username")`, `By.name("password")` | `visibilityOfElementLocated` | Обидва поля видимі й активні на всіх 3 сторінках. |
| 3 | Перевірити кнопку входу та посилання відновлення/реєстрації. | `By.cssSelector("#loginPanel input[type='submit'][value='Log In']")`, `By.linkText("Forgot login info?")`, `By.linkText("Register")` | `elementToBeClickable` | Кнопка `Log In` і обидва посилання видимі на кожній із 3 сторінок. |

### 3.2 `TC-001` (`F-10`): Реєстрація нового клієнта з 11 полями та автовхід
- **Цільовий метод:** `AuthAndPublicUiTest#register_validElevenFields_logsInAutomatically`
- **Передумови:** Неавторизований сеанс на `register.htm`.
- **Тестові дані:** Логін `fins4tc001_<rnd>`, пароль `FinKit#2026`, `Ivan Franchuk`, `10 Khreshchatyk St`, `Kyiv`, `Kyivska`, `01001`, `380501112233`, `1234567890`.

| Крок | Дія | Локатор | Очікування (`WebDriverWait`) | Очікуваний результат |
| --- | --- | --- | --- | --- |
| 1 | Відкрити `register.htm`. | `By.id("customer.firstName")` | `visibilityOfElementLocated` | Форма реєстрації з 11 полями видима. |
| 2 | Заповнити всі 11 полів синтетичними даними. | `By.id("customer.firstName")`, `customer.lastName`, `customer.address.street`, `customer.address.city`, `customer.address.state`, `customer.address.zipCode`, `customer.phoneNumber`, `customer.ssn`, `customer.username`, `customer.password`, `repeatedPassword` | Усі поля доступні для введення | Поля заповнені, пароль і підтвердження збігаються. |
| 3 | Натиснути кнопку `Register`. | `By.cssSelector("input[type='submit'][value='Register']")` | `visibilityOfElementLocated(By.id("rightPanel"))` та `By.linkText("Log Out")` | У `#rightPanel` текст містить `"Your account was created successfully. You are now logged in."`; у `#leftPanel` є `"Welcome Ivan Franchuk"` і посилання `Log Out`. |
| 4 | Натиснути `Log Out`. | `By.linkText("Log Out")` | `visibilityOfElementLocated(By.name("username"))` | Сеанс завершено, відображається форма входу. |

### 3.3 `TC-003` (`GUI-30`): Негативна реєстрація при розбіжності пароля і підтвердження
- **Цільовий метод:** `AuthAndPublicUiTest#register_passwordMismatch_showsErrorAndBlocksLogin`
- **Передумови:** Неавторизований сеанс на `register.htm`.
- **Тестові дані:** Унікальний логін `fins4tc003_<rnd>`, `Password = FinKit#2026`, `Confirm = FinKit#2027`.

| Крок | Дія | Локатор | Очікування (`WebDriverWait`) | Очікуваний результат |
| --- | --- | --- | --- | --- |
| 1 | На `register.htm` заповнити перші 10 полів валідно, а в `repeatedPassword` ввести `FinKit#2027`. | `By.id("customer.*")`, `By.id("repeatedPassword")` | `visibilityOfElementLocated(By.id("customer.firstName"))` | Поля заповнені з різними паролями. |
| 2 | Натиснути `Register`. | `By.cssSelector("input[type='submit'][value='Register']")` | `visibilityOfElementLocated(By.id("repeatedPassword.errors"))` | Показано текст `"Passwords did not match."`, посилання `Log Out` відсутнє. |
| 3 | Спробувати увійти на `index.htm` з `fins4tc003_<rnd>` і `FinKit#2026`. | `By.name("username")`, `By.name("password")`, `By.cssSelector("input[type='submit'][value='Log In']")` | `visibilityOfElementLocated(By.cssSelector("#rightPanel p.error"))` | Вхід відхилено з помилкою; обліковий запис не створено. |

### 3.4 `TC-004` (`F-20`): Повторний вхід після виходу
- **Цільовий метод:** `AuthAndPublicUiTest#login_afterLogout_opensCabinetWithGreeting`
- **Передумови:** У межах тесту зареєстровано ізольованого користувача `fins4tc004_<rnd>` (`FinKit#2026`) та виконано `Log Out`.
- **Тестові дані:** Логін `fins4tc004_<rnd>`, пароль `FinKit#2026`.

| Крок | Дія | Локатор | Очікування (`WebDriverWait`) | Очікуваний результат |
| --- | --- | --- | --- | --- |
| 1 | Відкрити `index.htm`, ввести логін і пароль у блок `Customer Login`, натиснути `Log In`. | `By.name("username")`, `By.name("password")`, `By.cssSelector("input[type='submit'][value='Log In']")` | `visibilityOfElementLocated(By.id("accountTable"))` | Відкрито сторінку `overview.htm`. |
| 2 | Перевірити привітання та пункти меню в лівій колонці. | `By.cssSelector("#leftPanel p.smallText")`, `By.linkText("Accounts Overview")`, `By.linkText("Transfer Funds")`, `By.linkText("Log Out")` | `visibilityOfElementLocated` | Привітання `"Welcome Ivan Franchuk"`, пункти меню видимі. |
| 3 | Натиснути `Log Out` і повторно увійти тими ж обліковими даними. | `By.linkText("Log Out")`, форма входу | `visibilityOfElementLocated(By.id("accountTable"))` | Повторний вхід успішно відкриває кабінет. |

### 3.5 `TC-007` (`F-30`): Негативне відновлення доступу при незбіжному SSN
- **Цільовий метод:** `AuthAndPublicUiTest#lookup_mismatchedSsn_showsNotFoundError`
- **Передумови:** Зареєстровано клієнта `fins4tc007_<rnd>` із `SSN = 1234567890` та виконано `Log Out`.
- **Тестові дані:** Анкетні дані `Ivan Franchuk` (`10 Khreshchatyk St`, `Kyiv`, `Kyivska`, `01001`) та незбіжний `SSN = 9999999999`.

| Крок | Дія | Локатор | Очікування (`WebDriverWait`) | Очікуваний результат |
| --- | --- | --- | --- | --- |
| 1 | Відкрити `lookup.htm` без входу. | `By.id("firstName")` | `visibilityOfElementLocated` | Відображається форма `Customer Lookup` із 7 полями. |
| 2 | Заповнити 6 полів актуальними даними, у `ssn` ввести `9999999999`. | `By.id("firstName")`, `lastName`, `address.street`, `address.city`, `address.state`, `address.zipCode`, `ssn` | - | Усі 7 полів заповнені. |
| 3 | Натиснути `Find My Login Info`. | `By.cssSelector("input[type='submit'][value='Find My Login Info']")` | `visibilityOfElementLocated(By.cssSelector("#rightPanel p.error"))` | Текст помилки містить `"The customer information provided could not be found."`; логін і пароль не розкрито. |
| 4 | Увійти на `index.htm` з `fins4tc007_<rnd>` і `FinKit#2026`. | `By.name("username")`, `By.name("password")`, `Log In` | `visibilityOfElementLocated(By.id("accountTable"))` | Обліковий запис лишається чинним. |

### 3.6 `TC-008` (`F-40`): Огляд рахунків і узгодженість рядка `Total`
- **Цільовий метод:** `AccountsAndPaymentsUiTest#overview_displaysAccountsAndConsistentTotal`
- **Передумови:** Зареєстровано користувача `fins4tc008_<rnd>` і відкрито 2-й рахунок через `openaccount.htm` (щоб таблиця мала 2 рахунки).
- **Тестові дані:** Два активні рахунки одного клієнта.

| Крок | Дія | Локатор | Очікування (`WebDriverWait`) | Очікуваний результат |
| --- | --- | --- | --- | --- |
| 1 | Перейти на `Accounts Overview` (`overview.htm`). | `By.linkText("Accounts Overview")` | `visibilityOfElementLocated(By.cssSelector("#accountTable tbody tr td a"))` | Таблиця `#accountTable` завантажена і містить не менше 2 рядків рахунків. |
| 2 | Перевірити наявність посилання на деталі рахунку, балансу та доступної суми в кожному рядку рахунку. | `By.cssSelector("#accountTable tbody tr")`, `td:nth-child(1) a`, `td:nth-child(2)`, `td:nth-child(3)` | Усі рядки рахунків видимі | Кожен рахунок має непорожнє посилання-номер та суми у форматі `$X.XX`. |
| 3 | Порівняти суму балансів рядків рахунків із коміркою `Total`. | `By.cssSelector("#accountTable tbody tr:last-child td:nth-child(2)")` | Текст непорожній | Числове значення в рядку `Total` точно дорівнює сумі балансів усіх рахунків клієнта (`BigDecimal`). |

### 3.7 `TC-012` (`F-70`): Переказ коштів між власними рахунками
- **Цільовий метод:** `AccountsAndPaymentsUiTest#transferFunds_betweenOwnAccounts_updatesBalances`
- **Передумови:** Користувач `fins4tc012_<rnd>` увійшов у кабінет і відкрив 2-й рахунок (`SAVINGS`) на `openaccount.htm`; початкові баланси `B1_before`, `B2_before` та `Total_before` зчитані з `overview.htm`.
- **Тестові дані:** Сума переказу `100.00`, рахунок-джерело `acc1`, рахунок-одержувач `acc2`.

| Крок | Дія | Локатор | Очікування (`WebDriverWait`) | Очікуваний результат |
| --- | --- | --- | --- | --- |
| 1 | Перейти на `Transfer Funds` (`transfer.htm`). | `By.linkText("Transfer Funds")` | `Select(fromAccountId).getOptions().size() >= 2` | Форма переказу завантажена, у списках є обидва рахунки. |
| 2 | Ввести суму `100.00`, обрати `fromAccountId = acc1`, `toAccountId = acc2`. | `By.id("amount")`, `By.id("fromAccountId")`, `By.id("toAccountId")` | Елементи видимі | Поле суми та обидва списки встановлено коректно. |
| 3 | Натиснути `Transfer`. | `By.cssSelector("input[type='submit'][value='Transfer']")` | `visibilityOfElementLocated(By.id("showResult"))` | `#showResult` містить `"Transfer Complete!"`, `#amountResult` = `"$100.00"`, `#fromAccountIdResult` = `acc1`, `#toAccountIdResult` = `acc2`. |
| 4 | Повернутися на `Accounts Overview` і звірити баланси. | `By.linkText("Accounts Overview")`, `#accountTable` | `visibilityOfElementLocated(By.cssSelector("#accountTable tbody tr td a"))` | `B1_after == B1_before - 100.00`, `B2_after == B2_before + 100.00`, `Total_after == Total_before`. |

### 3.8 `TC-018` (`GUI-60`): Оплата рахунку при розбіжності номера рахунку і підтвердження
- **Цільовий метод:** `AccountsAndPaymentsUiTest#billPay_mismatchedAccountNumbers_showsValidationError`
- **Передумови:** Користувач `fins4tc018_<rnd>` увійшов у кабінет; початковий баланс `Total_before` зчитано на `overview.htm`.
- **Тестові дані:** `Payee = Ivan Petrenko`, адреса `10 Main St, Kyiv, Kyivska, 01001`, телефон `380501112233`, рахунок `21322313`, підтвердження `21322314`, сума `1.00`.

| Крок | Дія | Локатор | Очікування (`WebDriverWait`) | Очікуваний результат |
| --- | --- | --- | --- | --- |
| 1 | Перейти на `Bill Pay` (`billpay.htm`). | `By.linkText("Bill Pay")` | `visibilityOfElementLocated(By.name("payee.name"))` | Форма оплати рахунку відкрита. |
| 2 | Заповнити дані одержувача (усі через `By.name`) із незбіжними `payee.accountNumber = 21322313` і `verifyAccount = 21322314`, сумою `1.00`. | `By.name("payee.name")`, `payee.address.street`, `payee.address.city`, `payee.address.state`, `payee.address.zipCode`, `payee.phoneNumber`, `payee.accountNumber`, `verifyAccount`, `amount` | - | Поля заповнені без вибору іншого рахунку списання (залишається перший). |
| 3 | Натиснути `Send Payment`. | `By.cssSelector("input[type='button'][value='Send Payment']")` | `visibilityOfElementLocated(By.id("validationModel-verifyAccount-mismatch"))` | Показано `"The account numbers do not match."`, блок `#billpayResult` прихований. |
| 4 | Відкрити `Accounts Overview` і перевірити баланс. | `By.linkText("Accounts Overview")`, `#accountTable` | `visibilityOfElementLocated(By.cssSelector("#accountTable tbody tr td a"))` | `Total_after == Total_before` (списання не відбулося). |

### 3.9 `TC-021` (`F-90`): Пошук транзакцій за сумою
- **Цільовий метод:** `AccountsAndPaymentsUiTest#findTransactions_byAmount_returnsMatchingTransfer`
- **Передумови:** Користувач `fins4tc021_<rnd>` увійшов у кабінет, відкрив 2-й рахунок і виконав переказ на `100.00` із `acc1` на `acc2`.
- **Тестові дані:** Рахунок `acc1`, критерій пошуку `Amount = 100.00`.

| Крок | Дія | Локатор | Очікування (`WebDriverWait`) | Очікуваний результат |
| --- | --- | --- | --- | --- |
| 1 | Перейти на `Find Transactions` (`findtrans.htm`). | `By.linkText("Find Transactions")` | `Select(By.id("accountId")).getOptions().size() >= 2` | Форма пошуку і список рахунків завантажені. |
| 2 | Обрати `acc1`, ввести `100.00` у поле суми і натиснути кнопку пошуку за сумою. | `By.id("accountId")`, `By.id("amount")`, `By.id("findByAmount")` | `visibilityOfElementLocated(By.id("transactionTable"))` | Таблиця результатів `#transactionTable` видима. |
| 3 | Перевірити знайдені рядки транзакцій. | `By.cssSelector("#transactionTable tbody tr")` | Не менше 1 рядка | Таблиця містить рядок із описом `"Funds Transfer Sent"` і сумою `"$100.00"`. |

### 3.10 `TC-022` (`F-100`): Оновлення контактних даних клієнта
- **Цільовий метод:** `AccountsAndPaymentsUiTest#updateProfile_persistsNewAddressAndPhone`
- **Передумови:** Користувач `fins4tc022_<rnd>` увійшов у кабінет.
- **Тестові дані:** Нова адреса `15 Nova St`, новий телефон `380671112233`.

| Крок | Дія | Локатор | Очікування (`WebDriverWait`) | Очікуваний результат |
| --- | --- | --- | --- | --- |
| 1 | Відкрити `Update Contact Info` (`updateprofile.htm`). | `By.linkText("Update Contact Info")` | `getAttribute("value")` у `By.id("customer.address.street")` непорожній | Форма попередньо заповнена поточними даними профілю. |
| 2 | Очистити й ввести `15 Nova St` в адресу та `380671112233` у телефон. | `By.id("customer.address.street")`, `By.id("customer.phoneNumber")` | Поля доступні | Введено оновлені дані. |
| 3 | Натиснути `Update Profile`. | `By.cssSelector("input[type='button'][value='Update Profile']")` | `visibilityOfElementLocated(By.id("updateProfileResult"))` | `#updateProfileResult` містить `"Profile Updated"`. |
| 4 | Повторно відкрити `Update Contact Info` та перевірити збережені значення. | `By.linkText("Update Contact Info")`, `By.id("customer.address.street")`, `By.id("customer.phoneNumber")` | Значення `customer.address.street` дорівнює `"15 Nova St"` | Поле адреси містить `"15 Nova St"`, телефону - `"380671112233"`. |

---

## 4. Резервні UI-сценарії (верифіковано на живому ParaBank)

У разі необхідності розширення або заміни сценарію доступні 3 перевірені резервні кейси зі Спринту 3:
1. **`TC-029` (`F-130`, `contact.htm`):** Після входу відкрити `By.linkText("Contact Us")`, заповнити `By.id("name")` (`Ivan`), `By.id("email")` (`ivan.franchuk@example.com`), `By.id("phone")` (`380501112233`), `By.id("message")`, натиснути `By.cssSelector("input[type='submit'][value='Send to Customer Care']")`, дочекатися `#rightPanel p` -> `"Thank you Ivan"` і `"A Customer Care Representative will be contacting you."`.
2. **`TC-034` (`GUI-20`, `lookup.htm`):** Без входу відкрити `lookup.htm`, перевірити видимість 7 полів (`firstName`, `lastName`, `address.street`, `address.city`, `address.state`, `address.zipCode`, `ssn`) і кнопки `input[type='submit'][value='Find My Login Info']`.
3. **`TC-036` (`GUI-45`, `openaccount.htm`):** Після входу відкрити `By.linkText("Open New Account")`, дочекатися заповнення `By.id("fromAccountId")`, перевірити наявність опцій `"CHECKING"` і `"SAVINGS"` у `By.id("type")` та активність кнопки `By.cssSelector("input[type='button'][value='Open New Account']")`.

---

## 5. Специфікація 2 сценаріїв для Selenium IDE (`selenium-ide/finkit.side` та `export/`)

Для фіксації у форматі Selenium IDE (та подальшого експорту в Java-чернетки в `selenium-ide/export/`) визначено 2 ключові наскрізні сценарії (`TC-004` і `TC-012`), кожен із яких включає навігацію, введення даних, очікування `waitForElementVisible`, жорстку перевірку `assertText` і м'яку перевірку `verify...`. Для автономності при відтворенні кожен тест починається з реєстрації свіжого користувача або входу в межах сеансу (без залежності від скидання бази ParaBank за `L-20`).

### 5.1 Сценарій IDE-1: `TC-004` (`F-20`) - Реєстрація/вихід, повторний вхід і перевірка привітання

| Крок | Команда IDE | Target | Value | Призначення |
| --- | --- | --- | --- | --- |
| 1 | `open` | `/parabank/register.htm` | - | Відкриття сторінки реєстрації для створення ізольованого профілю |
| 2 | `executeScript` | `return 'fins4ide04_' + Math.floor(Math.random()*900000+100000)` | `uname` | Генерація унікального логіна `L-20` |
| 3 | `waitForElementVisible` | `id=customer.firstName` | `10000` | Очікування форми реєстрації |
| 4-14 | `type` | `id=customer.firstName` ... `id=repeatedPassword` | `Ivan` ... `${uname}` / `FinKit#2026` | Заповнення 11 полів реєстрації |
| 15 | `click` | `css=input[type='submit'][value='Register']` | - | Створення облікового запису |
| 16 | `waitForElementVisible` | `linkText=Log Out` | `10000` | Очікування завершення автовходу |
| 17 | `click` | `linkText=Log Out` | - | Вихід для перевірки повторного входу |
| 18 | `waitForElementVisible` | `name=username` | `10000` | Очікування форми `Customer Login` |
| 19 | `type` | `name=username` | `${uname}` | Введення логіна |
| 20 | `type` | `name=password` | `FinKit#2026` | Введення пароля |
| 21 | `click` | `css=input[type='submit'][value='Log In']` | - | Виконання повторного входу |
| 22 | `waitForElementVisible` | `id=accountTable` | `10000` | Очікування завантаження кабінету |
| 23 | `assertText` | `css=#leftPanel p.smallText` | `Welcome Ivan Franchuk` | Жорстка перевірка привітання (`F-20`, `GUI-40`) |
| 24 | `verifyElementPresent` | `linkText=Log Out` | - | М'яка перевірка наявності виходу в меню |
| 25 | `click` | `linkText=Log Out` | - | Завершення сеансу |

### 5.2 Сценарій IDE-2: `TC-012` (`F-70`) - Відкриття 2-го рахунку та переказ 100.00 між власними рахунками

| Крок | Команда IDE | Target | Value | Призначення |
| --- | --- | --- | --- | --- |
| 1 | `open` | `/parabank/register.htm` | - | Підготовка ізольованого профілю `L-20` |
| 2 | `executeScript` | `return 'fins4ide12_' + Math.floor(Math.random()*900000+100000)` | `uname12` | Генерація унікального логіна |
| 3-15 | `type` / `click` | Поля реєстрації (`id=customer.*`, `id=repeatedPassword`) і `Register` | `FinKit#2026` | Реєстрація й автовхід |
| 16 | `waitForElementVisible` | `linkText=Open New Account` | `10000` | Очікування меню кабінету |
| 17 | `click` | `linkText=Open New Account` | - | Перехід до відкриття 2-го рахунку |
| 18 | `waitForElementVisible` | `css=#fromAccountId option` | `10000` | Очікування AJAX-завантаження списку рахунків |
| 19 | `select` | `id=type` | `label=SAVINGS` | Вибір типу ощадного рахунку |
| 20 | `click` | `css=input[type='button'][value='Open New Account']` | - | Відкриття 2-го рахунку |
| 21 | `waitForElementVisible` | `id=newAccountId` | `10000` | Очікування номера нового рахунку |
| 22 | `storeText` | `id=newAccountId` | `acc2` | Збереження номера 2-го рахунку |
| 23 | `click` | `linkText=Transfer Funds` | - | Перехід до форми переказу |
| 24 | `waitForElementVisible` | `css=#toAccountId option:nth-child(2)` | `10000` | Очікування 2-го рахунку у випадаючих списках |
| 25 | `type` | `id=amount` | `100.00` | Введення суми переказу `100.00` |
| 26 | `select` | `id=toAccountId` | `label=${acc2}` | Вибір рахунку-одержувача |
| 27 | `click` | `css=input[type='submit'][value='Transfer']` | - | Відправлення переказу |
| 28 | `waitForElementVisible` | `id=showResult` | `10000` | Очікування підтвердження переказу |
| 29 | `assertText` | `css=#showResult h1.title` | `Transfer Complete!` | Жорстка перевірка успішного заголовка (`F-70`) |
| 30 | `verifyText` | `id=amountResult` | `$100.00` | М'яка перевірка відображеної суми (`L-10`) |
