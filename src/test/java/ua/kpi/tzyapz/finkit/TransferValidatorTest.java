package ua.kpi.tzyapz.finkit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Модульні тести валідатора переказів {@link TransferValidator} за вимогами BL-40, BL-50.
 */
@DisplayName("BL-40, BL-50: Модульні тести TransferValidator")
class TransferValidatorTest {

    private static final String VALID_FROM_IBAN = "UA213223130000026007233566001";
    private static final String VALID_TO_IBAN = "UA903052990000026004012345678";

    private TransferValidator validator;

    @BeforeEach
    void setUp() {
        validator = new TransferValidator();
    }

    // Автор: ІТ Бурлачка Артем | Рецензент: СМ Топчій Микита | Вимога: BL-40, BL-50
    @Test
    @DisplayName("ІТ-1 (BL-40, BL-50): коректний переказ, мінімальна сума 0.01 та межа 49999.99 дозволені")
    void validate_validTransferAndBelowDailyLimit_returnsEmptyList() {
        // Arrange
        BigDecimal balance = new BigDecimal("5000.00");

        // Act
        List<TransferError> standardErrors = validator.validate(
            VALID_FROM_IBAN, VALID_TO_IBAN, new BigDecimal("1000.00"), balance, BigDecimal.ZERO);
        boolean standardValid = validator.isValid(
            VALID_FROM_IBAN, VALID_TO_IBAN, new BigDecimal("1000.00"), balance, BigDecimal.ZERO);

        List<TransferError> nearLimitErrors = validator.validate(
            VALID_FROM_IBAN, VALID_TO_IBAN, new BigDecimal("299.99"), balance, new BigDecimal("49700.00"));
        boolean nearLimitValid = validator.isValid(
            VALID_FROM_IBAN, VALID_TO_IBAN, new BigDecimal("299.99"), balance, new BigDecimal("49700.00"));

        List<TransferError> minAmountErrors = validator.validate(
            VALID_FROM_IBAN, VALID_TO_IBAN, new BigDecimal("0.01"), new BigDecimal("0.01"), BigDecimal.ZERO);
        boolean minAmountValid = validator.isValid(
            VALID_FROM_IBAN, VALID_TO_IBAN, new BigDecimal("0.01"), new BigDecimal("0.01"), BigDecimal.ZERO);

        // Assert
        assertAll(
            () -> assertTrue(standardErrors.isEmpty(), "Стандартний переказ 1000.00 не повинен мати помилок: " + standardErrors),
            () -> assertTrue(standardValid, "isValid має повертати true для коректного переказу"),
            () -> assertTrue(nearLimitErrors.isEmpty(), "Сума дня 49999.99 нижче ліміту 50000.00 не повинна мати помилок: " + nearLimitErrors),
            () -> assertTrue(nearLimitValid, "isValid має повертати true для суми дня 49999.99"),
            () -> assertTrue(minAmountErrors.isEmpty(), "Мінімальна додатна сума 0.01 при балансі 0.01 має бути дозволена: " + minAmountErrors),
            () -> assertTrue(minAmountValid, "isValid має повертати true для суми 0.01")
        );
    }

    // Автор: ІТ Бурлачка Артем | Рецензент: СМ Топчій Микита | Вимога: BL-50, BL-40
    // Виявляє BUG-003: у v1 метод validate не перевіряє формат toIban і не повертає TransferError.INVALID_TO_IBAN.
    @ParameterizedTest(name = "Недійсний toIban=\"{0}\" -> INVALID_TO_IBAN")
    @MethodSource("invalidIbanInputs")
    @DisplayName("ІТ-2 (BL-50, BL-40): недійсний IBAN одержувача повертає INVALID_TO_IBAN")
    void validate_invalidToIban_returnsInvalidToIbanError(String invalidToIban) {
        // Arrange
        BigDecimal amount = new BigDecimal("100.00");
        BigDecimal balance = new BigDecimal("5000.00");

        // Act
        List<TransferError> errors = validator.validate(
            VALID_FROM_IBAN, invalidToIban, amount, balance, BigDecimal.ZERO);
        boolean valid = validator.isValid(
            VALID_FROM_IBAN, invalidToIban, amount, balance, BigDecimal.ZERO);

        // Assert
        assertAll(
            () -> assertTrue(errors.contains(TransferError.INVALID_TO_IBAN),
                "Очікували INVALID_TO_IBAN для toIban='" + invalidToIban + "', фактично: " + errors),
            () -> assertFalse(valid, "isValid має повертати false при недійсному toIban")
        );
    }

    // Автор: ІТ Бурлачка Артем | Рецензент: СМ Топчій Микита | Вимога: BL-40
    // Виявляє BUG-004: у v1 умова >= 0 блокує переказ при досягненні точної межі денного ліміту 50000.00.
    @Test
    @DisplayName("ІТ-3 (BL-40): точна межа денного ліміту todaySpent + amount == 50000.00 дозволена")
    void validate_exactDailyLimit50000_isAllowed() {
        // Arrange
        BigDecimal partialSpent = new BigDecimal("49700.00");
        BigDecimal partialAmount = new BigDecimal("300.00");
        BigDecimal partialBalance = new BigDecimal("5000.00");

        BigDecimal fullAmount = new BigDecimal("50000.00");
        BigDecimal fullBalance = new BigDecimal("50000.00");

        // Act
        List<TransferError> splitLimitErrors = validator.validate(
            VALID_FROM_IBAN, VALID_TO_IBAN, partialAmount, partialBalance, partialSpent);
        boolean splitLimitValid = validator.isValid(
            VALID_FROM_IBAN, VALID_TO_IBAN, partialAmount, partialBalance, partialSpent);

        List<TransferError> singleTransferLimitErrors = validator.validate(
            VALID_FROM_IBAN, VALID_TO_IBAN, fullAmount, fullBalance, BigDecimal.ZERO);
        boolean singleTransferLimitValid = validator.isValid(
            VALID_FROM_IBAN, VALID_TO_IBAN, fullAmount, fullBalance, BigDecimal.ZERO);

        // Assert
        assertAll(
            () -> assertTrue(splitLimitErrors.isEmpty(),
                "49700.00 + 300.00 == 50000.00 входить у включний ліміт BL-40, отримано: " + splitLimitErrors),
            () -> assertTrue(splitLimitValid, "isValid має повертати true при 49700.00 + 300.00"),
            () -> assertTrue(singleTransferLimitErrors.isEmpty(),
                "0.00 + 50000.00 == 50000.00 входить у включний ліміт BL-40, отримано: " + singleTransferLimitErrors),
            () -> assertTrue(singleTransferLimitValid, "isValid має повертати true для одного переказу 50000.00")
        );
    }

    // Автор: ІТ Бурлачка Артем | Рецензент: СМ Топчій Микита | Вимога: BL-40
    @Test
    @DisplayName("ІТ-4 (BL-40): перевищення денного ліміту 50000.00 повертає DAILY_LIMIT_EXCEEDED")
    void validate_aboveDailyLimit50000_returnsDailyLimitExceeded() {
        // Arrange
        BigDecimal balance = new BigDecimal("5000.00");

        // Act
        List<TransferError> overByOneCentErrors = validator.validate(
            VALID_FROM_IBAN, VALID_TO_IBAN, new BigDecimal("300.01"), balance, new BigDecimal("49700.00"));
        boolean overByOneCentValid = validator.isValid(
            VALID_FROM_IBAN, VALID_TO_IBAN, new BigDecimal("300.01"), balance, new BigDecimal("49700.00"));

        List<TransferError> alreadyAtLimitErrors = validator.validate(
            VALID_FROM_IBAN, VALID_TO_IBAN, new BigDecimal("0.01"), balance, new BigDecimal("50000.00"));
        boolean alreadyAtLimitValid = validator.isValid(
            VALID_FROM_IBAN, VALID_TO_IBAN, new BigDecimal("0.01"), balance, new BigDecimal("50000.00"));

        // Assert
        assertAll(
            () -> assertTrue(overByOneCentErrors.contains(TransferError.DAILY_LIMIT_EXCEEDED),
                "49700.00 + 300.01 перевищує ліміт 50000.00: " + overByOneCentErrors),
            () -> assertFalse(overByOneCentValid, "isValid має повертати false при перевищенні ліміту на 0.01"),
            () -> assertTrue(alreadyAtLimitErrors.contains(TransferError.DAILY_LIMIT_EXCEEDED),
                "50000.00 + 0.01 перевищує ліміт 50000.00: " + alreadyAtLimitErrors),
            () -> assertFalse(alreadyAtLimitValid, "isValid має повертати false після вичерпання ліміту")
        );
    }

    // Автор: ІТ Бурлачка Артем | Рецензент: СМ Топчій Микита | Вимога: BL-40
    @ParameterizedTest(name = "Недодатна або null сума={0} -> AMOUNT_NOT_POSITIVE")
    @MethodSource("nonPositiveAmounts")
    @DisplayName("ІТ-5 (BL-40): нульова, від'ємна або null сума повертає AMOUNT_NOT_POSITIVE")
    void validate_nonPositiveOrNullAmount_returnsAmountNotPositive(BigDecimal invalidAmount) {
        // Arrange
        BigDecimal balance = new BigDecimal("5000.00");

        // Act
        List<TransferError> errors = validator.validate(
            VALID_FROM_IBAN, VALID_TO_IBAN, invalidAmount, balance, BigDecimal.ZERO);
        boolean valid = validator.isValid(
            VALID_FROM_IBAN, VALID_TO_IBAN, invalidAmount, balance, BigDecimal.ZERO);

        // Assert
        assertAll(
            () -> assertTrue(errors.contains(TransferError.AMOUNT_NOT_POSITIVE),
                "Очікували AMOUNT_NOT_POSITIVE для суми " + invalidAmount + ", фактично: " + errors),
            () -> assertFalse(valid, "isValid має повертати false для недодатної або null суми")
        );
    }

    private static Stream<String> invalidIbanInputs() {
        return Stream.of(
            "UA123",
            "PL213223130000026007233566001",
            "ua213223130000026007233566001",
            "",
            null
        );
    }

    private static Stream<BigDecimal> nonPositiveAmounts() {
        return Stream.of(
            new BigDecimal("0.00"),
            new BigDecimal("-0.01"),
            new BigDecimal("-10.00"),
            null
        );
    }
}
