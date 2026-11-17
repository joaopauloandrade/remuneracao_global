package br.gov.mgi.remuneracaoglobal.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class CalculadoraFinanceiraTest {

    @Test
    void totalMultiplicaSomaPelosMembros() {
        assertThat(CalculadoraFinanceira.totalLinha(new BigDecimal("1000.00"), new BigDecimal("500.00"), 3))
                .isEqualByComparingTo("4500.00");
    }

    @Test
    void variacaoPercentualUsaValorVigenteComoBase() {
        assertThat(CalculadoraFinanceira.variacaoPercentual(new BigDecimal("1000"), new BigDecimal("1100")))
                .isEqualByComparingTo("10.00");
        assertThat(CalculadoraFinanceira.variacaoPercentual(new BigDecimal("1000"), new BigDecimal("900")))
                .isEqualByComparingTo("-10.00");
    }

    @Test
    void vigenteZeroComPropostaPositivaResultaEmCemPorCento() {
        assertThat(CalculadoraFinanceira.variacaoPercentual(BigDecimal.ZERO, new BigDecimal("50")))
                .isEqualByComparingTo("100.00");
    }

    @Test
    void vigenteZeroSemPropostaResultaEmZero() {
        assertThat(CalculadoraFinanceira.variacaoPercentual(BigDecimal.ZERO, BigDecimal.ZERO))
                .isEqualByComparingTo("0.00");
    }
}
