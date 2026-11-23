package ua.kpi.tzyapz.finkit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Модульні тести кредитного калькулятора {@link LoanCalculator} за вимогами BL-10...BL-30.
 */
@DisplayName("BL-10...BL-30: Модульні тести LoanCalculator (ТА)")
class LoanCalculatorTest {

    private LoanCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new LoanCalculator();
    }

    // Автор: ТА Закусило Дмитрій | Рецензент: СМ Топчій Микита | Вимога: BL-10
    // Виявляє BUG-001: проміжне округлення місячної ставки i до 4 знаків у v1 спотворює платіж для ставок, некратних 12.
    @ParameterizedTest(name = "P={0}, r={1}%, n={2} міс. -> {3} грн")
    @CsvSource({
        "100000, 12, 12, 8884.88",
        "100000, 20, 12, 9263.45",
        "100000, 10, 12, 8791.59",
        "100000, 5, 12, 8560.75"
    })
    @DisplayName("ТА-1 (BL-10): monthlyPayment точно обчислює ануїтетний платіж без проміжного округлення")
    void monthlyPayment_validRatesAndReferenceCases_matchesAnnuityFormula(
            String principalStr, String rateStr, int months, String expectedStr) {
        // Arrange
        BigDecimal principal = new BigDecimal(principalStr);
        BigDecimal rate = new BigDecimal(rateStr);
        BigDecimal expected = new BigDecimal(expectedStr);

        // Act
        BigDecimal actual = calculator.monthlyPayment(principal, rate, months);

        // Assert
        assertAll(
            () -> assertEquals(2, actual.scale(), "Очікується шкала 2 знаки після коми"),
            () -> assertEquals(expected, actual, "Ануїтетний платіж має збігатися з формулою BL-10")
        );
    }

    // Автор: ТА Закусило Дмитрій | Рецензент: СМ Топчій Микита | Вимога: BL-20
    // Виявляє BUG-001: totalInterest каскадно спотворюється через похибку округленого щомісячного платежу в v1.
    @Test
    @DisplayName("ТА-2 (BL-20): totalInterest використовує округлений monthlyPayment за формулою A * n - P")
    void totalInterest_referenceAndNonDivisibleRate_usesRoundedMonthlyPayment() {
        // Arrange
        BigDecimal principal = new BigDecimal("100000");

        // Act
        BigDecimal interestAt20 = calculator.totalInterest(principal, new BigDecimal("20"), 12);
        BigDecimal interestAt10 = calculator.totalInterest(principal, new BigDecimal("10"), 12);

        // Assert
        assertAll(
            () -> assertEquals(2, interestAt20.scale(), "Шкала переплати для 20 % має дорівнювати 2"),
            () -> assertEquals(new BigDecimal("11161.40"), interestAt20, "Еталон test-plan.md для 20 %"),
            () -> assertEquals(2, interestAt10.scale(), "Шкала переплати для 10 % має дорівнювати 2"),
            () -> assertEquals(new BigDecimal("5499.08"), interestAt10, "Переплата для 10 % за формулою A*n - P")
        );
    }

    // Автор: ТА Закусило Дмитрій | Рецензент: СМ Топчій Микита | Вимога: BL-30, BL-10
    // Виявляє BUG-002: у v1 умова months <= MIN_MONTHS відхиляє включну нижню межу n = 3.
    @Test
    @DisplayName("ТА-3 (BL-30, BL-10): monthlyPayment приймає включну нижню межу строку n = 3 місяці")
    void monthlyPayment_minBoundaryMonths3_isAccepted() {
        // Arrange
        BigDecimal principal = new BigDecimal("1000.00");
        BigDecimal rate = new BigDecimal("5.00");
        int minMonths = 3;

        // Act
        BigDecimal actual = assertDoesNotThrow(
            () -> calculator.monthlyPayment(principal, rate, minMonths),
            "Нижня включна межа n = 3 не повинна кидати IllegalArgumentException"
        );

        // Assert
        assertAll(
            () -> assertEquals(2, actual.scale(), "Шкала результату має дорівнювати 2"),
            () -> assertEquals(new BigDecimal("336.11"), actual, "Очікуваний платіж для P=1000, r=5%, n=3")
        );
    }

    // Автор: ТА Закусило Дмитрій | Рецензент: СМ Топчій Микита | Вимога: BL-30
    @ParameterizedTest(name = "Включні межі: P={0}, r={1}%, n={2} міс.")
    @CsvSource({
        "1000.00, 5.00, 4",
        "1000000.00, 40.00, 60",
        "1000.01, 5.01, 59",
        "999999.99, 39.99, 59",
        "1000.00, 5.00, 60",
        "1000000.00, 40.00, 4"
    })
    @DisplayName("ТА-4 (BL-30): monthlyPayment приймає включні межі суми, ставки та максимального строку")
    void monthlyPayment_inclusivePrincipalRateAndMaxMonthsBoundaries_succeed(
            String principalStr, String rateStr, int months) {
        // Arrange
        BigDecimal principal = new BigDecimal(principalStr);
        BigDecimal rate = new BigDecimal(rateStr);

        // Act
        BigDecimal actual = assertDoesNotThrow(
            () -> calculator.monthlyPayment(principal, rate, months),
            "Включні границі BL-30 повинні прийматися без винятку"
        );

        // Assert
        assertAll(
            () -> assertNotNull(actual, "Результат не повинен бути null"),
            () -> assertEquals(2, actual.scale(), "Шкала результату має дорівнювати 2"),
            () -> assertTrue(actual.signum() > 0, "Щомісячний платіж має бути додатним")
        );
    }

    // Автор: ТА Закусило Дмитрій | Рецензент: СМ Топчій Микита | Вимога: BL-30
    @ParameterizedTest(name = "Поза межами або null: P={0}, r={1}, n={2}")
    @MethodSource("outOfBoundsAndNullInputs")
    @DisplayName("ТА-5 (BL-30): monthlyPayment кидає IllegalArgumentException при виході за межі або null")
    void monthlyPayment_outOfBoundsOrNullParameters_throwsIllegalArgumentException(
            BigDecimal principal, BigDecimal rate, int months) {
        // Arrange + Act + Assert
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> calculator.monthlyPayment(principal, rate, months),
            "Порушення меж BL-30 або null мають викликати IllegalArgumentException"
        );
        assertNotNull(ex.getMessage(), "Виняток має містити діагностичне повідомлення");
    }


    // Автор: КТ Бойко Олег | Рецензент: СМ Топчій Микита | Вимога: BL-10, BL-20, BL-30
    // Виявляє BUG-001: проміжне округлення місячної ставки i до 4 знаків у v1 спотворює переплату на граничних ставках 5.00 % і 40.00 %.
    @Test
    @DisplayName("КТ-1 (BL-10, BL-20, BL-30): totalInterest на граничних ставках 5.00 % і 40.00 % зберігає точність та шкалу 2")
    void totalInterest_boundaryRatesAndHalfUpScale_returnsTwoDecimalPlaces() {
        // Arrange
        BigDecimal minPrincipal = new BigDecimal("1000.00");
        BigDecimal minRate = new BigDecimal("5.00");
        int minSafeMonths = 4;

        BigDecimal maxPrincipal = new BigDecimal("1000000.00");
        BigDecimal maxRate = new BigDecimal("40.00");
        int maxMonths = 60;

        // Act
        BigDecimal minBoundaryInterest = calculator.totalInterest(minPrincipal, minRate, minSafeMonths);
        BigDecimal maxBoundaryInterest = calculator.totalInterest(maxPrincipal, maxRate, maxMonths);

        // Assert
        assertAll(
            () -> assertEquals(2, minBoundaryInterest.scale(),
                "Шкала переплати для нижньої границі ставки 5.00 % має дорівнювати 2"),
            () -> assertEquals(new BigDecimal("10.44"), minBoundaryInterest,
                "Очікувана переплата для (1000.00, 5.00 %, 4 міс.): 252.61 * 4 - 1000.00 = 10.44"),
            () -> assertEquals(2, maxBoundaryInterest.scale(),
                "Шкала переплати для верхньої границі ставки 40.00 % має дорівнювати 2"),
            () -> assertEquals(new BigDecimal("1325098.40"), maxBoundaryInterest,
                "Очікувана переплата для (1000000.00, 40.00 %, 60 міс.): 38751.64 * 60 - 1000000.00 = 1325098.40")
        );
    }

    // Автор: КТ Бойко Олег | Рецензент: СМ Топчій Микита | Вимога: BL-30, BL-20
    @ParameterizedTest(name = "totalInterest поза межами або null: P={0}, r={1}, n={2}")
    @MethodSource("outOfBoundsAndNullInputs")
    @DisplayName("КТ-2 (BL-30, BL-20): totalInterest кидає IllegalArgumentException при виході за межі або null")
    void totalInterest_outOfBounds_throwsIllegalArgumentException(
            BigDecimal principal, BigDecimal rate, int months) {
        // Arrange + Act + Assert
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> calculator.totalInterest(principal, rate, months),
            "totalInterest при порушенні меж BL-30 або null має кидати IllegalArgumentException"
        );
        assertNotNull(ex.getMessage(), "Виняток totalInterest має містити діагностичне повідомлення");
    }
    
    private static Stream<Arguments> outOfBoundsAndNullInputs() {
        return Stream.of(
            Arguments.of(new BigDecimal("999.99"), new BigDecimal("12"), 12),
            Arguments.of(new BigDecimal("1000000.01"), new BigDecimal("12"), 12),
            Arguments.of(new BigDecimal("10000"), new BigDecimal("12"), 2),
            Arguments.of(new BigDecimal("10000"), new BigDecimal("12"), 61),
            Arguments.of(new BigDecimal("10000"), new BigDecimal("4.99"), 12),
            Arguments.of(new BigDecimal("10000"), new BigDecimal("40.01"), 12),
            Arguments.of(null, new BigDecimal("12"), 12),
            Arguments.of(new BigDecimal("10000"), null, 12)
        );
    }
}
