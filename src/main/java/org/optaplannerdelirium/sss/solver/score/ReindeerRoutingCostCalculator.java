package org.optaplannerdelirium.sss.solver.score;

import java.math.BigDecimal;

public class ReindeerRoutingCostCalculator {

    public static final long MICROS_PER_ONE_AS_LONG = 100000000000L;
    public static final double MICROS_PER_ONE_AS_DOUBLE = 100000000000.0;
    public static final BigDecimal MICROS_PER_ONE_AS_BIG_DECIMAL = new BigDecimal(MICROS_PER_ONE_AS_LONG);

    public static long parseMicroCost(String costString) {
        BigDecimal costBigDecimal = new BigDecimal(costString);
        if (costBigDecimal.scale() > 11) {
            throw new IllegalArgumentException("The costString (" + costString + ") has a scale ("
                    + costBigDecimal.scale() + ") higher than 10.");
        }
        costBigDecimal = costBigDecimal.setScale(11);
        return costBigDecimal.multiply(MICROS_PER_ONE_AS_BIG_DECIMAL).longValueExact();
    }

    public static long multiplyWeightAndDistance(long weightLong, double distanceDouble) {
        // Long arithmetic overflows because maxPowerConsumption (675.4800000000) * maxPowerCost (0.0228608333)
        double aDouble = ((double) (weightLong)) / MICROS_PER_ONE_AS_DOUBLE;
        double result = aDouble * distanceDouble;
        return Math.round(result * MICROS_PER_ONE_AS_DOUBLE);
    }

    public static long divideTwoMicros(long aMicros, long bMicros) {
        double aDouble = ((double) (aMicros)) / MICROS_PER_ONE_AS_DOUBLE;
        double bDouble = ((double) (bMicros)) / MICROS_PER_ONE_AS_DOUBLE;
        double result = aDouble / bDouble;
        return Math.round(result * MICROS_PER_ONE_AS_DOUBLE);
    }

}
