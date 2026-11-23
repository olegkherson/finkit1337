package ua.kpi.tzyapz.finkit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Модульні тести книги обліку рахунку {@link AccountLedger} за вимогами BL-70, BL-80.
 */
@DisplayName("BL-70, BL-80: Модульні тести AccountLedger")
class AccountLedgerTest {

    private AccountLedger ledger;

    @BeforeEach
    void setUp() {
        ledger = new AccountLedger();
    }

    // Автор: СМ Топчій Микита | Рецензент: КТ Бойко Олег | Вимога: BL-70, BL-80
    @Test
    @DisplayName("СМ-1 (BL-70, BL-80): deposit збільшує баланс з HALF_UP округленням до 2 знаків")
    void deposit_validAmountsAndHalfUpRounding_updatesBalance() {
        // Arrange
        BigDecimal initialBalance = ledger.balance();

        // Act
        ledger.deposit(new BigDecimal("0.01"));
        BigDecimal balanceAfterMinDeposit = ledger.balance();

        ledger.deposit(new BigDecimal("100.005"));
        BigDecimal balanceAfterHalfUpUp = ledger.balance();

        ledger.deposit(new BigDecimal("10.004"));
        BigDecimal balanceAfterHalfUpDown = ledger.balance();

        // Assert
        assertAll(
            () -> assertEquals(new BigDecimal("0.00"), initialBalance,
                "Початковий баланс нового рахунку має дорівнювати 0.00"),
            () -> assertEquals(new BigDecimal("0.01"), balanceAfterMinDeposit,
                "Після мінімального поповнення 0.01 баланс має дорівнювати 0.01"),
            () -> assertEquals(new BigDecimal("100.02"), balanceAfterHalfUpUp,
                "100.005 має округлитися за HALF_UP до 100.01 і дати баланс 100.02"),
            () -> assertEquals(new BigDecimal("110.02"), balanceAfterHalfUpDown,
                "10.004 має округлитися за HALF_UP до 10.00 і дати баланс 110.02"),
            () -> assertEquals(2, balanceAfterHalfUpDown.scale(),
                "Шкала балансу має дорівнювати 2 знакам після коми")
        );
    }

    // Автор: СМ Топчій Микита | Рецензент: КТ Бойко Олег | Вимога: BL-70, BL-80
    @Test
    @DisplayName("СМ-2 (BL-70, BL-80): withdraw списує часткову суму з HALF_UP округленням до копійок")
    void withdraw_partialValidAmount_reducesBalance() {
        // Arrange
        ledger.deposit(new BigDecimal("500.00"));

        // Act
        ledger.withdraw(new BigDecimal("120.50"));
        BigDecimal balanceAfterFirst = ledger.balance();

        ledger.withdraw(new BigDecimal("0.01"));
        BigDecimal balanceAfterSecond = ledger.balance();

        ledger.withdraw(new BigDecimal("0.005"));
        BigDecimal balanceAfterThird = ledger.balance();

        // Assert
        assertAll(
            () -> assertEquals(new BigDecimal("379.50"), balanceAfterFirst,
                "Після списання 120.50 із 500.00 баланс має дорівнювати 379.50"),
            () -> assertEquals(new BigDecimal("379.49"), balanceAfterSecond,
                "Після списання 0.01 баланс має дорівнювати 379.49"),
            () -> assertEquals(new BigDecimal("379.48"), balanceAfterThird,
                "0.005 має округлитися за HALF_UP до 0.01 і дати баланс 379.48"),
            () -> assertEquals(2, balanceAfterThird.scale(),
                "Шкала балансу після списання має дорівнювати 2")
        );
    }

    // Автор: СМ Топчій Микита | Рецензент: КТ Бойко Олег | Вимога: BL-70, BL-80
    // Виявляє BUG-005: у v1 умова value.compareTo(balance) >= 0 забороняє зняття повного залишку (amount == balance).
    @Test
    @DisplayName("СМ-3 (BL-70, BL-80): зняття повного залишку amount == balance встановлює баланс 0.00")
    void withdraw_exactFullBalance_setsBalanceToZero() {
        // Arrange
        ledger.deposit(new BigDecimal("250.00"));

        // Act
        assertDoesNotThrow(
            () -> ledger.withdraw(new BigDecimal("250.00")),
            "Зняття суми, що дорівнює поточному балансу 250.00, не є овердрафтом і має виконуватися"
        );

        // Assert
        BigDecimal finalBalance = ledger.balance();
        List<LedgerEntry> history = ledger.history();
        assertAll(
            () -> assertEquals(new BigDecimal("0.00"), finalBalance,
                "Після зняття повного залишку баланс має дорівнювати 0.00"),
            () -> assertEquals(2, finalBalance.scale(),
                "Шкала нульового балансу має дорівнювати 2"),
            () -> assertEquals(2, history.size(),
                "Історія має містити 2 записи: DEPOSIT і WITHDRAWAL")
        );
    }

    // Автор: СМ Топчій Микита | Рецензент: КТ Бойко Олег | Вимога: BL-70, BL-80
    // Виявляє BUG-006: у v1 метод history() викликає Collections.reverse(copy) і повертає записи у зворотному порядку.
    @Test
    @DisplayName("СМ-4 (BL-70, BL-80): history повертає записи операцій у хронологічному порядку")
    void history_multipleOperations_returnsChronologicalOrder() {
        // Arrange
        ledger.deposit(new BigDecimal("100.00"));
        ledger.withdraw(new BigDecimal("30.00"));
        ledger.deposit(new BigDecimal("50.00"));

        // Act
        List<LedgerEntry> history = ledger.history();

        // Assert
        assertAll(
            () -> assertEquals(3, history.size(), "В історії має бути рівно 3 записи"),
            () -> assertEquals(
                new LedgerEntry(LedgerEntry.Type.DEPOSIT, new BigDecimal("100.00"), new BigDecimal("100.00")),
                history.get(0),
                "Першим записом має бути початкове зарахування 100.00"
            ),
            () -> assertEquals(
                new LedgerEntry(LedgerEntry.Type.WITHDRAWAL, new BigDecimal("30.00"), new BigDecimal("70.00")),
                history.get(1),
                "Другим записом має бути списання 30.00 із балансом 70.00"
            ),
            () -> assertEquals(
                new LedgerEntry(LedgerEntry.Type.DEPOSIT, new BigDecimal("50.00"), new BigDecimal("120.00")),
                history.get(2),
                "Третім записом має бути зарахування 50.00 із балансом 120.00"
            )
        );
    }

    // Автор: СМ Топчій Микита | Рецензент: КТ Бойко Олег | Вимога: BL-70, BL-80
    @ParameterizedTest(name = "Недодатна або null сума={0} для deposit і withdraw")
    @MethodSource("nonPositiveAmounts")
    @DisplayName("СМ-5 (BL-70, BL-80): нульова, від'ємна або null сума для deposit і withdraw кидає IllegalArgumentException і зберігає баланс")
    void depositAndWithdraw_zeroOrNegativeOrNullAmount_throwsAndPreservesBalance(BigDecimal invalidAmount) {
        // Arrange
        ledger.deposit(new BigDecimal("100.00"));

        // Act
        IllegalArgumentException depositException = assertThrows(
            IllegalArgumentException.class,
            () -> ledger.deposit(invalidAmount),
            "deposit з недодатною або null сумою " + invalidAmount + " має кидати IllegalArgumentException"
        );
        IllegalArgumentException withdrawException = assertThrows(
            IllegalArgumentException.class,
            () -> ledger.withdraw(invalidAmount),
            "withdraw з недодатною або null сумою " + invalidAmount + " має кидати IllegalArgumentException"
        );

        // Assert
        assertAll(
            () -> assertNotNull(depositException.getMessage(),
                "Виняток deposit має містити повідомлення"),
            () -> assertNotNull(withdrawException.getMessage(),
                "Виняток withdraw має містити повідомлення"),
            () -> assertEquals(new BigDecimal("100.00"), ledger.balance(),
                "Баланс 100.00 не повинен змінюватися після відхилених операцій"),
            () -> assertEquals(1, ledger.history().size(),
                "Історія має містити лише початкову операцію поповнення")
        );
    }

    // Автор: АО Сушко Ілля | Рецензент: СМ Топчій Микита | Вимога: BL-70, BL-80
    // Виявляє BUG-008: за SRS-1.1 (BL-70) овердрафт має кидати IllegalArgumentException, тоді як Javadoc і код v1 кидають IllegalStateException.
    @Test
    @DisplayName("BL-70, BL-80: АО-3 - зняття суми понад баланс кидає IllegalArgumentException і зберігає баланс")
    void withdraw_amountAboveBalance_throwsAndLeavesBalanceUnchanged() {
        // Arrange
        ledger.deposit(new BigDecimal("100.00"));
        BigDecimal overdraftAmount = new BigDecimal("100.01");

        // Act + Assert
        assertAll(
            () -> assertThrows(
                IllegalArgumentException.class,
                () -> ledger.withdraw(overdraftAmount),
                "За SRS-1.1 (BL-70) спроба зняття суми 100.01 понад баланс 100.00 має кидати IllegalArgumentException"
            ),
            () -> assertEquals(
                new BigDecimal("100.00"),
                ledger.balance(),
                "Після відхилення овердрафту баланс рахунку має залишатися 100.00"
            ),
            () -> assertEquals(
                1,
                ledger.history().size(),
                "Невдала спроба овердрафту не повинна додавати запис до історії"
            )
        );
    }

    // Автор: АО Сушко Ілля | Рецензент: СМ Топчій Микита | Вимога: BL-80, BL-70
    @Test
    @DisplayName("BL-80, BL-70: АО-4 - поповнення на верхню межу 999999999.99 і незмінність колекції history()")
    void deposit_upperBoundary999999999_99_andImmutableHistory_succeeds() {
        // Arrange
        BigDecimal maxOperationAmount = new BigDecimal("999999999.99");

        // Act
        ledger.deposit(maxOperationAmount);
        List<LedgerEntry> history = ledger.history();

        // Assert
        assertAll(
            () -> assertEquals(
                new BigDecimal("999999999.99"),
                ledger.balance(),
                "Баланс після поповнення на верхню межу 999999999.99 має дорівнювати 999999999.99"
            ),
            () -> assertEquals(2, ledger.balance().scale(), "Шкала балансу має дорівнювати 2"),
            () -> assertEquals(1, history.size(), "Історія має містити рівно 1 запис"),
            () -> assertThrows(
                UnsupportedOperationException.class,
                () -> history.add(new LedgerEntry(LedgerEntry.Type.DEPOSIT, BigDecimal.ONE, BigDecimal.ONE)),
                "Колекція history() має бути незмінюваною"
            )
        );
    }
    
    private static Stream<BigDecimal> nonPositiveAmounts() {
        return Stream.of(
            new BigDecimal("0.00"),
            new BigDecimal("-0.01"),
            new BigDecimal("-100.00"),
            null
        );
    }
}
