package br.gov.mgi.remuneracaoglobal.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Fórmulas financeiras de SPEC §5.1 e TASK-022.
 * Total da linha = (honorário + benefício) × membros.
 * Variação = ((proposto − vigente) / vigente) × 100, com regra para vigente zero.
 */
public final class CalculadoraFinanceira {

    private static final int ESCALA = 2;
    private static final BigDecimal CEM = new BigDecimal("100.00");

    private CalculadoraFinanceira() {
    }

    public static BigDecimal totalLinha(BigDecimal honorario, BigDecimal beneficio, int membros) {
        return honorario.add(beneficio)
                .multiply(BigDecimal.valueOf(membros))
                .setScale(ESCALA, RoundingMode.HALF_UP);
    }

    /**
     * Quando o valor vigente é zero (rubrica nova), retorna 100,00 se houver proposta positiva e 0,00 caso contrário.
     */
    public static BigDecimal variacaoPercentual(BigDecimal vigente, BigDecimal proposto) {
        if (vigente.signum() == 0) {
            return proposto.signum() > 0 ? CEM.setScale(ESCALA) : BigDecimal.ZERO.setScale(ESCALA);
        }
        return proposto.subtract(vigente)
                .multiply(BigDecimal.valueOf(100))
                .divide(vigente, ESCALA, RoundingMode.HALF_UP);
    }
}
