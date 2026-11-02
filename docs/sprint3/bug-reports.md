# Реєстр дефектів (Defect Register) - Спринт 3

**Проєкт:** "ФінКит" (П2, онлайн-банкінг на базі demo ParaBank)  
**Етап:** Спринт 3 (тестування інтерфейсу та функціональності на demo-стенді)  
**Дата:** 29.10.2026  
**Автор:** ІТ Топчій Микита  
**Рецензент:** СМ Сушко Ілля  

---

Очікування: SRS-1.1.

## Зведення

| Bug ID | Title | Severity | Priority | Status | Review | TC | Вимога |
| --- | --- | --- | --- | --- | --- | --- | --- |
| BUG-001 | Відновлення доступу показує чинний пароль клієнта відкритим текстом | Critical | P3 | New | reviewed | TC-006 | F-30, S-20 |
| BUG-002 | Відкриття рахунку списує 90.00 замість початкового внеску 100.00 | Major | P3 | New | reviewed | TC-010 | F-60 |
| BUG-003 | Переказ 100 000.00 виконується при балансі джерела 160.00 | Critical | P3 | New | reviewed | TC-015 | F-70, BL-70 |
| BUG-004 | Переказ на той самий рахунок виконується і показує Transfer Complete | Major | P3 | New | reviewed | TC-013 | F-70, BL-40 |
| BUG-005 | Переказ з нульовою або від'ємною сумою виконується | Major | P3 | New | reviewed | TC-014 | F-70, GUI-50, BL-40 |
| BUG-006 | Форма оплати рахунку не приймає IBAN одержувача | Major | P3 | New | reviewed | TC-016 | F-80, GUI-60, BL-50 |
| BUG-007 | Виписка рахунку не містить ідентифікатора операції та балансу після операції | Normal | P3 | New | reviewed | TC-009 | F-50 |
| BUG-008 | Заявка на кредит 999.00 схвалюється, хоча мінімальна сума 1000.00 | Major | P3 | New | reviewed | TC-025 | F-110 |
| BUG-009 | Заявка на кредит 1 000 001.00 обробляється замість помилки форми | Major | P3 | New | reviewed | TC-026 | F-110 |
| BUG-010 | Перший внесок 91 відсоток обробляється замість помилки форми | Normal | P3 | New | reviewed | TC-027 | F-110 |
| BUG-011 | Після виходу прямий перехід у кабінет показує внутрішню помилку замість сторінки входу | Normal | P3 | New | reviewed | TC-028 | F-120, S-20 |
| BUG-012 | Публічна форма підтримки показує подяку замість повідомлення Звернення отримано | Normal | P3 | New | reviewed | TC-030 | F-130 |
| BUG-013 | Поріг кредитного рішення 20 відсотків замість 30 | Major | P3 | New | reviewed | TC-032 | F-160, C-50 |
| BUG-014 | Меню кабінету не містить пункту Служба підтримки | Normal | P3 | New | reviewed | TC-035 | GUI-40 |
| BUG-015 | Шрифт і цільові елементи інтерфейсу менші за мінімуми U-10 | Minor | P3 | New | reviewed | TC-039 | U-10 |
| BUG-016 | Пошук операцій відхиляє дату у форматі ДД.ММ.РРРР | Normal | P3 | New | reviewed | TC-020 | F-90 |
| BUG-019 | Неправильний пароль або невідомий логін відкриває кабінет John Smith | Critical | P3 | New | reviewed | TC-005 | F-20, S-20 |
| BUG-020 | Поле Phone # не є обов'язковим у формі реєстрації | Normal | P3 | New | reviewed | TC-002 | GUI-30, U-20 |
| BUG-021 | Спосіб опрацювання заявки Available Funds замість автоматичного за порогом | Normal | P3 | New | reviewed | TC-032 | F-160, C-50 |
| BUG-022 | Сеанс клієнта не завершується після 15 хвилин бездіяльності | Major | P3 | New | reviewed | TC-038 | S-10, GUI-80 |
| BUG-023 | Відкриття рахунку при балансі 99.99 не відхиляється | Major | P3 | New | reviewed | TC-011 | F-60 |
| BUG-024 | Повідомлення про недійсний IBAN вказує на числовий формат | Normal | P3 | New | reviewed | TC-017 | F-80, GUI-60 |
| BUG-025 | Результат пошуку за ID не показує ідентифікатор операції | Normal | P3 | New | reviewed | TC-019 | F-90, F-50 |

## Повні звіти


## BUG-001: Відновлення доступу показує чинний пароль клієнта відкритим текстом

**Severity:** Critical  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-006; F-30, S-20  
**Reporter:** Топчій Микита, 28.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-30 дозволяє показати логін і задати новий пароль, але забороняє показувати чинний пароль. S-20 прямо вимагає не розкривати пароль клієнта. Фактично lookup показує рядок Password: FinKit#2026 і автоматично входить у кабінет. Розкриття пароля дає змогу захопити обліковий запис за відомих анкетних даних. Severity Critical: розкрито секрет автентифікації. Priority P3: цей дефект не блокує виконання жодного тест-кейса (усі 40 кейсів виконано), тому за визначенням 4.13 це P3. Пропозиція розробнику: виправити раніше за дефекти з нижчою severity.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта з даними Ivan Franchuk, 10 Khreshchatyk St, Kyiv, Kyivska, 01001, 380501112233, SSN 1234567890, логін fins3bug001, пароль FinKit#2026.
3. Натиснути Log Out.
4. Відкрити https://parabank.parasoft.com/parabank/lookup.htm.
5. Заповнити First Name, Last Name, Address, City, State, Zip Code, SSN тими самими значеннями.
6. Натиснути Find My Login Info.
7. Прочитати результат на сторінці.

### Actual result
Показано повідомлення Your login information was located successfully. You are now logged in. Нижче відображено Username: fins3bug001 і Password: FinKit#2026 відкритим текстом. Клієнт автоматично увійшов у кабінет.

### Expected result
За F-30 показано логін клієнта і можливість задати новий пароль; чинний пароль не відображається. За S-20 пароль клієнта не розкривається без успішної автентифікації.

### Environment
demo https://parabank.parasoft.com/parabank, 28.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug001.

### Attachments
- BUG-001-1.png - сторінка lookup.htm з відкритим паролем після кроку 6

## BUG-002: Відкриття рахунку списує 90.00 замість початкового внеску 100.00

**Severity:** Major  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-010; F-60  
**Reporter:** Топчій Микита, 28.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-60 вимагає початковий внесок 100.00 грн. Фактично новий ощадний рахунок відкривається з балансом 90.00, а з рахунку-джерела списується 90.00. Причина - Min. Balance 90 в адмінпанелі. Дефект впливає на кожне відкриття рахунку і на баланси клієнта. Severity Major: грошова операція виконується на неправильну суму. Priority P3: цей дефект не блокує виконання жодного тест-кейса (усі 40 кейсів виконано), тому за визначенням 4.13 це P3.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug002 з паролем FinKit#2026.
3. Відкрити пункт меню Open New Account.
4. У списку Type обрати SAVINGS.
5. У списку From Account обрати поточний рахунок клієнта.
6. Натиснути Open New Account.
7. Відкрити Accounts Overview і порівняти баланси до і після.

### Actual result
До відкриття поточний рахунок мав 250.00. Після відкриття поточний рахунок має 160.00, новий ощадний рахунок має 90.00. Показано Account Opened і номер нового рахунку.

### Expected result
За F-60 з поточного рахунку списується 100.00, новий ощадний рахунок отримує 100.00; баланс джерела 150.00, нового рахунку 100.00.

### Environment
demo https://parabank.parasoft.com/parabank, 28.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug002.

### Attachments
- BUG-002-1.png - баланси рахунків після відкриття: 160.00 і 90.00

## BUG-003: Переказ 100 000.00 виконується при балансі джерела 160.00

**Severity:** Critical  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-015; F-70, BL-70  
**Reporter:** Топчій Микита, 28.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-70 і BL-70 забороняють переказ, більший за доступну суму, і забороняють овердрафт. Фактично переказ 100 000.00 з рахунку з балансом 160.00 показує Transfer Complete. Це створює від'ємний баланс і руйнує цілісність даних. Severity Critical: втрата контролю над грошовими сумами. Priority P3: цей дефект не блокує виконання жодного тест-кейса (усі 40 кейсів виконано), тому за визначенням 4.13 це P3. Пропозиція розробнику: виправити раніше за дефекти з нижчою severity.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug003 з паролем FinKit#2026.
3. Відкрити ощадний рахунок, щоб у клієнта було два рахунки.
4. Відкрити пункт меню Transfer Funds.
5. У поле Amount ввести 100000.00.
6. Обрати рахунок-джерело з балансом 160.00 і рахунок-одержувач.
7. Натиснути Transfer.
8. Відкрити Accounts Overview.

### Actual result
Показано Transfer Complete. 100000.00 has been transferred from account #... to account #.... Баланс рахунку-джерела після операції від'ємний.

### Expected result
За F-70 показано Недостатньо коштів на рахунку; переказ не виконано; баланс рахунку-джерела не змінився.

### Environment
demo https://parabank.parasoft.com/parabank, 28.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug003.

### Attachments
- BUG-003-1.png - переказ 100 000.00 при балансі джерела 160.00

## BUG-004: Переказ на той самий рахунок виконується і показує Transfer Complete

**Severity:** Major  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-013; F-70, BL-40  
**Reporter:** Топчій Микита, 28.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-70 і BL-40 вимагають відхиляти переказ, якщо рахунок-джерело збігається з рахунком-одержувачем. Фактично переказ 5.00 з рахунку на той самий рахунок показує Transfer Complete і створює дві протилежні операції. Severity Major: бізнес-правило не працює, історія операцій спотворюється. Priority P3: цей дефект не блокує виконання жодного тест-кейса (усі 40 кейсів виконано), тому за визначенням 4.13 це P3.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug004 з паролем FinKit#2026.
3. Відкрити ощадний рахунок, щоб у клієнта було два рахунки.
4. Відкрити пункт меню Transfer Funds.
5. У поле Amount ввести 5.00.
6. У списку From Account і в списку To Account обрати той самий рахунок.
7. Натиснути Transfer.

### Actual result
Показано Transfer Complete. 5.00 has been transferred from account #... to account #... з однаковими номерами.

### Expected result
За F-70 показано Рахунок-джерело і рахунок-одержувач мають відрізнятися; переказ не виконано; баланси не змінилися.

### Environment
demo https://parabank.parasoft.com/parabank, 28.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug004.

### Attachments
- BUG-004-1.png - переказ на той самий рахунок і повідомлення Transfer Complete

## BUG-005: Переказ з нульовою або від'ємною сумою виконується

**Severity:** Major  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-014; F-70, GUI-50, BL-40  
**Reporter:** Топчій Микита, 28.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-70, GUI-50 і BL-40 вимагають додатної суми. Фактично переказ 0.00 показує Transfer Complete, а переказ -1.00 показує Transfer Complete з сумою -1.00. Це дозволяє створювати некоректні операції та обходити контроль сум. Severity Major: валідація ключового поля не працює. Priority P3: цей дефект не блокує виконання жодного тест-кейса (усі 40 кейсів виконано), тому за визначенням 4.13 це P3.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug005 з паролем FinKit#2026.
3. Відкрити ощадний рахунок, щоб у клієнта було два рахунки.
4. Відкрити пункт меню Transfer Funds.
5. У поле Amount ввести 0.00 і натиснути Transfer.
6. Повторити переказ із сумою -1.00.

### Actual result
Для 0.00 показано Transfer Complete і 0.00 has been transferred. Для -1.00 показано Transfer Complete і -1.00 has been transferred. Повідомлення про додатну суму відсутнє.

### Expected result
За F-70 і GUI-50 показано Сума має бути додатною; переказ не виконано; баланси не змінилися.

### Environment
demo https://parabank.parasoft.com/parabank, 28.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug005.

### Attachments
- BUG-005-1.png - переказ із нульовою сумою і повідомлення про успіх

## BUG-006: Форма оплати рахунку не приймає IBAN одержувача

**Severity:** Major  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-016; F-80, GUI-60, BL-50  
**Reporter:** Топчій Микита, 28.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-80 і GUI-60 вимагають приймати IBAN рахунку-одержувача у форматі UA і 27 цифр, перевіряти його за BL-50 і локальним реєстром BL-60. Фактично поле Account Number приймає лише число: IBAN UA213223130000026007233566001 відхиляється повідомленням Please enter a valid number. Користувач не може виконати оплату за вимогою. Severity Major: ключова функція оплати не працює за специфікацією. Priority P3: цей дефект не блокує виконання жодного тест-кейса (усі 40 кейсів виконано), тому за визначенням 4.13 це P3.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug006 з паролем FinKit#2026.
3. Відкрити пункт меню Bill Pay.
4. Заповнити Payee Name, Address, City, State, Zip Code, Phone.
5. У поле Account Number ввести UA213223130000026007233566001.
6. У поле Verify Account Number ввести UA213223130000026007233566001.
7. У поле Amount ввести 1.00 і натиснути Send Payment.

### Actual result
Поля Account Number і Verify Account Number показують Please enter a valid number. Платіж не відправляється, форма залишається відкритою.

### Expected result
За F-80 і GUI-60 IBAN приймається, перевіряється за BL-50 і BL-60; для валідного IBAN платіж виконується і показує Платіж на користь одержувача на суму 1.00 виконано.

### Environment
demo https://parabank.parasoft.com/parabank, 28.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug006.

### Attachments
- BUG-006-1.png - форма оплати рахунку з IBAN одержувача

## BUG-007: Виписка рахунку не містить ідентифікатора операції та балансу після операції

**Severity:** Normal  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-009; F-50  
**Reporter:** Топчій Микита, 28.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-50 вимагає поля дата, ідентифікатор операції, опис, сума списання, сума зарахування, баланс після операції. Фактично таблиця містить лише Date, Transaction, Debit, Credit. Немає ID операції та балансу після операції, тому клієнт не може зіставити виписку з іншими документами. Severity Normal: функція працює частково, але втрачає обов'язкові дані. Priority P3: не блокує інші кейси.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug007 з паролем FinKit#2026.
3. Відкрити ощадний рахунок і виконати переказ 100.00.
4. Відкрити Accounts Overview і вибрати номер поточного рахунку.
5. Переглянути таблицю Account Activity.

### Actual result
Таблиця має колонки Date, Transaction, Debit (-), Credit (+). Рядки містять дату, опис і суму. Колонок Transaction ID і Balance after operation немає.

### Expected result
За F-50 таблиця містить дату, ідентифікатор операції, опис, суму списання, суму зарахування і баланс після операції.

### Environment
demo https://parabank.parasoft.com/parabank, 28.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug007.

### Attachments
- BUG-007-1.png - виписка без ідентифікатора операції та балансу

## BUG-008: Заявка на кредит 999.00 схвалюється, хоча мінімальна сума 1000.00

**Severity:** Major  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-025; F-110  
**Reporter:** Бурлачка Артем, 29.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-110 встановлює мінімальну суму кредиту 1000.00 грн включно. Фактично заявка на 999.00 з першим внеском 0.00 показує Status: Approved, створює новий кредитний рахунок і нараховує кошти. Межа не перевіряється, тому клієнт отримує кредит поза лімітами банку. Severity Major: фінансове правило порушено. Priority P3: цей дефект не блокує виконання жодного тест-кейса (усі 40 кейсів виконано), тому за визначенням 4.13 це P3.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug008 з паролем FinKit#2026.
3. Відкрити пункт меню Request Loan.
4. У поле Loan Amount ввести 999.00.
5. У поле Down Payment ввести 0.00.
6. Обрати рахунок зі списку From Account.
7. Натиснути Apply Now.

### Actual result
Показано Loan Request Processed, Status: Approved, Congratulations, your loan has been approved, Your new account number: .... Новий кредитний рахунок створено.

### Expected result
За F-110 сума 999.00 є помилкою введення, форма не подається, кредитний рахунок не створюється.

### Environment
demo https://parabank.parasoft.com/parabank, 29.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug008.

### Attachments
- BUG-008-1.png - схвалена заявка на кредит 999.00

## BUG-009: Заявка на кредит 1 000 001.00 обробляється замість помилки форми

**Severity:** Major  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-026; F-110  
**Reporter:** Бурлачка Артем, 29.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-110 обмежує суму кредиту 1 000 000.00 включно. Фактично заявка на 1 000 001.00 обробляється: показує Loan Request Processed і Status: Denied із причиною про доступні кошти. Замість помилки введення клієнт отримує кредитне рішення за недопустимою сумою. Severity Major: межа не перевіряється, рішення ухвалюється за невалідними даними. Priority P3: цей дефект не блокує виконання жодного тест-кейса (усі 40 кейсів виконано), тому за визначенням 4.13 це P3.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug009 з паролем FinKit#2026.
3. Відкрити пункт меню Request Loan.
4. У поле Loan Amount ввести 1000001.00.
5. У поле Down Payment ввести 0.00.
6. Обрати рахунок зі списку From Account.
7. Натиснути Apply Now.

### Actual result
Показано Loan Request Processed, Status: Denied, We cannot grant a loan in that amount with your available funds. Заявка оброблена, хоча сума перевищує верхню межу.

### Expected result
За F-110 сума 1 000 001.00 є помилкою введення, форма не подається, кредитне рішення не ухвалюється.

### Environment
demo https://parabank.parasoft.com/parabank, 29.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug009.

### Attachments
- BUG-009-1.png - заявка на кредит 1 000 001.00 без помилки форми

## BUG-010: Перший внесок 91 відсоток обробляється замість помилки форми

**Severity:** Normal  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-027; F-110  
**Reporter:** Бурлачка Артем, 29.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-110 обмежує перший внесок 90 відсотками суми кредиту. Фактично заявка 1000.00 з внеском 910.00 (91 відсоток) обробляється і отримує Status: Denied із причиною про недостатній перший внесок. Замість помилки введення клієнт отримує кредитне рішення за недопустимим внеском. Severity Normal: сума не видається, але межа не перевіряється. Priority P3: не блокує інші кейси.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug010 з паролем FinKit#2026.
3. Відкрити пункт меню Request Loan.
4. У поле Loan Amount ввести 1000.00.
5. У поле Down Payment ввести 910.00.
6. Обрати рахунок зі списку From Account.
7. Натиснути Apply Now.

### Actual result
Показано Loan Request Processed, Status: Denied, You do not have sufficient funds for the given down payment. Заявка оброблена, хоча внесок 91 відсоток перевищує межу 90 відсотків.

### Expected result
За F-110 внесок 910.00 є помилкою введення, форма не подається, кредитне рішення не ухвалюється.

### Environment
demo https://parabank.parasoft.com/parabank, 29.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug010.

### Attachments
- BUG-010-1.png - перший внесок 91 відсоток

## BUG-011: Після виходу прямий перехід у кабінет показує внутрішню помилку замість сторінки входу

**Severity:** Normal  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-028; F-120, S-20  
**Reporter:** Бурлачка Артем, 29.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-120 вимагає після виходу повертати на головну сторінку з блоком входу і робити сторінки кабінету недоступними. Фактично після Log Out прямий перехід на overview.htm або transfer.htm показує блок Customer Login і повідомлення Error! An internal error has occurred and has been logged. Сторінка кабінету не відкривається, але замість нормальної сторінки входу відображається технічна помилка. Severity Normal: доступу до даних немає, але обробка стану некоректна. Priority P3: не блокує інші кейси.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug011 з паролем FinKit#2026.
3. Натиснути Log Out.
4. У адресному рядку відкрити https://parabank.parasoft.com/parabank/overview.htm.
5. Повторити для https://parabank.parasoft.com/parabank/transfer.htm.

### Actual result
На обох сторінках показано блок Customer Login і повідомлення Error! An internal error has occurred and has been logged. Сторінка кабінету не відкривається, але замість звичайної сторінки входу відображається технічна помилка.

### Expected result
За F-120 після виходу відкривається головна сторінка з блоком Customer Login; сторінки кабінету недоступні без повторного входу і не показують внутрішню помилку.

### Environment
demo https://parabank.parasoft.com/parabank, 29.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug011.

### Attachments
- BUG-011-1.png - внутрішня помилка після виходу з кабінету

## BUG-012: Публічна форма підтримки показує подяку замість повідомлення Звернення отримано

**Severity:** Normal  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-030; F-130  
**Reporter:** Бурлачка Артем, 29.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-130 вимагає після надсилання публічної форми показати Звернення отримано. Фактично показано Thank you Ivan Franchuk і A Customer Care Representative will be contacting you. Це текст F-130 для форми в кабінеті, а не F-130 для публічної форми. Користувач отримує підтвердження, але не те, що визначено вимогою. Severity Normal: звернення приймається, але поведінка не відповідає SRS. Priority P3: не блокує інші кейси.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm без входу.
2. Відкрити сторінку Contact.
3. У поле Name ввести Ivan Franchuk.
4. У поле Email ввести ivan.franchuk@example.com.
5. У поле Phone ввести 380501112233.
6. У поле Message ввести Public question.
7. Натиснути Send to Customer Care.

### Actual result
Показано Thank you Ivan Franchuk і A Customer Care Representative will be contacting you. Повідомлення Звернення отримано відсутнє.

### Expected result
За F-130 після надсилання показано Звернення отримано.

### Environment
demo https://parabank.parasoft.com/parabank, 29.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; без входу.

### Attachments
- BUG-012-1.png - повідомлення подяки у формі підтримки

## BUG-013: Поріг кредитного рішення 20 відсотків замість 30

**Severity:** Major  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-032; F-160, C-50  
**Reporter:** Бурлачка Артем, 29.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
C-50 визначає значення за замовчуванням: поріг схвалення 30 відсотків. Фактично адмінпанель показує Threshold 20. Поріг не відповідає SRS, тому автоматичне кредитне рішення за правилом 30 відсотків не гарантоване. Severity Major: кредитні рішення ухвалюються за неправильним порогом. Priority P3: цей дефект не блокує виконання жодного тест-кейса (усі 40 кейсів виконано), тому за визначенням 4.13 це P3.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/admin.htm.
2. Переглянути блок Application Settings.
3. Зафіксувати значення Loan Processor і Threshold.

### Actual result
Threshold: 20. Значення не відповідає C-50.

### Expected result
За C-50 і F-160 Threshold 30 відсотків.

### Environment
demo https://parabank.parasoft.com/parabank, 29.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; адмінпанель, читання без змін.

### Attachments
- BUG-013-1.png - поріг кредитного рішення 20 відсотків

## BUG-014: Меню кабінету не містить пункту Служба підтримки

**Severity:** Normal  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-035; GUI-40  
**Reporter:** Бурлачка Артем, 29.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
GUI-40 вимагає меню з 9 пунктів, серед них Служба підтримки між Заявкою на кредит і Вихід. Фактично меню містить 8 пунктів: Open New Account, Accounts Overview, Transfer Funds, Bill Pay, Find Transactions, Update Contact Info, Request Loan, Log Out. Пункту служби підтримки немає, тому F-130 недоступна з меню. Severity Normal: сторінка contact.htm доступна через верхнє меню, але навігація не відповідає SRS. Priority P3: не блокує інші кейси.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug014 з паролем FinKit#2026.
3. Переглянути ліве меню кабінету.
4. Порахувати пункти і перевірити наявність Служби підтримки.

### Actual result
Меню містить 8 пунктів, пункту Служба підтримки немає.

### Expected result
За GUI-40 меню містить 9 пунктів, серед них Служба підтримки між Заявкою на кредит і Вихід.

### Environment
demo https://parabank.parasoft.com/parabank, 29.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug014.

### Attachments
- BUG-014-1.png - меню кабінету без пункту Служба підтримки

## BUG-015: Шрифт і цільові елементи інтерфейсу менші за мінімуми U-10

**Severity:** Minor  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-039; U-10  
**Reporter:** Бурлачка Артем, 29.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
U-10 вимагає шрифт не менше 12 px і цільові елементи керування не менше 44x44 px. Фактично кнопка Log In має шрифт 10 px і розмір 66x19 px, а посилання Forgot login info? і Register мають висоту 15 px. Частина елементів менша за мінімум, тому людям з порушеннями зору складніше користуватися інтерфейсом. Severity Minor: функціональність не блокується, страждає доступність. Priority P3: дефект не блокує функції і виправляється поза релізом.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Відкрити Chrome DevTools.
3. Виміряти computed font-size і розміри кнопки Log In.
4. Виміряти розміри посилань Forgot login info? і Register.

### Actual result
Кнопка Log In: font-size 10 px, розмір 66x19 px. Посилання Forgot login info? і Register: висота 15 px. Це менше 12 px для шрифту і 44x44 px для цілей.

### Expected result
За U-10 шрифт не менше 12 px, цільові елементи керування не менше 44x44 px.

### Environment
demo https://parabank.parasoft.com/parabank, 29.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; без входу.

### Attachments
- BUG-015-1.png - малий шрифт і цільові елементи інтерфейсу

## BUG-016: Пошук операцій відхиляє дату у форматі ДД.ММ.РРРР

**Severity:** Normal  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-020; F-90  
**Reporter:** Топчій Микита, 28.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-90 вимагає формат дати ДД.ММ.РРРР, наприклад 30.09.2026. Фактично поле приймає лише MM-DD-YYYY: введення 30.09.2026 показує Invalid date format. Користувач не може шукати операції у форматі, передбаченому специфікацією. Severity Normal: пошук працює в іншому форматі, але вимога не виконується. Priority P3: обхідний формат існує.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug016 з паролем FinKit#2026.
3. Відкрити пункт меню Find Transactions.
4. Обрати рахунок.
5. У поле Find by Date ввести 30.09.2026.
6. Натиснути Find Transactions.

### Actual result
Під полем дати показано Invalid date format. Пошук не виконано. Формат 10-29-2026 приймається.

### Expected result
За F-90 дата 30.09.2026 приймається, пошук виконується і показує операції за 30.09.2026 або порожній список без повідомлення про помилку формату.

### Environment
demo https://parabank.parasoft.com/parabank, 28.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug016.

### Attachments
- BUG-016-1.png - повідомлення Invalid date format для 30.09.2026

## BUG-019: Неправильний пароль або невідомий логін відкриває кабінет John Smith

**Severity:** Critical  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-005; F-20, S-20  
**Reporter:** Топчій Микита, 28.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-20 вимагає не виконувати вхід, якщо пара логін/пароль не відповідає жодному обліковому запису, і показувати Логін або пароль не підтверджено. Фактично будь-який неправильний пароль або невідомий логін відкриває кабінет John Smith з усіма його рахунками. Це несанкціонований доступ до чужого облікового запису і грубе порушення S-20. Severity Critical: розкрито доступ до рахунків іншого клієнта. Priority P3: цей дефект не блокує виконання жодного тест-кейса (усі 40 кейсів виконано), тому за визначенням 4.13 це P3. Пропозиція розробнику: виправити раніше за дефекти з нижчою severity.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm у новій сесії Chrome.
2. У поле Username ввести fins3unknown2901.
3. У поле Password ввести WrongPass#2026.
4. Натиснути Log In.
5. Переглянути сторінку, що відкрилася.

### Actual result
Відкрито overview.htm з привітанням Welcome John Smith. Показано рахунки 12345, 12456 та інші, які належать John Smith. Повідомлення про помилку відсутнє.

### Expected result
За F-20 показано Логін або пароль не підтверджено; вхід не виконано; кабінет і рахунки не відкриваються. За S-20 доступ до рахунків без успішної автентифікації заборонено.

### Environment
demo https://parabank.parasoft.com/parabank, 28.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; нова сесія без входу.

### Attachments
- BUG-019-1.png - кабінет John Smith після невірного пароля

## BUG-020: Поле Phone # не є обов'язковим у формі реєстрації

**Severity:** Normal  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-002; GUI-30, U-20  
**Reporter:** Топчій Микита, 28.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
GUI-30 вимагає, щоб усі одинадцять полів форми реєстрації були обов'язковими, і для кожного порожнього поля показувалося повідомлення. Фактично після натискання Register з порожньою формою показано 10 повідомлень, а для Phone # повідомлення немає. Клієнт може зареєструватися без телефону, хоча вимога цього не дозволяє. Severity Normal: валідація форми неповна. Priority P3: не блокує інші кейси.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/register.htm.
2. Не заповнювати жодного поля.
3. Натиснути Register.
4. Порахувати повідомлення про обов'язкові поля і перевірити поле Phone #.

### Actual result
Показано повідомлення для First Name, Last Name, Address, City, State, Zip Code, SSN, Username, Password, Confirm. Для Phone # повідомлення про обов'язковість відсутнє.

### Expected result
За GUI-30 усі одинадцять полів обов'язкові; для Phone # також показано Поле Phone є обов'язковим.

### Environment
demo https://parabank.parasoft.com/parabank, 28.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; без входу.

### Attachments
- BUG-020-1.png - форма реєстрації без обов'язкового Phone #

## BUG-021: Спосіб опрацювання заявки Available Funds замість автоматичного за порогом

**Severity:** Normal  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-032; F-160, C-50  
**Reporter:** Бурлачка Артем, 29.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
C-50 визначає спосіб опрацювання заявки автоматичний за порогом. Фактично адмінпанель показує Loan Processor Available Funds. Режим змінює логіку кредитного рішення і не відповідає SRS. Severity Normal: рішення все одно ухвалюється, але за іншим правилом. Priority P3: не блокує інші кейси.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/admin.htm.
2. Переглянути блок Application Settings.
3. Зафіксувати значення Loan Processor.

### Actual result
Loan Processor: Available Funds. Значення не відповідає C-50.

### Expected result
За C-50 і F-160 спосіб опрацювання заявки автоматичний за порогом.

### Environment
demo https://parabank.parasoft.com/parabank, 29.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; адмінпанель, читання без змін.

### Attachments
- BUG-021-1.png - Loan Processor Available Funds замість автоматичного режиму

## BUG-022: Сеанс клієнта не завершується після 15 хвилин бездіяльності

**Severity:** Major  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-038; S-10, GUI-80  
**Reporter:** Бурлачка Артем, 29.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
S-10 і GUI-80 вимагають автоматичного завершення сеансу після 15 хвилин бездіяльності; для продовження потрібен повторний вхід. Фактично після 16.5 хвилини бездіяльності прямий перехід на overview.htm відкриває кабінет Ivan Franchuk з рахунком 89268 і балансом 250.00. Сеанс залишається активним, тому незахищене робоче місце дає доступ до рахунків. Severity Major: порушено вимогу безпеки сеансу. Priority P3: цей дефект не блокує виконання жодного тест-кейса (усі 40 кейсів виконано), тому за визначенням 4.13 це P3.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug022 з паролем FinKit#2026.
3. Відкрити Accounts Overview і зафіксувати активний сеанс.
4. Не виконувати жодної дії 16 хвилин.
5. Відкрити https://parabank.parasoft.com/parabank/overview.htm.
6. Перевірити, чи відкривається кабінет без повторного входу.

### Actual result
Після 16.5 хвилини бездіяльності overview.htm показує Welcome Ivan Franchuk, рахунок 89268, баланс 250.00 і меню кабінету. Повторний вхід не вимагається.

### Expected result
За S-10 і GUI-80 після 15 хвилин бездіяльності сеанс завершено; overview.htm вимагає повторного входу і не показує рахунки.

### Environment
demo https://parabank.parasoft.com/parabank, 29.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug022.

### Attachments
- BUG-022-1.png - активний сеанс після 16.5 хвилини бездіяльності

## BUG-023: Відкриття рахунку при балансі 99.99 не відхиляється

**Severity:** Major  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-011; F-60  
**Reporter:** Топчій Микита, 28.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-60 вимагає початковий внесок 100.00 і відмову, якщо на рахунку-джерелі недостатньо коштів. Фактично при балансі 99.99 новий ощадний рахунок відкривається, з рахунку списується 90.00, повідомлення про нестачу коштів відсутнє. Клієнт отримує рахунок без потрібної суми. Severity Major: грошова умова відкриття не виконується. Priority P3: цей дефект не блокує виконання жодного тест-кейса (усі 40 кейсів виконано), тому за визначенням 4.13 це P3.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug023 з паролем FinKit#2026.
3. Відкрити ощадний рахунок, щоб у клієнта було два рахунки.
4. Переказати 60.01 з ощадного рахунку на поточний, щоб баланс поточного став 99.99.
5. Відкрити пункт меню Open New Account.
6. Обрати SAVINGS і поточний рахунок як джерело.
7. Натиснути Open New Account.

### Actual result
Показано Account Opened і Your new account number: 91710. З рахунку-джерела списано 90.00, новий рахунок отримав 90.00. Повідомлення про нестачу коштів відсутнє.

### Expected result
За F-60 показано Недостатньо коштів на рахунку; новий рахунок не відкривається, баланс джерела 99.99 не змінюється.

### Environment
demo https://parabank.parasoft.com/parabank, 28.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug023.

### Attachments
- BUG-023-1.png - відкриття рахунку при балансі 99.99

## BUG-024: Повідомлення про недійсний IBAN вказує на числовий формат

**Severity:** Normal  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-017; F-80, GUI-60  
**Reporter:** Топчій Микита, 28.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-80 і GUI-60 вимагають приймати IBAN у форматі UA і 27 цифр та повідомляти про порушення саме формату IBAN. Фактично після введення UA123 у полях Account Number і Verify Account Number показано Please enter a valid number. Користувач не розуміє, що поле призначене для IBAN, і не отримує підказки про очікуваний формат. Severity Normal: платіж не виконується, але повідомлення вводить в оману. Priority P3: не блокує інші кейси.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug024 з паролем FinKit#2026.
3. Відкрити пункт меню Bill Pay.
4. Заповнити Payee Name, Address, City, State, Zip Code, Phone.
5. У поле Account Number ввести UA123.
6. У поле Verify Account Number ввести UA123.
7. У поле Amount ввести 1.00 і натиснути Send Payment.

### Actual result
Поля Account Number і Verify Account Number показують Please enter a valid number. Повідомлення про формат IBAN або про невідомий IBAN відсутнє.

### Expected result
За F-80 і GUI-60 показано повідомлення про формат IBAN: IBAN має містити UA і 27 цифр; платіж не виконується.

### Environment
demo https://parabank.parasoft.com/parabank, 28.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug024.

### Attachments
- BUG-024-1.png - повідомлення про недійсний IBAN

## BUG-025: Результат пошуку за ID не показує ідентифікатор операції

**Severity:** Normal  
**Priority:** P3  
**Status:** New  
**Review:** reviewed  
**Linked:** TC-019; F-90, F-50  
**Reporter:** Топчій Микита, 28.10.2026  
**Reviewer:** Сушко Ілля, 29.10.2026

### Description
F-90 вимагає пошук операції за ID і результат, за яким можна підтвердити, що знайдено саме запитану операцію. F-50 вимагає ідентифікатор операції у виписці. Фактично результат пошуку за ID містить дату, опис і суму, але не містить ID операції та балансу після операції. Користувач не може зіставити рядок із запитаним ID. Severity Normal: пошук повертає дані, але не дає підтвердження. Priority P3: не блокує інші кейси.

### Steps to reproduce
1. Відкрити https://parabank.parasoft.com/parabank/index.htm.
2. Зареєструвати клієнта fins3bug025 з паролем FinKit#2026.
3. Відкрити ощадний рахунок і переказати 100.00.
4. Відкрити виписку поточного рахунку і скопіювати ID операції з посилання.
5. Відкрити пункт меню Find Transactions.
6. Обрати рахунок і ввести скопійований ID у поле Find by Transaction ID.
7. Натиснути FIND TRANSACTIONS.

### Actual result
У додатковому прогоні пошук за ID 89845 повернув Transaction Results: 10-29-2026, Funds Transfer Sent, $90.00. Ідентифікатор операції та баланс після операції у рядку відсутні. Основний прогін додатково завершився внутрішньою помилкою через витягання ID.

### Expected result
За F-90 і F-50 результат показує ID 89845, дату, опис, суму і баланс після операції; видно, що це саме запитана операція.

### Environment
demo https://parabank.parasoft.com/parabank, 28.10.2026; Google Chrome 156.0.8078.49; Windows 11 Pro 10.0.26200; обліковий запис fins3bug025.

### Attachments
- BUG-025-1.png - результат пошуку за ID без ідентифікатора операції
- BUG-025-2.json - журнал додаткового прогону пошуку за ID 89845
